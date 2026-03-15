package com.wms.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wms.models.RefreshSession;

public interface RefreshSessionRepository extends JpaRepository<RefreshSession, Long> {

    Optional<RefreshSession> findByTokenId(String tokenId);
}
