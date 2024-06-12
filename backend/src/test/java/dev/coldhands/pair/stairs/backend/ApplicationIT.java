package dev.coldhands.pair.stairs.backend;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import dev.coldhands.pair.stairs.backend.domain.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationIT implements WithBackendHttpClient {

    @Test
    @Sql(value = "/delete-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/delete-test-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void basicFlowStartingFromScratch() throws Exception {
        final long dev0Id = createDeveloper("dev-0");
        createDeveloper("dev-1");
        createDeveloper("dev-2");
        createDeveloper("dev-3");

        List<Long> developersToIncludeInCombinations = getDeveloperIdsFor(List.of(
                "dev-0",
                "dev-1",
                "dev-2"
        ));

        createStream("stream-a");
        createStream("stream-b");

        List<Long> streamsToIncludeInCombinations = getStreamIdsFor(List.of(
                "stream-a",
                "stream-b"
        ));

        final List<ScoredCombination> scoredCombinations = calculateCombinations(developersToIncludeInCombinations, streamsToIncludeInCombinations);

        assertThat(scoredCombinations).isNotEmpty();

        final ScoredCombination bestCombination = scoredCombinations.getFirst();

        assertThat(bestCombination.combination().stream()
                .flatMap(ps -> ps.developers().stream())
                .map(DeveloperInfo::displayName)
                .toList())
                .containsExactlyInAnyOrder("dev-0", "dev-1", "dev-2");

        assertThat(bestCombination.combination().stream()
                .map(PairStream::stream)
                .map(StreamInfo::displayName)
                .toList())
                .containsExactlyInAnyOrder("stream-a", "stream-b");


        final LocalDate today = LocalDate.of(2024, 4, 27);
        saveCombinationEventFor(today, bestCombination.combination());

        final List<CombinationEvent> combinationEvents = getCombinationEvents();

        assertThat(combinationEvents).hasSize(1);

        final CombinationEvent savedEvent = combinationEvents.getFirst();

        assertThat(savedEvent.date()).isEqualTo(today);
        assertThat(savedEvent.combination()).hasSize(2);
        assertThat(savedEvent.combination()).isEqualTo(bestCombination.combination());

        deleteCombinationEvent(savedEvent.id());

        final List<CombinationEvent> combinationEventsAfterDelete = getCombinationEvents();
        assertThat(combinationEventsAfterDelete).isEmpty();

        final DeveloperStats developerStats = getDeveloperStatsBetween(dev0Id, today, today);

        assertThat(developerStats.developerStats()).hasSize(4);
        assertThat(developerStats.streamStats()).hasSize(2);
    }

    @Nested
    class Actuator {

        @Test
        void health() {
            final ResponseEntity<String> response = REST_TEMPLATE.getForEntity(BASE_URL + "/actuator/health", String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            final String responseBody = response.getBody();
            final DocumentContext parsed = JsonPath.parse(responseBody);

            assertThat(parsed.read("$.status", String.class)).isEqualTo("UP");
            assertThat(parsed.read("$.components.db.status", String.class)).isEqualTo("UP");
            assertThat(parsed.read("$.components.db.details.database", String.class)).isEqualTo("MySQL");
            assertThat(parsed.read("$.components.livenessState.status", String.class)).isEqualTo("UP");
            assertThat(parsed.read("$.components.readinessState.status", String.class)).isEqualTo("UP");
        }

        @Test
        void info() {
            final ResponseEntity<String> response = REST_TEMPLATE.getForEntity(BASE_URL + "/actuator/info", String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            final String responseBody = response.getBody();
            final DocumentContext parsed = JsonPath.parse(responseBody);

            assertThat(parsed.read("$.build.version", String.class)).isNotNull();

            assertThat(parsed.read("$.git.branch", String.class)).isNotNull();
            assertThat(parsed.read("$.git.commit.id", String.class)).isNotNull();
        }
    }
}
