package com.echowave.backend.service;

import com.echowave.backend.entity.AudioEntity;
import com.echowave.backend.repository.AudioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.LocalDateTime;

@Service
public class ArchivoService {

    private final ArchivoUploader uploader;
    private final AudioRepository audioRepository;

    public ArchivoService(ArchivoUploader uploader, AudioRepository audioRepository) {
        this.uploader = uploader;
        this.audioRepository = audioRepository;
    }

    private static final String[] TIPOS_PERMITIDOS = {
            "audio/mpeg"
    };

    private static final long tamanoValido = 100 * 1024 * 1024; // 100MB

    public boolean esTipoValido(String contentType) {
        if (contentType == null) return false;
        for (String tipo : TIPOS_PERMITIDOS) {
            if (contentType.startsWith(tipo)) return true;
        }
        return false;
    }

    public boolean esTamanoValido(Long tamano){
        return tamano <= tamanoValido;
    }

    public void eliminarArchivoSesion(HttpSession session){
        session.removeAttribute("audioFile");
    }

    public String subirArchivo(MultipartFile archivo, HttpSession session) throws IOException {
        return uploader.subirArchivo(archivo, session);
    }

    public MediaType getTipoArchivo() {
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
