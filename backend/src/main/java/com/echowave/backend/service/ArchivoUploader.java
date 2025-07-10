package com.echowave.backend.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ArchivoUploader {
    String subirArchivo(MultipartFile archivo, HttpSession session) throws IOException;
}
