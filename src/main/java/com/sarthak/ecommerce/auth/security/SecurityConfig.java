package com.sarthak.ecommerce.auth.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                // Disable CSRF for REST APIs
                .csrf(csrf -> csrf.disable())

                // No HTTP session; authentication will be handled using JWT
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                // Authorization rules
                .authorizeHttpRequests(auth -> auth

                        // Authentication APIs are public
                        .requestMatchers("/api/auth/**").permitAll()

                        // Anyone can browse products
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()

                        // Only sellers and admins can create products
                        .requestMatchers(HttpMethod.POST, "/api/products/**")
                        .hasAnyRole("SELLER", "ADMIN")

                        // Only sellers and admins can update products
                        .requestMatchers(HttpMethod.PUT, "/api/products/**")
                        .hasAnyRole("SELLER", "ADMIN")

                        // Only sellers and admins can delete products
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**")
                        .hasAnyRole("SELLER", "ADMIN")

                        // Only admins can see all users
                        .requestMatchers(HttpMethod.GET, "/api/users")
                        .hasRole("ADMIN")

                        // Authenticated users can access a specific user
                        .requestMatchers(HttpMethod.GET, "/api/users/**")
                        .authenticated()

                        // Authenticated users can update a specific user
                        .requestMatchers(HttpMethod.PUT, "/api/users/**")
                        .authenticated()

                        // Authenticated users can delete a specific user
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**")
                        .authenticated()

                        .anyRequest().authenticated()


                );

        return http.build();
    }
}