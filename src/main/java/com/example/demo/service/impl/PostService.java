package com.example.demo.service.impl;

import com.example.demo.dto.PostDTO.CreatePostRequest;
import com.example.demo.dto.PostDTO.PostResponse;
import com.example.demo.dto.PostDTO.UpdatePostRequest;
import com.example.demo.entity.Group;
import com.example.demo.entity.GroupMember;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.event.NewPostEvent;
import com.example.demo.exception.*;
import com.example.demo.mapper.PostMapper;
import com.example.demo.repository.GroupMemberRepository;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.interf.PostInterface;
import com.example.demo.util.SecurityUtil;

import java.io.IOException;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PostService implements PostInterface {
    private final PostRepository postRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final CloudinaryService cloudinaryService;
    private final SecurityUtil securityUtil;
    private final PostMapper postMapper;
    private final PostWebSocketService postWebSocketService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public PostResponse createPost(CreatePostRequest createPostRequest) {
        if (createPostRequest.getGroupId() == null) {
            throw new InvalidRequestException("Group ID không được để trống");
        }
        
        if (createPostRequest.getContent() == null || createPostRequest.getContent().trim().isEmpty()) {
            throw new InvalidRequestException("Nội dung bài post không được để trống");
        }
        
        if (createPostRequest.getContent().length() > 10000) {
            throw new InvalidRequestException("Nội dung không được vượt quá 10000 ký tự");
        }
        
        Long currentUserId = securityUtil.getCurrentUserId();
        
        Group group = groupRepository.findById(createPostRequest.getGroupId())
                .orElseThrow(() -> new GroupNotFoundException("Nhóm không tồn tại"));
        
        GroupMember member = groupMemberRepository.findByUserIdAndGroupId(currentUserId, createPostRequest.getGroupId())
                .orElseThrow(() -> new UserNotInGroupException("Bạn không thuộc nhóm này"));
        
        if (!member.getIsActive()) {
            throw new UserNotInGroupException("Bạn không còn là thành viên của nhóm này");
        }

        User author = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException("Người dùng không tồn tại"));
        
        Post post = new Post();
        post.setGroup(group);
        post.setAuthor(author);
        post.setGroupId(createPostRequest.getGroupId());
        post.setAuthorId(currentUserId);
        post.setContent(createPostRequest.getContent());
        post.setIsDeleted(false);

        if (createPostRequest.getFiles() != null && !createPostRequest.getFiles().isEmpty()) {
            try {
                List<MultipartFile> validFiles = createPostRequest.getFiles().stream()
                        .filter(file -> file != null && !file.isEmpty())
                        .collect(Collectors.toList());

                if (!validFiles.isEmpty()) {
                    List<Map<String, Object>> uploadResults =
                            cloudinaryService.uploadMultipleFiles(validFiles);

                    List<String> mediaUrls = new ArrayList<>();
                    List<String> mediaPublicIds = new ArrayList<>();
                    List<Map<String, Object>> mediaMetadata = new ArrayList<>();
                    Set<String> fileTypes = new HashSet<>();

                    for (int i = 0; i < uploadResults.size(); i++) {
                        Map<String, Object> result = uploadResults.get(i);
                        MultipartFile file = validFiles.get(i);
                        
                        String url = (String) result.get("secure_url");
                        String publicId = (String) result.get("public_id");
                        String resourceType = (String) result.get("resource_type");

                        mediaUrls.add(url);
                        mediaPublicIds.add(publicId);
                        
                        Map<String, Object> metadata = new HashMap<>();
                        metadata.put("url", url);
                        metadata.put("publicId", publicId);
                        metadata.put("originalName", file.getOriginalFilename());
                        metadata.put("size", file.getSize());
                        metadata.put("mimeType", file.getContentType());
                        metadata.put("resourceType", resourceType);
                        mediaMetadata.add(metadata);

                        if ("image".equals(resourceType)) {
                            fileTypes.add("image");
                        } else if ("video".equals(resourceType)) {
                            fileTypes.add("video");
                        } else {
                            fileTypes.add("file");
                        }
                    }

                    post.setMediaUrls(mediaUrls);
                    post.setMediaPublicIds(mediaPublicIds);
                    post.setMediaMetadata(mediaMetadata);

                    if (fileTypes.size() > 1 || validFiles.size() > 1) {
                        post.setMediaType(Post.MediaType.MULTIPLE);
                    } else if (fileTypes.contains("image")) {
                        post.setMediaType(Post.MediaType.IMAGE);
                    } else if (fileTypes.contains("video")) {
                        post.setMediaType(Post.MediaType.VIDEO);
                    } else {
                        post.setMediaType(Post.MediaType.FILE);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Không thể upload file: " + e.getMessage());
            }
        } else {
            post.setMediaType(Post.MediaType.NONE);
        }

        Post savedPost = postRepository.save(post);
        eventPublisher.publishEvent(new NewPostEvent(currentUserId, createPostRequest.getGroupId(), post.getId()));
        
        PostResponse response = postMapper.toResponse(savedPost, currentUserId);
        
        postWebSocketService.broadcastNewPost(savedPost.getGroupId(), response);
        
        return response;
    }

    @Override
    @Transactional
    public PostResponse updatePost(UpdatePostRequest updatePostRequest, Long postId) {
        Long currentUserId = securityUtil.getCurrentUserId();
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Bài viết không tồn tại"));

        if (!post.getAuthorId().equals(currentUserId)) {
            throw new UserIsNotPostAuthorException("Bạn không có quyền chỉnh sửa bài viết này");
        }
        
        if (post.getIsDeleted()) {
            throw new PostIsDeletedException("Bài viết đã bị xóa");
        }

        if (updatePostRequest.getContent() != null && !updatePostRequest.getContent().trim().isEmpty()) {
            if (updatePostRequest.getContent().length() > 10000) {
                throw new InvalidRequestException("Nội dung không được vượt quá 10000 ký tự");
            }
            post.setContent(updatePostRequest.getContent().trim());
        }

        List<String> keepMediaUrls = updatePostRequest.getKeepMediaUrls();
        List<String> existingUrls = post.getMediaUrls() != null ? new ArrayList<>(post.getMediaUrls()) : new ArrayList<>();
        List<String> existingPublicIds = post.getMediaPublicIds() != null ? new ArrayList<>(post.getMediaPublicIds()) : new ArrayList<>();
        List<Map<String, Object>> existingMetadata = post.getMediaMetadata() != null ? new ArrayList<>(post.getMediaMetadata()) : new ArrayList<>();

        List<String> urlsToDelete = new ArrayList<>();
        List<String> publicIdsToDelete = new ArrayList<>();

        if (keepMediaUrls != null) {
            keepMediaUrls = keepMediaUrls.stream()
                    .filter(url -> url != null && !url.trim().isEmpty())
                    .collect(Collectors.toList());

            for (int i = 0; i < existingUrls.size(); i++) {
                String url = existingUrls.get(i);
                if (!keepMediaUrls.contains(url)) {
                    urlsToDelete.add(url);
                    if (i < existingPublicIds.size()) {
                        publicIdsToDelete.add(existingPublicIds.get(i));
                    }
                }
            }

            if (!publicIdsToDelete.isEmpty()) {
                cloudinaryService.deleteMultipleFiles(publicIdsToDelete);
            }

            existingUrls.removeAll(urlsToDelete);
            for (String publicId : publicIdsToDelete) {
                existingPublicIds.remove(publicId);
            }
            existingMetadata.removeIf(metadata -> {
                String url = (String) metadata.get("url");
                return urlsToDelete.contains(url);
            });
        }

        if (updatePostRequest.getMediaInfoList() != null && !updatePostRequest.getMediaInfoList().isEmpty()) {
            List<MultipartFile> newFiles = updatePostRequest.getMediaInfoList().stream()
                    .filter(file -> file != null && !file.isEmpty())
                    .collect(Collectors.toList());

            if (!newFiles.isEmpty()) {

                try {
                    List<Map<String, Object>> uploadResults =
                            cloudinaryService.uploadMultipleFiles(newFiles);

                    Set<String> fileTypes = new HashSet<>();

                    for (int i = 0; i < uploadResults.size(); i++) {
                        Map<String, Object> result = uploadResults.get(i);
                        MultipartFile file = newFiles.get(i);
                        
                        String url = (String) result.get("secure_url");
                        String publicId = (String) result.get("public_id");
                        String resourceType = (String) result.get("resource_type");

                        existingUrls.add(url);
                        existingPublicIds.add(publicId);
                        
                        Map<String, Object> metadata = new HashMap<>();
                        metadata.put("url", url);
                        metadata.put("publicId", publicId);
                        metadata.put("originalName", file.getOriginalFilename());
                        metadata.put("size", file.getSize());
                        metadata.put("mimeType", file.getContentType());
                        metadata.put("resourceType", resourceType);
                        existingMetadata.add(metadata);

                        if ("image".equals(resourceType)) {
                            fileTypes.add("image");
                        } else if ("video".equals(resourceType)) {
                            fileTypes.add("video");
                        } else {
                            fileTypes.add("file");
                        }
                    }

                    post.setMediaUrls(existingUrls);
                    post.setMediaPublicIds(existingPublicIds);
                    post.setMediaMetadata(existingMetadata);

                    for (Map<String, Object> metadata : existingMetadata) {
                        String mimeType = (String) metadata.get("mimeType");
                        String resourceType = (String) metadata.get("resourceType");
                        if (mimeType != null) {
                            if (mimeType.startsWith("image/") || "image".equals(resourceType)) {
                                fileTypes.add("image");
                            } else if (mimeType.startsWith("video/") || "video".equals(resourceType)) {
                                fileTypes.add("video");
                            } else {
                                fileTypes.add("file");
                            }
                        }
                    }

                    if (fileTypes.size() > 1 || existingUrls.size() > 1) {
                        post.setMediaType(Post.MediaType.MULTIPLE);
                    } else if (fileTypes.contains("image")) {
                        post.setMediaType(Post.MediaType.IMAGE);
                    } else if (fileTypes.contains("video")) {
                        post.setMediaType(Post.MediaType.VIDEO);
                    } else if (fileTypes.contains("file")) {
                        post.setMediaType(Post.MediaType.FILE);
                    } else {
                        post.setMediaType(Post.MediaType.NONE);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Không thể upload file: " + e.getMessage());
                }
            }
        } else {
            post.setMediaUrls(existingUrls);
            post.setMediaPublicIds(existingPublicIds);
            post.setMediaMetadata(existingMetadata);

            if (existingUrls.isEmpty()) {
                post.setMediaType(Post.MediaType.NONE);
            }
        }

        Post updatedPost = postRepository.save(post);
        
        PostResponse response = postMapper.toResponse(updatedPost, currentUserId);
        
        postWebSocketService.broadcastUpdatePost(updatedPost.getGroupId(), response);
        
        return response;
    }

    @Override
    @Transactional
    public PostResponse deletePost(Long postId) {
        Long currentUserId = securityUtil.getCurrentUserId();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new GroupNotFoundException("Bài viết không tồn tại"));

        if(!post.getAuthorId().equals(currentUserId)) {
            throw new UserIsNotPostAuthorException("Bạn không có quyền xóa bài viết này");
        }

        if (post.getIsDeleted()) {
            throw new PostIsDeletedException("Bài viết đã bị xóa trước đó");
        }

        post.setIsDeleted(true);
        Post deletePost = postRepository.save(post);
        
        PostResponse response = postMapper.toResponse(deletePost, currentUserId);
        
        postWebSocketService.broadcastDeletePost(deletePost.getGroupId(), response);
        
        return response;
    }

    
    @Override
    public List<PostResponse> getActivePostInGroup(Long groupId) {
        Long currentUserId = securityUtil.getCurrentUserId();
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Nhóm không tồn tại"));
        GroupMember member = groupMemberRepository.findByUserIdAndGroupId(currentUserId, groupId)
                .orElseThrow(() -> new UserNotInGroupException("Bạn không phải thành viên của nhóm"));
        
        List<Post> postList = postRepository.findByGroupIdAndIsDeletedOrderByCreatedAtAsc(groupId,false);
        
        return postMapper.toResponseList(postList, currentUserId);
    }
}
