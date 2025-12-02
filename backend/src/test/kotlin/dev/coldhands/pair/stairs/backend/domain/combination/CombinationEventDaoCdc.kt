package dev.coldhands.pair.stairs.backend.domain.combination

import dev.coldhands.pair.stairs.backend.aCombinationEventId
import dev.coldhands.pair.stairs.backend.aDeveloperId
import dev.coldhands.pair.stairs.backend.aStreamId
import dev.coldhands.pair.stairs.backend.domain.CombinationEventId
import dev.coldhands.pair.stairs.backend.domain.DeveloperId
import dev.coldhands.pair.stairs.backend.domain.PageRequest
import dev.coldhands.pair.stairs.backend.domain.StreamId
import dev.coldhands.pair.stairs.backend.domain.combination.CombinationEvent.PairStream
import dev.coldhands.pair.stairs.backend.domain.combination.CombinationEventDao.FindAllSort.DATE_DESCENDING
import dev.coldhands.pair.stairs.backend.domain.developer.Developer
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperDaoCdc.TestFixtures.someDeveloperDetails
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperDetails
import dev.coldhands.pair.stairs.backend.domain.stream.Stream
import dev.coldhands.pair.stairs.backend.domain.stream.StreamDaoCdc.TestFixtures.someStreamDetails
import dev.coldhands.pair.stairs.backend.domain.stream.StreamDetails
import dev.forkhandles.result4k.kotest.shouldBeFailure
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeUnique
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit
import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.concurrent.thread

@Suppress("unused")
abstract class CombinationEventDaoCdc<T : CombinationEventDao> {

    val precision: TemporalUnit = ChronoUnit.MILLIS
    abstract val underTest: T
    private val now = LocalDate.now()

    @Nested
    inner class FindById {

        @Test
        fun `should return null when no combination event exists with that id`() {
            val combinationEventId = aCombinationEventId()
            assertNoCombinationEventExistsWithId(combinationEventId)

            underTest.findById(combinationEventId).shouldBeNull()
        }

        @Test
        fun `should find combination event by id when combination event exists`() {
            val combinationEvent = createCombinationEvent()

            underTest.findById(combinationEvent.id) shouldBe combinationEvent
        }
    }

