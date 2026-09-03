package com.keepguard.ms_auth.infrastructure.config.security;

import com.keepguard.ms_auth.infrastructure.filter.CorrelationIdFilter;
import com.keepguard.ms_auth.infrastructure.filter.JwtAuditMdcFilter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CorrelationIdFilter correlationIdFilter;
    private final JwtAuditMdcFilter jwtAuditMdcFilter;

    public SecurityConfig(CorrelationIdFilter correlationIdFilter, JwtAuditMdcFilter jwtAuditMdcFilter) {
        this.correlationIdFilter = correlationIdFilter;
        this.jwtAuditMdcFilter = jwtAuditMdcFilter;
    }

    @Bean
    public FilterRegistrationBean<JwtAuditMdcFilter> jwtAuditMdcFilterRegistration(JwtAuditMdcFilter filter) {
        FilterRegistrationBean<JwtAuditMdcFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(correlationIdFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtAuditMdcFilter, BearerTokenAuthenticationFilter.class)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/oauth/token").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/oauth/runtime/**").permitAll()
                        .requestMatchers("/api/v1/auth/oauth/clients/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/users/block/**", "/api/v1/users/unlock/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users/delete/**").authenticated()
                        .requestMatchers("/api/v1/users/me/**").authenticated()
                        .requestMatchers("/api/v1/users/*/sessions", "/api/v1/users/*/sessions/**").authenticated()
                        .requestMatchers("/api/v1/users/*/devices/blacklist", "/api/v1/users/*/devices/blacklist/**").authenticated()
                        .requestMatchers("/api/v1/devices/blacklist", "/api/v1/devices/blacklist/**").authenticated()
                        .requestMatchers("/api/v1/sessions", "/api/v1/sessions/**").authenticated()
                        .requestMatchers("/api/v1/admin/devices/blacklist", "/api/v1/admin/devices/blacklist/**").authenticated()
                        .requestMatchers("/api/v1/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("*");
        configuration.addAllowedOrigin("http://localhost:3000");
        configuration.addAllowedOrigin("http://localhost:3001");
        configuration.addAllowedOrigin("http://127.0.0.1:3000");
        configuration.addAllowedOrigin("http://127.0.0.1:3001");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}