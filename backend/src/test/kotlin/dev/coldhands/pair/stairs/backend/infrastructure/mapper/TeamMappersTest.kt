package dev.coldhands.pair.stairs.backend.infrastructure.mapper

import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.TeamEntity
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.TeamDto
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class TeamMappersTest : FunSpec({

    test("Map TeamEntity to TeamDto") {
        TeamEntity(
            id = 1L,
            name = "name",
            slug = "slug"
        ).toDto() shouldBe TeamDto(id = 1L, name = "name", slug = "slug")
    }
    test("throw exception when id is null") {
        shouldThrow<IllegalArgumentException> {
            TeamEntity(
                id = null,
                name = "name",
                slug = "slug"
            ).toDto()
        }.message shouldBe "Cannot create TeamDto when id is null. Likely, the TeamEntity hasn't been persisted yet."
    }
})
