package com.echowave.backend;

import com.echowave.backend.config.SessionCleanupListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EchoWaveApplication {
	public static void main(String[] args) {
		SpringApplication.run(EchoWaveApplication.class, args);
	}

	@Bean
	public ServletListenerRegistrationBean<SessionCleanupListener> sessionListener(SessionCleanupListener listener) {
		return new ServletListenerRegistrationBean<>(listener);
	}

}