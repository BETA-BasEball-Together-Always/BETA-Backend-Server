package com.beta.community.infra.config;

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AbstractAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimplePrivateKeySupplier;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


@Slf4j
@Configuration
@Profile({"dev", "prod"})
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
    public AbstractAuthenticationDetailsProvider authenticationDetailsProvider() {
        log.info("Oracle Cloud API Key 인증 초기화: region={}, keyPath={}", region, privateKeyPath);

        return SimpleAuthenticationDetailsProvider.builder()
                .tenantId(tenancyOcid)
                .userId(userOcid)
                .fingerprint(fingerprint)
                .privateKeySupplier(new SimplePrivateKeySupplier(privateKeyPath))
                .region(Region.fromRegionCodeOrId(region))
                .build();
    }

    @Bean
    public ObjectStorage objectStorage(AbstractAuthenticationDetailsProvider authProvider) {
        log.info("Oracle Cloud ObjectStorage 클라이언트 초기화");

        return ObjectStorageClient.builder()
                .region(Region.fromRegionCodeOrId(region))
                .build(authProvider);
    }
}
