package dev.coldhands.pair.stairs.backend.infrastructure.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.coldhands.pair.stairs.backend.domain.Stream;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        void whenMultipleStreamsThenReturnThem() throws Exception {
            final Long stream0Id = testEntityManager.persist(new Stream("stream-0")).getId();
            final Long stream1Id = testEntityManager.persist(new Stream("stream-1")).getId();

            mockMvc.perform(get("/api/v1/streams"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$.[0].id").value(stream0Id))
                    .andExpect(jsonPath("$.[0].name").value("stream-0"))
                    .andExpect(jsonPath("$.[1].id").value(stream1Id))
                    .andExpect(jsonPath("$.[1].name").value("stream-1"))

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
            final Long actualId = stream.getId();

            final Stream savedStream = testEntityManager.find(Stream.class, actualId);

            assertThat(savedStream.getId(), equalTo(actualId));
            assertThat(savedStream.getName(), equalTo("stream-0"));
        }

    }
}
