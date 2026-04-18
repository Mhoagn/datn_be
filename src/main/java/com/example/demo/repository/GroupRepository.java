package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Group;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    boolean existsByJoinCode(String joinCode);

    Group findByJoinCode(String joinCode);
}
