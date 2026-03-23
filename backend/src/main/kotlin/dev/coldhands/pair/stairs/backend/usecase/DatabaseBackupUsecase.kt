package dev.coldhands.pair.stairs.backend.usecase

import dev.coldhands.pair.stairs.backend.domain.DateProvider
import dev.coldhands.pair.stairs.backend.domain.FileOperations
import dev.coldhands.pair.stairs.backend.domain.backup.DatabaseBackupService
import dev.forkhandles.result4k.*
import java.nio.file.Path
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.io.path.name

class DatabaseBackupUsecase(
    private val backupBaseDir: Path,
    private val dateProvider: DateProvider,
    private val databaseBackupService: DatabaseBackupService,
    private val fileOperations: FileOperations,
) {
    private val deleteRegex = Regex("pair-stairs-backup_([\\d-]+)_\\d+.zip")

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

    fun retainMostRecentBackups(count: Int): Result<Unit, Unit> {
        fileOperations.listFiles(backupBaseDir)
            .filter { it.name.isBackupFilename() }
            .sortedByDescending { it.name }
            .drop(count)
            .forEach { fileOperations.deleteFile(it) }

        return Unit.asSuccess()
    }

    private fun String.isBackupFilename(): Boolean =
        deleteRegex.find(this)
            ?.groupValues
            ?.getOrNull(1)
            ?.let {
                resultFrom { DateTimeFormatter.ISO_DATE.parse(it) }
                    .map { true }
                    .mapFailure { false }
                    .get()
            } ?: false

    sealed class BackupError
    class CannotCreateBackupDirectory(val cause: FileOperations.CreateDirectoryError) : BackupError()
    class UnableToBackupError(val cause: DatabaseBackupService.Companion.BackupError) : BackupError()
}