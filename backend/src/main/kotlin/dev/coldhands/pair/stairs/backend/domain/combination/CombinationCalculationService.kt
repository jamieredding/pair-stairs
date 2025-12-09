package dev.coldhands.pair.stairs.backend.domain.combination

import dev.coldhands.pair.stairs.backend.domain.DeveloperId
import dev.coldhands.pair.stairs.backend.domain.Page
import dev.coldhands.pair.stairs.backend.domain.StreamId

interface CombinationCalculationService {

    fun calculate(developerIds: Collection<DeveloperId>, streamIds: Collection<StreamId>, page: Int, pageSize: Int): Page<ScoredCombination>
}