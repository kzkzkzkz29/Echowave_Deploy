package com.echowave.backend.service;

import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

@Service
public class ReproductorService {

    private final ArchivoService archivoService;

    public ReproductorService(ArchivoService archivoService) {
        this.archivoService = archivoService;
    }

    public ResponseEntity<Resource> reproducirDesdeArchivo(String filePath, String filename, String sessionId, boolean repetir) {
        try {
            if (!archivoService.existeArchivo(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = archivoService.cargarRecurso(filePath);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaTypeFactory.getMediaType(resource)
                    .orElse(MediaType.APPLICATION_OCTET_STREAM));
            headers.setContentDisposition(ContentDisposition.inline()
                    .filename(filename != null ? filename : "audio")
                    .build());
            headers.add("X-EchoWave-Session", sessionId);
            if (repetir) {
                headers.add("X-EchoWave-Repeat", "true");
            }

            return new ResponseEntity<>(resource, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
