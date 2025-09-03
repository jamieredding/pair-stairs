package dev.coldhands.pair.stairs.backend.infrastructure.web.controller;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.DeveloperEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.StreamEntity;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
@TestPropertySource(properties = {
        "app.combinations.calculate.pageSize=2"
})
public class CombinationCalculationControllerTest {

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

    static java.util.stream.Stream<Arguments> whenAnonymousUserThenReturnUnauthorized() {
        return java.util.stream.Stream.of(
                Arguments.of(HttpMethod.POST, "/api/v1/combinations/calculate")
        );
    }

    @Nested
    class Calculate {

        @Test
        void calculateCombinationsHasADefaultPageSize() throws Exception {
            final Long dev0Id = testEntityManager.persist(new DeveloperEntity("dev-0")).getId();
            final Long dev1Id = testEntityManager.persist(new DeveloperEntity("dev-1")).getId();
            final Long dev2Id = testEntityManager.persist(new DeveloperEntity("dev-2")).getId();

            final Long stream0Id = testEntityManager.persist(new StreamEntity("stream-a")).getId();
            final Long stream1Id = testEntityManager.persist(new StreamEntity("stream-b")).getId();

            final String contentAsString = mockMvc.perform(post("/api/v1/combinations/calculate")
                            .with(oidcLogin())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "developerIds": [%s, %s, %s],
                                      "streamIds": [%s, %s]
                                    }""".formatted(dev0Id, dev1Id, dev2Id, stream0Id, stream1Id))
                    )
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse().getContentAsString();

            final DocumentContext parsed = JsonPath.parse(contentAsString);

            assertThat(parsed.read("$", List.class)).size().isEqualTo(2);
        }

        @Test
        void calculateCombinationsRequestingPageWithNoResults() throws Exception {
            final Long dev0Id = testEntityManager.persist(new DeveloperEntity("dev-0")).getId();
            final Long dev1Id = testEntityManager.persist(new DeveloperEntity("dev-1")).getId();
            final Long dev2Id = testEntityManager.persist(new DeveloperEntity("dev-2")).getId();

            final Long stream0Id = testEntityManager.persist(new StreamEntity("stream-a")).getId();
            final Long stream1Id = testEntityManager.persist(new StreamEntity("stream-b")).getId();

            final String contentAsString = mockMvc.perform(post("/api/v1/combinations/calculate")
                            .queryParam("page", "10")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "developerIds": [%s, %s, %s],
                                      "streamIds": [%s, %s]
                                    }""".formatted(dev0Id, dev1Id, dev2Id, stream0Id, stream1Id))
                    )
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse().getContentAsString();

            final DocumentContext parsed = JsonPath.parse(contentAsString);

            assertThat(parsed.read("$", List.class)).size().isEqualTo(0);
        }

        @Test
        void returnBadRequestWhenThereAreNotEnoughDevelopersToBeAbleToPair() throws Exception {
            final Long dev0Id = testEntityManager.persist(new DeveloperEntity("dev-0")).getId();
            final Long dev1Id = testEntityManager.persist(new DeveloperEntity("dev-1")).getId();

            final Long stream0Id = testEntityManager.persist(new StreamEntity("stream-a")).getId();
            final Long stream1Id = testEntityManager.persist(new StreamEntity("stream-b")).getId();

            final String contentAsString = mockMvc.perform(post("/api/v1/combinations/calculate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "developerIds": [%s, %s],
                                      "streamIds": [%s, %s]
                                    }""".formatted(dev0Id, dev1Id, stream0Id, stream1Id))
                    )
                    .andExpect(status().isBadRequest())
                    .andReturn()
                    .getResponse().getContentAsString();

            final DocumentContext parsed = JsonPath.parse(contentAsString);

            assertThat(parsed.read("$.errorCode", String.class)).isEqualTo("NOT_ENOUGH_DEVELOPERS");
        }

        @Test
        void returnBadRequestWhenThereAreNotEnoughStreamsToBeAbleToPair() throws Exception {
            final Long dev0Id = testEntityManager.persist(new DeveloperEntity("dev-0")).getId();
            final Long dev1Id = testEntityManager.persist(new DeveloperEntity("dev-1")).getId();
            final Long dev2Id = testEntityManager.persist(new DeveloperEntity("dev-2")).getId();

            final Long stream0Id = testEntityManager.persist(new StreamEntity("stream-a")).getId();

            final String contentAsString = mockMvc.perform(post("/api/v1/combinations/calculate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "developerIds": [%s, %s, %s],
                                      "streamIds": [%s]
                                    }""".formatted(dev0Id, dev1Id, dev2Id, stream0Id))
                    )
                    .andExpect(status().isBadRequest())
                    .andReturn()
                    .getResponse().getContentAsString();

            final DocumentContext parsed = JsonPath.parse(contentAsString);

            assertThat(parsed.read("$.errorCode", String.class)).isEqualTo("NOT_ENOUGH_STREAMS");
        }
    }
}
