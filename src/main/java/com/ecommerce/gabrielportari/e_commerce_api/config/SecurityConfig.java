package com.ecommerce.gabrielportari.e_commerce_api.config;

import com.ecommerce.gabrielportari.e_commerce_api.auth.security.JwtFilter;
import com.ecommerce.gabrielportari.e_commerce_api.config.ratelimit.RateLimitFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final RateLimitFilter rateLimitFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // HSTS is only emitted by Spring Security when request.isSecure() is true. If TLS
                // terminates at a reverse proxy, that requires server.forward-headers-strategy=framework
                // (or native) PLUS a proxy that strips any client-supplied X-Forwarded-* — enabling it
                // without a trusted proxy in front would let clients spoof their own IP, undermining
                // RateLimitFilter. Left off here; turn it on only once the deployment topology is known.
                .headers(headers -> headers
                        .frameOptions(frame -> frame.deny())
                        .httpStrictTransportSecurity(
                                hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                        .contentSecurityPolicy(csp ->
                                csp.policyDirectives("default-src 'none'; img-src 'self'; frame-ancestors 'none'")))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/settings").permitAll()
                        .requestMatchers("/api/cart/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/products/*/reviews").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/reviews/**").hasRole("ADMIN")
                        .requestMatchers(
                                HttpMethod.POST, "/api/categories/**", "/api/products/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/categories/**", "/api/products/**")
                        .hasRole("ADMIN")
                        .requestMatchers(
                                HttpMethod.DELETE, "/api/categories/**", "/api/products/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/settings").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(rateLimitFilter, JwtFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
