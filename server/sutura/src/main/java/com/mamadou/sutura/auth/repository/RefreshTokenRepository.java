package com.mamadou.sutura.auth.repository;

import com.mamadou.sutura.auth.entity.RefreshToken;
import com.mamadou.sutura.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenId(String tokenId);

    List<RefreshToken> findAllByUserAndRevokedFalse(User user);
}
