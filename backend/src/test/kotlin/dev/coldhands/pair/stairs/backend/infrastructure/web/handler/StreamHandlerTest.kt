package dev.coldhands.pair.stairs.backend.infrastructure.web.handler

import dev.coldhands.pair.stairs.backend.ParameterizedJsonApprovalTest
import dev.coldhands.pair.stairs.backend.aDeveloperDetails
import dev.coldhands.pair.stairs.backend.aStreamDetails
import dev.coldhands.pair.stairs.backend.domain.StreamId
import dev.coldhands.pair.stairs.backend.domain.combination.PairStream
import dev.coldhands.pair.stairs.backend.domain.stream.Stream
import dev.coldhands.pair.stairs.backend.infrastructure.web.testContext
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.http4k.core.Body
import org.http4k.core.Method
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.format.Jackson.auto
import org.http4k.kotest.shouldHaveStatus
import org.http4k.lens.Path
import org.http4k.lens.Query
import org.http4k.lens.long
import org.http4k.testing.Approver
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.time.LocalDate
import kotlin.random.Random

@ExtendWith(ParameterizedJsonApprovalTest::class)
class StreamHandlerTest {
    private val pathIdLens = Path.long().of("id")

    @Test
    @Disabled
    fun `when anonymous user then return unauthorized`() {

    }

    @Nested
    inner class Read {

        @Test
        fun `when no streams then return empty array`(approver: Approver) = testContext {
            val response = underTest(
                Request(
                    method = GET,
                    uri = "/api/v1/streams",
                )
            )

            response shouldHaveStatus OK
            approver.assertApproved(response)
        }

        @Test
        fun `when multiple streams then return them all`(approver: Approver) = testContext {
            streamDao.create(aStreamDetails("stream-0")).shouldBeSuccess()
            streamDao.create(aStreamDetails("stream-1")).shouldBeSuccess()

            val response = underTest(
                Request(
                    method = GET,
                    uri = "/api/v1/streams",
                )
            )

            response shouldHaveStatus OK

            approver.assertApproved(response)
        }
    }

    @Nested
    inner class ReadStreamInfo {

        @Test
        fun `when no streams then return empty array`(approver: Approver) = testContext {
            val response = underTest(
                Request(
                    method = GET,
                    uri = "/api/v1/streams/info",
                )
            )

            response shouldHaveStatus OK
            approver.assertApproved(response)
        }

        @Test
        fun `when multiple streams then return them all`(approver: Approver) = testContext {
            streamDao.create(aStreamDetails("stream-0")).shouldBeSuccess()
            streamDao.create(aStreamDetails("stream-1")).shouldBeSuccess()

            val response = underTest(
                Request(
                    method = GET,
                    uri = "/api/v1/streams/info",
                )
            )

            response shouldHaveStatus OK
            approver.assertApproved(response)
        }
    }

    @Nested
    inner class Write {

        @Test
        fun `save a stream`(approver: Approver) = testContext {
            val requestBodyLens = Body.auto<PostStream>().toLens()
            val responseBodyLens = Body.auto<Stream>().toLens()

            val response = underTest(
                Request(
                    method = POST,
                    uri = "/api/v1/streams",
                ).with(requestBodyLens of PostStream("stream-0"))
            )

            response shouldHaveStatus CREATED
            approver.assertApproved(response)

            val createdStream = responseBodyLens(response)

            streamDao.findById(createdStream.id).shouldNotBeNull {
                id shouldBe createdStream.id
                name shouldBe "stream-0"
                archived shouldBe false
            }
        }
    }

