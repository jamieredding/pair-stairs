package dev.coldhands.pair.stairs.backend.domain

import java.nio.file.Path

interface FileOperations {
    fun listFiles(directory: Path): Set<Path>
}