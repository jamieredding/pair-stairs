package dev.coldhands.pair.stairs.backend.infrastructure.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.TeamEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.TeamDto;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
public class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestEntityManager testEntityManager;

    @ParameterizedTest
    @MethodSource
    void whenAnonymousUserThenReturnUnauthorized(HttpMethod httpMethod, String uri) throws Exception {
        mockMvc.perform(request(httpMethod, URI.create(uri))
                        .with(anonymous()))
                .andExpect(status().isUnauthorized());
    }

    static Stream<Arguments> whenAnonymousUserThenReturnUnauthorized() {
        return Stream.of(
//                Arguments.of(HttpMethod.GET, "/api/v1/teams"),
//                Arguments.of(HttpMethod.GET, "/api/v1/developers/info"),
                Arguments.of(HttpMethod.POST, "/api/v1/teams")
//                Arguments.of(HttpMethod.GET, "/api/v1/developers/1/stats")
        );
    }

    @Nested
    class Write {

        @Test
        void saveATeam() throws Exception {
            final MvcResult result = mockMvc.perform(post("/api/v1/teams")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "name": "Team 0",
                                      "slug": "team-0"
                                    }""")
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.name").value("Team 0"))
                    .andExpect(jsonPath("$.slug").value("team-0"))
                    .andReturn();

            final TeamDto team = new ObjectMapper().readValue(result.getResponse().getContentAsString(), TeamDto.class);
            final Long actualId = team.id();

            final TeamEntity savedTeam = testEntityManager.find(TeamEntity.class, actualId);

            assertThat(savedTeam.getId()).isEqualTo(actualId);
            assertThat(savedTeam.getName()).isEqualTo("Team 0");
            assertThat(savedTeam.getSlug()).isEqualTo("team-0");
            assertThat(savedTeam.getCreatedAt()).isCloseTo(Instant.now(), within(Duration.of(1, SECONDS)));
            assertThat(savedTeam.getUpdatedAt()).isCloseTo(Instant.now(), within(Duration.of(1, SECONDS)));
        }

        @Nested
        class BadRequest {

            @Test
            void whenBodyIsEmpty() throws Exception {
                mockMvc.perform(post("/api/v1/teams")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                        )
                        .andExpect(status().isBadRequest());
            }

            @ParameterizedTest
            @NullAndEmptySource
            void whenNameIs(String name) throws Exception {
                mockMvc.perform(post("/api/v1/teams")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": %s,
                                          "slug": "team-0"
                                        }""".formatted(asJsonString(name)))
                        )
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.errorCode").value("INVALID_NAME"));
            }

            @ParameterizedTest
            @NullAndEmptySource
            void whenSlugIs(String slug) throws Exception {
                mockMvc.perform(post("/api/v1/teams")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": "Team 0",
                                          "slug": %s
                                        }""".formatted(asJsonString(slug)))
                        )
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.errorCode").value("INVALID_SLUG"));
            }

            @Test
            void whenSlugAlreadyExists() throws Exception {
                mockMvc.perform(post("/api/v1/teams")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": "Team 0",
                                          "slug": "team-0"
                                        }""")
                        )
                        .andExpect(status().isCreated());

                mockMvc.perform(post("/api/v1/teams")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": "Team 1",
                                          "slug": "team-0"
                                        }""")
                        )
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.errorCode").value("DUPLICATE_SLUG"));
            }

            @ParameterizedTest
            @ValueSource(strings = {
                    "A",
                    "abc#",
                    "abc "
            })
            void whenSlugIsInvalidFormat(String slug) throws Exception {
                mockMvc.perform(post("/api/v1/teams")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": "Team 0",
                                          "slug": %s
                                        }""".formatted(asJsonString(slug)))
                        )
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.errorCode").value("INVALID_SLUG"));
            }
        }

    }

    private static String asJsonString(String s) {
        return Optional.ofNullable(s).map(_ -> "\"" + s + "\"").orElse(null);
    }

}
