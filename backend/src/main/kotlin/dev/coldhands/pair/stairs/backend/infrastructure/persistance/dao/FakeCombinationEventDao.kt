package dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao

import dev.coldhands.pair.stairs.backend.domain.CombinationEventId
import dev.coldhands.pair.stairs.backend.domain.DeveloperId
import dev.coldhands.pair.stairs.backend.domain.PageRequest
import dev.coldhands.pair.stairs.backend.domain.StreamId
import dev.coldhands.pair.stairs.backend.domain.combination.CombinationEvent
import dev.coldhands.pair.stairs.backend.domain.combination.CombinationEventCreateError
import dev.coldhands.pair.stairs.backend.domain.combination.CombinationEventDao
import dev.coldhands.pair.stairs.backend.domain.combination.CombinationEventDetails
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.asFailure
import dev.forkhandles.result4k.asSuccess
import java.time.LocalDate
import java.util.Collections.unmodifiableMap
import java.util.concurrent.atomic.AtomicLong

class FakeCombinationEventDao(
    private val developerDao: FakeDeveloperDao,
    private val streamDao: FakeStreamDao
) : CombinationEventDao {
    private val combinationEvents = mutableMapOf<CombinationEventId, CombinationEvent>()
    val combinationEventsView: Map<CombinationEventId, CombinationEvent> = unmodifiableMap(combinationEvents)
    private var nextId = AtomicLong(0)

    override fun findById(combinationEventId: CombinationEventId): CombinationEvent? =
        combinationEvents[combinationEventId]

    override fun findAll(pageRequest: PageRequest<CombinationEventDao.FindAllSort>): List<CombinationEvent> {
        val (requestedPage, pageSize, sort) = pageRequest
        val sortedEvents = when (sort) {
            CombinationEventDao.FindAllSort.DATE_DESCENDING -> combinationEvents.values.sortedByDescending { it.date }
        }
        return sortedEvents.windowed(size = pageSize, step = pageSize, partialWindows = true)
            .getOrElse(requestedPage) { listOf() }
    }

    override fun findByDeveloperId(developerId: DeveloperId): Set<CombinationEvent> =
        combinationEvents.values.filter { event ->
            event.combination.flatMap { it.developerIds }.contains(developerId)
        }
            .toSet()

    override fun findByDeveloperIdBetween(
        developerId: DeveloperId,
        startDate: LocalDate,
        endDate: LocalDate
    ): Set<CombinationEvent> =
        findByDeveloperId(developerId).filter { it.date in startDate..endDate }
            .toSet()

    override fun findByStreamId(streamId: StreamId): Set<CombinationEvent> =
        combinationEvents.values.filter { event ->
            event.combination.any { pairStream -> pairStream.streamId == streamId }
        }
            .toSet()

    override fun findByStreamIdBetween(
        streamId: StreamId,
        startDate: LocalDate,
        endDate: LocalDate
    ): Set<CombinationEvent> =
        findByStreamId(streamId).filter { it.date in startDate..endDate }
            .toSet()

    override fun create(combinationEventDetails: CombinationEventDetails): Result<CombinationEvent, CombinationEventCreateError> {
        combinationEventDetails.combination.forEach { pairStream ->
            streamDao.findById(pairStream.streamId)
                ?: return CombinationEventCreateError.StreamNotFound(pairStream.streamId).asFailure()
            pairStream.developerIds.forEach { developerId ->
                developerDao.findById(developerId) ?: return CombinationEventCreateError.DeveloperNotFound(developerId)
                    .asFailure()
            }
        }

        val combinationEventId = CombinationEventId(nextId.getAndIncrement())
        val combinationEvent = CombinationEvent(
            id = combinationEventId,
            date = combinationEventDetails.date,
            combination = combinationEventDetails.combination
        )
        combinationEvents[combinationEventId] = combinationEvent
        return combinationEvent.asSuccess()
    }

    override fun delete(combinationEventId: CombinationEventId) {
        combinationEvents.remove(combinationEventId)
    }
}