package dev.coldhands.pair.stairs.backend.infrastructure

import dev.coldhands.pair.stairs.backend.domain.FileOperations
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.mapFailure
import dev.forkhandles.result4k.resultFrom
import java.nio.file.Path
import kotlin.io.path.createDirectories

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
}