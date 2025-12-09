package dev.coldhands.pair.stairs.backend.usecase

import dev.coldhands.pair.stairs.backend.domain.CombinationEventId
import dev.coldhands.pair.stairs.backend.domain.PageRequest
import dev.coldhands.pair.stairs.backend.domain.combination.CombinationEvent
import dev.coldhands.pair.stairs.backend.domain.combination.CombinationEventDao
import dev.coldhands.pair.stairs.backend.domain.combination.CombinationEventDetails
import dev.coldhands.pair.stairs.backend.domain.combination.PairStream
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.SaveCombinationEventDto
import jakarta.persistence.EntityNotFoundException
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

    fun saveEvent(date: LocalDate, combinationByIds: List<SaveCombinationEventDto.PairStreamByIds>) {
        combinationEventDao.create(
            CombinationEventDetails(
                date = date,
                combination = combinationByIds.map {
                    PairStream(
                        it.developerIds().toSet(),
                        it.streamId()
                    )
                }.toSet()
            )
        ) // todo what about return of this?
    }

    fun deleteEvent(id: CombinationEventId) {
        if (combinationEventDao.findById(id) == null) {
            throw EntityNotFoundException()
        }
        combinationEventDao.delete(id)
    }
}