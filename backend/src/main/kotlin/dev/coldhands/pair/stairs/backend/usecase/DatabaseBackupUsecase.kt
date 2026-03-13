package dev.coldhands.pair.stairs.backend.usecase

import dev.coldhands.pair.stairs.backend.domain.DatabaseBackupService
import dev.coldhands.pair.stairs.backend.domain.DateProvider
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.mapFailure
import java.nio.file.Path
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class DatabaseBackupUsecase(
    private val backupBaseDir: Path,
    private val dateProvider: DateProvider,
    private val databaseBackupService: DatabaseBackupService
) {
    fun backup(): Result<Unit, BackupError> {
        val localDate = dateProvider.instant().atZone(ZoneOffset.UTC).toLocalDate()
        val localDateString = DateTimeFormatter.ISO_DATE.format(localDate)
        val backupFileName = "pair-stairs-backup_${localDateString}_0.zip"
        val backupPath = backupBaseDir.resolve(backupFileName)

        return databaseBackupService.backup(backupPath)
            .mapFailure { TODO() }
    }

    sealed class BackupError
}