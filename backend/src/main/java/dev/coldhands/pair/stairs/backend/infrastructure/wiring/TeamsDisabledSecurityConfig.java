package dev.coldhands.pair.stairs.backend.infrastructure.wiring;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@ConditionalOnBooleanProperty(value = "app.feature.flag.teams.enabled", havingValue = false)
public class TeamsDisabledSecurityConfig {

    @Bean
    SecurityFilterChain noSecurityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(a ->
                        a.anyRequest().permitAll()
                )
                .csrf(c -> c.ignoringRequestMatchers("/api/**"))
                .build();
    }

}
