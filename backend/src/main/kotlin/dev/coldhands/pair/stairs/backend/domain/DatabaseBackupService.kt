package dev.coldhands.pair.stairs.backend.domain

import dev.forkhandles.result4k.Result
import java.nio.file.Path

interface DatabaseBackupService {

    fun backup(path: Path): Result<Unit, BackupError>

    companion object {
        sealed interface BackupError
    }
}