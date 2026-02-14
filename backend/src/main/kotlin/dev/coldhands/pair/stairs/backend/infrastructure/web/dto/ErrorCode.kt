package dev.coldhands.pair.stairs.backend.infrastructure.web.dto

enum class ErrorCode {
    DEVELOPER_NOT_FOUND,
    STREAM_NOT_FOUND,
    TEAM_NOT_FOUND,
    BAD_REQUEST,
    NOT_ENOUGH_DEVELOPERS,
    NOT_ENOUGH_STREAMS,
    UNSUPPORTED_PROJECTION,
    INVALID_REQUEST_BODY,
    INVALID_NAME,
    NAME_TOO_LONG,
    INVALID_SLUG,
    SLUG_TOO_LONG,
    DUPLICATE_SLUG,
}