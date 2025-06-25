package com.echowave.backend.config;

import com.echowave.backend.service.ArchivoService;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SessionCleanupListener implements HttpSessionListener {

    private final ArchivoService archivoService;

    @Autowired
    public SessionCleanupListener(ArchivoService archivoService) {
        this.archivoService = archivoService;
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        try {
            archivoService.eliminarArchivoSesion(se.getSession());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}