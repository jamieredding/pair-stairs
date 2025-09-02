package dev.coldhands.pair.stairs.backend.infrastructure.web.controller;

import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.CurrentUserDto;
import dev.coldhands.pair.stairs.backend.usecase.UserDisplayNameService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CurrentUserController {

    private final UserDisplayNameService userDisplayNameService = new UserDisplayNameService();

    @GetMapping("/api/v1/me")
    public CurrentUserDto getCurrentUser(@AuthenticationPrincipal OidcUser user) {
        final var userInfo = user.getUserInfo();

        return new CurrentUserDto(userInfo.getFullName(), userDisplayNameService.getDisplayNameFor(userInfo));
    }
}
