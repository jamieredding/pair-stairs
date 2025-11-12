package dev.coldhands.pair.stairs.backend.infrastructure.wiring

import dev.coldhands.pair.stairs.backend.domain.DateProvider
import dev.coldhands.pair.stairs.backend.domain.team.TeamDao
import dev.coldhands.pair.stairs.backend.domain.team.membership.TeamMembershipDao
import dev.coldhands.pair.stairs.backend.domain.user.UserDao
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao.JpaTeamDao
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao.JpaTeamMembershipDao
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao.JpaUserDao
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.TeamMembershipRepository
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.TeamRepository
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.UserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.temporal.ChronoUnit

@Configuration
open class DaoConfig {

    @Bean
    open fun userDao(userRepository: UserRepository, dateProvider: DateProvider): UserDao =
        JpaUserDao(userRepository, dateProvider, ChronoUnit.MILLIS)

    @Bean
    open fun teamDao(teamRepository: TeamRepository, dateProvider: DateProvider): TeamDao =
        JpaTeamDao(teamRepository, dateProvider, ChronoUnit.MILLIS)

    @Bean
    open fun teamMembershipDao(
        teamMembershipRepository: TeamMembershipRepository, teamRepository: TeamRepository,
        userRepository: UserRepository, dateProvider: DateProvider
    ): TeamMembershipDao =
        JpaTeamMembershipDao(teamMembershipRepository, teamRepository, userRepository, dateProvider, ChronoUnit.MILLIS)
}