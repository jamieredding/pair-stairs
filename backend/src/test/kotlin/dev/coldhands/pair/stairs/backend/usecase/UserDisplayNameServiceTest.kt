package dev.coldhands.pair.stairs.backend.usecase

import dev.coldhands.pair.stairs.backend.domain.UserName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class UserDisplayNameServiceTest: FunSpec( {

    listOf(
        UserName(null, null, null) to "Unknown",
        UserName(null, null, "Full Name") to "Full",
        UserName(null, null, "Full") to "Full",
        UserName(null, "First", "Full Name") to "First",
        UserName("Nickname", "First", "Full Name") to "Nickname"
    ).forEach { (userName, displayName) ->
        test("Should return $displayName for user $userName") {
            UserDisplayNameService().getDisplayNameFor(userName) shouldBe displayName
        }
    }
})