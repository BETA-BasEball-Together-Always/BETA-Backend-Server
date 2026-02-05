package com.beta.config;

import com.beta.core.exception.ErrorCode;
import com.beta.core.response.ErrorResponse;
import com.beta.filter.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 테스트를 위해 모든 Origin 허용 (추후 운영 환경에 맞게 수정 필요)
        config.setAllowedOrigins(List.of("*"));
        // 허용할 HTTP Method
        config.setAllowedMethods(List.of("*"));
        // 허용할 헤더
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화 (JWT 사용으로 불필요)
            .csrf(AbstractHttpConfigurer::disable)

            // 세션 사용하지 않음 (JWT 기반 Stateless 인증)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // CORS 설정 적용
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // JWT 인증 필터 추가 (UsernamePasswordAuthenticationFilter 전에 실행)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

            // 인증 실패 시 401 반환 (Spring Security 기본 403 방지)
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(unauthorizedEntryPoint())
            )

            // 요청별 인증 설정
            .authorizeHttpRequests(auth -> auth
                // 인증 없이 접근 가능한 엔드포인트
                .requestMatchers(
                        "/api/v1/auth/login/**",          // 소셜 로그인
                        "/api/v1/auth/refresh",           // 토큰 리프레시
                        "/api/swagger-ui/**",             // Swagger UI
                        "/api/v3/api-docs/**",            // OpenAPI docs
                        "/actuator/**"                    // 모니터링 (내부망 전용)
                ).permitAll()

                // 그 외 모든 요청은 인증 필요
                .anyRequest().authenticated()
            )

            // 폼 로그인, HTTP Basic 인증 비활성화
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            var errorResponse = ErrorResponse.of(
                    ErrorCode.INVALID_TOKEN
            );
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        };
    }
}
