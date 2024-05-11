package dev.coldhands.pair.stairs.core.domain;

import dev.coldhands.pair.stairs.core.domain.pairstream.PairStream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CombinationTest {

    @Nested
    class HashCode {

        @Test
        void hashCodeForSameRecordIsEqual() {
            final Combination<PairStream> c1 = new Combination<>(Set.of(
                    new PairStream(Set.of("dev-0", "dev-1"), "stream-a"),
                    new PairStream(Set.of("dev-2"), "stream-b")
            ));
            final Combination<PairStream> c2 = new Combination<>(Set.of(
                    new PairStream(Set.of("dev-0", "dev-1"), "stream-a"),
                    new PairStream(Set.of("dev-2"), "stream-b")
            ));

            assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
        }

        @Test
        void hashCodeForSameDevelopersButGroupedDifferentlyIsDifferent() {
            final Combination<PairStream> c1 = new Combination<>(Set.of(
                    new PairStream(Set.of("dev-0", "dev-1"), "stream-a"),
                    new PairStream(Set.of("dev-2"), "stream-b")
            ));
            final Combination<PairStream> c2 = new Combination<>(Set.of(
                    new PairStream(Set.of("dev-0", "dev-2"), "stream-a"),
                    new PairStream(Set.of("dev-1"), "stream-b")
            ));

            assertThat(c1.hashCode()).isNotEqualTo(c2.hashCode());
        }

        @Test
        void hashCodeForSameStreamsButGroupedDifferentlyIsDifferent() {
            final Combination<PairStream> c1 = new Combination<>(Set.of(
                    new PairStream(Set.of("dev-0", "dev-1"), "stream-a"),
                    new PairStream(Set.of("dev-2"), "stream-b")
            ));
            final Combination<PairStream> c2 = new Combination<>(Set.of(
                    new PairStream(Set.of("dev-0", "dev-1"), "stream-b"),
                    new PairStream(Set.of("dev-2"), "stream-a")
            ));

            assertThat(c1.hashCode()).isNotEqualTo(c2.hashCode());
        }
    }

    @Nested
    class Equals {

        @Test
        void equalsForSameRecordIsEqual() {
            final Combination<PairStream> c1 = new Combination<>(Set.of(
                    new PairStream(Set.of("dev-0", "dev-1"), "stream-a"),
                    new PairStream(Set.of("dev-2"), "stream-b")
            ));
            final Combination<PairStream> c2 = new Combination<>(Set.of(
                    new PairStream(Set.of("dev-0", "dev-1"), "stream-a"),
                    new PairStream(Set.of("dev-2"), "stream-b")
            ));

            assertThat(c1).isEqualTo(c2);
        }

        @Test
        void equalsForSameDevelopersButGroupedDifferentlyIsDifferent() {
            final Combination<PairStream> c1 = new Combination<>(Set.of(
                    new PairStream(Set.of("dev-0", "dev-1"), "stream-a"),
                    new PairStream(Set.of("dev-2"), "stream-b")
            ));
            final Combination<PairStream> c2 = new Combination<>(Set.of(
                    new PairStream(Set.of("dev-0", "dev-2"), "stream-a"),
                    new PairStream(Set.of("dev-1"), "stream-b")
            ));

            assertThat(c1).isNotEqualTo(c2);
        }

        @Test
        void equalsForSameStreamsButGroupedDifferentlyIsDifferent() {
            final Combination<PairStream> c1 = new Combination<>(Set.of(
                    new PairStream(Set.of("dev-0", "dev-1"), "stream-a"),
                    new PairStream(Set.of("dev-2"), "stream-b")
            ));
            final Combination<PairStream> c2 = new Combination<>(Set.of(
                    new PairStream(Set.of("dev-0", "dev-1"), "stream-b"),
                    new PairStream(Set.of("dev-2"), "stream-a")
            ));

            assertThat(c1).isNotEqualTo(c2);
        }
    }

}