package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.PostComment;

import java.util.List;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    List<PostComment> findByPostIdAndIsDeletedFalseAndParentCommentIdIsNullOrderByCreatedAtAsc(Long postId);
    List<PostComment> findByParentCommentIdAndIsDeletedFalseOrderByCreatedAtAsc(Long parentCommentId);
    List<PostComment> findByParentCommentId(Long parentCommentId);
    Long countByParentCommentIdAndIsDeletedFalse(Long parentCommentId);

}
