package com.beta.community.infra.repository;

import com.beta.community.domain.entity.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserBlockJpaRepository extends JpaRepository<UserBlock, Long> {

    Optional<UserBlock> findByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    List<UserBlock> findAllByBlockerId(Long blockerId);

    @Modifying
    @Query("DELETE FROM UserBlock ub WHERE ub.blockerId = :userId OR ub.blockedId = :userId")
    void deleteAllByBlockerIdOrBlockedId(@Param("userId") Long userId);
}
