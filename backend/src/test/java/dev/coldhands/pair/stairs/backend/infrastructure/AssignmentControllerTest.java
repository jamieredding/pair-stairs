package dev.coldhands.pair.stairs.backend.infrastructure;

import dev.coldhands.pair.stairs.backend.domain.*;
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

@WebMvcTest(controllers = AssignmentController.class)
public class AssignmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AssignmentService assignmentService;

    @Nested
    class Calculate {

        @Captor
        ArgumentCaptor<List<Developer>> developersCaptor;
        @Captor
        ArgumentCaptor<List<Stream>> streamsCaptor;

        @Test
        void calculateAssignments() throws Exception {
            when(assignmentService.calculate(any(), any(), anyInt()))
                    .thenReturn(List.of(
                            new ScoredAssignment(0,
                                    List.of(new PairStream(List.of("dev-0", "dev-1"), "stream-a"),
                                            new PairStream(List.of("dev-2"), "stream-b"))
                            ),
                            new ScoredAssignment(1,
                                    List.of(new PairStream(List.of("dev-0", "dev-2"), "stream-a"),
                                            new PairStream(List.of("dev-1"), "stream-b"))
                            )));

            mockMvc.perform(post("/api/v1/assignments/calculate")
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

            verify(assignmentService).calculate(developersCaptor.capture(), streamsCaptor.capture(), eq(0));

            assertThat(developersCaptor.getValue().stream()
                    .map(Developer::getName)
                    .toList(), Matchers.contains("dev-0", "dev-1", "dev-2"));
            assertThat(streamsCaptor.getValue().stream()
                    .map(Stream::getName)
                    .toList(), Matchers.contains("stream-a", "stream-b"));
        }

        @Test
        void calculateAssignmentsRequestingPage2() throws Exception {
            when(assignmentService.calculate(any(), any(), anyInt()))
                    .thenReturn(List.of(
                            new ScoredAssignment(0, List.of()),
                            new ScoredAssignment(1, List.of())));

            mockMvc.perform(post("/api/v1/assignments/calculate")
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

            verify(assignmentService).calculate(developersCaptor.capture(), streamsCaptor.capture(), eq(2));
        }

    }
}
