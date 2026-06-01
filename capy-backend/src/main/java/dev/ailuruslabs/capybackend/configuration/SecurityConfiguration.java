package dev.ailuruslabs.capybackend.configuration;

import dev.ailuruslabs.capybackend.domain.UserProfile;
import dev.ailuruslabs.capybackend.repository.UserProfileRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Set;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, UserProfileRepository userRepository) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/ws-capy/**", "/app/**", "/topic/**", "/queue/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        // Force redirect back to the React app after a successful login
                        .defaultSuccessUrl("http://localhost:5173/dashboard", true)
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(this.oAuth2UserService(userRepository))
                        )
                );

        return http.build();
    }

    private OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService(UserProfileRepository userProfileRepository) {
        var delegate = new DefaultOAuth2UserService();

        return userRequest -> {
            var oAuth2User = delegate.loadUser(userRequest);

            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            String picture = oAuth2User.getAttribute("picture");
            var existingUser = userProfileRepository.findByEmail(email)
                                            .orElseGet(() -> new UserProfile(null, name, email, picture, Set.of(),
                                                                             Set.of()));

            var updatedUser = new UserProfile(
                    existingUser.id(),
                    name,
                    email,
                    picture,
                    existingUser.offeredSkills(),
                    existingUser.requestedSkills()
            );

            userProfileRepository.save(updatedUser);

            return oAuth2User;
        };
    }
}
