package dev.coldhands.pair.stairs.backend.usecase;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.*;

class UserDisplayNameServiceTest {

    private final UserDisplayNameService underTest = new UserDisplayNameService();

    @ParameterizedTest
    @MethodSource
    void getDisplayNameFor(Map<String, Object> claims, String expectedDisplayName) {
        assertThat(underTest.getDisplayNameFor(new OidcUserInfo(claims)))
                .isEqualTo(expectedDisplayName);
    }

    static Stream<Arguments> getDisplayNameFor() {
        return Stream.of(
                Arguments.of(Map.of(SUB, "some-subject id"), "Unknown"),
                Arguments.of(Map.of(NAME, "Full Name"), "Full"),
                Arguments.of(Map.of(NAME, "Full"), "Full"),
                Arguments.of(Map.of(NAME, "Full Name", GIVEN_NAME, "First"), "First"),
                Arguments.of(Map.of(NAME, "Full Name", GIVEN_NAME, "First", NICKNAME, "Nickname"), "Nickname")
        );
    }
}