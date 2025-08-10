package dev.coldhands.pair.stairs.backend.infrastructure;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
public class HealthEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthIsConfigured() throws Exception {
        mockMvc.perform(get("/actuator/health")
                        .with(anonymous())
                )
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "status": "UP"
                        }
                        """));
    }

    @Test
    void readinessIsConfigured() throws Exception {
        mockMvc.perform(get("/actuator/health/readiness")
                        .with(anonymous())
                )
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "status": "UP"
                        }
                        """));
    }

    @Test
    void livenessIsConfigured() throws Exception {
        mockMvc.perform(get("/actuator/health/liveness")
                        .with(anonymous())
                )
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "status": "UP"
                        }
                        """));
    }

}
