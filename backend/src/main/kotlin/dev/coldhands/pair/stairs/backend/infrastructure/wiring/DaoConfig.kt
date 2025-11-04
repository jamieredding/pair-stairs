package dev.coldhands.pair.stairs.backend.infrastructure.wiring

import dev.coldhands.pair.stairs.backend.domain.UserDao
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao.JpaUserDao
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.UserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class DaoConfig {

    @Bean
    open fun userDao(userRepository: UserRepository): UserDao = JpaUserDao(userRepository)
}