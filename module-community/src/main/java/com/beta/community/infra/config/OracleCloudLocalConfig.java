package com.beta.community.infra.config;

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AbstractAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimplePrivateKeySupplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
@Configuration
@Profile("dev")
public class OracleCloudLocalConfig {

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
    public AbstractAuthenticationDetailsProvider authenticationDetailsProvider() throws IOException {
        log.info("Oracle Cloud API Key 인증 초기화 (dev): region={}", region);

        String privateKeyContent = new String(Files.readAllBytes(Paths.get(privateKeyPath)));

        return SimpleAuthenticationDetailsProvider.builder()
                .tenantId(tenancyOcid)
                .userId(userOcid)
                .fingerprint(fingerprint)
                .privateKeySupplier(new SimplePrivateKeySupplier(privateKeyContent))
                .region(Region.fromRegionCodeOrId(region))
                .build();
    }
}
