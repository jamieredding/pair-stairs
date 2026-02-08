package dev.coldhands.pair.stairs.backend.infrastructure.web.handler

import dev.coldhands.pair.stairs.backend.ParameterizedJsonApprovalTest
import dev.coldhands.pair.stairs.backend.aDeveloperDetails
import dev.coldhands.pair.stairs.backend.aStreamDetails
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.SaveCombinationEventDto.PairStreamByIds
import dev.coldhands.pair.stairs.backend.infrastructure.web.testContext
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import org.http4k.config.Environment
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.kotest.shouldHaveStatus
import org.http4k.lens.Query
import org.http4k.lens.int
import org.http4k.testing.Approver
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDate

@ExtendWith(ParameterizedJsonApprovalTest::class)
class CombinationEventHandlerTest {
    private val queryPageLens = Query.int().required("page")

    @Test
    @Disabled
    fun `when anonymous user then return unauthorized`() {

    }

    @Nested
    inner class Read {

        @Test
        fun `when no events then return empty array`(approver: Approver) = testContext {
            val response = underTest(
                Request(
                    method = GET,
                    uri = "/api/v1/combinations/event",
                )
            )

            response shouldHaveStatus OK
            approver.assertApproved(response)
        }

        @Test
        fun `when multiple events then return them in descending order`(approver: Approver) = testContext {
            val dev0Id = developerDao.create(aDeveloperDetails("dev-0")).shouldBeSuccess().id
            val dev1Id = developerDao.create(aDeveloperDetails("dev-1")).shouldBeSuccess().id
            val dev2Id = developerDao.create(aDeveloperDetails("dev-2")).shouldBeSuccess().id

            val stream0Id = streamDao.create(aStreamDetails("stream-a")).shouldBeSuccess().id
            val stream1Id = streamDao.create(aStreamDetails("stream-b")).shouldBeSuccess().id

            combinationEventService.saveEvent(
                LocalDate.of(2024, 5, 5),
                listOf(
                    PairStreamByIds(listOf(dev0Id, dev1Id), stream0Id),
                    PairStreamByIds(listOf(dev2Id), stream1Id),
                ),
            )
            combinationEventService.saveEvent(
                LocalDate.of(2024, 5, 6),
                listOf(
                    PairStreamByIds(listOf(dev0Id, dev2Id), stream0Id),
                    PairStreamByIds(listOf(dev1Id), stream1Id),
                ),
            )
            combinationEventService.saveEvent(
                LocalDate.of(2024, 5, 7),
                listOf(
                    PairStreamByIds(listOf(dev1Id, dev2Id), stream0Id),
                    PairStreamByIds(listOf(dev0Id), stream1Id),
                ),
            )

            val response = underTest(
                Request(
                    method = GET,
                    uri = "/api/v1/combinations/event",
                )
            )

            response shouldHaveStatus OK
            approver.assertApproved(response)
        }

        @Test
        fun `when multiple events then return page 1`(approver: Approver) = testContext {
            val dev0Id = developerDao.create(aDeveloperDetails("dev-0")).shouldBeSuccess().id
            val dev1Id = developerDao.create(aDeveloperDetails("dev-1")).shouldBeSuccess().id
            val dev2Id = developerDao.create(aDeveloperDetails("dev-2")).shouldBeSuccess().id

            val stream0Id = streamDao.create(aStreamDetails("stream-a")).shouldBeSuccess().id
            val stream1Id = streamDao.create(aStreamDetails("stream-b")).shouldBeSuccess().id

            combinationEventService.saveEvent(
                LocalDate.of(2024, 5, 5),
                listOf(
                    PairStreamByIds(listOf(dev0Id, dev1Id), stream0Id),
                    PairStreamByIds(listOf(dev2Id), stream1Id),
                ),
            )
            combinationEventService.saveEvent(
                LocalDate.of(2024, 5, 6),
                listOf(
                    PairStreamByIds(listOf(dev0Id, dev2Id), stream0Id),
                    PairStreamByIds(listOf(dev1Id), stream1Id),
                ),
            )
            combinationEventService.saveEvent(
                LocalDate.of(2024, 5, 7),
                listOf(
                    PairStreamByIds(listOf(dev1Id, dev2Id), stream0Id),
                    PairStreamByIds(listOf(dev0Id), stream1Id),
                ),
            )

            environment = Environment.from(
                "app.combinations.event.pageSize" to "2"
            ).overrides(environment)

            val response = underTest(
                Request(
                    method = GET,
                    uri = "/api/v1/combinations/event",
                ).with(
                    queryPageLens of 1
                )
            )
            response shouldHaveStatus OK
            approver.assertApproved(response)
        }

        @Test
        fun `when combinations in events were saved in unsorted order then return them sorted by stream display name and developers sorted by display name`(approver: Approver) = testContext {
            val dev0Id = developerDao.create(aDeveloperDetails("dev-0")).shouldBeSuccess().id
            val dev1Id = developerDao.create(aDeveloperDetails("dev-1")).shouldBeSuccess().id
            val dev2Id = developerDao.create(aDeveloperDetails("dev-2")).shouldBeSuccess().id

            val stream0Id = streamDao.create(aStreamDetails("stream-a")).shouldBeSuccess().id
            val stream1Id = streamDao.create(aStreamDetails("stream-b")).shouldBeSuccess().id

            combinationEventService.saveEvent(
                LocalDate.of(2024, 5, 5),
                listOf(
                    PairStreamByIds(listOf(dev0Id, dev1Id), stream0Id),
                    PairStreamByIds(listOf(dev2Id), stream1Id),
                ),
            )
            combinationEventService.saveEvent(
                LocalDate.of(2024, 5, 6),
                listOf(
                    PairStreamByIds(listOf(dev0Id, dev2Id), stream0Id),
                    PairStreamByIds(listOf(dev1Id), stream1Id),
                ),
            )
            combinationEventService.saveEvent(
                LocalDate.of(2024, 5, 7),
                listOf(
                    PairStreamByIds(listOf(dev1Id, dev2Id), stream0Id),
                    PairStreamByIds(listOf(dev0Id), stream1Id),
                ),
            )

            val response = underTest(
                Request(
                    method = GET,
                    uri = "/api/v1/combinations/event",
                )
            )
            response shouldHaveStatus OK
            approver.assertApproved(response)
        }
    }
}