    @Nested
    inner class FindAll {

        @Test
        fun `should return empty list when no combination events exist`() {
            ensureNoCombinationEventsExist()

            underTest.findAll(
                PageRequest(
                    requestedPage = 0,
                    pageSize = 1,
                    sort = DATE_DESCENDING
                )
            ).shouldBeEmpty()
        }

        @Test
        fun `should find all combination events when multiple exist`() {
            ensureNoCombinationEventsExist()
            val combinationEvent1 = createCombinationEvent(date = now)
            val combinationEvent2 = createCombinationEvent(date = now.minusDays(1))

            underTest.findAll(
                PageRequest(
                    requestedPage = 0,
                    pageSize = 2,
                    sort = DATE_DESCENDING
                )
            ) shouldBe listOf(combinationEvent1, combinationEvent2)
        }

        @Nested
        inner class Pagination {

            @Test
            fun `return first page when more than a single page exists`() {
                ensureNoCombinationEventsExist()
                val combinationEvent1 = createCombinationEvent(date = now)
                val combinationEvent2 = createCombinationEvent(date = now.minusDays(1))

                underTest.findAll(
                    PageRequest(
                        requestedPage = 0,
                        pageSize = 1,
                        sort = DATE_DESCENDING
                    )
                ) shouldBe listOf(combinationEvent1)
            }

            @Test
            fun `return requested page when more than a single page exists`() {
                ensureNoCombinationEventsExist()
                val combinationEvent1 = createCombinationEvent(date = now)
                val combinationEvent2 = createCombinationEvent(date = now.minusDays(1))

                underTest.findAll(
                    PageRequest(
                        requestedPage = 1,
                        pageSize = 1,
                        sort = DATE_DESCENDING
                    )
                ) shouldBe listOf(combinationEvent2)
            }

            @Test
            fun `return empty page when no pages remain`() {
                ensureNoCombinationEventsExist()
                val combinationEvent1 = createCombinationEvent(date = now)
                val combinationEvent2 = createCombinationEvent(date = now.minusDays(1))

                underTest.findAll(
                    PageRequest(
                        requestedPage = 2,
                        pageSize = 1,
                        sort = DATE_DESCENDING
                    )
                ).shouldBeEmpty()
            }

            @Test
            fun `return partial page when out of results`() {
                ensureNoCombinationEventsExist()
                val combinationEvent1 = createCombinationEvent(date = now)
                val combinationEvent2 = createCombinationEvent(date = now.minusDays(1))
                val combinationEvent3 = createCombinationEvent(date = now.minusDays(2))

                underTest.findAll(
                    PageRequest(
                        requestedPage = 1,
                        pageSize = 2,
                        sort = DATE_DESCENDING
                    )
                ) shouldBe listOf(combinationEvent3)
            }

        }

        @Nested
        inner class Sorting {

            @Test
            fun `results should be sorted by DATE_DESCENDING`() {
                ensureNoCombinationEventsExist()
                val combinationEvent1 = createCombinationEvent(date = now)
                val combinationEvent2 = createCombinationEvent(date = now.plusDays(1))
                val combinationEvent3 = createCombinationEvent(date = now.minusDays(2))

                underTest.findAll(
                    PageRequest(
                        requestedPage = 0,
                        pageSize = 3,
                        sort = DATE_DESCENDING
                    )
                ) shouldBe listOf(combinationEvent2, combinationEvent1, combinationEvent3)
            }
        }
    }

    @Nested
    inner class FindByDeveloperId {

        @Test
        fun `should return empty list when no combination events exist for developer id`() {
            val dev0 = createDeveloper(someDeveloperDetails(name = "dev-0"))
            val dev1 = createDeveloper(someDeveloperDetails(name = "dev-1"))
            val stream0 = createStream(someStreamDetails(name = "stream-0"))

            val combinationEventDetails = CombinationEventDetails(
                date = now,
                combination = setOf(
                    PairStream(
                        developerIds = setOf(dev0.id),
                        streamId = stream0.id
                    ),
                )
            )
            underTest.create(combinationEventDetails).shouldBeSuccess()

            underTest.findByDeveloperId(dev1.id).shouldBeEmpty()
        }

        @Test
        fun `should return all combination events that contain developer id`() {
            val dev0 = createDeveloper(someDeveloperDetails(name = "dev-0"))
            val dev1 = createDeveloper(someDeveloperDetails(name = "dev-1"))
            val dev2 = createDeveloper(someDeveloperDetails(name = "dev-2"))
            val stream0 = createStream(someStreamDetails(name = "stream-0"))
            val stream1 = createStream(someStreamDetails(name = "stream-1"))

            val event0 = underTest.create(
                CombinationEventDetails(
                    date = now,
                    combination = setOf(
                        PairStream(
                            developerIds = setOf(dev0.id),
                            streamId = stream0.id
                        ),
                    )
                )
            ).shouldBeSuccess()
            val event1 = underTest.create(
                CombinationEventDetails(
                    date = now.plusDays(1),
                    combination = setOf(
                        PairStream(
                            developerIds = setOf(dev1.id),
                            streamId = stream0.id
                        ),
                    )
                )
            ).shouldBeSuccess()
            val event2 = underTest.create(
                CombinationEventDetails(
                    date = now.plusDays(2),
                    combination = setOf(
                        PairStream(
                            developerIds = setOf(dev0.id, dev2.id),
                            streamId = stream0.id
                        ),
                        PairStream(
                            developerIds = setOf(dev1.id),
                            streamId = stream1.id
                        ),
                    )
                )
            ).shouldBeSuccess()

            underTest.findByDeveloperId(dev0.id).shouldContainExactly(
                event0,
                event2
            )
        }
    }

