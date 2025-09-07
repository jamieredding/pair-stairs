package dev.coldhands.pair.stairs.backend.usecase;

import dev.coldhands.pair.stairs.backend.domain.UserName;

public class UserDisplayNameService {

    public String getDisplayNameFor(UserName userName) {

        if (userName.nickName() != null) {
            return userName.nickName();
        }

        if (userName.givenName() != null) {
            return userName.givenName();
        }

        if (userName.fullName() != null) {
            final var fullName = userName.fullName();

            return fullName.split(" ", 2)[0];
        }

        return "Unknown";
    }
}
