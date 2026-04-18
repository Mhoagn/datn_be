package com.example.demo.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.GroupMember;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    Optional<GroupMember> findByUserIdAndGroupId(Long userId, Long groupId);
    Long countByGroupIdAndIsActiveTrue(Long groupId);
    
    @Query("SELECT gm FROM GroupMember gm WHERE gm.userId = :userId AND gm.isActive = true ORDER BY gm.group.id ASC")
    Slice<GroupMember> findAllByUserIdAndIsActiveTrue(Long userId, Pageable pageable);

    @Query("SELECT gm FROM GroupMember gm WHERE gm.userId = :userId AND gm.isActive = true " +
           "AND LOWER(gm.group.groupName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "ORDER BY gm.group.id ASC")
    Slice<GroupMember> findByUserIdAndGroupName(Long userId, String search, Pageable pageable);

    boolean existsByUserIdAndGroupId(Long userId, Long groupId);

    @Query("SELECT gm.user.id FROM GroupMember gm WHERE gm.group.id = :groupId")
    List<Long> findUserIdsByGroupId(@Param("groupId") Long groupId);

    @Query("SELECT gm.user.id FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.role = :role")
    List<Long> findUserIdsByGroupIdAndRole(@Param("groupId") Long groupId, @Param("role") GroupMember.Role role);

    @Query("SELECT gm.user.id FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.role IN :roles")
    List<Long> findUserIdsByGroupIdAndRoleIn(@Param("groupId") Long groupId, @Param("roles") List<GroupMember.Role> roles);
}
