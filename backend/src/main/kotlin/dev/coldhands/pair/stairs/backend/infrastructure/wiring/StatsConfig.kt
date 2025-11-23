package dev.coldhands.pair.stairs.backend.infrastructure.wiring

import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperDao
import dev.coldhands.pair.stairs.backend.domain.stream.StreamDao
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.CombinationEventRepository
import dev.coldhands.pair.stairs.backend.usecase.StatsService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class StatsConfig {

    @Bean
    open fun statsService(
        developerDao: DeveloperDao,
        streamDao: StreamDao,
        combinationEventRepository: CombinationEventRepository
    ): StatsService =
        StatsService(developerDao, streamDao, combinationEventRepository)
}