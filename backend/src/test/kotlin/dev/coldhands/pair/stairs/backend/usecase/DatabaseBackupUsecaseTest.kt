package dev.coldhands.pair.stairs.backend.usecase

import dev.coldhands.pair.stairs.backend.FakeDateProvider
import dev.coldhands.pair.stairs.backend.domain.DatabaseBackupService
import dev.forkhandles.result4k.asSuccess
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class DatabaseBackupUsecaseTest(@TempDir private var tempDir: Path) {
    private val databaseBackupService = mockk<DatabaseBackupService>(relaxed = true)
    private val fakeDateProvider = FakeDateProvider()

    @Test
    fun `backup when no backups exist`() {
        val underTest = DatabaseBackupUsecase(tempDir, fakeDateProvider, databaseBackupService)
        fakeDateProvider.set("2026-01-01")
        every { databaseBackupService.backup(any()) }.returns(Unit.asSuccess())

        underTest.backup()

        databaseBackupService.backedUp(tempDir.resolve("pair-stairs-backup_2026-01-01_0.zip"))
    }

    /*
    todo
        - backup when backup already exists for day
        - backup when backup doesn't exist for day
        - backup when > max retained backups
        - error while backing up
        - create backup directory if it doesn't exist
            - multiple nested directories
        - concurrent requests should sequential
        - error from backup service
     */

    private fun DatabaseBackupService.backedUp(expected: Path) {
        val slot = slot<Path>()
        verify { backup(capture(slot)) }

        slot.captured.absolutePathString() shouldBe expected
            .absolutePathString()
    }
}