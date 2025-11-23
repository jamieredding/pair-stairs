package dev.coldhands.pair.stairs.backend

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.coldhands.pair.stairs.backend.domain.*
import dev.coldhands.pair.stairs.backend.domain.developer.Developer
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperStats
import dev.coldhands.pair.stairs.backend.domain.stream.Stream
import dev.coldhands.pair.stairs.backend.domain.stream.StreamStats
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.CalculateInputDto
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.SaveCombinationEventDto
import io.kotest.matchers.shouldBe
import org.springframework.http.*
import org.springframework.http.HttpMethod.DELETE
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_DATE

interface WithBackendHttpClient {

    fun createDeveloper(developerName: String): DeveloperId {
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val payload = """
            {
              "name": "$developerName"
            }
        """.trimIndent()
        val request = HttpEntity(payload, headers)

        val response: ResponseEntity<Developer> =
            REST_TEMPLATE.postForEntity("$BASE_URL/api/v1/developers", request, Developer::class.java)

        response.statusCode shouldBe HttpStatus.CREATED
        return response.body!!.id
    }

    fun getDeveloperIdsFor(developerNames: List<String>): List<DeveloperId> {
        val response: ResponseEntity<String> =
            REST_TEMPLATE.getForEntity("$BASE_URL/api/v1/developers", String::class.java)

        response.statusCode shouldBe HttpStatus.OK

        val developers: List<Developer> = OBJECT_MAPPER.readValue(response.body!!)
        return developers
            .filter { it.name in developerNames }
            .map(Developer::id)
    }

    fun createStream(streamName: String): StreamId {
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val payload = """
            {
              "name": "$streamName"
            }
        """.trimIndent()
        val request = HttpEntity(payload, headers)

        val response: ResponseEntity<Stream> =
            REST_TEMPLATE.postForEntity("$BASE_URL/api/v1/streams", request, Stream::class.java)

        response.statusCode shouldBe HttpStatus.CREATED
        return response.body!!.id
    }

    fun getStreamIdsFor(streamNames: List<String>): List<StreamId> {
        val response: ResponseEntity<String> =
            REST_TEMPLATE.getForEntity("$BASE_URL/api/v1/streams", String::class.java)

        response.statusCode shouldBe HttpStatus.OK

        val streams: List<Stream> = OBJECT_MAPPER.readValue(response.body!!)
        return streams
            .filter { it.name in streamNames }
            .map(Stream::id)
    }

    fun calculateCombinations(developerIds: List<DeveloperId>, streamIds: List<StreamId>): List<ScoredCombination> {
        val input = CalculateInputDto(developerIds, streamIds)

        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request = HttpEntity(OBJECT_MAPPER.writeValueAsString(input), headers)

        val response: ResponseEntity<String> =
            REST_TEMPLATE.postForEntity("$BASE_URL/api/v1/combinations/calculate", request, String::class.java)

        response.statusCode shouldBe HttpStatus.OK

        return OBJECT_MAPPER.readValue(response.body!!)
    }

    fun saveCombinationEventFor(date: LocalDate, combination: List<PairStream>) {
        val combinationByIds: List<SaveCombinationEventDto.PairStreamByIds> =
            combination.map { ps ->
                val developerIds = ps.developers().map(DeveloperInfo::id)
                // todo just use DeveloperId directly once DeveloperInfo is kotlin-ed
                SaveCombinationEventDto.PairStreamByIds(developerIds.map { DeveloperId(it) }, StreamId(ps.stream().id()))
            }

        val dto = SaveCombinationEventDto(date, combinationByIds)

        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request = HttpEntity(OBJECT_MAPPER.writeValueAsString(dto), headers)

        val response: ResponseEntity<Void> =
            REST_TEMPLATE.postForEntity("$BASE_URL/api/v1/combinations/event", request, Void::class.java)

        response.statusCode shouldBe HttpStatus.CREATED
    }

    fun getCombinationEvents(): List<CombinationEvent> {
        val response: ResponseEntity<String> =
            REST_TEMPLATE.getForEntity("$BASE_URL/api/v1/combinations/event", String::class.java)

        response.statusCode shouldBe HttpStatus.OK

        return OBJECT_MAPPER.readValue(response.body!!)
    }

    fun deleteCombinationEvent(id: Long) {
        val response: ResponseEntity<Void> =
            REST_TEMPLATE.exchange("$BASE_URL/api/v1/combinations/event/{id}", DELETE, null, Void::class.java, id)

        response.statusCode shouldBe HttpStatus.NO_CONTENT
    }

    fun getDeveloperStatsBetween(developerId: DeveloperId, startDate: LocalDate, endDate: LocalDate): DeveloperStats {
        val url =
            "$BASE_URL/api/v1/developers/${developerId.value}/stats?startDate=${startDate.format(ISO_DATE)}&endDate=${endDate.format(ISO_DATE)}"

        val response: ResponseEntity<String> = REST_TEMPLATE.getForEntity(url, String::class.java)

        response.statusCode shouldBe HttpStatus.OK

        return OBJECT_MAPPER.readValue(response.body!!)
    }

    fun getStreamStatsBetween(streamId: StreamId, startDate: LocalDate, endDate: LocalDate): StreamStats {
        val url =
            "$BASE_URL/api/v1/streams/${streamId.value}/stats?startDate=${startDate.format(ISO_DATE)}&endDate=${endDate.format(ISO_DATE)}"

        val response: ResponseEntity<String> = REST_TEMPLATE.getForEntity(url, String::class.java)

        response.statusCode shouldBe HttpStatus.OK

        return OBJECT_MAPPER.readValue(response.body!!)
    }

    data class TokenResponse(
        @JsonProperty("access_token") val accessToken: String)

    companion object {
        const val BASE_URL: String = "http://localhost:8080"
        const val IDP_TOKEN_URL: String = "http://localhost:5556/dex/token"
        const val OAUTH_CLIENT_ID: String = "pair-stairs"
        const val OAUTH_CLIENT_SECRET: String = "ZXhhbXBsZS1hcHAtc2VjcmV0"
        const val USERNAME: String = "admin@example.com"
        const val PASSWORD: String = "password"

        private val OBJECT_MAPPER: ObjectMapper = jacksonObjectMapper()
            .registerModule(JavaTimeModule())

        val REST_TEMPLATE: RestTemplate = initialiseRestTemplate()

        private fun initialiseRestTemplate(): RestTemplate {
            val rt = RestTemplate()
            val jwt = fetchJwt(rt)
            rt.interceptors.add { request, body, execution ->
                request.headers.setBearerAuth(jwt)
                execution.execute(request, body)
            }
            return rt
        }

        private fun fetchJwt(restTemplate: RestTemplate): String {
            val form = LinkedMultiValueMap<String, String>().apply {
                add("grant_type", "password")
                add("username", USERNAME)
                add("password", PASSWORD)
                add("scope", "openid profile email") // add offline_access if needed
            }

            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                setBasicAuth(OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET)
            }

            val response = restTemplate.postForEntity(
                IDP_TOKEN_URL,
                HttpEntity(form, headers),
                TokenResponse::class.java
            )

            return response.body!!.accessToken
        }
    }
}
