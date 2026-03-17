package dev.coldhands.pair.stairs.backend.domain

import dev.forkhandles.result4k.Result
import java.nio.file.Path

interface FileOperations {
    fun listFiles(directory: Path): Set<Path>
    fun createDirectory(directory: Path): Result<Unit, CreateDirectoryError>
    fun deleteFile(path: Path): Result<Unit, DeleteFileError>

    class CreateDirectoryError(val cause: Throwable)
    sealed interface DeleteFileError {
        class NotAFileError(val path: Path): DeleteFileError
        class HadException(val cause: Throwable): DeleteFileError
    }
}