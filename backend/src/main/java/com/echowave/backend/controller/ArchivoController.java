package com.echowave.backend.controller;

import com.echowave.backend.entity.AudioEntity;
import com.echowave.backend.service.ArchivoService;
import com.echowave.backend.service.AudioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/archivo")
@CrossOrigin(
        origins = {
                "http://localhost:3000",
                "https://echowave-frontend.vercel.app",
                "https://echowave-frontend-b40nqjryq-angels-projects-dfee7c94.vercel.app"
        },
        allowCredentials = "true"
)
public class ArchivoController {

    private final ArchivoService archivoService;
    private final AudioService audioService;

    public ArchivoController(ArchivoService archivoService, AudioService audioService) {
        this.archivoService = archivoService;
        this.audioService = audioService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> subirAudio(
            @RequestParam("file") MultipartFile archivo,
            HttpSession session) {

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("application", "EchoWave");
        respuesta.put("version", "1.0");
        respuesta.put("timestamp", System.currentTimeMillis());

        try {
            if (archivo.isEmpty()) {
                respuesta.put("status", "error");
                respuesta.put("message", "No se subió ningún archivo");
                return ResponseEntity.badRequest().body(respuesta);
            }

            if (!archivoService.esTamanoValido(archivo.getSize())) {
                respuesta.put("status", "error");
                respuesta.put("message", "El archivo excede el límite de 100MB");
                return ResponseEntity.badRequest().body(respuesta);
            }

            if (!archivoService.esTipoValido(archivo.getContentType())) {
                respuesta.put("status", "error");
                respuesta.put("message", "Formato de audio no soportado. Permitido: MP3");
                respuesta.put("receivedType", archivo.getContentType());
                return ResponseEntity.badRequest().body(respuesta);
            }

            archivoService.eliminarArchivoSesion(session);

            String url = archivoService.subirArchivo(archivo, session);

            AudioEntity guardado = audioService.guardarAudio(
                    archivo.getOriginalFilename(),
                    url,
                    session.getId()
            );

            session.setAttribute("audioFile", url);
            session.setAttribute("originalFilename", archivo.getOriginalFilename());
            respuesta.put("status", "success");
            respuesta.put("message", "Archivo subido y guardado");
            respuesta.put("filename", guardado.getFileName());
            respuesta.put("path", guardado.getUrl());
            respuesta.put("sessionId", session.getId());

            return ResponseEntity.ok(respuesta);

        } catch (IOException e) {
            respuesta.put("status", "error");
            respuesta.put("message", "Error al procesar el archivo");
            respuesta.put("errorDetails", e.getMessage());
            return ResponseEntity.internalServerError().body(respuesta);
        }
    }

    @GetMapping("/ultimo")
    public ResponseEntity<Map<String, Object>> obtenerUltimoArchivo(HttpSession session) {
        String path = (String) session.getAttribute("audioFile");
        String nombre = (String) session.getAttribute("originalFilename");

        if (path == null || nombre == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("filename", nombre);
        respuesta.put("path", path);
        return ResponseEntity.ok(respuesta);
    }

}
