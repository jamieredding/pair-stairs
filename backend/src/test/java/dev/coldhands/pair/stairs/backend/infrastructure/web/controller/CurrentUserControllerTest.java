package dev.coldhands.pair.stairs.backend.infrastructure.web.controller;

import dev.coldhands.pair.stairs.backend.domain.UserName;
import dev.coldhands.pair.stairs.backend.usecase.UserDetailsService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@Transactional
public class CurrentUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDetailsService userDetailsService;

    @ParameterizedTest
    @MethodSource
    void whenAnonymousUserThenReturnUnauthorized(HttpMethod httpMethod, String uri) throws Exception {
        mockMvc.perform(request(httpMethod, URI.create(uri))
                        .with(anonymous()))
                .andExpect(status().isUnauthorized());
    }

    static java.util.stream.Stream<Arguments> whenAnonymousUserThenReturnUnauthorized() {
        return java.util.stream.Stream.of(
                Arguments.of(HttpMethod.GET, "/api/v1/me")
        );
    }

    @Nested
    class Names {

        @Test
        void returnADisplayNameForALoggedInUser() throws Exception {
            final var oidcSub = UUID.randomUUID().toString();
            userDetailsService.createOrUpdate(oidcSub, new UserName(null, null, "Full Name"));

            mockMvc.perform(get("/api/v1/me")
                            .with(oidcLogin().userInfoToken(builder ->
                                    builder.name("Full Name")
                                            .subject(oidcSub))
                            )
                    )
                    .andExpect(status().isOk())
                    .andExpect(content().json("""
                            {
                              "fullName": "Full Name",
                              "displayName": "Full"
                            }
                            """))
            ;
        }
    }
}
