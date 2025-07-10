package com.echowave.backend.controller;

import com.echowave.backend.model.AudioDTO;
import com.echowave.backend.service.ListaReproduccionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/playlist")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:63342"}, allowCredentials = "true")
public class ListaReproduccionController {

    private final ListaReproduccionService listaReproduccionService;

    public ListaReproduccionController(ListaReproduccionService listaReproduccionService) {
        this.listaReproduccionService = listaReproduccionService;
    }

    @GetMapping
    public ResponseEntity<List<AudioDTO>> obtenerPlaylist(HttpSession session) {
        return ResponseEntity.ok(listaReproduccionService.obtener(session));
    }

    @PostMapping("/agregar")
    public ResponseEntity<Map<String, Object>> agregarAudio(
            @RequestBody AudioDTO audio,
            HttpSession session) {

        listaReproduccionService.agregar(session, audio);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("status", "success");
        respuesta.put("playlist", listaReproduccionService.obtener(session));
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/mezclar")
    public ResponseEntity<List<AudioDTO>> mezclarPlaylist(HttpSession session) {
        listaReproduccionService.mezclar(session);
        return ResponseEntity.ok(listaReproduccionService.obtener(session));
    }

    @DeleteMapping("/eliminar/{index}")
    public ResponseEntity<Map<String, Object>> eliminarAudio(
            @PathVariable int index,
            HttpSession session) {

        Map<String, Object> respuesta = new HashMap<>();
        try {
            AudioDTO eliminado = listaReproduccionService.eliminar(session, index);
            respuesta.put("status", "success");
            respuesta.put("remove", eliminado);
            respuesta.put("playlist", listaReproduccionService.obtener(session));
        } catch (IndexOutOfBoundsException e) {
            respuesta.put("status", "error");
            respuesta.put("message", "Índice fuera de rango");
            return ResponseEntity.badRequest().body(respuesta);
        }
        return ResponseEntity.ok(respuesta);
    }

}
