package dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository;

import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    UserEntity findByOidcSub(String oidcSub);
}
