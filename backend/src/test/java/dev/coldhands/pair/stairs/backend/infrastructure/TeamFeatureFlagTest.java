package dev.coldhands.pair.stairs.backend.infrastructure;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
        "app.feature.flag.teams.enabled=false"
})
public class TeamFeatureFlagTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void currentUserApiIsDisabled() throws Exception {
        mockMvc.perform(get("/api/v1/me")
                        .with(anonymous()))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void apiEndpointsAreNotProtected() throws Exception {
        mockMvc.perform(get("/api/v1/streams")
                        .with(anonymous()))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void teamFeatureFlagIsFalse() throws Exception {
        mockMvc.perform(get("/api/v1/feature-flags")
                        .with(anonymous()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamsEnabled").value(false));
    }

}
