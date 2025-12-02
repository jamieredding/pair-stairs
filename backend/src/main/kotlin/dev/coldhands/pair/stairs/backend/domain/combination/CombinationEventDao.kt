package dev.coldhands.pair.stairs.backend.domain.combination

import dev.coldhands.pair.stairs.backend.domain.CombinationEventId
import dev.coldhands.pair.stairs.backend.domain.DeveloperId
import dev.coldhands.pair.stairs.backend.domain.PageRequest
import dev.coldhands.pair.stairs.backend.domain.StreamId
import dev.coldhands.pair.stairs.backend.domain.combination.CombinationEventDao.FindAllSort.DATE_DESCENDING
import dev.forkhandles.result4k.Result
import java.time.LocalDate

interface CombinationEventDao {
    fun findById(combinationEventId: CombinationEventId): CombinationEvent?
    fun findAll(pageRequest: PageRequest<FindAllSort>): List<CombinationEvent>
    fun getMostRecentCombinationEvents(count: Int): List<CombinationEvent> =
        findAll(
            PageRequest(
                requestedPage = 0,
                pageSize = count,
                sort = DATE_DESCENDING
            )
        )

    fun findByDeveloperId(developerId: DeveloperId): List<CombinationEvent>
    fun findByDeveloperIdBetween(
        developerId: DeveloperId,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<CombinationEvent>

    fun findByStreamId(streamId: StreamId): List<CombinationEvent>
    fun findByStreamIdBetween(streamId: StreamId, startDate: LocalDate, endDate: LocalDate): List<CombinationEvent>

    fun create(combinationEventDetails: CombinationEventDetails): Result<CombinationEvent, CombinationEventCreateError>
    fun delete(combinationEventId: CombinationEventId)

    enum class FindAllSort {
        DATE_DESCENDING
    }
}

sealed class CombinationEventCreateError {
    data class DeveloperNotFound(val developerId: DeveloperId) : CombinationEventCreateError()
    data class StreamNotFound(val streamId: StreamId) : CombinationEventCreateError()

}
