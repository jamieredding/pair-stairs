package dev.coldhands.pair.stairs.backend.infrastructure.web.controller

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.DeveloperEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.StreamEntity
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.net.URI
import java.util.stream.Stream

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
@TestPropertySource(
    properties = [
        "app.combinations.calculate.pageSize=2"
    ]
)
open class CombinationCalculationControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val testEntityManager: TestEntityManager
) {

    @ParameterizedTest
    @MethodSource
    fun whenAnonymousUserThenReturnUnauthorized(httpMethod: HttpMethod, uri: String) {
        mockMvc.perform(
            request(httpMethod, URI.create(uri))
                .with(anonymous())
        ).andExpect(status().isUnauthorized)
    }

    companion object {
        @JvmStatic
        fun whenAnonymousUserThenReturnUnauthorized(): Stream<Arguments> =
            Stream.of(
                Arguments.of(HttpMethod.POST, "/api/v1/combinations/calculate")
            )
    }

    @Nested
    inner class Calculate {

        @Test
        fun calculateCombinationsHasADefaultPageSize() {
            val dev0Id = testEntityManager.persist(DeveloperEntity("dev-0")).id
            val dev1Id = testEntityManager.persist(DeveloperEntity("dev-1")).id
            val dev2Id = testEntityManager.persist(DeveloperEntity("dev-2")).id

            val stream0Id = testEntityManager.persist(StreamEntity("stream-a")).id
            val stream1Id = testEntityManager.persist(StreamEntity("stream-b")).id

            val contentAsString = mockMvc.perform(
                post("/api/v1/combinations/calculate")
                    .with(oidcLogin())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
            {
              "developerIds": [$dev0Id, $dev1Id, $dev2Id],
              "streamIds": [$stream0Id, $stream1Id]
            }
            """.trimIndent()
                    )
            )
                .andExpect(status().isOk)
                .andReturn()
                .response
                .contentAsString

            val parsed: DocumentContext = JsonPath.parse(contentAsString)
            parsed.read<List<Any>>("$").shouldHaveSize(2)
        }

        @Test
        fun calculateCombinationsRequestingPageWithNoResults() {
            val dev0Id = testEntityManager.persist(DeveloperEntity("dev-0")).id
            val dev1Id = testEntityManager.persist(DeveloperEntity("dev-1")).id
            val dev2Id = testEntityManager.persist(DeveloperEntity("dev-2")).id

            val stream0Id = testEntityManager.persist(StreamEntity("stream-a")).id
            val stream1Id = testEntityManager.persist(StreamEntity("stream-b")).id

            val contentAsString = mockMvc.perform(
                post("/api/v1/combinations/calculate")
                    .queryParam("page", "10")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
            {
              "developerIds": [$dev0Id, $dev1Id, $dev2Id],
              "streamIds": [$stream0Id, $stream1Id]
            }
            """.trimIndent()
                    )
            )
                .andExpect(status().isOk)
                .andReturn()
                .response
                .contentAsString

            val parsed: DocumentContext = JsonPath.parse(contentAsString)
            parsed.read<List<Any>>("$").shouldHaveSize(0)
        }

        @Test
        fun returnBadRequestWhenThereAreNotEnoughDevelopersToBeAbleToPair() {
            val dev0Id = testEntityManager.persist(DeveloperEntity("dev-0")).id
            val dev1Id = testEntityManager.persist(DeveloperEntity("dev-1")).id

            val stream0Id = testEntityManager.persist(StreamEntity("stream-a")).id
            val stream1Id = testEntityManager.persist(StreamEntity("stream-b")).id

            val contentAsString = mockMvc.perform(
                post("/api/v1/combinations/calculate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
            {
              "developerIds": [$dev0Id, $dev1Id],
              "streamIds": [$stream0Id, $stream1Id]
            }
            """.trimIndent()
                    )
            )
                .andExpect(status().isBadRequest)
                .andReturn()
                .response
                .contentAsString

            val parsed: DocumentContext = JsonPath.parse(contentAsString)
            parsed.read<String>("$.errorCode") shouldBe "NOT_ENOUGH_DEVELOPERS"
        }

        @Test
        fun returnBadRequestWhenThereAreNotEnoughStreamsToBeAbleToPair() {
            val dev0Id = testEntityManager.persist(DeveloperEntity("dev-0")).id
            val dev1Id = testEntityManager.persist(DeveloperEntity("dev-1")).id
            val dev2Id = testEntityManager.persist(DeveloperEntity("dev-2")).id

            val stream0Id = testEntityManager.persist(StreamEntity("stream-a")).id

            val contentAsString = mockMvc.perform(
                post("/api/v1/combinations/calculate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
            {
              "developerIds": [$dev0Id, $dev1Id, $dev2Id],
              "streamIds": [$stream0Id]
            }
            """.trimIndent()
                    )
            )
                .andExpect(status().isBadRequest)
                .andReturn()
                .response
                .contentAsString

            val parsed: DocumentContext = JsonPath.parse(contentAsString)
            parsed.read<String>("$.errorCode") shouldBe "NOT_ENOUGH_STREAMS"
        }
    }
}
