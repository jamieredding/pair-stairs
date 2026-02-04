package dev.coldhands.pair.stairs.backend.infrastructure.web.handler

import dev.coldhands.pair.stairs.backend.ParameterizedJsonApprovalTest
import dev.coldhands.pair.stairs.backend.aDeveloperDetails
import dev.coldhands.pair.stairs.backend.aStreamDetails
import dev.coldhands.pair.stairs.backend.domain.DeveloperId
import dev.coldhands.pair.stairs.backend.domain.developer.Developer
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.SaveCombinationEventDto.PairStreamByIds
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
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.format.Jackson.auto
import org.http4k.kotest.shouldHaveStatus
import org.http4k.lens.Path
import org.http4k.lens.long
import org.http4k.testing.Approver
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.LocalDate
import kotlin.random.Random

@ExtendWith(ParameterizedJsonApprovalTest::class)
class DeveloperHandlerTest {
    private val pathIdLens = Path.long().of("id")

    @Test
    @Disabled
    fun `when anonymous user then return unauthorized`() {

    }

    @Nested
    inner class Read {

        @Test
        fun `when no developers then return empty array`(approver: Approver) = testContext {
            val response = underTest(
                Request(
                    method = GET,
                    uri = "/api/v1/developers",
                )
            )

            response shouldHaveStatus OK
            approver.assertApproved(response)
        }

        @Test
        fun `when multiple developers then return them all`(approver: Approver) = testContext {
            developerDao.create(aDeveloperDetails("dev-0")).shouldBeSuccess()
            developerDao.create(aDeveloperDetails("dev-1")).shouldBeSuccess()

            val response = underTest(
                Request(
                    method = GET,
                    uri = "/api/v1/developers",
                )
            )

            response shouldHaveStatus OK

            approver.assertApproved(response)
        }
    }

    @Nested
    inner class ReadDeveloperInfo {

        @Test
        fun `when no developers then return empty array`(approver: Approver) = testContext {
            val response = underTest(
                Request(
                    method = GET,
                    uri = "/api/v1/developers/info",
                )
            )

            response shouldHaveStatus OK
            approver.assertApproved(response)
        }

        @Test
        fun `when multiple developers then return them all`(approver: Approver) = testContext {
            developerDao.create(aDeveloperDetails("dev-0")).shouldBeSuccess()
            developerDao.create(aDeveloperDetails("dev-1")).shouldBeSuccess()

            val response = underTest(
                Request(
                    method = GET,
                    uri = "/api/v1/developers/info",
                )
            )

            response shouldHaveStatus OK
            approver.assertApproved(response)
        }
    }

    @Nested
    inner class Write {

        @Test
        fun `save a developer`(approver: Approver) = testContext {
            val requestBodyLens = Body.auto<PostDeveloper>().toLens()
            val responseBodyLens = Body.auto<Developer>().toLens()

            val response = underTest(
                Request(
                    method = POST,
                    uri = "/api/v1/developers",
                ).with(requestBodyLens of PostDeveloper("dev-0"))
            )

            response shouldHaveStatus CREATED
            approver.assertApproved(response)

            val createdDeveloper = responseBodyLens(response)

            developerDao.findById(createdDeveloper.id).shouldNotBeNull {
                id shouldBe createdDeveloper.id
                name shouldBe "dev-0"
                archived shouldBe false
            }
        }
    }

    @Nested
    inner class Patch {
        private val bodyPatchDeveloperLens = Body.auto<PatchDeveloper>().toLens()
        private val responseBodyLens = Body.auto<Developer>().toLens()

        @ParameterizedTest(name = "set archived to {0}")
        @ValueSource(booleans = [true, false])
        fun archived(newArchivedValue: Boolean, approver: Approver) = testContext {
            val developer = developerDao.create(aDeveloperDetails("dev-0"))
                .shouldBeSuccess()
                .also { it.archived shouldBe false }

            val response = underTest(
                Request(
                    method = Method.PATCH,
                    uri = "/api/v1/developers/{id}",
                ).with(
                    pathIdLens of developer.id.value,
                    bodyPatchDeveloperLens of PatchDeveloper(archived = newArchivedValue)
                )
            )

            response shouldHaveStatus OK
            approver.assertApproved(response)

            val updatedDeveloper = responseBodyLens(response)

            developerDao.findById(updatedDeveloper.id).shouldNotBeNull {
                id shouldBe updatedDeveloper.id
                name shouldBe "dev-0"
                archived shouldBe newArchivedValue
            }
        }

        @Test
        fun `when developer does not exist with id then return not found`(approver: Approver) = testContext {
            val madeUpDeveloperId = DeveloperId(Random.nextLong())
            developerDao.findById(madeUpDeveloperId).shouldBeNull()

            val response = underTest(
                Request(
                    method = Method.PATCH,
                    uri = "/api/v1/developers/{id}",
                ).with(
                    pathIdLens of madeUpDeveloperId.value,
                    bodyPatchDeveloperLens of PatchDeveloper(archived = false)
                )
            )

            response shouldHaveStatus NOT_FOUND
            approver.assertApproved(response)
        }
    }

