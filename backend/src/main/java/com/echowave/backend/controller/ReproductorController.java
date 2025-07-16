package com.echowave.backend.controller;

import com.echowave.backend.model.AudioDTO;
import com.echowave.backend.service.ListaReproduccionService;
import com.echowave.backend.service.ReproductorService;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reproductor")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true")
public class ReproductorController {

    private final ReproductorService reproductorService;
    private final ListaReproduccionService listaReproduccionService;

    public ReproductorController(ReproductorService reproductorService, ListaReproduccionService listaReproduccionService) {
        this.reproductorService = reproductorService;
        this.listaReproduccionService = listaReproduccionService;
    }

    @GetMapping("/stream")
    public ResponseEntity<Void> reproducirUltimoAudio(HttpSession session) {
        String url = (String) session.getAttribute("audioFile");
        String filename = (String) session.getAttribute("originalFilename");

        if (url == null || filename == null) {
            return ResponseEntity.notFound().build();
        }
        return reproductorService.reproducirDesdeUrl(url, filename, session.getId(), false);
    }

    @GetMapping("/playlist/{index}")
    public ResponseEntity<Void> reproducirDesdePlaylist(
            @PathVariable int index,
            @RequestParam(name = "repeat", required = false, defaultValue = "false") boolean repetir,
            HttpSession session) {

        List<AudioDTO> playlist = listaReproduccionService.obtener(session);

        if (index < 0 || index >= playlist.size()) {
            return ResponseEntity.notFound().build();
        }

        AudioDTO audio = playlist.get(index);
        return reproductorService.reproducirDesdeUrl(audio.getUrl(), audio.getFileName(), session.getId(), repetir);
    }

}
