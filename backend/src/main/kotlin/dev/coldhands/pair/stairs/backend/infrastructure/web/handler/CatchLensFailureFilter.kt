package dev.coldhands.pair.stairs.backend.infrastructure.web.handler

import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.ErrorCode
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.ErrorDto
import org.http4k.core.Body
import org.http4k.core.Filter
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.filter.ServerFilters
import org.http4k.format.Jackson.auto

object CatchLensFailureFilter {
    private val errorBodyLens = Body.auto<ErrorDto>().toLens()

    operator fun invoke(): Filter = ServerFilters.CatchLensFailure {
        errorBodyLens(ErrorDto(ErrorCode.INVALID_REQUEST_BODY), Response(BAD_REQUEST))
    }
}