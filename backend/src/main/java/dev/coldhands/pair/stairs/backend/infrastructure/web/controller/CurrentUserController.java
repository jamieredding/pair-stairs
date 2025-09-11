package dev.coldhands.pair.stairs.backend.infrastructure.web.controller;

import dev.coldhands.pair.stairs.backend.domain.User;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.UserRepository;
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.CurrentUserDto;
import dev.coldhands.pair.stairs.backend.usecase.UserDetailsService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnBooleanProperty("app.feature.flag.teams.enabled")
public class CurrentUserController {

    private final UserDetailsService userDetailsService;

    public CurrentUserController(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @GetMapping("/api/v1/me")
    public CurrentUserDto getCurrentUser(@AuthenticationPrincipal OidcUser oidcUser) {
        final var userInfo = oidcUser.getUserInfo();

        final var user = userDetailsService.getUserByOidcSub(userInfo.getSubject());

        return new CurrentUserDto(userInfo.getFullName(), user.displayName());
    }
}