    @Nested
    inner class FindByDeveloperIdBetween {

        @Test
        fun `should return empty list when no combination events exist for developer id`() {
            val dev0 = createDeveloper(someDeveloperDetails(name = "dev-0"))
            val dev1 = createDeveloper(someDeveloperDetails(name = "dev-1"))
            val stream0 = createStream(someStreamDetails(name = "stream-0"))

            val combinationEventDetails = CombinationEventDetails(
                date = now,
                combination = setOf(
                    PairStream(
                        developerIds = setOf(dev0.id),
                        streamId = stream0.id
                    ),
                )
            )
            underTest.create(combinationEventDetails).shouldBeSuccess()

            underTest.findByDeveloperIdBetween(
                developerId = dev1.id,
                startDate = now,
                endDate = now.plusDays(1)
            ).shouldBeEmpty()
        }

        @Test
        fun `should return empty list when no combination events exist for developer id between date range`() {
            val dev0 = createDeveloper(someDeveloperDetails(name = "dev-0"))
            val stream0 = createStream(someStreamDetails(name = "stream-0"))

            val combinationEventDetails = CombinationEventDetails(
                date = now.plusDays(2),
                combination = setOf(
                    PairStream(
                        developerIds = setOf(dev0.id),
                        streamId = stream0.id
                    ),
                )
            )
            underTest.create(combinationEventDetails).shouldBeSuccess()

            underTest.findByDeveloperIdBetween(
                developerId = dev0.id,
                startDate = now,
                endDate = now.plusDays(1)
            ).shouldBeEmpty()
        }

        @Test
        fun `should return all combination events that contain developer id and occur between date range`() {
            val dev0 = createDeveloper(someDeveloperDetails(name = "dev-0"))
            val dev1 = createDeveloper(someDeveloperDetails(name = "dev-1"))
            val dev2 = createDeveloper(someDeveloperDetails(name = "dev-2"))
            val stream0 = createStream(someStreamDetails(name = "stream-0"))
            val stream1 = createStream(someStreamDetails(name = "stream-1"))

            val event0 = underTest.create(
                CombinationEventDetails(
                    date = now,
                    combination = setOf(
                        PairStream(
                            developerIds = setOf(dev0.id),
                            streamId = stream0.id
                        ),
                    )
                )
            ).shouldBeSuccess()
            val event1 = underTest.create(
                CombinationEventDetails(
                    date = now.plusDays(1),
                    combination = setOf(
                        PairStream(
                            developerIds = setOf(dev1.id),
                            streamId = stream0.id
                        ),
                    )
                )
            ).shouldBeSuccess()
            val event2 = underTest.create(
                CombinationEventDetails(
                    date = now.plusDays(2),
                    combination = setOf(
                        PairStream(
                            developerIds = setOf(dev0.id, dev2.id),
                            streamId = stream0.id
                        ),
                        PairStream(
                            developerIds = setOf(dev1.id),
                            streamId = stream1.id
                        ),
                    )
                )
            ).shouldBeSuccess()
            val event3 = underTest.create(
                CombinationEventDetails(
                    date = now.plusDays(3),
                    combination = setOf(
                        PairStream(
                            developerIds = setOf(dev0.id, dev2.id),
                            streamId = stream0.id
                        ),
                        PairStream(
                            developerIds = setOf(dev1.id),
                            streamId = stream1.id
                        ),
                    )
                )
            ).shouldBeSuccess()

            underTest.findByDeveloperIdBetween(
                developerId = dev0.id,
                startDate = now,
                endDate = now.plusDays(2)
            ).shouldContainExactly(
                event0,
                event2
            )
        }
    }

