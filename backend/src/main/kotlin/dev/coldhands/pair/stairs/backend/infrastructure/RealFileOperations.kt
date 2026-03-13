package dev.coldhands.pair.stairs.backend.infrastructure

import dev.coldhands.pair.stairs.backend.domain.FileOperations
import java.nio.file.Path

object RealFileOperations : FileOperations {
    override fun listFiles(directory: Path): Set<Path> {
        val files = directory.toFile().listFiles() ?: return emptySet()

        return files
            .filter { it.isFile }
            .map { it.toPath() }
            .toSet()
    }
}