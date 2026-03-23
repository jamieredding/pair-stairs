package dev.coldhands.pair.stairs.backend.usecase

import dev.coldhands.pair.stairs.backend.FakeDateProvider
import dev.coldhands.pair.stairs.backend.domain.FileOperations
import dev.coldhands.pair.stairs.backend.domain.backup.DatabaseBackupService
import dev.coldhands.pair.stairs.backend.domain.backup.DatabaseBackupService.Companion.BackupError
import dev.coldhands.pair.stairs.backend.domain.backup.DatabaseBackupService.Companion.HadException
import dev.coldhands.pair.stairs.backend.usecase.DatabaseBackupUsecase.CannotCreateBackupDirectory
import dev.coldhands.pair.stairs.backend.usecase.DatabaseBackupUsecase.UnableToBackupError
import dev.forkhandles.result4k.asFailure
import dev.forkhandles.result4k.asSuccess
import dev.forkhandles.result4k.kotest.shouldBeFailure
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class DatabaseBackupUsecaseTest(@TempDir private var tempDir: Path) {
    private val databaseBackupService = mockk<DatabaseBackupService>(relaxed = true)
    private val fileOperations = mockk<FileOperations>(relaxed = true) {
        every { createDirectory(any()) }.returns(Unit.asSuccess())
    }
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

        @Test
        fun `create backup base dir`() {
            val backupBaseDir = tempDir.resolve("some-other-directory")

            val underTest =
                DatabaseBackupUsecase(backupBaseDir, fakeDateProvider, databaseBackupService, fileOperations)
            fakeDateProvider.set("2026-01-01")
            givenBackupIsSuccessful()
            every { fileOperations.listFiles(backupBaseDir) } returns setOf()

            underTest.backup().shouldBeSuccess()

            databaseBackupService.backedUp(backupBaseDir.resolve("pair-stairs-backup_2026-01-01_0.zip"))
            verify { fileOperations.createDirectory(backupBaseDir) }
        }
    }

    @Nested
    inner class RetainBackups {

        @ParameterizedTest
        @ValueSource(ints = [0, 1])
        fun `do not delete if number of backups less than specified`(backupsToRetain: Int) {
            val underTest = DatabaseBackupUsecase(tempDir, fakeDateProvider, databaseBackupService, fileOperations)
            givenFilesExist((0..<backupsToRetain).map { "file-$it" })

            underTest.retainMostRecentBackups(backupsToRetain).shouldBeSuccess()

            verify(exactly = 0) { fileOperations.deleteFile(any()) }
        }

        @Test
        fun `delete oldest backup when oldest backup is same day`() {
            val underTest = DatabaseBackupUsecase(tempDir, fakeDateProvider, databaseBackupService, fileOperations)
            givenFilesExist(
                listOf(
                    "pair-stairs-backup_2026-01-01_0.zip",
                    "pair-stairs-backup_2026-01-01_1.zip",
                )
            )

            underTest.retainMostRecentBackups(1).shouldBeSuccess()

            verify { fileOperations.deleteFile(tempDir.resolve("pair-stairs-backup_2026-01-01_0.zip")) }
            verify(exactly = 0) { fileOperations.deleteFile(tempDir.resolve("pair-stairs-backup_2026-01-01_1.zip")) }
        }

        @Test
        fun `delete oldest backup when oldest backup is different day`() {
            val underTest = DatabaseBackupUsecase(tempDir, fakeDateProvider, databaseBackupService, fileOperations)
            givenFilesExist(
                listOf(
                    "pair-stairs-backup_2025-12-31_0.zip",
                    "pair-stairs-backup_2026-01-01_0.zip",
                )
            )

            underTest.retainMostRecentBackups(1).shouldBeSuccess()

            verify { fileOperations.deleteFile(tempDir.resolve("pair-stairs-backup_2025-12-31_0.zip")) }
            verify(exactly = 0) { fileOperations.deleteFile(tempDir.resolve("pair-stairs-backup_2026-01-01_0.zip")) }
        }

        @Test
        fun `delete all old backups until only remaining count`() {
            val underTest = DatabaseBackupUsecase(tempDir, fakeDateProvider, databaseBackupService, fileOperations)
            givenFilesExist(
                listOf(
                    "pair-stairs-backup_2026-01-01_0.zip",
                    "pair-stairs-backup_2026-01-02_0.zip",
                    "pair-stairs-backup_2026-01-02_1.zip",
                    "pair-stairs-backup_2026-01-03_0.zip",
                    "pair-stairs-backup_2026-01-04_0.zip",
                    "pair-stairs-backup_2026-01-05_0.zip",
                )
            )

            underTest.retainMostRecentBackups(2).shouldBeSuccess()

            listOf(
                "pair-stairs-backup_2026-01-01_0.zip",
                "pair-stairs-backup_2026-01-02_0.zip",
                "pair-stairs-backup_2026-01-02_1.zip",
                "pair-stairs-backup_2026-01-03_0.zip",
            ).forEach {
                verify { fileOperations.deleteFile(tempDir.resolve(it)) }
            }
            listOf(
                "pair-stairs-backup_2026-01-04_0.zip",
                "pair-stairs-backup_2026-01-05_0.zip",
            ).forEach {
                verify(exactly = 0) { fileOperations.deleteFile(tempDir.resolve(it)) }
            }
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

        @Test
        fun `return backup failed when create directory returns failure`() {
            val backupBaseDir = tempDir.resolve("some-other-directory")

            val createDirectoryError = FileOperations.CreateDirectoryError(RuntimeException())
            val fileOperations = mockk<FileOperations> {
                every { createDirectory(any()) }.returns(createDirectoryError.asFailure())
            }
            val underTest =
                DatabaseBackupUsecase(backupBaseDir, fakeDateProvider, databaseBackupService, fileOperations)
            fakeDateProvider.set("2026-01-01")

            underTest.backup().shouldBeFailure { backupError ->
                backupError.shouldBeInstanceOf<CannotCreateBackupDirectory> {
                    it.cause shouldBe createDirectoryError
                }
            }

            verify { databaseBackupService wasNot Called }
        }
    }

    /*
    todo
        - concurrent requests should sequential
        - do not delete if error happened while backing up
        - return delete errors
        - ignore files that don't match regex
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
