package com.echowave.backend.service;

import com.google.cloud.storage.*;
import com.google.firebase.cloud.StorageClient;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@Service
public class FirebaseUploaderService implements ArchivoUploader {

    @Override
    public String subirArchivo(MultipartFile archivo, HttpSession session) throws IOException {
        String nombreArchivo = "audios/" + UUID.randomUUID() + "-" + archivo.getOriginalFilename();

        String token = UUID.randomUUID().toString();

        Storage storage = StorageClient.getInstance().bucket().getStorage();

        BlobId blobId = BlobId.of("echowave-b2c81", nombreArchivo);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(archivo.getContentType())
                .setMetadata(Map.of("firebaseStorageDownloadTokens", token))
                .build();

        storage.create(blobInfo, archivo.getBytes());

        return String.format(
                "https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media&token=%s",
                "echowave-b2c81",
                URLEncoder.encode(nombreArchivo, StandardCharsets.UTF_8),
                token
        );
    }
}
