package dev.coldhands.pair.stairs.backend.usecase;

import dev.coldhands.pair.stairs.backend.domain.User;
import dev.coldhands.pair.stairs.backend.domain.UserName;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.UserEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.UserRepository;

public class UserDetailsService {

    private final UserRepository userRepository;
    private final UserDisplayNameService userDisplayNameService = new UserDisplayNameService();

    public UserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    User createOrUpdate(String oidcSub,
                        UserName userName) {
        final String displayName = userDisplayNameService.getDisplayNameFor(userName);
        final UserEntity userEntity = userRepository.save(new UserEntity(oidcSub, displayName));

        return new User(
                userEntity.getId(),
                userEntity.getOidcSub(),
                userEntity.getDisplayName()
        );
    }
}
