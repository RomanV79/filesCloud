package ru.vlasov.fileclouds.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Value("${minio.endpoint}")
    private String endpoint;
    @Value("${minio.user_id}")
    private String userId;
    @Value("${minio.password}")
    private String password;


    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(userId, password)
                .build();
    }
}
