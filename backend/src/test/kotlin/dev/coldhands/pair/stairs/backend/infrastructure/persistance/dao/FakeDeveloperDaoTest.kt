package dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao

import dev.coldhands.pair.stairs.backend.domain.DeveloperId
import dev.coldhands.pair.stairs.backend.domain.developer.Developer
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperDaoCdc
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperDetails
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class FakeDeveloperDaoTest : DeveloperDaoCdc<FakeDeveloperDao>() {
    override val underTest: FakeDeveloperDao = FakeDeveloperDao()

    override fun assertNoDeveloperExistsWithId(developerId: DeveloperId) {
        underTest.developersView[developerId].shouldBeNull()
    }

    override fun assertDeveloperExistsWithId(developerId: DeveloperId) {
        underTest.developersView[developerId].shouldNotBeNull()
    }

    override fun ensureNoDevelopersExist() {
        underTest.developersView.shouldBeEmpty()
    }

    override fun assertDeveloperExists(developer: Developer) {
        underTest.developersView[developer.id].shouldNotBeNull {
            id shouldBe developer.id
            name shouldBe developer.name
            archived shouldBe developer.archived
        }
    }

    override fun createDeveloper(developerDetails: DeveloperDetails): Developer =
        underTest.create(developerDetails).shouldBeSuccess()

}