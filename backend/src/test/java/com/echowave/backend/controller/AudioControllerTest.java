package com.echowave.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AudioController.class)
class AudioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deberiaAceptarArchivoMp3Valido() throws Exception {
        MockMultipartFile archivo = new MockMultipartFile(
                "file", "musica.mp3", "audio/mpeg", "sonido de musicaxd".getBytes()
        );

        mockMvc.perform(multipart("/api/audio/upload").file(archivo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.filename").value("musica.mp3"));
    }

    @Test
    void deberiaRechazarArchivoNoMp3() throws Exception {
        MockMultipartFile archivo = new MockMultipartFile(
                "file", "documento.txt", "text/plain", "texto".getBytes()
        );

        mockMvc.perform(multipart("/api/audio/upload").file(archivo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Formato de audio no soportado. Permitido: MP3"));
    }
}
