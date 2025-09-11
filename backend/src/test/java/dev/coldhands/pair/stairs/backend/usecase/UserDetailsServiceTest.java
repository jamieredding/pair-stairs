package dev.coldhands.pair.stairs.backend.usecase;

import dev.coldhands.pair.stairs.backend.domain.User;
import dev.coldhands.pair.stairs.backend.domain.UserName;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.UserEntity;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
class UserDetailsServiceTest {

    @Autowired
    private UserDetailsService underTest;

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    void createUserDetailsWhenUserDoesNotExist() {
        var userName = new UserName(
                null,
                null,
                "Jamie Redding"
        );
        var oidcSub = UUID.randomUUID().toString();

        final User user = underTest.createOrUpdate(oidcSub, userName);

        final UserEntity userEntity = testEntityManager.find(UserEntity.class, user.id());

        assertThat(userEntity).isNotNull();
        assertThat(userEntity.getId()).isEqualTo(user.id());
        assertThat(userEntity.getOidcSub()).isEqualTo(oidcSub);
        assertThat(userEntity.getDisplayName()).isEqualTo("Jamie");
        assertThat(userEntity.getCreatedAt()).isCloseTo(Instant.now(), within(Duration.of(1, SECONDS)));
        assertThat(userEntity.getUpdatedAt()).isCloseTo(Instant.now(), within(Duration.of(1, SECONDS)));
    }

    @Test
    void updateUserDetailsWhenUserDoesExist() {
        var oidcSub = UUID.randomUUID().toString();

        final User user = underTest.createOrUpdate(oidcSub, new UserName(
                null,
                null,
                "Jamie Redding"
        ));
        final UserEntity initialEntity = testEntityManager.find(UserEntity.class, user.id());
        final Instant initialCreatedAt = initialEntity.getCreatedAt();
        final Instant initialUpdatedAt = initialEntity.getUpdatedAt();

        underTest.createOrUpdate(oidcSub, new UserName(
                "Jay",
                null,
                "Jamie Redding"
        ));

        final UserEntity userEntity = testEntityManager.find(UserEntity.class, user.id());

        assertThat(userEntity).isNotNull();
        assertThat(userEntity.getId()).isEqualTo(user.id());
        assertThat(userEntity.getOidcSub()).isEqualTo(oidcSub);
        assertThat(userEntity.getDisplayName()).isEqualTo("Jay");
        assertThat(userEntity.getCreatedAt()).isEqualTo(initialCreatedAt);
        assertThat(userEntity.getUpdatedAt()).isAfter(initialUpdatedAt);
    }
}