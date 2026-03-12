package dev.coldhands.pair.stairs.backend.infrastructure.persistance.backup

import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.collections.shouldMatchInOrder
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.paths.shouldExist
import io.kotest.matchers.paths.shouldNotExist
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.BufferedInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager
import java.util.zip.ZipInputStream
import kotlin.io.path.createDirectories
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream


class H2DatabaseBackupServiceTest {

    @Test
    fun backupAllDataToFile(@TempDir tempDir: Path) {
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

            val underTest = H2DatabaseBackupService(jdbcUrl = "jdbc:h2:$h2DbFile")

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

    /*
    todo
        - jdbc url invalid
        - jdbc url is mysql (it is running but doesn't support backup)
        - exception when executing statement
        - do I need to check return value on execute?
        - what happens when I backup to an existing file
        - what if the existing file is read only?
     */

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