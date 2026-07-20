package com.project.config;

import com.project.filters.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@Configuration
@EnableMethodSecurity
public class SpringSecurity {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher(EndpointRequest.toAnyEndpoint())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()
                        .requestMatchers(HttpMethod.POST, "/security/registration/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/security/generate").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/swagger-config").permitAll()
                        .requestMatchers("/v3/api-docs").permitAll()
                        .requestMatchers(HttpMethod.GET, "/security/**").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/parking").permitAll()
                        .requestMatchers(HttpMethod.GET, "/parking/spot/**").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/parking/user/**").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/parking/**").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/parking/**").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/parking/**").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/bookings/**").hasAnyRole("RENTER", "OWNER")
                        .requestMatchers(HttpMethod.GET, "/users").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/users/{id}").hasAnyRole( "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/users/info/myself").hasAnyRole("OWNER", "RENTER")
                        .requestMatchers(HttpMethod.POST, "/users/**").hasAnyRole( "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/users/**").hasAnyRole("ADMIN", "OWNER", "RENTER")
                        .requestMatchers(HttpMethod.DELETE, "/users/**").hasAnyRole("OWNER", "ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}
