package dev.coldhands.pair.stairs.backend.usecase

import dev.coldhands.pair.stairs.backend.domain.CombinationEventId
import dev.coldhands.pair.stairs.backend.domain.PageRequest
import dev.coldhands.pair.stairs.backend.domain.combination.*
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.asFailure
import dev.forkhandles.result4k.asSuccess
import java.time.LocalDate

class CombinationEventService(
    private val combinationEventDao: CombinationEventDao,
) {

    fun getCombinationEvents(requestedPage: Int, pageSize: Int): List<CombinationEvent> {
        return combinationEventDao.findAll(
            PageRequest(
                requestedPage = requestedPage,
                pageSize = pageSize,
                sort = CombinationEventDao.FindAllSort.DATE_DESCENDING
            )
        )
    }

    fun saveEvent(date: LocalDate, pairStreams: Collection<PairStream>): Result<CombinationEvent, CombinationEventCreateError> =
        combinationEventDao.create(
            CombinationEventDetails(
                date = date,
                combination = pairStreams.toSet()
            )
        )

    fun deleteEvent(id: CombinationEventId): Result<Unit, CombinationEventNotFound>{
        if (combinationEventDao.findById(id) == null) {
            return CombinationEventNotFound.asFailure()
        }
        combinationEventDao.delete(id)
        return Unit.asSuccess()
    }

    object CombinationEventNotFound
}