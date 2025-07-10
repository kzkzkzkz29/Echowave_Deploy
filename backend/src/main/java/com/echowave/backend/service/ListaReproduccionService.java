package com.echowave.backend.service;

import com.echowave.backend.model.AudioDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ListaReproduccionService {

    private static final String ATRIBUTO_PLAYLIST = "playlist";

    public List<AudioDTO> obtener(HttpSession session) {
        List<AudioDTO> playlist = (List<AudioDTO>) session.getAttribute(ATRIBUTO_PLAYLIST);
        return playlist != null ? playlist : new ArrayList<>();
    }

    public void guardar(HttpSession session, List<AudioDTO> playlist) {
        session.setAttribute(ATRIBUTO_PLAYLIST, playlist);
    }

    public void agregar(HttpSession session, AudioDTO audio) {
        List<AudioDTO> playlist = obtener(session);

        boolean yaExiste = playlist.stream()
                .anyMatch(a -> a.getFileName().equals(audio.getFileName()) && a.getUrl().equals(audio.getUrl()));

        if (!yaExiste) {
            playlist.add(audio);
            guardar(session, playlist);
        }
    }

    public AudioDTO eliminar(HttpSession session, int index) {
        List<AudioDTO> playlist = obtener(session);
        if (index < 0 || index >= playlist.size()){
            throw new IndexOutOfBoundsException("indice fuera de rango");
        }
        AudioDTO eliminado = playlist.remove(index);
        guardar(session,playlist);
        return eliminado;
    }

    public void mezclar(HttpSession session) {
        List<AudioDTO> playlist = obtener(session);
        Collections.shuffle(playlist);
        guardar(session, playlist);
    }
}
