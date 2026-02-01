package com.beta.community.infra.config;

import com.oracle.bmc.auth.AbstractAuthenticationDetailsProvider;
import com.oracle.bmc.auth.InstancePrincipalsAuthenticationDetailsProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
@Profile("prod")
public class OracleCloudProdConfig {

    @Bean
    public AbstractAuthenticationDetailsProvider authenticationDetailsProvider() {
        log.info("Oracle Cloud Instance Principal 인증 초기화 (prod)");

        return InstancePrincipalsAuthenticationDetailsProvider.builder()
                .build();
    }
}
