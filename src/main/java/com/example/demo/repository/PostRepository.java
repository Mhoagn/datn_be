package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Post;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    boolean existsByIdAndAuthorId(Long id, Long authorId);
    List<Post> findByGroupIdAndIsDeletedOrderByCreatedAtAsc(Long groupId,boolean isActive);

}
