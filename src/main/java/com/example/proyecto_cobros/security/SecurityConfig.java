package com.example.proyecto_cobros.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Desactivar CSRF (para que el POST /usuarios/login funcione sin token)
            .csrf(csrf -> csrf.disable())

            // Desactivar TODAS las restricciones de autorización
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )

            // Desactivar el formLogin de Spring Security
            .formLogin(form -> form.disable())

            // Desactivar el logout de Spring Security
            .logout(logout -> logout.disable())

            // Desactivar el httpBasic
            .httpBasic(basic -> basic.disable());

        return http.build();
    }
}