package dev.coldhands.pair.stairs.backend.infrastructure.wiring

import dev.coldhands.pair.stairs.backend.domain.CombinationCalculationService
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperDao
import dev.coldhands.pair.stairs.backend.domain.stream.StreamDao
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.CombinationEventRepository
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.CombinationRepository
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.PairStreamRepository
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
            developerDao = developerDao,
            streamDao = streamDao,
            entryPointFactory = EntryPointFactory(backendCombinationHistoryRepository)
        )

    @Bean
    open fun combinationEventService(developerDao: DeveloperDao, streamDao: StreamDao,
                                     pairStreamRepository: PairStreamRepository,
                                     combinationRepository: CombinationRepository,
                                     combinationEventRepository: CombinationEventRepository
    ): CombinationEventService =
        CombinationEventService(
            developerDao = developerDao,
            streamDao = streamDao,
            pairStreamRepository = pairStreamRepository,
            combinationRepository = combinationRepository,
            combinationEventRepository = combinationEventRepository
        )

    @Bean
    open fun backendCombinationHistoryRepository(combinationEventRepository: CombinationEventRepository): BackendCombinationHistoryRepository =
        BackendCombinationHistoryRepository(
            combinationEventRepository
        )
}