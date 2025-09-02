package dev.coldhands.pair.stairs.backend.usecase;

import org.springframework.security.oauth2.core.oidc.OidcUserInfo;

public class UserDisplayNameService {

    public String getDisplayNameFor(OidcUserInfo userInfo) {
        if (userInfo.getNickName() != null) {
            return userInfo.getNickName();
        }

        if (userInfo.getGivenName() != null) {
            return userInfo.getGivenName();
        }

        if (userInfo.getFullName() != null) {
            final var fullName = userInfo.getFullName();

            return fullName.split(" ", 2)[0];
        }

        return "Unknown";
    }
}
