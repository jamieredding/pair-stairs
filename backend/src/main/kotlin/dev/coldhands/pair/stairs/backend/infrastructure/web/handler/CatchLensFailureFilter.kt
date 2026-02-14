package dev.coldhands.pair.stairs.backend.infrastructure.web.handler

import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.ErrorCode
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.ErrorDto
import org.http4k.core.Body
import org.http4k.core.Filter
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.filter.ServerFilters
import org.http4k.format.Jackson.auto
import org.http4k.lens.Invalid
import org.http4k.lens.LensFailure

object CatchLensFailureFilter {
    private val errorBodyLens = Body.auto<ErrorDto>().toLens()

    operator fun invoke(): Filter = ServerFilters.CatchLensFailure {lensFailure ->
        val errorCode = lensFailure.errorCode() ?: ErrorCode.INVALID_REQUEST_BODY
        errorBodyLens(ErrorDto(errorCode), Response(BAD_REQUEST))
    }

    private fun LensFailure.errorCode(): ErrorCode? = failures.asSequence()
        .mapNotNull { it as? Invalid }
        .mapNotNull { it.meta.metadata[ErrorCode::class.simpleName!!] }
        .firstNotNullOfOrNull { it as? ErrorCode }
}