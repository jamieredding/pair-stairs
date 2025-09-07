package dev.coldhands.pair.stairs.backend.usecase;

import dev.coldhands.pair.stairs.backend.domain.UserName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class UserDisplayNameServiceTest {

    private final UserDisplayNameService underTest = new UserDisplayNameService();

    @ParameterizedTest
    @MethodSource
    void getDisplayNameFor(UserName userName, String expectedDisplayName) {
        assertThat(underTest.getDisplayNameFor(userName))
                .isEqualTo(expectedDisplayName);
    }

    static Stream<Arguments> getDisplayNameFor() {
        return Stream.of(
                Arguments.of(new UserName(null, null, null), "Unknown"),
                Arguments.of(new UserName(null, null, "Full Name"), "Full"),
                Arguments.of(new UserName(null, null, "Full") , "Full"),
                Arguments.of(new UserName(null, "First", "Full Name"), "First"),
                Arguments.of(new UserName("Nickname", "First", "Full Name"), "Nickname")
        );
    }
}