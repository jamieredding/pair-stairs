package dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao

import dev.coldhands.pair.stairs.backend.domain.CombinationEventId
import dev.coldhands.pair.stairs.backend.domain.DeveloperId
import dev.coldhands.pair.stairs.backend.domain.StreamId
import dev.coldhands.pair.stairs.backend.domain.combination.CombinationEvent
import dev.coldhands.pair.stairs.backend.domain.combination.CombinationEventDaoCdc
import dev.coldhands.pair.stairs.backend.domain.developer.Developer
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperDetails
import dev.coldhands.pair.stairs.backend.domain.stream.Stream
import dev.coldhands.pair.stairs.backend.domain.stream.StreamDetails
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class FakeCombinationEventDaoTest : CombinationEventDaoCdc<FakeCombinationEventDao>() {
    private val developerDao: FakeDeveloperDao = FakeDeveloperDao()
    private val streamDao: FakeStreamDao = FakeStreamDao()
    override val underTest: FakeCombinationEventDao = FakeCombinationEventDao(developerDao, streamDao)

    override fun assertNoCombinationEventExistsWithId(combinationEventId: CombinationEventId) {
        underTest.combinationEventsView[combinationEventId].shouldBeNull()
    }

    override fun assertNoCombinationEventExistsWithId(developerId: DeveloperId) {
        underTest.combinationEventsView.values.find { events ->
            events.combination
                .flatMap { pairStream -> pairStream.developerIds }
                .any { it == developerId }
        }.shouldBeNull()
    }

    override fun assertNoCombinationEventExistsWithId(streamId: StreamId) {
        underTest.combinationEventsView.values.find { events ->
            events.combination
                .map { pairStream -> pairStream.streamId }
                .any { it == streamId }
        }.shouldBeNull()
    }

    override fun assertNoDeveloperExistsWithId(developerId: DeveloperId) {
        developerDao.developersView[developerId].shouldBeNull()
    }

    override fun assertNoStreamExistsWithId(streamId: StreamId) {
        streamDao.streamsView[streamId].shouldBeNull()
    }

    override fun assertCombinationEventExistsWithId(combinationEventId: CombinationEventId) {
        underTest.combinationEventsView[combinationEventId].shouldNotBeNull()
    }

    override fun ensureNoCombinationEventsExist() {
        underTest.combinationEventsView.shouldBeEmpty()
    }

    override fun assertCombinationEventExists(combinationEvent: CombinationEvent) {
        underTest.combinationEventsView[combinationEvent.id].shouldNotBeNull {
            id shouldBe combinationEvent.id
            date shouldBe combinationEvent.date
            combination shouldBe combinationEvent.combination
        }
    }

    override fun createDeveloper(developerDetails: DeveloperDetails): Developer =
        developerDao.create(developerDetails).shouldBeSuccess()

    override fun createStream(streamDetails: StreamDetails): Stream =
        streamDao.create(streamDetails).shouldBeSuccess()
}