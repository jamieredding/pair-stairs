package dev.coldhands.pair.stairs.backend.infrastructure.wiring

import dev.coldhands.pair.stairs.backend.domain.combination.CombinationCalculationService
import dev.coldhands.pair.stairs.backend.domain.combination.CombinationEventDao
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperDao
import dev.coldhands.pair.stairs.backend.domain.stream.StreamDao
import dev.coldhands.pair.stairs.backend.usecase.BackendCombinationHistoryRepository
import dev.coldhands.pair.stairs.backend.usecase.CombinationEventService
import dev.coldhands.pair.stairs.backend.usecase.CoreCombinationCalculationService
import dev.coldhands.pair.stairs.backend.usecase.EntryPointFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class CombinationConfig {

    @Bean
    open fun combinationCalculationService(
        developerDao: DeveloperDao,
        streamDao: StreamDao,
        backendCombinationHistoryRepository: BackendCombinationHistoryRepository
    ): CombinationCalculationService =
        CoreCombinationCalculationService(
            entryPointFactory = EntryPointFactory(backendCombinationHistoryRepository)
        )

    @Bean
    open fun combinationEventService(combinationEventDao: CombinationEventDao): CombinationEventService =
        CombinationEventService(
            combinationEventDao = combinationEventDao
        )

    @Bean
    open fun backendCombinationHistoryRepository(
        combinationEventDao: CombinationEventDao
    ): BackendCombinationHistoryRepository =
        BackendCombinationHistoryRepository(
            combinationEventDao
        )
}