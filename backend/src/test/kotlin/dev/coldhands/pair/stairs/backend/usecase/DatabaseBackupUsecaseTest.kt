package dev.coldhands.pair.stairs.backend.usecase

import dev.coldhands.pair.stairs.backend.FakeDateProvider
import dev.coldhands.pair.stairs.backend.domain.FileOperations
import dev.coldhands.pair.stairs.backend.domain.backup.DatabaseBackupService
import dev.coldhands.pair.stairs.backend.domain.backup.DatabaseBackupService.Companion.BackupError
import dev.coldhands.pair.stairs.backend.domain.backup.DatabaseBackupService.Companion.HadException
import dev.coldhands.pair.stairs.backend.usecase.DatabaseBackupUsecase.UnableToBackupError
import dev.forkhandles.result4k.asFailure
import dev.forkhandles.result4k.asSuccess
import dev.forkhandles.result4k.kotest.shouldBeFailure
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class DatabaseBackupUsecaseTest(@TempDir private var tempDir: Path) {
    private val databaseBackupService = mockk<DatabaseBackupService>(relaxed = true)
    private val fileOperations = mockk<FileOperations>(relaxed = true)
    private val fakeDateProvider = FakeDateProvider()

    @Nested
    inner class SimpleBackup {
        @Test
        fun `backup when no backups exist`() {
            val underTest = DatabaseBackupUsecase(tempDir, fakeDateProvider, databaseBackupService, fileOperations)
            fakeDateProvider.set("2026-01-01")
            givenBackupIsSuccessful()
            givenFilesExist(listOf())

            underTest.backup().shouldBeSuccess()

            databaseBackupService.backedUp(tempDir.resolve("pair-stairs-backup_2026-01-01_0.zip"))
        }

        @Test
        fun `backup when backup for today already exists`() {
            val underTest = DatabaseBackupUsecase(tempDir, fakeDateProvider, databaseBackupService, fileOperations)
            fakeDateProvider.set("2026-01-01")

            givenBackupIsSuccessful()
            givenFilesExist(
                listOf(
                    "pair-stairs-backup_2026-01-01_0.zip",
                    "pair-stairs-backup_2026-01-01_12.zip"
                )
            )

            underTest.backup().shouldBeSuccess()

            databaseBackupService.backedUp(tempDir.resolve("pair-stairs-backup_2026-01-01_13.zip"))
        }

        @Test
        fun `backup when backup for another day already exists`() {
            val underTest = DatabaseBackupUsecase(tempDir, fakeDateProvider, databaseBackupService, fileOperations)
            fakeDateProvider.set("2026-01-01")

            givenBackupIsSuccessful()
            givenFilesExist(
                listOf(
                    "pair-stairs-backup_2025-12-31_0.zip",
                )
            )

            underTest.backup().shouldBeSuccess()

            databaseBackupService.backedUp(tempDir.resolve("pair-stairs-backup_2026-01-01_0.zip"))
        }

        @Test
        fun `ignore irrelevant or invalid file names in backup directory`() {
            val underTest = DatabaseBackupUsecase(tempDir, fakeDateProvider, databaseBackupService, fileOperations)
            fakeDateProvider.set("2026-01-01")

            givenBackupIsSuccessful()
            givenFilesExist(
                listOf(
                    "not a normal file name",

                    "pair-stairs-backupZZZ_2026-01-01_12.zip",
                    "pair-stairs-backup_abd-01-01_12.zip",
                    "pair-stairs-backup_2026-01-01_abc.zip",
                    "pair-stairs-backup_2026-01-01_12.txt",
                    "pair-stairs-backup_2026-01-01_-2.zip",
                )
            )

            underTest.backup().shouldBeSuccess()

            databaseBackupService.backedUp(tempDir.resolve("pair-stairs-backup_2026-01-01_0.zip"))
        }
    }

    @Nested
    inner class ErrorHandling {
        @Test
        fun `return backup failed when backup service returns failure`() {
            val underTest = DatabaseBackupUsecase(tempDir, fakeDateProvider, databaseBackupService, fileOperations)
            fakeDateProvider.set("2026-01-01")
            val serviceBackupError = HadException(RuntimeException())
            givenBackupFailsWith(serviceBackupError)
            givenFilesExist(listOf())

            underTest.backup().shouldBeFailure { backupError ->
                backupError.shouldBeInstanceOf<UnableToBackupError> {
                    it.cause shouldBe serviceBackupError
                }
            }
        }
    }

    /*
    todo
        - backup when > max retained backups, should delete oldest file
            - oldest date
            - oldest date + oldest counter
        - if max = 5 and 10 are found, delete oldest 5
        - create backup directory if it doesn't exist
            - multiple nested directories
        - concurrent requests should sequential
        - error from backup service
        - what to do about skipped numbers/days
        - do not delete if error happened while backing up
        - return delete errors
     */

    private fun DatabaseBackupService.backedUp(expected: Path) {
        val slot = slot<Path>()
        verify { backup(capture(slot)) }

        slot.captured.absolutePathString() shouldBe expected
            .absolutePathString()
    }

    private fun givenBackupIsSuccessful() {
        every { databaseBackupService.backup(any()) }.returns(Unit.asSuccess())
    }

    private fun givenBackupFailsWith(backupError: BackupError) {
        every { databaseBackupService.backup(any()) }.returns(
            backupError.asFailure()
        )
    }

    private fun givenFilesExist(listOf: List<String>) {
        every { fileOperations.listFiles(tempDir) }
            .returns(
                listOf
                    .map { tempDir.resolve(it) }
                    .toSet())
    }
}
