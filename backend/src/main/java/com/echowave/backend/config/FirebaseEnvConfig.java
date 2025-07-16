package com.echowave.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.util.Base64;

@Configuration
public class FirebaseEnvConfig {

    @PostConstruct
    public void init() throws Exception {
        String base64Credentials = System.getenv("FIREBASE_CREDENTIALS_BASE64");
        if (base64Credentials == null || base64Credentials.isEmpty()) {
            throw new IllegalStateException("FIREBASE_CREDENTIALS_BASE64 no está definida");
        }

        byte[] decodedBytes = Base64.getDecoder().decode(base64Credentials);

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(new ByteArrayInputStream(decodedBytes)))
                .setStorageBucket("echowave-b2c81.appspot.com") // nombre completo del bucket
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }
    }
}
