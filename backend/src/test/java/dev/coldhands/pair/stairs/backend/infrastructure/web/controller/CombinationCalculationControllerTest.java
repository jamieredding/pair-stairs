package dev.coldhands.pair.stairs.backend.infrastructure.web.controller;

import dev.coldhands.pair.stairs.backend.domain.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CombinationCalculationController.class)
public class CombinationCalculationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CombinationCalculationService service;

    @Nested
    class Calculate {

        @Test
        void calculateCombinations() throws Exception {
            when(service.calculate(any(), any(), anyInt()))
                    .thenReturn(List.of(
                            new ScoredCombination(10,
                                    List.of(new PairStream(
                                                    List.of(
                                                            new DeveloperInfo(0, "dev-0"),
                                                            new DeveloperInfo(1, "dev-1")
                                                    ),
                                                    new StreamInfo(0, "stream-a")
                                            ),
                                            new PairStream(
                                                    List.of(
                                                            new DeveloperInfo(2, "dev-2")
                                                    ),
                                                    new StreamInfo(1, "stream-b")
                                            ))
                            ),
                            new ScoredCombination(20,
                                    List.of(new PairStream(
                                                    List.of(
                                                            new DeveloperInfo(0, "dev-0"),
                                                            new DeveloperInfo(2, "dev-2")
                                                    ),
                                                    new StreamInfo(0, "stream-a")
                                            ),
                                            new PairStream(
                                                    List.of(
                                                            new DeveloperInfo(1, "dev-1")
                                                    ),
                                                    new StreamInfo(1, "stream-b")
                                            ))
                            )));

            mockMvc.perform(post("/api/v1/combinations/calculate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "developerIds": [0, 1, 2],
                                      "streamIds": [0, 1]
                                    }""")
                    )
                    .andExpect(status().isOk())
                    .andExpect(content().json("""
                            [
                              {
                                "score": 10,
                                "combination": [
                                  {
                                    "developers": [
                                      {
                                        "id": 0,
                                        "displayName": "dev-0"
                                      },
                                      {
                                        "id": 1,
                                        "displayName": "dev-1"
                                      }
                                    ],
                                    "stream": {
                                      "id": 0,
                                      "displayName": "stream-a"
                                    }
                                  },
                                  {
                                    "developers": [
                                      {
                                        "id": 2,
                                        "displayName": "dev-2"
                                      }
                                    ],
                                    "stream": {
                                      "id": 1,
                                      "displayName": "stream-b"
                                    }
                                  }
                                ]
                              },
                              {
                                "score": 20,
                                "combination": [
                                  {
                                    "developers": [
                                      {
                                        "id": 0,
                                        "displayName": "dev-0"
                                      },
                                      {
                                        "id": 2,
                                        "displayName": "dev-2"
                                      }
                                    ],
                                    "stream": {
                                      "id": 0,
                                      "displayName": "stream-a"
                                    }
                                  },
                                  {
                                    "developers": [
                                      {
                                        "id": 1,
                                        "displayName": "dev-1"
                                      }
                                    ],
                                    "stream": {
                                      "id": 1,
                                      "displayName": "stream-b"
                                    }
                                  }
                                ]
                              }
                            ]"""))
            ;

            verify(service).calculate(eq(List.of(0L, 1L, 2L)), eq(List.of(0L, 1L)), eq(0));
        }

        @Test
        void calculateCombinationsRequestingPage2() throws Exception {
            when(service.calculate(any(), any(), anyInt()))
                    .thenReturn(List.of(
                            new ScoredCombination(0, List.of()),
                            new ScoredCombination(1, List.of())));

            mockMvc.perform(post("/api/v1/combinations/calculate")
                            .queryParam("page", "2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "developerIds": [],
                                      "streamIds": []
                                    }""")
                    )
                    .andExpect(status().isOk())
            ;

            verify(service).calculate(any(), any(), eq(2));
        }

    }
}
