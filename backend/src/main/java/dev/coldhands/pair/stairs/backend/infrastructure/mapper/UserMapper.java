package dev.coldhands.pair.stairs.backend.infrastructure.mapper;

import dev.coldhands.pair.stairs.backend.domain.User;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.UserEntity;

public final class UserMapper {

    private UserMapper() {}

    public static User entityToDomain(UserEntity userEntity) {
        return new User(
                userEntity.getId(),
                userEntity.getOidcSub(),
                userEntity.getDisplayName()
        );
    }
}
