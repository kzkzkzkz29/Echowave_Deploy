package com.echowave.backend.service;

import com.echowave.backend.entity.AudioEntity;
import com.echowave.backend.repository.AudioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AudioService {

    @Autowired
    private AudioRepository audioRepository;

    public AudioEntity guardarAudio(String fileName, String url, String sessionId) {
        AudioEntity audio = new AudioEntity();
        audio.setFileName(fileName);
        audio.setUrl(url);
        audio.setSessionId(sessionId);
        audio.setUploadedAt(LocalDateTime.now());

        return audioRepository.save(audio);
    }

    public List<AudioEntity> obtenerAudiosPorSesion(HttpSession session) {
        String sessionId = session.getId();
        return audioRepository.findBySessionId(sessionId);
    }
}
