package dev.coldhands.pair.stairs.backend.infrastructure.persistance.backup

import dev.coldhands.pair.stairs.backend.domain.backup.DatabaseBackupService
import dev.forkhandles.result4k.kotest.shouldBeFailure
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.collections.shouldMatchInOrder
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.paths.shouldExist
import io.kotest.matchers.paths.shouldNotExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.BufferedInputStream
import java.nio.file.AccessDeniedException
import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager
import java.util.zip.ZipInputStream
import kotlin.io.path.*


class H2DatabaseBackupServiceTest(@TempDir private var tempDir: Path) {

    @Test
    fun backupAllDataToFile() {
        val h2DbFile = tempDir.resolve("originalFile")
        val outputFile = tempDir.resolve("h2-db-backup-all-data.zip")
        val unzipDestination = tempDir.resolve("unzip")
        val unzippedBackup = unzipDestination.resolve("originalFile")

        outputFile.shouldNotExist()

        withConnection(h2DbFile) {
            prepareStatement(
                """
                CREATE TABLE some_table
                (
                    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(255) NOT NULL
                );

            """.trimIndent()
            ).execute()

            prepareStatement("INSERT INTO some_table (name) VALUES ('some-name')").execute()

            val underTest = H2DatabaseBackupService(
                jdbcUrl = "jdbc:h2:$h2DbFile",
                username = "",
                password = ""
            )

            underTest.backup(outputFile).shouldBeSuccess()

            outputFile.shouldExist()

            outputFile.unzipTo(unzipDestination)

            withConnection(unzippedBackup) {
                prepareStatement("INSERT INTO some_table (name) VALUES ('another-name')").execute()

                data class Row(val id: Long, val name: String)

                prepareStatement("SELECT * FROM some_table").executeQuery().let { rs ->
                    buildList {
                        while (rs.next()) {
                            add(Row(rs.getLong("id"), rs.getString("name")))
                        }
                    }
                }.shouldMatchInOrder(
                    {
                        it.id shouldBe 1
                        it.name shouldBe "some-name"
                    },
                    {
                        it.id shouldBeGreaterThan 1
                        it.name shouldBe "another-name"
                    }
                )
            }
        }
    }

    @Test
    fun `invalid jdbc url`() {
        val underTest = H2DatabaseBackupService(jdbcUrl = "jdbc:h2::::", username = "unused", password = "unused")

        underTest.backup(tempDir.resolve("some-path"))
            .shouldBeFailure {
                it.shouldBeInstanceOf<DatabaseBackupService.Companion.HadException>()
            }
    }

    @Test
    fun `database is not h2`() {
        val underTest = H2DatabaseBackupService(
            jdbcUrl = "jdbc:mysql://localhost:3306/pair_stairs",
            username = "pair_stairs_user",
            password = "some-password"
        )

        underTest.backup(tempDir.resolve("some-path"))
            .shouldBeFailure { error ->
                error.shouldBeInstanceOf<DatabaseBackupService.Companion.UnsupportedDatabase>() {
                    it.providedDatabase shouldStartWith "jdbc:mysql"
                }
            }
    }

    @Test
    fun `backup file already exists`() {
        val backupFile = tempDir.resolve("some-path").also {
            it.writeText("some text")
        }

        val underTest = H2DatabaseBackupService(
            jdbcUrl = "jdbc:h2:${tempDir.resolve("originalFile")}",
            username = "",
            password = ""
        )

        underTest.backup(backupFile)
            .shouldBeFailure { error ->
                error.shouldBeInstanceOf<DatabaseBackupService.Companion.BackupAlreadyExists> {
                    it.backupFile.toString() shouldBe backupFile.toString()
                }
            }
    }

    @Test
    fun `unable to write backup file due to exception while backing up`() {
        val backupDir = tempDir.resolve("read-only-directory").also {
            it.createDirectory()
            it.toFile().setWritable(false)
        }

        val underTest = H2DatabaseBackupService(
            jdbcUrl = "jdbc:h2:${tempDir.resolve("originalFile")}",
            username = "",
            password = ""
        )

        underTest.backup(backupDir.resolve("backupFile"))
            .shouldBeFailure { error ->
                error.shouldBeInstanceOf<DatabaseBackupService.Companion.HadException>() {
                    it.cause.cause.shouldBeInstanceOf<AccessDeniedException>()
                }
            }
    }

    fun withConnection(h2DbFile: Path, block: Connection.() -> Unit) {
        DriverManager.getConnection("jdbc:h2:$h2DbFile").use { block(it) }
    }

    fun Path.unzipTo(destinationDir: Path) {
        require(Files.exists(this)) { "Zip file does not exist: $this" }

        destinationDir.createDirectories()
        val normalizedDestination = destinationDir.toAbsolutePath().normalize()

        ZipInputStream(BufferedInputStream(inputStream())).use { zip ->
            var entry = zip.nextEntry

            while (entry != null) {
                val target = normalizedDestination.resolve(entry.name).normalize()

                require(target.startsWith(normalizedDestination)) {
                    "Zip entry escapes destination directory: ${entry.name}"
                }

                if (entry.isDirectory) {
                    target.createDirectories()
                } else {
                    target.parent?.createDirectories()
                    target.outputStream().use { output ->
                        zip.copyTo(output)
                    }
                }

                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
    }
}