package com.example.demo.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public Map<String, Object> uploadImage(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File phải là ảnh (jpg, png, gif, webp)");
        }

        Map<String, Object> uploadParams = ObjectUtils.asMap(
                "folder", folder,
                "resource_type", "image"
        );

        return cloudinary.uploader().upload(file.getBytes(), uploadParams);
    }

    public void deleteImage(String publicId) {
        if (publicId == null || publicId.isEmpty()) {
            return;
        }

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Đã xóa ảnh Cloudinary: {}", publicId);
        } catch (IOException e) {
            log.error("Lỗi khi xóa ảnh Cloudinary: {}", e.getMessage());
        }
    }

    public Map<String, Object> uploadVideo(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        Map<String, Object> uploadParams = ObjectUtils.asMap(
                "folder", folder,
                "resource_type", "video"
        );

        return cloudinary.uploader().upload(file.getBytes(), uploadParams);
    }

    public Map<String, Object> uploadFile(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        Map<String, Object> uploadParams = ObjectUtils.asMap(
                "folder", folder,
                "resource_type", "raw"
        );

        return cloudinary.uploader().upload(file.getBytes(), uploadParams);
    }

    public List<Map<String, Object>> uploadMultipleFiles(List<MultipartFile> files) throws IOException {
        List<Map<String, Object>> results = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }

            String contentType = file.getContentType();
            Map<String, Object> result;

            if (contentType != null && contentType.startsWith("image/")) {
                result = uploadImage(file, "post-images");
            } else if (contentType != null && contentType.startsWith("video/")) {
                result = uploadVideo(file, "post-videos");
            } else {
                result = uploadFile(file, "post-files");
            }

            results.add(result);
        }

        return results;
    }

    public void deleteMultipleFiles(List<String> publicIds) {
        if (publicIds == null || publicIds.isEmpty()) {
            return;
        }

        for (String publicId : publicIds) {
            try {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                log.info("Đã xóa file Cloudinary: {}", publicId);
            } catch (IOException e) {
                log.error("Lỗi khi xóa file Cloudinary: {}", e.getMessage());
            }
        }
    }
}