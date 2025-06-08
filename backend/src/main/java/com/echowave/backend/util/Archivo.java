package com.echowave.backend.util;

import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;

public class Archivo {

    private static final String[] ALLOWED_MIME_TYPES = {
            "audio/mpeg", "audio/wav", "audio/ogg", "audio/webm", "audio/aac"
    };

    public static boolean isValidAudioType(String contentType) {
        if (contentType == null) return false;
        for (String type : ALLOWED_MIME_TYPES) {
            if (contentType.startsWith(type)) return true;
        }
        return false;
    }

    public static void cleanupSessionFile(HttpSession session) throws IOException {
        String existingFilePath = (String) session.getAttribute("audioFile");
        if (existingFilePath != null) {
            Files.deleteIfExists(Paths.get(existingFilePath));
        }
    }

    public static File guardarArchivoTemporal(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ?
                originalFilename.substring(originalFilename.lastIndexOf(".")) :
                ".tmp";
        File tempFile = File.createTempFile("echowave-", extension);
        file.transferTo(tempFile);
        return tempFile;
    }

    public static Resource cargarRecurso(String path) throws MalformedURLException {
        Path file = Paths.get(path);
        return new UrlResource(file.toUri());
    }

    public static boolean existe(String filePath) {
        return filePath != null && Files.exists(Paths.get(filePath));
    }

    public static Path getPath(String filePath) {
        return Paths.get(filePath);
    }

    public static MediaType getTipoArchivo() {
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
