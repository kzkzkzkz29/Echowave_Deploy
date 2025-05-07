package com.echowave.backend;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpSession;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Controller for handling audio file operations in EchoWave application
 */
@RestController
@RequestMapping("/api/audio")
public class AudioController {

    private static final String[] ALLOWED_MIME_TYPES = {
            "audio/mpeg",
            "audio/wav",
            "audio/ogg",
            "audio/webm",
            "audio/aac"
    };

    /**
     * Uploads an audio file to the current session
     * @param file Multipart audio file
     * @param session HTTP session
     * @return ResponseEntity with upload status
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadAudio(
            @RequestParam("file") MultipartFile file,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        response.put("application", "EchoWave");
        response.put("version", "1.0");
        response.put("timestamp", System.currentTimeMillis());

        try {
            // Validate file presence
            if (file.isEmpty()) {
                response.put("status", "error");
                response.put("message", "No file uploaded");
                return ResponseEntity.badRequest().body(response);
            }

            // Validate file size (handled by Spring but double-checking)
            if (file.getSize() > 100 * 1024 * 1024) { // 100MB
                response.put("status", "error");
                response.put("message", "File size exceeds 100MB limit");
                return ResponseEntity.badRequest().body(response);
            }

            // Validate content type
            if (!isValidAudioType(file.getContentType())) {
                response.put("status", "error");
                response.put("message", "Unsupported audio format. Allowed: MP3, WAV, OGG, WEBM, AAC");
                response.put("receivedType", file.getContentType());
                return ResponseEntity.badRequest().body(response);
            }

            // Clean up previous session file
            cleanupSessionFile(session);

            // Create temp file with original filename extension
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ?
                    originalFilename.substring(originalFilename.lastIndexOf(".")) :
                    ".tmp";

            File tempFile = File.createTempFile("echowave-", extension);
            file.transferTo(tempFile);

            // Store file reference in session
            session.setAttribute("audioFile", tempFile.getAbsolutePath());
            session.setAttribute("originalFilename", originalFilename);

            // Build success response
            response.put("status", "success");
            response.put("message", "File uploaded successfully");
            response.put("filename", originalFilename);
            response.put("size", file.getSize());
            response.put("contentType", file.getContentType());
            response.put("sessionId", session.getId());
            response.put("tempPath", tempFile.getAbsolutePath());

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("status", "error");
            response.put("message", "File processing error");
            response.put("errorDetails", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Streams the audio file from current session
     * @param session HTTP session
     * @return Audio file as stream with proper headers
     */
    @GetMapping("/stream")
    public ResponseEntity<Resource> streamAudio(HttpSession session) {
        try {
            String filePath = (String) session.getAttribute("audioFile");
            if (filePath == null || !Files.exists(Paths.get(filePath))) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(filePath);
            String filename = (String) session.getAttribute("originalFilename");

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + (filename != null ? filename : "audio") + "\"")
                    .header("X-EchoWave-Session", session.getId())
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Returns metadata about the current audio file
     * @param session HTTP session
     * @return File metadata or not found
     */
    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getAudioMetadata(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "EchoWave");
        response.put("version", "1.0");

        String filePath = (String) session.getAttribute("audioFile");
        if (filePath == null || !Files.exists(Paths.get(filePath))) {
            response.put("status", "not_found");
            return ResponseEntity.ok(response);
        }

        try {
            Path path = Paths.get(filePath);
            response.put("status", "available");
            response.put("filename", session.getAttribute("originalFilename"));
            response.put("size", Files.size(path));
            response.put("lastModified", Files.getLastModifiedTime(path).toMillis());
            response.put("sessionId", session.getId());
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            response.put("status", "error");
            response.put("error", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Validates if the content type is an allowed audio format
     */
    private boolean isValidAudioType(String contentType) {
        if (contentType == null) return false;
        for (String type : ALLOWED_MIME_TYPES) {
            if (contentType.startsWith(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Cleans up any existing session file
     */
    private void cleanupSessionFile(HttpSession session) throws IOException {
        String existingFilePath = (String) session.getAttribute("audioFile");
        if (existingFilePath != null) {
            Files.deleteIfExists(Paths.get(existingFilePath));
        }
    }
}