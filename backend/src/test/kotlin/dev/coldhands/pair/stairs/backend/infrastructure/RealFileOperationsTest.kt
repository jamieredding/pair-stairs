package dev.coldhands.pair.stairs.backend.infrastructure

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
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
}