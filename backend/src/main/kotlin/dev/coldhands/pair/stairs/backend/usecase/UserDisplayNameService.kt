package dev.coldhands.pair.stairs.backend.usecase

import dev.coldhands.pair.stairs.backend.domain.UserName

class UserDisplayNameService {

    fun getDisplayNameFor(userName: UserName): String =
        userName.nickName
            ?: userName.givenName
            ?: userName.fullName?.let { it.split(" ", limit = 2)[0] }
            ?: "Unknown"
}