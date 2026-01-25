package com.beta.community.infra.config;

import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimplePrivateKeySupplier;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
@Configuration
public class OracleCloudConfig {

    @Value("${oracle.cloud.storage.auth.tenancy-ocid}")
    private String tenancyOcid;

    @Value("${oracle.cloud.storage.auth.user-ocid}")
    private String userOcid;

    @Value("${oracle.cloud.storage.auth.fingerprint}")
    private String fingerprint;

    @Value("${oracle.cloud.storage.auth.private-key-path}")
    private String privateKeyPath;

    @Value("${oracle.cloud.storage.region}")
    private String region;

    @Bean
    public AuthenticationDetailsProvider authenticationDetailsProvider() throws IOException {
        log.info("Oracle Cloud 인증 설정 초기화: region={}", region);

        String privateKeyContent = new String(Files.readAllBytes(Paths.get(privateKeyPath)));

        return SimpleAuthenticationDetailsProvider.builder()
                .tenantId(tenancyOcid)
                .userId(userOcid)
                .fingerprint(fingerprint)
                .privateKeySupplier(new SimplePrivateKeySupplier(privateKeyContent))
                .region(com.oracle.bmc.Region.fromRegionCodeOrId(region))
                .build();
    }

    @Bean
    public ObjectStorage objectStorageClient(AuthenticationDetailsProvider provider) {
        log.info("ObjectStorage 클라이언트 생성");
        return ObjectStorageClient.builder()
                .build(provider);
    }
}
