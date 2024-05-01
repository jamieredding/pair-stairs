package dev.coldhands.pair.stairs.backend.infrastructure.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.coldhands.pair.stairs.backend.domain.Stream;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.StreamEntity;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
public class StreamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestEntityManager testEntityManager;

    @Nested
    class Read {

        @Test
        void whenNoStreamThenReturnEmptyArray() throws Exception {
            mockMvc.perform(get("/api/v1/streams"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("[]"));
        }

        @Test
        void whenMultipleStreamsThenReturnThem() throws Exception {
            final Long stream0Id = testEntityManager.persist(new StreamEntity("stream-0")).getId();
            final Long stream1Id = testEntityManager.persist(new StreamEntity("stream-1")).getId();

            mockMvc.perform(get("/api/v1/streams"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("""
                            [
                                {
                                    "id": %s,
                                    "name": "%s"
                                },
                                {
                                    "id": %s,
                                    "name": "%s"
                                }
                            ]""".formatted(stream0Id, "stream-0", stream1Id, "stream-1")))

            ;
        }
    }

    @Nested
    class ReadStreamInfo {

        @Test
        void whenNoStreamThenReturnEmptyArray() throws Exception {
            mockMvc.perform(get("/api/v1/streams/info"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("[]"));
        }

        @Test
        void whenMultipleStreamsThenReturnThem() throws Exception {
            final Long stream0Id = testEntityManager.persist(new StreamEntity("stream-0")).getId();
            final Long stream1Id = testEntityManager.persist(new StreamEntity("stream-1")).getId();

            mockMvc.perform(get("/api/v1/streams/info"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("""
                            [
                                {
                                    "id": %s,
                                    "displayName": "%s"
                                },
                                {
                                    "id": %s,
                                    "displayName": "%s"
                                }
                            ]""".formatted(stream0Id, "stream-0", stream1Id, "stream-1")))

            ;
        }
    }


    @Nested
    class Write {

        @Test
        void saveAStream() throws Exception {
            final MvcResult result = mockMvc.perform(post("/api/v1/streams")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "name": "stream-0"
                                    }""")
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.name").value("stream-0"))
                    .andReturn();

            final Stream stream = new ObjectMapper().readValue(result.getResponse().getContentAsString(), Stream.class);
            final Long actualId = stream.id();

            final StreamEntity savedStream = testEntityManager.find(StreamEntity.class, actualId);

            assertThat(savedStream.getId(), equalTo(actualId));
            assertThat(savedStream.getName(), equalTo("stream-0"));
        }

    }
}
