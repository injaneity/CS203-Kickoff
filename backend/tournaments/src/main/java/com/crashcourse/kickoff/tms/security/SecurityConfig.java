package com.crashcourse.kickoff.tms.security;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;

    public SecurityConfig(JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests((authz) -> authz
                        .requestMatchers(HttpMethod.POST, "/tournaments/webhook").permitAll()
                        .requestMatchers(HttpMethod.POST, "/clubs").permitAll()
                        .requestMatchers(HttpMethod.POST, "/users/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/**").authenticated()
                        .anyRequest().permitAll())
                .sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(Customizer.withDefaults())
                .csrf(csrf -> csrf
                    .ignoringRequestMatchers("/tournaments/webhook"))
                .formLogin(form -> form.disable())
                .headers(header -> header.disable());

        // Add JWT filter before the default UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        configuration.addAllowedHeader("Stripe-Signature");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}