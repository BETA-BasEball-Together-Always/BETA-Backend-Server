package com.beta.account.infra.repository;

import com.beta.account.application.dto.SocialProvider;
import com.beta.account.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<User, Long> {
    Optional<User> findBySocialIdAndSocialProvider(String socialId, SocialProvider socialProvider);

    Optional<User> findByEmail(String email);

    boolean existsByNickname(String nickName);

    boolean existsByEmail(String email);
}
