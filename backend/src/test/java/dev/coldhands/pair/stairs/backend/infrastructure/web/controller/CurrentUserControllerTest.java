package dev.coldhands.pair.stairs.backend.infrastructure.web.controller;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@Transactional
public class CurrentUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    class Names {

        @Test
        void returnADisplayNameForALoggedInUser() throws Exception {
            mockMvc.perform(get("/api/v1/me")
                            .with(oidcLogin().userInfoToken(builder -> builder.name("Full Name")))
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
