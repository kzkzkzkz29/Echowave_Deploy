package com.echowave.backend.util;

import jakarta.servlet.http.HttpSession;

import java.util.*;

public class ListaReproduccion {

    public static List<Map<String, String>> obtener(HttpSession session) {
        List<Map<String, String>> playlist = (List<Map<String, String>>) session.getAttribute("playlist");
        return playlist != null ? playlist : new ArrayList<>();
    }

    public static void guardar(HttpSession session, List<Map<String, String>> playlist) {
        session.setAttribute("playlist", playlist);
    }

    public static void agregar(HttpSession session, String filename, String path) {
        List<Map<String, String>> playlist = obtener(session);

        Map<String, String> songInfo = new HashMap<>();
        songInfo.put("filename", filename);
        songInfo.put("path", path);

        playlist.add(songInfo);
        guardar(session, playlist);
    }

    public static Map<String, String> eliminar(HttpSession session, int index) {
        List<Map<String, String>> playlist = obtener(session);
        Map<String, String> removed = playlist.remove(index);
        guardar(session, playlist);
        return removed;
    }

    public static void mezclar(HttpSession session) {
        List<Map<String, String>> playlist = obtener(session);
        Collections.shuffle(playlist);
        guardar(session, playlist);
    }
}
