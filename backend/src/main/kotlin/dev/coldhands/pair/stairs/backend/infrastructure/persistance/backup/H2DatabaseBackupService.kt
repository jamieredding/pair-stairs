package dev.coldhands.pair.stairs.backend.infrastructure.persistance.backup

import dev.coldhands.pair.stairs.backend.domain.DatabaseBackupService
import dev.coldhands.pair.stairs.backend.domain.DatabaseBackupService.Companion.BackupError
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.asSuccess
import java.nio.file.Path
import java.sql.DriverManager

class H2DatabaseBackupService(private val jdbcUrl: String) : DatabaseBackupService {
    override fun backup(path: Path): Result<Unit, BackupError> {
        DriverManager.getConnection(jdbcUrl).use {
            it.prepareStatement("BACKUP TO '$path'").execute()
        }
        return Unit.asSuccess()
    }
}