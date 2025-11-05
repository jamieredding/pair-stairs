package dev.coldhands.pair.stairs.backend.infrastructure.wiring

import dev.coldhands.pair.stairs.backend.domain.DateProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Instant

@Configuration
open class DateConfig {

    @Bean
    open fun dateProvider(): DateProvider = object : DateProvider {
        override fun instant(): Instant = Instant.now()
    }
}