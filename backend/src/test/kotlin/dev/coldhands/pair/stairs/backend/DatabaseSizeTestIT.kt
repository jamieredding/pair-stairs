package dev.coldhands.pair.stairs.backend

import dev.coldhands.pair.stairs.backend.domain.CombinationEvent
import dev.coldhands.pair.stairs.backend.domain.DeveloperId
import dev.coldhands.pair.stairs.backend.domain.ScoredCombination
import dev.coldhands.pair.stairs.backend.domain.StreamId
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Disabled("Only for manually running, 1.4M size in h2")
class DatabaseSizeTestIT : WithBackendHttpClient {

    @Test
    fun runSimulationToFillTheDatabase() {
        var currentDay = LocalDate.now()
        val yearsToSimulate = 30L
        val daysToSimulate = ChronoUnit.DAYS.between(currentDay, currentDay.plusYears(yearsToSimulate))

        println("Simulating $yearsToSimulate years ($daysToSimulate days)")

        repeat(daysToSimulate.toInt()) {
            simulateChoiceForDay(currentDay)
            currentDay = currentDay.plusDays(1)
        }
    }

    private fun simulateChoiceForDay(currentDay: LocalDate) {
        val developersToIncludeInCombinations: List<DeveloperId> = getDeveloperIdsFor(
            listOf(
                "dev-0",
                "dev-1",
                "dev-2",
                "dev-3",
                "dev-4",
            ),
        )

        val streamsToIncludeInCombinations: List<StreamId> = getStreamIdsFor(
            listOf(
                "stream-a",
                "stream-b",
                "stream-c",
            ),
        )

        val scoredCombinations: List<ScoredCombination> =
            calculateCombinations(developersToIncludeInCombinations, streamsToIncludeInCombinations)

        scoredCombinations.shouldNotBeEmpty()

        val bestCombination = scoredCombinations.first()

        saveCombinationEventFor(currentDay, bestCombination.combination())

        val combinationEvents: List<CombinationEvent> = getCombinationEvents()
        val savedEvent = combinationEvents.first()

        savedEvent.date() shouldBe currentDay
    }
}
