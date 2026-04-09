package com.agrabandhan.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class R2StorageConfig {

    @Value("${agrabandhan.r2.endpoint:}")
    private String endpoint;

    @Value("${agrabandhan.r2.access-key:}")
    private String accessKey;

    @Value("${agrabandhan.r2.secret-key:}")
    private String secretKey;

    @Bean
    public S3Client s3Client() {
        if (endpoint == null || endpoint.isBlank()) {
            // Return a no-op client for local dev without R2
            return S3Client.builder()
                    .region(Region.US_EAST_1)
                    .build();
        }

        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.of("auto"))
                .forcePathStyle(true)
                .build();
    }
}
