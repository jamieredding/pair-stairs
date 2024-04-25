package dev.coldhands.pair.stairs.backend.infrastructure.web.controller;

import dev.coldhands.pair.stairs.backend.domain.CombinationService;
import dev.coldhands.pair.stairs.backend.domain.Developer;
import dev.coldhands.pair.stairs.backend.domain.Stream;
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.PairStreamDto;
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.ScoredCombinationDto;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CombinationController.class)
public class CombinationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CombinationService combinationService;

    @Nested
    class Calculate {

        @Captor
        ArgumentCaptor<List<Developer>> developersCaptor;
        @Captor
        ArgumentCaptor<List<Stream>> streamsCaptor;

        @Test
        void calculateCombinations() throws Exception {
            when(combinationService.calculate(any(), any(), anyInt()))
                    .thenReturn(List.of(
                            new ScoredCombinationDto(0,
                                    List.of(new PairStreamDto(List.of("dev-0", "dev-1"), "stream-a"),
                                            new PairStreamDto(List.of("dev-2"), "stream-b"))
                            ),
                            new ScoredCombinationDto(1,
                                    List.of(new PairStreamDto(List.of("dev-0", "dev-2"), "stream-a"),
                                            new PairStreamDto(List.of("dev-1"), "stream-b"))
                            )));

            mockMvc.perform(post("/api/v1/combinations/calculate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "developers": ["dev-0", "dev-1", "dev-2"],
                                      "streams": ["stream-a", "stream-b"]
                                    }""")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))

                    .andExpect(jsonPath("$.[0].score").value(0))
                    .andExpect(jsonPath("$.[0].combinations[0].developers[0]").value("dev-0"))
                    .andExpect(jsonPath("$.[0].combinations[0].developers[1]").value("dev-1"))
                    .andExpect(jsonPath("$.[0].combinations[0].stream").value("stream-a"))
                    .andExpect(jsonPath("$.[0].combinations[1].developers[0]").value("dev-2"))
                    .andExpect(jsonPath("$.[0].combinations[1].developers[1]").doesNotExist())
                    .andExpect(jsonPath("$.[0].combinations[1].stream").value("stream-b"))

                    .andExpect(jsonPath("$.[1].score").value(1))
                    .andExpect(jsonPath("$.[1].combinations[0].developers[0]").value("dev-0"))
                    .andExpect(jsonPath("$.[1].combinations[0].developers[1]").value("dev-2"))
                    .andExpect(jsonPath("$.[1].combinations[0].stream").value("stream-a"))
                    .andExpect(jsonPath("$.[1].combinations[1].developers[0]").value("dev-1"))
                    .andExpect(jsonPath("$.[1].combinations[1].developers[1]").doesNotExist())
                    .andExpect(jsonPath("$.[1].combinations[1].stream").value("stream-b"))
            ;

            verify(combinationService).calculate(developersCaptor.capture(), streamsCaptor.capture(), eq(0));

            assertThat(developersCaptor.getValue().stream()
                    .map(Developer::getName)
                    .toList(), Matchers.contains("dev-0", "dev-1", "dev-2"));
            assertThat(streamsCaptor.getValue().stream()
                    .map(Stream::getName)
                    .toList(), Matchers.contains("stream-a", "stream-b"));
        }

        @Test
        void calculateCombinationsRequestingPage2() throws Exception {
            when(combinationService.calculate(any(), any(), anyInt()))
                    .thenReturn(List.of(
                            new ScoredCombinationDto(0, List.of()),
                            new ScoredCombinationDto(1, List.of())));

            mockMvc.perform(post("/api/v1/combinations/calculate")
                            .queryParam("page", "2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "developers": [],
                                      "streams": []
                                    }""")
                    )
                    .andExpect(status().isOk())
            ;

            verify(combinationService).calculate(developersCaptor.capture(), streamsCaptor.capture(), eq(2));
        }

    }
}
