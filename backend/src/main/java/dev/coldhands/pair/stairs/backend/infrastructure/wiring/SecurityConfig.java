package dev.coldhands.pair.stairs.backend.infrastructure.wiring;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.DelegatingAuthenticationEntryPoint;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class SecurityConfig {

    @Bean
    @ConditionalOnBooleanProperty(value = "app.feature.flag.teams.enabled", havingValue = false)
    SecurityFilterChain noSecurityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(a -> a.anyRequest().permitAll())
                .build();
    }


    @Bean
    @ConditionalOnBooleanProperty("app.feature.flag.teams.enabled")
    SecurityFilterChain oauthSecurityFilterChain(HttpSecurity http) throws Exception {
        RequestMatcher apiMatcher = PathPatternRequestMatcher.withDefaults().matcher("/api/**");

        // Where to send browsers when they’re unauthenticated (non-API):
        var loginEntryPoint = new LoginUrlAuthenticationEntryPoint("/oauth2/authorization/oauth");
        // For API calls, return 401 (don’t HTML-redirect):
        AuthenticationEntryPoint apiEntryPoint = new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);

        var delegating = new DelegatingAuthenticationEntryPoint(
                new LinkedHashMap<>(Map.of(apiMatcher, apiEntryPoint)));
        delegating.setDefaultEntryPoint(loginEntryPoint);

        http
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/login**", "/oauth2/**", "/login/oauth2/**", "/error").permitAll()

                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/health/readiness",
                                "/actuator/health/liveness",
                                "/actuator/info",
                                "/actuator/prometheus"
                        )
                        .permitAll()

                        .requestMatchers(
                                "/api/v1/feature-flags"
                        )
                        .permitAll()

                        .anyRequest().authenticated()
                )
                // Send users directly to IDP (no intermediate default login page)
                .oauth2Login(o -> o.loginPage("/oauth2/authorization/oauth"))
                .logout(l -> l.logoutSuccessUrl("/"))
                // Make APIs return 401 instead of redirect
                .exceptionHandling(e -> e.authenticationEntryPoint(delegating))
                // While developing a cookie-session SPA, ignore CSRF for APIs
                .csrf(c -> c.ignoringRequestMatchers("/api/**"))
                .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()))
        ;

        return http.build();
    }
}
