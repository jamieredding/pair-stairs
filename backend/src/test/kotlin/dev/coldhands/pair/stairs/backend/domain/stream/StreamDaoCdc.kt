package dev.coldhands.pair.stairs.backend.domain.stream

import dev.coldhands.pair.stairs.backend.aStreamId
import dev.coldhands.pair.stairs.backend.domain.StreamId
import dev.forkhandles.result4k.kotest.shouldBeFailure
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeUnique
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.concurrent.thread

@Suppress("unused")
abstract class StreamDaoCdc<T : StreamDao> {

    abstract val underTest: T

    @Nested
    inner class FindById {

        @Test
        fun `should return null when no stream exists with that id`() {
            val streamId = aStreamId()
            assertNoStreamExistsWithId(streamId)

            underTest.findById(streamId).shouldBeNull()
        }

        @Test
        fun `should find stream by id when stream exists`() {
            val streamDetails = someStreamDetails()
            val stream = createStream(streamDetails)

            underTest.findById(stream.id) shouldBe stream
        }
    }

    @Nested
    inner class FindAllById {

        @Test
        fun `should return empty list when no streams exist`() {
            ensureNoStreamsExist()

            underTest.findAllById(listOf(StreamId(1))).shouldBeEmpty()
        }

        @Test
        fun `should return empty list when no streams match passed ids`() {
            ensureNoStreamsExist()
            val stream = createStream(someStreamDetails())

            underTest.findAllById(listOf(StreamId(stream.id.value + 1))).shouldBeEmpty()
        }

        @Test
        fun `should return empty list when no ids are passed in`() {
            ensureNoStreamsExist()
            createStream(someStreamDetails())

            underTest.findAllById(listOf()).shouldBeEmpty()
        }

        @Test
        fun `should find all streams when multiple exist`() {
            ensureNoStreamsExist()

            val stream1 = createStream(someStreamDetails())
            createStream(someStreamDetails().copy(name = "another-name"))
            val stream3 = createStream(someStreamDetails().copy(name = "yet-another-name"))

            underTest.findAllById(listOf(stream1.id, stream3.id)).shouldContainAll(stream1, stream3)
        }
    }

    @Nested
    inner class FindAll {

        @Test
        fun `should return empty list when no streams exist`() {
            ensureNoStreamsExist()

            underTest.findAll().shouldBeEmpty()
        }

        @Test
        fun `should find all streams when multiple exist`() {
            ensureNoStreamsExist()
            val stream1 = createStream(someStreamDetails())
            val stream2 = createStream(someStreamDetails().copy(name = "another-name"))

            underTest.findAll().shouldContainAll(stream1, stream2)
        }
    }

    @Nested
    inner class Create {

        @Test
        fun `should create stream with specified details and generate id`() {
            val streamDetails = someStreamDetails()
            val stream = underTest.create(streamDetails).shouldBeSuccess()

            stream.id.shouldNotBeNull()
            stream.name shouldBe streamDetails.name
            stream.archived shouldBe streamDetails.archived

            assertStreamExists(stream)
        }

        @Test
        fun `should create streams with ascending ids`() {
            val stream1 = underTest.create(someStreamDetails()).shouldBeSuccess()
            val stream2 = underTest.create(someStreamDetails().copy(name = "another-name")).shouldBeSuccess()

            stream1.id shouldNotBe stream2.id
            stream2.id.value shouldBeGreaterThan stream1.id.value
        }

        @Test
        fun `allow max length fields`() {
            val name = "A".repeat(255)

            val stream = underTest.create(
                someStreamDetails().copy(
                    name = name,
                )
            ).shouldBeSuccess()

            stream.name shouldBe name
        }

        @Test
        fun `can create streams concurrently`() {
            val ids = ConcurrentLinkedDeque<StreamId>()

            (1..10).map { i ->
                thread(name = "many-threads-$i") {
                    repeat(10) {
                        underTest.create(someStreamDetails()).shouldBeSuccess {
                            ids.add(it.id)
                        }
                    }
                }
            }.map { it.join() }

            ids.size shouldBe 100
            ids.shouldBeUnique()
            ids.forEach { assertStreamExistsWithId(it) }
        }

        @Nested
        inner class SadPath {

            @Test
            fun `name must be 255 characters or less`() {
                val name = "A".repeat(256)

                underTest.create(someStreamDetails().copy(name = name))
                    .shouldBeFailure(StreamCreateError.NameTooLong(name))
            }
        }
    }

    @Nested
    inner class Update {

        @Test
        fun `can update archived on existing stream`() {
            val stream = givenStreamExistsWith(
                archived = false
            )

            val actualStream = underTest.update(
                streamId = stream.id,
                archived = true
            ).shouldBeSuccess()

            actualStream.id shouldBe stream.id
            actualStream.name shouldBe stream.name
            actualStream.archived shouldBe true

            assertStreamExists(actualStream)
        }

        @Nested
        inner class SadPath {

            @Test
            fun `return failure when stream does not exist`() {
                val madeUpStreamId = aStreamId()
                assertNoStreamExistsWithId(madeUpStreamId)

                underTest.update(streamId = madeUpStreamId, archived = true)
                    .shouldBeFailure(StreamUpdateError.StreamNotFound(madeUpStreamId))
            }
        }

        private fun givenStreamExistsWith(
            name: String = "some-name",
            archived: Boolean = false
        ): Stream {
            val streamDetails = someStreamDetails().copy(
                name = name,
                archived = archived
            )

            val stream = underTest.create(streamDetails).shouldBeSuccess()
            assertStreamExists(stream)
            return stream
        }
    }

    abstract fun assertNoStreamExistsWithId(streamId: StreamId)
    abstract fun assertStreamExistsWithId(streamId: StreamId)
    abstract fun ensureNoStreamsExist()
    abstract fun assertStreamExists(stream: Stream)
    abstract fun createStream(streamDetails: StreamDetails): Stream

    companion object TestFixtures {
        fun someStreamDetails() = StreamDetails(
            name = "some-name",
            archived = false,
        )
    }
}