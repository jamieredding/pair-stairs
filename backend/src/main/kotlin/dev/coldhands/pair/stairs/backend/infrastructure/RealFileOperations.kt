package dev.coldhands.pair.stairs.backend.infrastructure

import dev.coldhands.pair.stairs.backend.domain.FileOperations
import dev.forkhandles.result4k.*
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.isDirectory

object RealFileOperations : FileOperations {
    override fun listFiles(directory: Path): Set<Path> {
        val files = directory.toFile().listFiles() ?: return emptySet()

        return files
            .filter { it.isFile }
            .map { it.toPath() }
            .toSet()
    }

    override fun createDirectory(directory: Path): Result<Unit, FileOperations.CreateDirectoryError> = resultFrom {
        directory.createDirectories()
    }.map { Unit }
        .mapFailure {
            FileOperations.CreateDirectoryError(it)
        }

    override fun deleteFile(path: Path): Result<Unit, FileOperations.DeleteFileError> {
        if (path.isDirectory()) return FileOperations.DeleteFileError.NotAFileError(path).asFailure()

        return resultFrom { path.deleteIfExists() }
            .map { Unit }
            .mapFailure { FileOperations.DeleteFileError.HadException(it) }
    }
}