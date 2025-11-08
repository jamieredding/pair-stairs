package dev.coldhands.pair.stairs.backend.infrastructure.wiring

import dev.coldhands.pair.stairs.backend.domain.DateProvider
import dev.coldhands.pair.stairs.backend.domain.RealDateProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class DateConfig {

    @Bean
    open fun dateProvider(): DateProvider = RealDateProvider()
}