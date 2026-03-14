package dev.coldhands.pair.stairs.backend.infrastructure

import dev.forkhandles.result4k.kotest.shouldBeFailure
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.paths.shouldBeADirectory
import io.kotest.matchers.paths.shouldExist
import io.kotest.matchers.paths.shouldNotExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile

class RealFileOperationsTest(@TempDir private var tempDir: Path) {

    @Nested
    inner class ListFiles {

        @Test
        fun `list files in empty directory returns empty list`() {
            RealFileOperations.listFiles(tempDir).shouldBeEmpty()
        }

        @Test
        fun `list files in directory with files directory returns files`() {
            val first = tempDir.resolve("first.txt").createFile().absolutePathString()
            val second = tempDir.resolve("second.txt").createFile().absolutePathString()

            RealFileOperations.listFiles(tempDir)
                .map { it.absolutePathString() }.toSet() shouldBe setOf(
                first,
                second
            )
        }

        @Test
        fun `list files in directory with only directories returns empty list`() {
            tempDir.resolve("directory").createDirectory()

            RealFileOperations.listFiles(tempDir).shouldBeEmpty()
        }

        @Test
        fun `list files on file returns empty list`() {
            val file = tempDir.resolve("first.txt").createFile()

            RealFileOperations.listFiles(file).shouldBeEmpty()
        }
    }

    @Nested
    inner class CreateDirectory {
        @Test
        fun `successfully create a directory`() {
            val toBeCreated = tempDir.resolve("some-directory")

            toBeCreated.shouldNotExist()

            RealFileOperations.createDirectory(toBeCreated).shouldBeSuccess()

            toBeCreated.shouldExist()
            toBeCreated.shouldBeADirectory()
        }

        @Test
        fun `successfully create all missing directories`() {
            val toBeCreated = tempDir.resolve("some-directory/another")

            toBeCreated.shouldNotExist()
            toBeCreated.parent.shouldNotExist()

            RealFileOperations.createDirectory(toBeCreated).shouldBeSuccess()

            toBeCreated.shouldExist()
            toBeCreated.shouldBeADirectory()
        }

        @Test
        fun `return true if directory already exists`() {
            val toBeCreated = tempDir.resolve("some-directory")
            toBeCreated.createDirectory().shouldExist()

            RealFileOperations.createDirectory(toBeCreated).shouldBeSuccess()
        }

        @Test
        fun `return false if unable to create a directory`() {
            val toBeCreated = tempDir.resolve("some-directory")
            toBeCreated.createFile().shouldExist()

            RealFileOperations.createDirectory(toBeCreated).shouldBeFailure {
                it.cause.shouldBeInstanceOf<FileAlreadyExistsException>()
            }
        }
    }
}