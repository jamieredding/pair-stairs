package dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao

import dev.coldhands.pair.stairs.backend.domain.StreamId
import dev.coldhands.pair.stairs.backend.domain.stream.Stream
import dev.coldhands.pair.stairs.backend.domain.stream.StreamDaoCdc
import dev.coldhands.pair.stairs.backend.domain.stream.StreamDetails
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class FakeStreamDaoTest : StreamDaoCdc<FakeStreamDao>() {
    override val underTest: FakeStreamDao = FakeStreamDao()

    override fun assertNoStreamExistsWithId(streamId: StreamId) {
        underTest.streamsView[streamId].shouldBeNull()
    }

    override fun assertStreamExistsWithId(streamId: StreamId) {
        underTest.streamsView[streamId].shouldNotBeNull()
    }

    override fun ensureNoStreamsExist() {
        underTest.streamsView.shouldBeEmpty()
    }

    override fun assertStreamExists(stream: Stream) {
        underTest.streamsView[stream.id].shouldNotBeNull {
            id shouldBe stream.id
            name shouldBe stream.name
            archived shouldBe stream.archived
        }
    }

    override fun createStream(streamDetails: StreamDetails): Stream =
        underTest.create(streamDetails).shouldBeSuccess()

}