    @Nested
    inner class FindByStreamId {

        @Test
        fun `should return empty list when no combination events exist for stream id`() {
            val dev0 = createDeveloper(someDeveloperDetails(name = "dev-0"))
            val stream0 = createStream(someStreamDetails(name = "stream-0"))
            val stream1 = createStream(someStreamDetails(name = "stream-1"))

            val combinationEventDetails = CombinationEventDetails(
                date = now,
                combination = setOf(
                    PairStream(
                        developerIds = setOf(dev0.id),
                        streamId = stream0.id
                    ),
                )
            )
            underTest.create(combinationEventDetails).shouldBeSuccess()

            underTest.findByStreamId(stream1.id).shouldBeEmpty()
        }

        @Test
        fun `should return all combination events that contain stream id`() {
            val dev0 = createDeveloper(someDeveloperDetails(name = "dev-0"))
            val dev1 = createDeveloper(someDeveloperDetails(name = "dev-1"))
            val dev2 = createDeveloper(someDeveloperDetails(name = "dev-2"))
            val stream0 = createStream(someStreamDetails(name = "stream-0"))
            val stream1 = createStream(someStreamDetails(name = "stream-1"))

            val event0 = underTest.create(
                CombinationEventDetails(
                    date = now,
                    combination = setOf(
                        PairStream(
                            developerIds = setOf(dev0.id),
                            streamId = stream0.id
                        ),
                    )
                )
            ).shouldBeSuccess()
            val event1 = underTest.create(
                CombinationEventDetails(
                    date = now.plusDays(1),
                    combination = setOf(
                        PairStream(
                            developerIds = setOf(dev1.id),
                            streamId = stream1.id
                        ),
                    )
                )
            ).shouldBeSuccess()
            val event2 = underTest.create(
                CombinationEventDetails(
                    date = now.plusDays(2),
                    combination = setOf(
                        PairStream(
                            developerIds = setOf(dev0.id, dev2.id),
                            streamId = stream0.id
                        ),
                        PairStream(
                            developerIds = setOf(dev1.id),
                            streamId = stream1.id
                        ),
                    )
                )
            ).shouldBeSuccess()

            underTest.findByStreamId(stream0.id).shouldContainExactly(
                event0,
                event2
            )
        }
    }

