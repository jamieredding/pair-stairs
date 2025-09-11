package dev.coldhands.pair.stairs.backend.infrastructure.web.security;

import dev.coldhands.pair.stairs.backend.domain.UserName;
import dev.coldhands.pair.stairs.backend.usecase.UserDetailsService;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

@SuppressWarnings("ClassCanBeRecord")
public class AuthenticationEvents {

    private final UserDetailsService userDetailsService;

    public AuthenticationEvents(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        final Authentication authentication = event.getAuthentication();

        if (authentication.getPrincipal() instanceof OidcUser oidcUser) {
            final var userInfo = oidcUser.getUserInfo();

            final var userName = new UserName(
                    userInfo.getNickName(),
                    userInfo.getGivenName(),
                    userInfo.getFullName()
            );

            userDetailsService.createOrUpdate(userInfo.getSubject(), userName);
        }
    }
}
