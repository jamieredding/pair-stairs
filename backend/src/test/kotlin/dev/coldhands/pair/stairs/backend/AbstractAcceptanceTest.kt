package dev.coldhands.pair.stairs.backend

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.coldhands.pair.stairs.backend.domain.*
import dev.coldhands.pair.stairs.backend.domain.developer.Developer
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperStats
import dev.coldhands.pair.stairs.backend.domain.stream.StreamStats
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.CalculateInputDto
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.SaveCombinationEventDto
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldStartWith
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
abstract class AbstractAcceptanceTest(
    private val objectMapper: ObjectMapper,
    private val mockMvc: MockMvc,
    private val dbUrl: String
) {

    protected abstract fun getExpectedDbUrlPrefix(): String

    @Test
    fun checkDbUrl() {
        dbUrl.shouldStartWith(getExpectedDbUrlPrefix())
    }

    @Test
    fun basicFlowStartingFromScratch() {
        val dev0Id = createDeveloper("dev-0")
        createDeveloper("dev-1")
        createDeveloper("dev-2")
        createDeveloper("dev-3")

        val developersToIncludeInCombinations = getDeveloperIdsFor(
            listOf("dev-0", "dev-1", "dev-2")
        )

        val streamAId = createStream("stream-a")
        createStream("stream-b")

        val streamsToIncludeInCombinations = getStreamIdsFor(
            listOf("stream-a", "stream-b")
        )

        val scoredCombinations =
            calculateCombinations(developersToIncludeInCombinations, streamsToIncludeInCombinations)

        scoredCombinations.shouldNotBeEmpty()

        val bestCombination = scoredCombinations.first()

        bestCombination.combination()
            .flatMap { it.developers() }
            .map(DeveloperInfo::displayName)
            .shouldContainExactlyInAnyOrder("dev-0", "dev-1", "dev-2")

        bestCombination.combination()
            .map { it.stream().displayName() }
            .shouldContainExactlyInAnyOrder("stream-a", "stream-b")

        val startingEvents = findAllCombinationEvents()
        startingEvents.shouldBeEmpty()

        val today = LocalDate.of(2024, 4, 27)
        saveCombinationEventFor(today, bestCombination.combination())

        val allCombinationEvents = findAllCombinationEvents()
        allCombinationEvents shouldHaveSize 1

        val savedEvent = allCombinationEvents.first()
        savedEvent.date() shouldBe today
        savedEvent.combination() shouldHaveSize 2
        savedEvent.combination() shouldBe bestCombination.combination()

        val developerStats = getDeveloperStatsBetween(dev0Id, today, today)
        developerStats.developerStats shouldHaveSize 4
        developerStats.streamStats shouldHaveSize 2

        val streamStats = getStreamStatsBetween(streamAId, today, today)
        streamStats.developerStats shouldHaveSize 4
    }

    @Test
    fun exampleOfSecondDay() {
        basicFlowStartingFromScratch()

        val yesterdayEvent = findAllCombinationEvents().first()

        val developersToIncludeInCombinations = getDeveloperIdsFor(
            listOf("dev-0", "dev-1", "dev-2")
        )
        val streamsToIncludeInCombinations = getStreamIdsFor(
            listOf("stream-a", "stream-b")
        )

        val scoredCombinations =
            calculateCombinations(developersToIncludeInCombinations, streamsToIncludeInCombinations)
        scoredCombinations.shouldNotBeEmpty()

        val todayCombination = scoredCombinations.first().combination()
        val yesterdayCombination = yesterdayEvent.combination()

        todayCombination shouldNotBe yesterdayCombination

        val tomorrow = LocalDate.of(2024, 4, 28)
        saveCombinationEventFor(tomorrow, todayCombination)

        val allCombinationEvents = findAllCombinationEvents()
        allCombinationEvents shouldHaveSize 2

        val savedEvent = allCombinationEvents.first()
        savedEvent.date() shouldBe tomorrow
        savedEvent.combination() shouldHaveSize 2

        deleteCombinationEvent(savedEvent.id())

        val allCombinationEventsAfterDelete = findAllCombinationEvents()
        allCombinationEventsAfterDelete shouldHaveSize 1
        allCombinationEventsAfterDelete shouldBe listOf(yesterdayEvent)
    }

    private fun createDeveloper(developerName: String): DeveloperId {
        val result: MvcResult = mockMvc.perform(
            post("/api/v1/developers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "name": "$developerName"
                    }
                    """.trimIndent()
                )
        ).andExpect(status().isCreated)
            .andReturn()

        val responseBody = result.response.contentAsString
        val developer: Developer = objectMapper.readValue(responseBody)
        return developer.id
    }

    private fun getDeveloperIdsFor(developerNames: List<String>): List<DeveloperId> {
        val result = mockMvc.perform(get("/api/v1/developers"))
            .andExpect(status().isOk)
            .andReturn()

        val responseBody = result.response.contentAsString
        val developers: List<Developer> = objectMapper.readValue(responseBody)

        return developers
            .filter { it.name in developerNames }
            .map(Developer::id)
    }

    private fun createStream(streamName: String): Long {
        val result = mockMvc.perform(
            post("/api/v1/streams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "name": "$streamName"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isCreated)
            .andReturn()

        val responseBody = result.response.contentAsString
        val stream: Stream = objectMapper.readValue(responseBody)
        return stream.id()
    }

    private fun getStreamIdsFor(streamNames: List<String>): List<Long> {
        val responseBody = mockMvc.perform(get("/api/v1/streams"))
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString

        val streams: List<Stream> = objectMapper.readValue(responseBody)

        return streams
            .filter { it.name() in streamNames }
            .map(Stream::id)
    }

    private fun calculateCombinations(developerIds: List<DeveloperId>, streamIds: List<Long>): List<ScoredCombination> {
        val input = CalculateInputDto(developerIds, streamIds)

        val responseBody = mockMvc.perform(
            post("/api/v1/combinations/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input))
        )
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString

        return objectMapper.readValue(responseBody)
    }

    private fun saveCombinationEventFor(date: LocalDate, combination: List<PairStream>) {
        val combinationByIds: List<SaveCombinationEventDto.PairStreamByIds> =
            combination.map { ps ->
                val developerIds = ps.developers().map(DeveloperInfo::id)
                // todo just use DeveloperId directly once DeveloperInfo is kotlin-ed
                SaveCombinationEventDto.PairStreamByIds(developerIds.map { DeveloperId(it) }, ps.stream().id())
            }

        val dto = SaveCombinationEventDto(date, combinationByIds)

        mockMvc.perform(
            post("/api/v1/combinations/event")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        )
            .andExpect(status().isCreated)
    }

    private fun findAllCombinationEvents(): List<CombinationEvent> {
        val responseBody = mockMvc.perform(get("/api/v1/combinations/event"))
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString

        return objectMapper.readValue(responseBody)
    }

    private fun deleteCombinationEvent(id: Long) {
        mockMvc.perform(delete("/api/v1/combinations/event/{id}", id))
            .andExpect(status().isNoContent)
    }

    private fun getDeveloperStatsBetween(
        developerId: DeveloperId,
        startDate: LocalDate,
        endDate: LocalDate
    ): DeveloperStats {
        val responseBody = mockMvc.perform(
            get("/api/v1/developers/{id}/stats", developerId.value)
                .queryParam("startDate", startDate.format(DateTimeFormatter.ISO_DATE))
                .queryParam("endDate", endDate.format(DateTimeFormatter.ISO_DATE))
        )
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString

        return objectMapper.readValue(responseBody)
    }

    private fun getStreamStatsBetween(
        streamId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): StreamStats {
        val responseBody = mockMvc.perform(
            get("/api/v1/streams/{id}/stats", streamId)
                .queryParam("startDate", startDate.format(DateTimeFormatter.ISO_DATE))
                .queryParam("endDate", endDate.format(DateTimeFormatter.ISO_DATE))
        )
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString

        return objectMapper.readValue(responseBody)
    }
}
