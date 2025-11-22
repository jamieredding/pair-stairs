package dev.coldhands.pair.stairs.backend.infrastructure.wiring

import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperDao
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.CombinationEventRepository
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.StreamRepository
import dev.coldhands.pair.stairs.backend.usecase.StatsService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class StatsConfig {

    @Bean
    open fun statsService(
        developerDao: DeveloperDao,
        streamRepository: StreamRepository,
        combinationEventRepository: CombinationEventRepository
    ): StatsService =
        StatsService(developerDao, streamRepository, combinationEventRepository)
}