    @Nested
    inner class Patch {
        private val bodyPatchStreamLens = Body.auto<PatchStream>().toLens()
        private val responseBodyLens = Body.auto<Stream>().toLens()

        @ParameterizedTest(name = "set archived to {0}")
        @ValueSource(booleans = [true, false])
        fun archived(newArchivedValue: Boolean, approver: Approver) = testContext {
            val stream = streamDao.create(aStreamDetails("stream-0"))
                .shouldBeSuccess()
                .also { it.archived shouldBe false }

            val response = underTest(
                Request(
                    method = Method.PATCH,
                    uri = "/api/v1/streams/{id}",
                ).with(
                    pathIdLens of stream.id.value,
                    bodyPatchStreamLens of PatchStream(archived = newArchivedValue)
                )
            )

            response shouldHaveStatus OK
            approver.assertApproved(response)

            val updatedStream = responseBodyLens(response)

            streamDao.findById(updatedStream.id).shouldNotBeNull {
                id shouldBe updatedStream.id
                name shouldBe "stream-0"
                archived shouldBe newArchivedValue
            }
        }

        @Test
        fun `when stream does not exist with id then return not found`(approver: Approver) = testContext {
            val madeUpStreamId = StreamId(Random.nextLong())
            streamDao.findById(madeUpStreamId).shouldBeNull()

            val response = underTest(
                Request(
                    method = Method.PATCH,
                    uri = "/api/v1/streams/{id}",
                ).with(
                    pathIdLens of madeUpStreamId.value,
                    bodyPatchStreamLens of PatchStream(archived = false)
                )
            )

            response shouldHaveStatus NOT_FOUND
            approver.assertApproved(response)
        }
    }

