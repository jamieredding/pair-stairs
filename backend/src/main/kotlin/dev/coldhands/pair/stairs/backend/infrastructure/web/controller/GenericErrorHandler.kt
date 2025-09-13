package dev.coldhands.pair.stairs.backend.infrastructure.web.controller

import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.ErrorDto
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.badRequest
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GenericErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleError(e: MethodArgumentNotValidException): ResponseEntity<ErrorDto> {
        val errorCode: String = e.bindingResult.allErrors.firstOrNull()?.defaultMessage
            ?: "UNKNOWN_VALIDATION_ERROR"
        return badRequest().body(ErrorDto(errorCode))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleError(): ResponseEntity<ErrorDto> {
        return badRequest().body(ErrorDto("INVALID_REQUEST_BODY"))
    }
}