package dev.coldhands.pair.stairs.backend.infrastructure.wiring

import dev.coldhands.pair.stairs.backend.domain.DateProvider
import dev.coldhands.pair.stairs.backend.domain.UserDao
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao.JpaUserDao
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.UserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.temporal.ChronoUnit

@Configuration
open class DaoConfig {

    @Bean
    open fun userDao(userRepository: UserRepository, dateProvider: DateProvider): UserDao =
        JpaUserDao(userRepository, dateProvider, ChronoUnit.MILLIS)
}