    @Nested
    inner class ReadStreamStats {

        @Test
        fun `when stream does not exist with id then return not found`(approver: Approver) = testContext {
            val madeUpStreamId = StreamId(Random.nextLong())
            streamDao.findById(madeUpStreamId).shouldBeNull()

            val response = underTest(
                Request(
                    method = GET,
                    uri = "/api/v1/streams/{id}/stats",
                ).with(
                    pathIdLens of madeUpStreamId.value,
                )
            )

            response shouldHaveStatus NOT_FOUND
            approver.assertApproved(response)
        }

        @Test
        fun `when stream exists but no pairs have happened then return all developers`(approver: Approver) =
            testContext {
                val streamAId = streamDao.create(aStreamDetails("stream-a")).shouldBeSuccess().id
                developerDao.create(aDeveloperDetails("dev-0")).shouldBeSuccess().id

                val response = underTest(
                    Request(
                        method = GET,
                        uri = "/api/v1/streams/{id}/stats",
                    ).with(
                        pathIdLens of streamAId.value,
                    )
                )

                response shouldHaveStatus OK
                approver.assertApproved(response)
            }

        @Test
        // the sort is actually the wrong direction but keeping same behaviour while porting code
        fun `when stream exists and has been paired on then return statistics`(approver: Approver) = testContext {
            val dev0Id = developerDao.create(aDeveloperDetails("dev-0")).shouldBeSuccess().id
            val dev1Id = developerDao.create(aDeveloperDetails("dev-1")).shouldBeSuccess().id
            val dev2Id = developerDao.create(aDeveloperDetails("dev-2")).shouldBeSuccess().id
            developerDao.create(aDeveloperDetails("dev-3")).shouldBeSuccess().id

            val streamAId = streamDao.create(aStreamDetails("stream-a")).shouldBeSuccess().id
            val streamBId = streamDao.create(aStreamDetails("stream-b")).shouldBeSuccess().id

            combinationEventService.saveEvent(
                LocalDate.of(2024, 5, 5), listOf(
                    PairStream(setOf(dev0Id, dev1Id), streamAId),
                    PairStream(setOf(dev2Id), streamBId)
                )
            )
            combinationEventService.saveEvent(
                LocalDate.of(2024, 5, 6), listOf(
                    PairStream(setOf(dev0Id, dev2Id), streamAId),
                    PairStream(setOf(dev1Id), streamBId)
                )
            )
            combinationEventService.saveEvent(
                LocalDate.of(2024, 5, 7), listOf(
                    PairStream(setOf(dev0Id, dev1Id), streamAId),
                    PairStream(setOf(dev2Id), streamBId)
                )
            )

            val response = underTest(
                Request(
                    method = GET,
                    uri = "/api/v1/streams/{id}/stats",
                ).with(
                    pathIdLens of streamAId.value,
                )
            )

            response shouldHaveStatus OK
            approver.assertApproved(response)
        }

        @Test
        // the sort is actually the wrong direction but keeping same behaviour while porting code
        fun `when stream exists and developers but no events have happened then sort alphabetically`(
            approver: Approver
        ) = testContext {
            developerDao.create(aDeveloperDetails("dev-3")).shouldBeSuccess().id
            developerDao.create(aDeveloperDetails("dev-2")).shouldBeSuccess().id
            developerDao.create(aDeveloperDetails("dev-1")).shouldBeSuccess().id
            developerDao.create(aDeveloperDetails("dev-0")).shouldBeSuccess().id

            val streamAId = streamDao.create(aStreamDetails("stream-a")).shouldBeSuccess().id


            val response = underTest(
                Request(
                    method = GET,
                    uri = "/api/v1/streams/{id}/stats",
                ).with(
                    pathIdLens of streamAId.value,
                )
            )

            response shouldHaveStatus OK
            approver.assertApproved(response)
        }

        @Test
        // the sort is actually the wrong direction but keeping same behaviour while porting code
        fun `allows filtering by date range`(
            approver: Approver
        ) = testContext {
            val dev0Id = developerDao.create(aDeveloperDetails("dev-0")).shouldBeSuccess().id
            val dev1Id = developerDao.create(aDeveloperDetails("dev-1")).shouldBeSuccess().id
            val dev2Id = developerDao.create(aDeveloperDetails("dev-2")).shouldBeSuccess().id
            developerDao.create(aDeveloperDetails("dev-3")).shouldBeSuccess().id

            val streamAId = streamDao.create(aStreamDetails("stream-a")).shouldBeSuccess().id
            val streamBId = streamDao.create(aStreamDetails("stream-b")).shouldBeSuccess().id

            combinationEventService.saveEvent(
                LocalDate.of(2024, 5, 5), listOf(
                    PairStream(setOf(dev0Id, dev1Id), streamAId),
                    PairStream(setOf(dev2Id), streamBId)
                )
            )
            combinationEventService.saveEvent(
                LocalDate.of(2024, 5, 6), listOf(
                    PairStream(setOf(dev0Id, dev2Id), streamAId),
                    PairStream(setOf(dev1Id), streamBId)
                )
            )
            combinationEventService.saveEvent(
                LocalDate.of(2024, 5, 7), listOf(
                    PairStream(setOf(dev0Id, dev1Id), streamAId),
                    PairStream(setOf(dev2Id), streamBId)
                )
            )
            combinationEventService.saveEvent(
                LocalDate.of(2024, 5, 8), listOf(
                    PairStream(setOf(dev0Id, dev2Id), streamAId),
                    PairStream(setOf(dev1Id), streamBId)
                )
            )

            val response = underTest(
                Request(
                    method = GET,
                    uri = "/api/v1/streams/{id}/stats",
                ).with(
                    pathIdLens of streamAId.value,
                    startDateLens of "2024-05-06",
                    endDateLens of "2024-05-07"
                )
            )

            response shouldHaveStatus OK
            approver.assertApproved(response)
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("dev.coldhands.pair.stairs.backend.infrastructure.web.handler.StreamHandlerTest#badRequestIfInvalidRange")
        fun `bad request if invalid range`(
            @Suppress("unused") testName: String,
            builder: (Request) -> Request,
            approver: Approver
        ) = testContext {
            val stream0Id = streamDao.create(aStreamDetails("stream-0")).shouldBeSuccess().id

            val response = underTest(
                Request(
                    method = GET,
                    uri = "/api/v1/streams/{id}/stats",
                ).with(
                    pathIdLens of stream0Id.value,
                    builder
                )
            )

            response shouldHaveStatus BAD_REQUEST
            approver.assertApproved(response)
        }

    }

    data class PostStream(
        val name: String,
    )

    data class PatchStream(
        val archived: Boolean,
    )

    companion object {
        private val startDateLens = Query.required("startDate")
        private val endDateLens = Query.required("endDate")

        @JvmStatic
        fun badRequestIfInvalidRange() = listOf(
            Arguments.of(
                "startDate only",
                {request: Request ->
                    request.with(
                        startDateLens of "2024-05-06"
                    )
                }
            ),
            Arguments.of(
                "endDate only",
                {request: Request ->
                    request.with(
                        endDateLens of "2024-05-06"
                    )
                }
            ),
            Arguments.of(
                "startDate after endDate",
                {request: Request ->
                    request.with(
                        startDateLens of "2024-05-07",
                        endDateLens of "2024-05-06"
                    )
                }
            )
        )
    }

}