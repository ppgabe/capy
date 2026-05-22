package dev.ailuruslabs.capybackend.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF since we aren't using traditional session cookies right now
                .csrf(AbstractHttpConfigurer::disable)
                // Allow the frontend to connect without hitting authentication blocks
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/matchmaking/**", "/ws-capy/**", "/app/**", "/topic/**", "/queue/**").permitAll()
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}
