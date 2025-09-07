package dev.coldhands.pair.stairs.backend.infrastructure.web.controller;

import dev.coldhands.pair.stairs.backend.domain.UserName;
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.CurrentUserDto;
import dev.coldhands.pair.stairs.backend.usecase.UserDisplayNameService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnBooleanProperty("app.feature.flag.teams.enabled")
public class CurrentUserController {

    private final UserDisplayNameService userDisplayNameService = new UserDisplayNameService();

    // TODO update me to use user details service
    @GetMapping("/api/v1/me")
    public CurrentUserDto getCurrentUser(@AuthenticationPrincipal OidcUser user) {
        final var userInfo = user.getUserInfo();

        final UserName userName = new UserName(
                userInfo.getNickName(),
                userInfo.getGivenName(),
                userInfo.getFullName()
        );

        return new CurrentUserDto(userInfo.getFullName(), userDisplayNameService.getDisplayNameFor(userName));
    }
}
