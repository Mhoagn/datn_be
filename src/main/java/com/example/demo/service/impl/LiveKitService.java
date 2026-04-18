package com.example.demo.service.impl;

import io.livekit.server.*;
import jakarta.annotation.PostConstruct;
import livekit.LivekitEgress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;

import java.util.UUID;

@Service
public class LiveKitService {

    @Value("${livekit.api-key}")
    private String apiKey;

    @Value("${livekit.api-secret}")
    private String apiSecret;

    @Value("${livekit.ws-url}")
    private String wsUrl;

    @Value("${livekit.s3.bucket}")
    private String s3Bucket;

    @Value("${livekit.s3.region}")
    private String s3Region;

    @Value("${livekit.s3.access-key}")
    private String awsAccessKeyId;

    @Value("${livekit.s3.secret-key}")
    private String awsSecretAccessKey;

    private RoomServiceClient roomServiceClient;
    private EgressServiceClient egressServiceClient;

    // Khởi tạo client 1 lần sau khi inject xong
    @PostConstruct
    public void init() {
        String httpUrl = wsUrl
                .replaceFirst("^wss://", "https://")
                .replaceFirst("^ws://", "http://");
        this.roomServiceClient = RoomServiceClient.createClient(httpUrl, apiKey, apiSecret);
        this.egressServiceClient = EgressServiceClient.createClient(httpUrl, apiKey, apiSecret);
    }

    public String getWsUrl() { return wsUrl; }

    // ===================== ROOM =====================

    public String generateRoomName(Long groupId) {
        return "group-" + groupId + "-"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    public void deleteRoom(String roomName) {
        try {
            roomServiceClient.deleteRoom(roomName).execute();
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa LiveKit room " + roomName + ": " + e.getMessage());
        }
    }

    // ===================== TOKEN =====================

    public String generateToken(String roomName, String identity, boolean isHost) {
        try {
            AccessToken token = new AccessToken(apiKey, apiSecret);
            token.setName(identity);
            token.setIdentity(identity);
            token.setTtl(3600 * 1000L);

            if (isHost) {
                token.addGrants(
                        new RoomJoin(true),
                        new RoomName(roomName),
                        new CanPublish(true),
                        new CanSubscribe(true),
                        new RoomAdmin(true)
                );
            } else {
                token.addGrants(
                        new RoomJoin(true),
                        new RoomName(roomName),
                        new CanPublish(true),
                        new CanSubscribe(true)
                );
            }
            return token.toJwt();

        } catch (Exception e) {
            throw new RuntimeException("Không thể tạo LiveKit token: " + e.getMessage(), e);
        }
    }

    // ===================== EGRESS =====================

    /**
     * Bắt đầu record room → lưu vào S3
     * @return egressId để dùng khi stop
     */
    public String startRecord(String roomName, String fileName) {
        try {
            LivekitEgress.EncodedFileOutput fileOutput = LivekitEgress.EncodedFileOutput.newBuilder()
                    .setFileType(LivekitEgress.EncodedFileType.MP4)
                    .setFilepath(fileName)
                    .setS3(LivekitEgress.S3Upload.newBuilder()
                            .setAccessKey(awsAccessKeyId)
                            .setSecret(awsSecretAccessKey)
                            .setBucket(s3Bucket)
                            .setRegion(s3Region)
                            .build())
                    .build();

            Call<LivekitEgress.EgressInfo> call = egressServiceClient.startRoomCompositeEgress(
                    roomName,
                    fileOutput
            );

            retrofit2.Response<LivekitEgress.EgressInfo> response = call.execute();

            if (!response.isSuccessful()) {
                System.out.println("Error body: " + response.errorBody().string());
            }

            LivekitEgress.EgressInfo egressInfo = response.body();
            if (egressInfo == null) {
                throw new RuntimeException("LiveKit không trả về egressInfo");
            }


            return egressInfo.getEgressId();

        } catch (Exception e) {
            System.out.println("Egress Exception: " + e.getMessage());
            throw new RuntimeException("Không thể bắt đầu record: " + e.getMessage(), e);
        }
    }

    /**
     * Dừng record theo egressId
     * @return EgressInfo chứa thông tin file sau khi stop
     */
    public LivekitEgress.EgressInfo stopRecord(String egressId) {
        try {
            LivekitEgress.EgressInfo egressInfo = egressServiceClient
                    .stopEgress(egressId)
                    .execute()
                    .body();

            if (egressInfo == null) {
                throw new RuntimeException("LiveKit không trả về egressInfo khi stop");
            }

            return egressInfo;

        } catch (Exception e) {
            throw new RuntimeException("Không thể dừng record: " + e.getMessage(), e);
        }
    }
}
