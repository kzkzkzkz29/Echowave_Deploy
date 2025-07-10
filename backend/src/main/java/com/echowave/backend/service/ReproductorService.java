package com.echowave.backend.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;

@Service
public class ReproductorService {

    public ResponseEntity<Void> reproducirDesdeUrl(String url, String filename, String sessionId, boolean repetir) {
        if (url == null || url.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(java.net.URI.create(url)); // Redirección 302
        headers.add("X-EchoWave-Session", sessionId);
        if (repetir) {
            headers.add("X-EchoWave-Repeat", "true");
        }

        return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build();
    }
}

