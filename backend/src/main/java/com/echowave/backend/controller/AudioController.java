package com.echowave.backend.controller;

import com.echowave.backend.util.Archivo;
import com.echowave.backend.util.ListaReproduccion;
import com.echowave.backend.util.Reproductor;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/audio")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AudioController {

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadAudio(
            @RequestParam("file") MultipartFile file,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        response.put("application", "EchoWave");
        response.put("version", "1.0");
        response.put("timestamp", System.currentTimeMillis());

        try {
            if (file.isEmpty()) {
                response.put("status", "error");
                response.put("message", "No se subió ningún archivo");
                return ResponseEntity.badRequest().body(response);
            }

            if (file.getSize() > 100 * 1024 * 1024) {
                response.put("status", "error");
                response.put("message", "El archivo excede el límite de 100MB");
                return ResponseEntity.badRequest().body(response);
            }

            if (!Archivo.isValidAudioType(file.getContentType())) {
                response.put("status", "error");
                response.put("message", "Formato de audio no soportado. Permitidos: MP3, WAV, OGG, WEBM, AAC");
                response.put("receivedType", file.getContentType());
                return ResponseEntity.badRequest().body(response);
            }

            Archivo.cleanupSessionFile(session);
            File tempFile = Archivo.guardarArchivoTemporal(file);

            session.setAttribute("audioFile", tempFile.getAbsolutePath());
            session.setAttribute("originalFilename", file.getOriginalFilename());

            response.put("status", "success");
            response.put("message", "Archivo subido exitosamente");
            response.put("filename", file.getOriginalFilename());
            response.put("size", file.getSize());
            response.put("contentType", file.getContentType());
            response.put("sessionId", session.getId());
            response.put("tempPath", tempFile.getAbsolutePath());

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("status", "error");
            response.put("message", "Error al procesar el archivo");
            response.put("errorDetails", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/stream")
    public ResponseEntity<Resource> streamAudio(HttpSession session) {
        String filePath = (String) session.getAttribute("audioFile");
        String filename = (String) session.getAttribute("originalFilename");
        return Reproductor.reproducirDesdeArchivo(filePath, filename, session.getId(), false);
    }

    @PostMapping("/playlist/add")
    public ResponseEntity<Map<String, Object>> addToPlaylist(
            @RequestParam("filename") String filename,
            @RequestParam("path") String path,
            HttpSession session) {

        ListaReproduccion.agregar(session, filename, path);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("playlist", ListaReproduccion.obtener(session));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/playlist")
    public ResponseEntity<List<Map<String, String>>> getPlaylist(HttpSession session) {
        return ResponseEntity.ok(ListaReproduccion.obtener(session));
    }

    @PostMapping("/playlist/shuffle")
    public ResponseEntity<List<Map<String, String>>> shufflePlaylist(HttpSession session) {
        ListaReproduccion.mezclar(session);
        return ResponseEntity.ok(ListaReproduccion.obtener(session));
    }

    @DeleteMapping("/playlist/remove/{index}")
    public ResponseEntity<Map<String, Object>> removeFromPlaylist(
            @PathVariable int index,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, String> removed = ListaReproduccion.eliminar(session, index);
            response.put("status", "success");
            response.put("removed", removed);
            response.put("playlist", ListaReproduccion.obtener(session));
        } catch (IndexOutOfBoundsException e) {
            response.put("status", "error");
            response.put("message", "Índice fuera de rango");
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/playlist/play/{index}")
    public ResponseEntity<Resource> playFromPlaylist(
            @PathVariable int index,
            @RequestParam(name = "repeat", required = false, defaultValue = "false") boolean repeat,
            HttpSession session) {

        List<Map<String, String>> playlist = ListaReproduccion.obtener(session);
        if (index < 0 || index >= playlist.size()) {
            return ResponseEntity.notFound().build();
        }

        Map<String, String> song = playlist.get(index);
        return Reproductor.reproducirDesdeArchivo(
                song.get("path"),
                song.get("filename"),
                session.getId(),
                repeat
        );
    }
}
