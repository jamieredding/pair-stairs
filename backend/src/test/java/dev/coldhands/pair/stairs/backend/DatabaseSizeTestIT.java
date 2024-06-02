package dev.coldhands.pair.stairs.backend;

import dev.coldhands.pair.stairs.backend.domain.CombinationEvent;
import dev.coldhands.pair.stairs.backend.domain.ScoredCombination;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("Only for manually running, 1.4M size in h2")
public class DatabaseSizeTestIT implements WithBackendHttpClient {

    @Test
    void runSimulationToFillTheDatabase() throws Exception {
        LocalDate currentDay = LocalDate.now();
        final int yearsToSimulate = 30;
        final long daysToSimulate = ChronoUnit.DAYS.between(currentDay, currentDay.plusYears(yearsToSimulate));

        System.out.println(STR."Simulating \{yearsToSimulate} years (\{daysToSimulate} days)");

        for (int dayNumber = 0; dayNumber < daysToSimulate; dayNumber++, currentDay = currentDay.plusDays(1)) {
            simulateChoiceForDay(currentDay);
        }
    }

    private void simulateChoiceForDay(LocalDate currentDay) throws Exception {
        final List<Long> developersToIncludeInCombinations = getDeveloperIdsFor(List.of(
                "dev-0",
                "dev-1",
                "dev-2",
                "dev-3",
                "dev-4"
        ));

        final List<Long> streamsToIncludeInCombinations = getStreamIdsFor(List.of(
                "stream-a",
                "stream-b",
                "stream-c"
        ));

        final List<ScoredCombination> scoredCombinations = calculateCombinations(developersToIncludeInCombinations, streamsToIncludeInCombinations);

        assertThat(scoredCombinations).isNotEmpty();

        final ScoredCombination bestCombination = scoredCombinations.getFirst();

        saveCombinationEventFor(currentDay, bestCombination.combination());

        final List<CombinationEvent> combinationEvents = getCombinationEvents();

        final CombinationEvent savedEvent = combinationEvents.getFirst();

        assertThat(savedEvent.date()).isEqualTo(currentDay);
    }

}
