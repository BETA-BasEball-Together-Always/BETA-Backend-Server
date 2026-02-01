package com.beta.community.infra.config;

import com.oracle.bmc.auth.AbstractAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class OracleCloudConfig {

    @Bean
    public ObjectStorage objectStorageClient(AbstractAuthenticationDetailsProvider provider) {
        log.info("ObjectStorage 클라이언트 생성");
        return ObjectStorageClient.builder()
                .build(provider);
    }
}
