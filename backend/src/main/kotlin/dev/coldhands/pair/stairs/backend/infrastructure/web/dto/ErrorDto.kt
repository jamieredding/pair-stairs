package dev.coldhands.pair.stairs.backend.infrastructure.web.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL

data class ErrorDto(
    val errorCode: ErrorCode,
    @JsonInclude(NON_NULL) val errorMessage: String? = null)