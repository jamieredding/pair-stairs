package dev.coldhands.pair.stairs.backend

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import dev.coldhands.pair.stairs.backend.WithBackendHttpClient.Companion.REST_TEMPLATE
import dev.coldhands.pair.stairs.backend.domain.*
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.jdbc.Sql
import java.time.LocalDate

class ApplicationIT : WithBackendHttpClient {

    @Test
    @Sql(value = ["/delete-test-data.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = ["/delete-test-data.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun basicFlowStartingFromScratch() {
        val dev0Id = createDeveloper("dev-0")
        createDeveloper("dev-1")
        createDeveloper("dev-2")
        createDeveloper("dev-3")

        val developersToIncludeInCombinations = getDeveloperIdsFor(
            listOf(
                "dev-0",
                "dev-1",
                "dev-2",
            ),
        )

        val streamAId = createStream("stream-a")
        createStream("stream-b")

        val streamsToIncludeInCombinations = getStreamIdsFor(
            listOf(
                "stream-a",
                "stream-b",
            ),
        )

        val scoredCombinations: List<ScoredCombination> =
            calculateCombinations(developersToIncludeInCombinations, streamsToIncludeInCombinations)

        scoredCombinations.shouldNotBeEmpty()

        val bestCombination = scoredCombinations.first()

        bestCombination
            .combination()
            .flatMap { pairStream -> pairStream.developers() }
            .map(DeveloperInfo::displayName)
            .shouldContainExactlyInAnyOrder("dev-0", "dev-1", "dev-2")

        bestCombination
            .combination()
            .map(PairStream::stream)
            .map(StreamInfo::displayName)
            .shouldContainExactlyInAnyOrder("stream-a", "stream-b")

        val today = LocalDate.of(2024, 4, 27)
        saveCombinationEventFor(today, bestCombination.combination())

        val combinationEvents: List<CombinationEvent> = getCombinationEvents()
        combinationEvents shouldHaveSize 1

        val savedEvent = combinationEvents.first()

        savedEvent.date() shouldBe today
        savedEvent.combination() shouldHaveSize 2
        savedEvent.combination() shouldBe bestCombination.combination()

        deleteCombinationEvent(savedEvent.id())

        val combinationEventsAfterDelete: List<CombinationEvent> = getCombinationEvents()
        combinationEventsAfterDelete.shouldBeEmpty()

        val developerStats = getDeveloperStatsBetween(dev0Id, today, today)

        developerStats.developerStats shouldHaveSize 4
        developerStats.streamStats shouldHaveSize 2

        val streamStats = getStreamStatsBetween(streamAId, today, today)

        streamStats.developerStats shouldHaveSize 4
    }

    @Nested
    inner class Actuator {

        @Test
        fun health() {
            val response: ResponseEntity<String> =
                REST_TEMPLATE.getForEntity(
                    WithBackendHttpClient.BASE_URL + "/actuator/health",
                    String::class.java,
                )

            response.statusCode shouldBe HttpStatus.OK

            val parsed: DocumentContext = JsonPath.parse(response.body!!)

            parsed.read("$.status", String::class.java) shouldBe "UP"
            parsed.read("$.components.db.status", String::class.java) shouldBe "UP"
            parsed.read("$.components.db.details.database", String::class.java) shouldBe "MySQL"
            parsed.read("$.components.livenessState.status", String::class.java) shouldBe "UP"
            parsed.read("$.components.readinessState.status", String::class.java) shouldBe "UP"
        }

        @Test
        fun info() {
            val response: ResponseEntity<String> =
                REST_TEMPLATE.getForEntity(
                    WithBackendHttpClient.BASE_URL + "/actuator/info",
                    String::class.java,
                )

            response.statusCode shouldBe HttpStatus.OK

            val parsed: DocumentContext = JsonPath.parse(response.body!!)

            parsed.read("$.build.version", String::class.java).shouldNotBeNull()
            parsed.read("$.git.branch", String::class.java).shouldNotBeNull()
            parsed.read("$.git.commit.id", String::class.java).shouldNotBeNull()
        }
    }
}