    @Nested
    inner class FindByStreamIdBetween {

        @Test
        fun `should return empty list when no combination events exist for stream id`() {
            val dev0 = createDeveloper(someDeveloperDetails(name = "dev-0"))
            val stream0 = createStream(someStreamDetails(name = "stream-0"))
            val stream1 = createStream(someStreamDetails(name = "stream-1"))

            val combinationEventDetails = CombinationEventDetails(
                date = now,
                combination = setOf(
                    PairStream(
                        developerIds = setOf(dev0.id),
                        streamId = stream0.id
                    ),
                )
            )
            underTest.create(combinationEventDetails).shouldBeSuccess()

            underTest.findByStreamIdBetween(
                streamId = stream1.id,
                startDate = now,
                endDate = now.plusDays(1)
            ).shouldBeEmpty()
        }

        @Test
        fun `should return empty list when no combination events exist for stream id between date range`() {
            val dev0 = createDeveloper(someDeveloperDetails(name = "dev-0"))
            val stream0 = createStream(someStreamDetails(name = "stream-0"))

            val combinationEventDetails = CombinationEventDetails(
                date = now.plusDays(2),
                combination = setOf(
                    PairStream(
                        developerIds = setOf(dev0.id),
                        streamId = stream0.id
                    ),
                )
            )
            underTest.create(combinationEventDetails).shouldBeSuccess()

            underTest.findByStreamIdBetween(
                streamId = stream0.id,
                startDate = now,
                endDate = now.plusDays(1)
            ).shouldBeEmpty()
        }

        @Test
        fun `should return all combination events that contain stream id`() {
            val dev0 = createDeveloper(someDeveloperDetails(name = "dev-0"))
            val dev1 = createDeveloper(someDeveloperDetails(name = "dev-1"))
            val dev2 = createDeveloper(someDeveloperDetails(name = "dev-2"))
            val stream0 = createStream(someStreamDetails(name = "stream-0"))
            val stream1 = createStream(someStreamDetails(name = "stream-1"))

            val event0 = underTest.create(
                CombinationEventDetails(
                    date = now,
                    combination = setOf(
                        PairStream(
                            developerIds = setOf(dev0.id),
                            streamId = stream0.id
                        ),
                    )
                )
            ).shouldBeSuccess()
            val event1 = underTest.create(
                CombinationEventDetails(
                    date = now.plusDays(1),
                    combination = setOf(
                        PairStream(
                            developerIds = setOf(dev1.id),
                            streamId = stream1.id
                        ),
                    )
                )
            ).shouldBeSuccess()
            val event2 = underTest.create(
                CombinationEventDetails(
                    date = now.plusDays(2),
                    combination = setOf(
                        PairStream(
                            developerIds = setOf(dev0.id, dev2.id),
                            streamId = stream0.id
                        ),
                        PairStream(
                            developerIds = setOf(dev1.id),
                            streamId = stream1.id
                        ),
                    )
                )
            ).shouldBeSuccess()
            val event3 = underTest.create(
                CombinationEventDetails(
                    date = now.plusDays(3),
                    combination = setOf(
                        PairStream(
                            developerIds = setOf(dev0.id, dev2.id),
                            streamId = stream0.id
                        ),
                        PairStream(
                            developerIds = setOf(dev1.id),
                            streamId = stream1.id
                        ),
                    )
                )
            ).shouldBeSuccess()

            underTest.findByStreamIdBetween(
                streamId = stream0.id,
                startDate = now,
                endDate = now.plusDays(2)
            ).shouldContainExactly(
                event0,
                event2
            )
        }
    }

    @Nested
    inner class GetMostRecentCombinationEvents {

        @Test
        fun `should return empty list when no combination events exist`() {
            ensureNoCombinationEventsExist()

            underTest.getMostRecentCombinationEvents(5).shouldBeEmpty()
        }

        @Test
        fun `should most recent x combination events when multiple exist`() {
            ensureNoCombinationEventsExist()
            val combinationEvent1 = createCombinationEvent(date = now)
            val combinationEvent2 = createCombinationEvent(date = now.minusDays(1))
            val combinationEvent3 = createCombinationEvent(date = now.minusDays(2))

            underTest.getMostRecentCombinationEvents(2) shouldBe listOf(combinationEvent1, combinationEvent2)
        }

        @Test
        fun `results should be sorted by date descending`() {
            ensureNoCombinationEventsExist()
            val combinationEvent1 = createCombinationEvent(date = now)
            val combinationEvent2 = createCombinationEvent(date = now.plusDays(1))
            val combinationEvent3 = createCombinationEvent(date = now.minusDays(2))

            underTest.getMostRecentCombinationEvents(3) shouldBe listOf(combinationEvent2, combinationEvent1, combinationEvent3)
        }
    }