    @Nested
    inner class ReadDeveloperStats {

        @Test
        fun `when developer does not exist with id then return not found`(approver: Approver) = testContext {
            val madeUpDeveloperId = DeveloperId(Random.nextLong())
            developerDao.findById(madeUpDeveloperId).shouldBeNull()

            val response = underTest(
                Request(
                    method = GET,
                    uri = "/api/v1/developers/{id}/stats",
                ).with(
                    pathIdLens of madeUpDeveloperId.value,
                )
            )

            response shouldHaveStatus NOT_FOUND
            approver.assertApproved(response)
        }

        @Test
        fun `when developer exists but no pairs have happened then return just themselves`(approver: Approver) =
            testContext {
                val dev0Id = developerDao.create(aDeveloperDetails("dev-0")).shouldBeSuccess().id

                val response = underTest(
                    Request(
                        method = GET,
                        uri = "/api/v1/developers/{id}/stats",
                    ).with(
                        pathIdLens of dev0Id.value,
                    )
                )

                response shouldHaveStatus OK
                approver.assertApproved(response)
            }

        @Test
        // the sort is actually the wrong direction but keeping same behaviour while porting code
        fun `when developer exists and has paired then return statistics`(approver: Approver) = testContext {
            val dev0Id = developerDao.create(aDeveloperDetails("dev-0")).shouldBeSuccess().id
            val dev1Id = developerDao.create(aDeveloperDetails("dev-1")).shouldBeSuccess().id
            val dev2Id = developerDao.create(aDeveloperDetails("dev-2")).shouldBeSuccess().id
            developerDao.create(aDeveloperDetails("dev-3")).shouldBeSuccess().id

            val streamAId = streamDao.create(aStreamDetails("stream-a")).shouldBeSuccess().id
            val streamBId = streamDao.create(aStreamDetails("stream-b")).shouldBeSuccess().id

            combinationEventService.saveEvent(
                LocalDate.of(2024, 5, 5), listOf(
                    PairStreamByIds(listOf(dev0Id, dev1Id), streamAId),
                    PairStreamByIds(listOf(dev2Id), streamBId)
                )
            )
            combinationEventService.saveEvent(
                LocalDate.of(2024, 5, 6), listOf(
                    PairStreamByIds(listOf(dev0Id, dev2Id), streamAId),
                    PairStreamByIds(listOf(dev1Id), streamBId)
                )
            )
            combinationEventService.saveEvent(
                LocalDate.of(2024, 5, 7), listOf(
                    PairStreamByIds(listOf(dev0Id, dev1Id), streamAId),
                    PairStreamByIds(listOf(dev2Id), streamBId)
                )
            )

            val response = underTest(
                Request(
                    method = GET,
                    uri = "/api/v1/developers/{id}/stats",
                ).with(
                    pathIdLens of dev0Id.value,
                )
            )

            response shouldHaveStatus OK
            approver.assertApproved(response)
        }

        @Test
        // the sort is actually the wrong direction but keeping same behaviour while porting code
        fun `when developer exists and other developers and streams but no events have happened then sort alphabetically`(
            approver: Approver
        ) = testContext {
            developerDao.create(aDeveloperDetails("dev-3")).shouldBeSuccess()
            developerDao.create(aDeveloperDetails("dev-2")).shouldBeSuccess()
            developerDao.create(aDeveloperDetails("dev-1")).shouldBeSuccess()
            val dev0Id = developerDao.create(aDeveloperDetails("dev-0")).shouldBeSuccess().id

            streamDao.create(aStreamDetails("stream-c")).shouldBeSuccess()
            streamDao.create(aStreamDetails("stream-b")).shouldBeSuccess()
            streamDao.create(aStreamDetails("stream-a")).shouldBeSuccess()


            val response = underTest(
                Request(
                    method = GET,
                    uri = "/api/v1/developers/{id}/stats",
                ).with(
                    pathIdLens of dev0Id.value,
                )
            )

            response shouldHaveStatus OK
            approver.assertApproved(response)
        }

    }

    data class PostDeveloper(
        val name: String,
    )

    data class PatchDeveloper(
        val archived: Boolean,
    )

}