package dev.coldhands.pair.stairs.backend.infrastructure;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
public class PrometheusMetricsEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void prometheusMetricsIsConfigured() throws Exception {
        mockMvc.perform(get("/actuator/prometheus")
                        .with(anonymous())
                )
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("jvm_memory_committed_bytes")));
    }

}