    @Nested
    inner class Create {

        @Test
        fun `should create combination event with specified details and generate id`() {
            val dev0 = createDeveloper(someDeveloperDetails(name = "dev-0"))
            val dev1 = createDeveloper(someDeveloperDetails(name = "dev-1"))
            val dev2 = createDeveloper(someDeveloperDetails(name = "dev-2"))
            val stream0 = createStream(someStreamDetails(name = "stream-0"))
            val stream1 = createStream(someStreamDetails(name = "stream-1"))

            val combinationEventDetails = CombinationEventDetails(
                date = now,
                combination = setOf(
                    PairStream(
                        developerIds = setOf(dev0.id, dev1.id),
                        streamId = stream0.id
                    ),
                    PairStream(
                        developerIds = setOf(dev2.id),
                        streamId = stream1.id
                    )
                )
            )
            val combinationEvent = underTest.create(combinationEventDetails).shouldBeSuccess()

            combinationEvent.id.shouldNotBeNull()
            combinationEvent.date shouldBe combinationEventDetails.date
            combinationEvent.combination shouldBe combinationEventDetails.combination

            assertCombinationEventExists(combinationEvent)
        }

        @Test
        fun `should create combination events with ascending ids`() {
            val dev0 = createDeveloper(someDeveloperDetails(name = "dev-0"))
            val dev1 = createDeveloper(someDeveloperDetails(name = "dev-1"))
            val stream0 = createStream(someStreamDetails(name = "stream-0"))

            val combinationEventDetails = CombinationEventDetails(
                date = now,
                combination = setOf(
                    PairStream(
                        developerIds = setOf(dev0.id, dev1.id),
                        streamId = stream0.id
                    ),
                )
            )
            val event1 = underTest.create(combinationEventDetails).shouldBeSuccess()
            val event2 = underTest.create(
                combinationEventDetails
                    .copy(date = event1.date.plusDays(1))
            ).shouldBeSuccess()

            event1.id shouldNotBe event2.id
            event2.id.value shouldBeGreaterThan event1.id.value
        }

        @Test
        fun `can create combination events concurrently`() {
            val dev0 = createDeveloper(someDeveloperDetails(name = "dev-0"))
            val dev1 = createDeveloper(someDeveloperDetails(name = "dev-1"))
            val stream0 = createStream(someStreamDetails(name = "stream-0"))
            val combinationEventDetails = CombinationEventDetails(
                date = now,
                combination = setOf(
                    PairStream(
                        developerIds = setOf(dev0.id, dev1.id),
                        streamId = stream0.id
                    ),
                )
            )

            val ids = ConcurrentLinkedDeque<CombinationEventId>()

            (1..10).map { i ->
                thread(name = "many-threads-$i") {
                    repeat(10) { j ->
                        underTest.create(
                            combinationEventDetails.copy(
                                date = combinationEventDetails.date.plusDays(i.toLong()).plusYears(j.toLong())
                            )
                        ).shouldBeSuccess {
                            ids.add(it.id)
                        }
                    }
                }
            }.map { it.join() }

            ids.size shouldBe 100
            ids.shouldBeUnique()
            ids.forEach { assertCombinationEventExistsWithId(it) }
        }

        // todo should not duplicate combination/pairstreams? is this just something to test in the jpa level?

        @Nested
        inner class SadPath {

            @Test
            fun `do not create combination event if developer id does not exist`() {
                val dev0 = createDeveloper(someDeveloperDetails(name = "dev-0"))
                val madeUpDeveloperId = aDeveloperId() + 1
                val stream0 = createStream(someStreamDetails(name = "stream-0"))
                val combinationEventDetails = CombinationEventDetails(
                    date = now,
                    combination = setOf(
                        PairStream(
                            developerIds = setOf(dev0.id, madeUpDeveloperId),
                            streamId = stream0.id
                        ),
                    )
                )

                assertNoDeveloperExistsWithId(madeUpDeveloperId)

                underTest.create(combinationEventDetails)
                    .shouldBeFailure(CombinationEventCreateError.DeveloperNotFound(madeUpDeveloperId))

                assertNoCombinationEventExistsWithId(madeUpDeveloperId)
            }

            @Test
            fun `do not create combination event if stream id does not exist`() {
                val dev0 = createDeveloper(someDeveloperDetails(name = "dev-0"))
                val madeUpStreamId = aStreamId() + 1
                val combinationEventDetails = CombinationEventDetails(
                    date = now,
                    combination = setOf(
                        PairStream(
                            developerIds = setOf(dev0.id),
                            streamId = madeUpStreamId
                        ),
                    )
                )

                assertNoStreamExistsWithId(madeUpStreamId)

                underTest.create(combinationEventDetails)
                    .shouldBeFailure(CombinationEventCreateError.StreamNotFound(madeUpStreamId))

                assertNoCombinationEventExistsWithId(madeUpStreamId)
            }
        }
    }

