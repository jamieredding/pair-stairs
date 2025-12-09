package dev.coldhands.pair.stairs.backend.usecase

import dev.coldhands.pair.stairs.backend.domain.combination.CombinationEventDao
import dev.coldhands.pair.stairs.core.domain.Combination
import dev.coldhands.pair.stairs.core.domain.CombinationHistoryRepository
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStream as CorePairStream

class BackendCombinationHistoryRepository(
    private val combinationEventDao: CombinationEventDao,
) : CombinationHistoryRepository<CorePairStream> {
    override fun getMostRecentCombinations(count: Int): List<Combination<CorePairStream>> {
        return combinationEventDao.getMostRecentCombinationEvents(count)
            .map { event ->
                Combination(event.combination.map {
                    val developerIdsAsString = it.developerIds
                        .map { developerId -> developerId.value.toString() }
                        .toSet()
                    CorePairStream(
                        developerIdsAsString,
                        it.streamId.value.toString()
                    )
                }
                    .toMutableSet()
                )
            }
    }
}