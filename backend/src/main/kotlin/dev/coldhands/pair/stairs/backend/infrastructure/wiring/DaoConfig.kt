package dev.coldhands.pair.stairs.backend.infrastructure.wiring

import dev.coldhands.pair.stairs.backend.domain.DateProvider
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperDao
import dev.coldhands.pair.stairs.backend.domain.stream.StreamDao
import dev.coldhands.pair.stairs.backend.domain.team.TeamDao
import dev.coldhands.pair.stairs.backend.domain.team.membership.TeamMembershipDao
import dev.coldhands.pair.stairs.backend.domain.user.UserDao
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao.*
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.*
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
        teamMembershipRepository: TeamMembershipRepository,
        teamDao: TeamDao,
        userDao: UserDao,
        dateProvider: DateProvider,
    ): TeamMembershipDao =
        JpaTeamMembershipDao(teamMembershipRepository, teamDao, userDao, dateProvider, ChronoUnit.MILLIS)

    @Bean
    open fun developerDao(developerRepository: DeveloperRepository): DeveloperDao =
        JpaDeveloperDao(developerRepository = developerRepository)

    @Bean
    open fun streamDao(streamRepository: StreamRepository): StreamDao =
        JpaStreamDao(streamRepository = streamRepository)
}