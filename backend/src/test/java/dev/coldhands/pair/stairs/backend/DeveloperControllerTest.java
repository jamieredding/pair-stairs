package dev.coldhands.pair.stairs.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.coldhands.pair.stairs.backend.domain.Developer;
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
public class DeveloperControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestEntityManager testEntityManager;

    @Nested
    class Read {

        @Test
        void whenNoDevelopersThenReturnEmptyArray() throws Exception {
            mockMvc.perform(get("/api/v1/developers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        void whenMultipleDevelopersThenReturnThem() throws Exception {
            testEntityManager.persist(new Developer("dev-0"));
            testEntityManager.persist(new Developer("dev-1"));

            mockMvc.perform(get("/api/v1/developers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$.[0].name").value("dev-0"))
                    .andExpect(jsonPath("$.[1].name").value("dev-1"))

            ;
        }
    }

    @Nested
    class Write {

        @Test
        void saveADeveloper() throws Exception {
            final MvcResult result = mockMvc.perform(post("/api/v1/developers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "name": "dev-0"
                                    }""")
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.name").value("dev-0"))
                    .andReturn();

            final Developer developer = new ObjectMapper().readValue(result.getResponse().getContentAsString(), Developer.class);
            final Long actualId = developer.getId();

            final Developer savedDeveloper = testEntityManager.find(Developer.class, actualId);

            assertThat(savedDeveloper.getId(), equalTo(actualId));
            assertThat(savedDeveloper.getName(), equalTo("dev-0"));
        }

    }
}