    @Nested
    inner class Delete {

        @Test
        fun `can delete existing combination event`() {
            val dev0 = createDeveloper(someDeveloperDetails(name = "dev-0"))
            val dev1 = createDeveloper(someDeveloperDetails(name = "dev-1"))
            val stream0 = createStream(someStreamDetails(name = "stream-0"))

            val combinationEventDetails = CombinationEventDetails(
                date = now,
                combination = setOf(
                    PairStream(
                        developerIds = setOf(dev0.id, dev1.id),
                        streamId = stream0.id
                    ),
                )
            )
            val combinationEvent = underTest.create(combinationEventDetails).shouldBeSuccess()
            assertCombinationEventExists(combinationEvent)

            underTest.delete(combinationEvent.id)

            assertNoCombinationEventExistsWithId(combinationEvent.id)
        }

        @Test
        fun `do not throw exception when combination event doesn't exist`() {
            val combinationEventId = aCombinationEventId()
            assertNoCombinationEventExistsWithId(combinationEventId)

            shouldNotThrow<Throwable> {
                underTest.delete(combinationEventId)
            }
        }

    }

    abstract fun assertNoCombinationEventExistsWithId(combinationEventId: CombinationEventId)
    abstract fun assertNoCombinationEventExistsWithId(developerId: DeveloperId)
    abstract fun assertNoCombinationEventExistsWithId(streamId: StreamId)
    abstract fun assertNoDeveloperExistsWithId(developerId: DeveloperId)
    abstract fun assertNoStreamExistsWithId(streamId: StreamId)
    abstract fun assertCombinationEventExistsWithId(combinationEventId: CombinationEventId)
    abstract fun ensureNoCombinationEventsExist()
    abstract fun assertCombinationEventExists(combinationEvent: CombinationEvent)
    fun createCombinationEvent(date: LocalDate = now): CombinationEvent {
        val dev0 = createDeveloper(someDeveloperDetails(name = "dev-0"))
        val dev1 = createDeveloper(someDeveloperDetails(name = "dev-1"))
        val dev2 = createDeveloper(someDeveloperDetails(name = "dev-2"))
        val stream0 = createStream(someStreamDetails(name = "stream-0"))
        val stream1 = createStream(someStreamDetails(name = "stream-1"))

        val combinationEventDetails = CombinationEventDetails(
            date = date,
            combination = setOf(
                PairStream(
                    developerIds = setOf(dev0.id, dev1.id),
                    streamId = stream0.id
                ),
                PairStream(
                    developerIds = setOf(dev2.id),
                    streamId = stream1.id
                )
            )
        )
        return underTest.create(combinationEventDetails).shouldBeSuccess()
    }

    abstract fun createDeveloper(developerDetails: DeveloperDetails): Developer
    abstract fun createStream(streamId: StreamDetails): Stream

    companion object TestFixtures {
        operator fun DeveloperId.plus(number: Long) = DeveloperId(value + number)
        operator fun StreamId.plus(number: Long) = StreamId(value + number)

        fun someCombinationEventDetails(
            date: LocalDate = LocalDate.now(),
            combination: Set<PairStream> = setOf(
                PairStream(
                    developerIds = setOf(aDeveloperId(), aDeveloperId() + 1),
                    streamId = aStreamId()
                )
            )
        ) = CombinationEventDetails(
            date = date,
            combination = combination
        )

        fun someCombinationEvent() = CombinationEvent(
            id = aCombinationEventId(),
            date = LocalDate.now(),
            combination = setOf(
                PairStream(
                    developerIds = setOf(aDeveloperId(), aDeveloperId() + 1),
                    streamId = aStreamId()
                )
            )
        )
    }
}