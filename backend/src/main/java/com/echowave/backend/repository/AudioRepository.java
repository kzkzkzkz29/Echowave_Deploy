package com.echowave.backend.repository;

import com.echowave.backend.entity.AudioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AudioRepository extends JpaRepository<AudioEntity, Long> {
    List<AudioEntity> findBySessionId(String sessionId);
}
