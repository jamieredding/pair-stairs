package dev.coldhands.pair.stairs.backend.infrastructure.mapper

import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.TeamEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.TeamMembershipEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.UserEntity
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.TeamMembershipDto
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.time.Instant

class TeamMembershipMappersTest : FunSpec({

    test("Map TeamMembershipEntity to TeamMembershipDto") {
        TeamMembershipEntity(
            id = 1L,
            displayName = "some-display-name",
            user = UserEntity(
                id = 2L,
                oidcSub = "unused",
                displayName = "unused",
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            ),
            team = TeamEntity(
                name = "unused",
                slug = "unused"
            )
        ).toDto() shouldBe TeamMembershipDto(id = 1L, userId = 2L, displayName = "some-display-name")
    }

    test("throw exception when id is null") {
        shouldThrow<IllegalArgumentException> {
            TeamMembershipEntity(
                id = null,
                displayName = "some-display-name",
                user = UserEntity(
                    id = 2L,
                    oidcSub = "unused",
                    displayName = "unused",
                    createdAt = Instant.now(),
                    updatedAt = Instant.now()
                ),
                team = TeamEntity(
                    name = "unused",
                    slug = "unused"
                )
            ).toDto()
        }.message shouldBe "Cannot create TeamMembershipDto when id is null. Likely, the TeamMembershipEntity hasn't been persisted yet."
    }

    test("throw exception when userId is null") {
        shouldThrow<IllegalArgumentException> {
            TeamMembershipEntity(
                id = 1L,
                displayName = "some-display-name",
                user = UserEntity(
                    id = null,
                    oidcSub = "unused",
                    displayName = "unused",
                    createdAt = Instant.now(),
                    updatedAt = Instant.now()
                ),
                team = TeamEntity(
                    name = "unused",
                    slug = "unused"
                )
            ).toDto()
        }.message shouldBe "Cannot create TeamMembershipDto when user has null id. Likely, the UserEntity hasn't been persisted yet."
    }

})
