package dev.coldhands.pair.stairs.backend.usecase

import dev.coldhands.pair.stairs.backend.domain.DateProvider
import dev.coldhands.pair.stairs.backend.domain.FileOperations
import dev.coldhands.pair.stairs.backend.domain.backup.DatabaseBackupService
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.asFailure
import dev.forkhandles.result4k.mapFailure
import java.nio.file.Path
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class DatabaseBackupUsecase(
    private val backupBaseDir: Path,
    private val dateProvider: DateProvider,
    private val databaseBackupService: DatabaseBackupService,
    private val fileOperations: FileOperations
) {

    fun backup(): Result<Unit, BackupError> {
        fileOperations.createDirectory(backupBaseDir)
            .mapFailure { return CannotCreateBackupDirectory(it).asFailure() }

        val localDate = dateProvider.instant().atZone(ZoneOffset.UTC).toLocalDate()
        val localDateString = DateTimeFormatter.ISO_DATE.format(localDate)
        val filePattern = "pair-stairs-backup_${localDateString}_(\\d+).zip".toRegex()

        val backupCounter = fileOperations.listFiles(backupBaseDir)
            .map { it.fileName.toString() }
            .mapNotNull { fileName -> filePattern.find(fileName)?.groupValues?.get(1) }
            .maxOfOrNull { it.toInt() + 1 }
            ?: 0

        val backupPath = backupBaseDir.resolve("pair-stairs-backup_${localDateString}_${backupCounter}.zip")

        return databaseBackupService.backup(backupPath)
            .mapFailure { UnableToBackupError(it) }
    }

    sealed class BackupError
    class CannotCreateBackupDirectory(val cause: FileOperations.CreateDirectoryError) : BackupError()
    class UnableToBackupError(val cause: DatabaseBackupService.Companion.BackupError) : BackupError()
}