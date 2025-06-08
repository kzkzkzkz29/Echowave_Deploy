package com.echowave.backend.util;

import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.Resource;
import org.springframework.http.*;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Reproductor {

    public static ResponseEntity<Resource> reproducirDesdeArchivo(String filePath, String filename, String sessionId, boolean repetir) {
        try {
            if (!Archivo.existe(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = Archivo.cargarRecurso(filePath);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM));
            headers.setContentDisposition(ContentDisposition.inline().filename(filename != null ? filename : "audio").build());
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
