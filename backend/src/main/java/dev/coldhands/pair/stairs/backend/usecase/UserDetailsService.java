package dev.coldhands.pair.stairs.backend.usecase;

import dev.coldhands.pair.stairs.backend.domain.User;
import dev.coldhands.pair.stairs.backend.domain.UserName;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.UserEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.UserRepository;

import static java.util.Optional.ofNullable;

public class UserDetailsService {

    private final UserRepository userRepository;
    private final UserDisplayNameService userDisplayNameService = new UserDisplayNameService();

    public UserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    User createOrUpdate(String oidcSub,
                        UserName userName) {
        final String displayName = userDisplayNameService.getDisplayNameFor(userName);

        final UserEntity toPersist = ofNullable(userRepository.findByOidcSub(oidcSub))
                .map(existing -> {
                    existing.setDisplayName(displayName);
                    return existing;
                })
                .orElseGet(() -> new UserEntity(oidcSub, displayName));

        final UserEntity userEntity = userRepository.saveAndFlush(toPersist);

        return new User(
                userEntity.getId(),
                userEntity.getOidcSub(),
                userEntity.getDisplayName()
        );
    }
}
