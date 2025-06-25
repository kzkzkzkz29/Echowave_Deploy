package com.echowave.backend.service;

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

@Service
public class ArchivoService {

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

    public File guardarArchivoPersistente(MultipartFile archivo) throws IOException {
        String nombreOriginal = archivo.getOriginalFilename();

        // Aseguramos que tenga una extensión válida
        String extension = nombreOriginal != null && nombreOriginal.contains(".")
                ? nombreOriginal.substring(nombreOriginal.lastIndexOf("."))
                : ".mp3";

        // Carpeta donde se guardarán los audios de forma permanente
        Path directorioDestino = Paths.get("C:/EchoWave/audios");

        // Crea el directorio si no existe
        if (!Files.exists(directorioDestino)) {
            Files.createDirectories(directorioDestino);
        }

        // Nombre del archivo: lo dejamos como original o le puedes agregar un timestamp si quieres evitar duplicados
        String nombreArchivo = "echowave-" + System.currentTimeMillis() + extension;
        Path rutaFinal = directorioDestino.resolve(nombreArchivo);

        // Guardar archivo
        archivo.transferTo(rutaFinal.toFile());

        return rutaFinal.toFile();
    }

    public Resource cargarRecurso(String ruta) throws MalformedURLException {
        Path file = Paths.get(ruta);
        return new UrlResource(file.toUri());
    }

    public boolean existeArchivo(String ruta) {
        return ruta != null && Files.exists(Paths.get(ruta));
    }

    public MediaType getTipoArchivo() {
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
