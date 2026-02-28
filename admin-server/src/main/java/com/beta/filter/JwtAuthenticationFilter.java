package com.beta.filter;

import com.beta.core.exception.ErrorCode;
import com.beta.core.response.ErrorResponse;
import com.beta.core.security.JwtTokenProvider;
import com.beta.security.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String ADMIN_CLIENT = "ADMIN";

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String token = extractTokenFromRequest(request);

            if (token != null) {
                if (jwtTokenProvider.isTokenExpired(token)) {
                    sendErrorResponse(response, ErrorResponse.of(ErrorCode.TOKEN_EXPIRED));
                    return;
                }

                if (!jwtTokenProvider.isTokenValid(token)) {
                    sendErrorResponse(response, ErrorResponse.of(ErrorCode.INVALID_TOKEN));
                    return;
                }

                String userId = jwtTokenProvider.getSubject(token);
                String teamCode = jwtTokenProvider.getClaim(token, JwtTokenProvider.ClaimEnum.TEAM_CODE.name(), String.class);
                String role = jwtTokenProvider.getClaim(token, JwtTokenProvider.ClaimEnum.ROLE.name(), String.class);
                String client = jwtTokenProvider.getClaim(token, JwtTokenProvider.ClaimEnum.CLIENT.name(), String.class);

                if (userId != null && role != null && ADMIN_CLIENT.equals(client)) {
                    CustomUserDetails userDetails = new CustomUserDetails(Long.valueOf(userId), teamCode, role, client);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Successfully authenticated admin request userId={}", userId);
                } else {
                    sendErrorResponse(response, ErrorResponse.of(ErrorCode.INVALID_TOKEN));
                    return;
                }
            }
        } catch (Exception e) {
            log.warn("JWT authentication failed: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            sendErrorResponse(response, ErrorResponse.of(ErrorCode.UNAUTHORIZED));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void sendErrorResponse(HttpServletResponse response, ErrorResponse errorResponse) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
