package dev.coldhands.pair.stairs.logic.streams;

import dev.coldhands.pair.stairs.domain.streams.Pair;
import dev.coldhands.pair.stairs.domain.streams.PairCombination;
import dev.coldhands.pair.stairs.logic.PairCombinationService;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StreamsPairCombinationServiceTest {

    private PairCombinationService<PairCombination> underTest;

    private void given(Collection<String> developers, Collection<String> streams) {
        underTest = new StreamsPairCombinationService(developers, streams);
    }

    @Test
    void calculateAllPairCombinationsForOddNumberOfDevelopers() {
        List<String> developers = List.of("a-dev", "b-dev", "c-dev");
        List<String> streams = List.of("1-stream", "2-stream");

        given(developers, streams);

        Set<PairCombination> allPairCombinations = underTest.getAllPairCombinations();

        assertThat(allPairCombinations)
                .containsOnly(
                        new PairCombination(Set.of(new Pair(Set.of("a-dev", "b-dev"), "1-stream"), new Pair(Set.of("c-dev"), "2-stream"))),
                        new PairCombination(Set.of(new Pair(Set.of("a-dev", "b-dev"), "2-stream"), new Pair(Set.of("c-dev"), "1-stream"))),

                        new PairCombination(Set.of(new Pair(Set.of("a-dev", "c-dev"), "1-stream"), new Pair(Set.of("b-dev"), "2-stream"))),
                        new PairCombination(Set.of(new Pair(Set.of("a-dev", "c-dev"), "2-stream"), new Pair(Set.of("b-dev"), "1-stream"))),

                        new PairCombination(Set.of(new Pair(Set.of("b-dev", "c-dev"), "1-stream"), new Pair(Set.of("a-dev"), "2-stream"))),
                        new PairCombination(Set.of(new Pair(Set.of("b-dev", "c-dev"), "2-stream"), new Pair(Set.of("a-dev"), "1-stream")))
                );
    }

    @Test
    void calculateAllPairCombinationsForEvenNumberOfDevelopers() {
        List<String> developers = List.of("a-dev", "b-dev", "c-dev", "d-dev");
        List<String> streams = List.of("1-stream", "2-stream");

        given(developers, streams);

        Set<PairCombination> allPairCombinations = underTest.getAllPairCombinations();

        assertThat(allPairCombinations)
                .containsOnly(
                        new PairCombination(Set.of(new Pair(Set.of("a-dev", "b-dev"), "1-stream"), new Pair(Set.of("c-dev", "d-dev"), "2-stream"))),
                        new PairCombination(Set.of(new Pair(Set.of("a-dev", "b-dev"), "2-stream"), new Pair(Set.of("c-dev", "d-dev"), "1-stream"))),

                        new PairCombination(Set.of(new Pair(Set.of("a-dev", "c-dev"), "1-stream"), new Pair(Set.of("b-dev", "d-dev"), "2-stream"))),
                        new PairCombination(Set.of(new Pair(Set.of("a-dev", "c-dev"), "2-stream"), new Pair(Set.of("b-dev", "d-dev"), "1-stream"))),

                        new PairCombination(Set.of(new Pair(Set.of("b-dev", "c-dev"), "1-stream"), new Pair(Set.of("a-dev", "d-dev"), "2-stream"))),
                        new PairCombination(Set.of(new Pair(Set.of("b-dev", "c-dev"), "2-stream"), new Pair(Set.of("a-dev", "d-dev"), "1-stream")))
                );
    }

    @Test
    void calculateAllPairCombinationsWhereThereShouldBeThreePairsInEachPairCombination() {
        List<String> developers = List.of("a-dev", "b-dev", "c-dev", "d-dev", "e-dev", "f-dev");
        List<String> streams = List.of("1-stream", "2-stream", "3-stream");

        given(developers, streams);

        Set<PairCombination> allPairCombinations = underTest.getAllPairCombinations();

        assertThat(allPairCombinations)
                .hasSize(90) // 15 pair combinations X 3! ways of permuting with streams
                .allSatisfy(pairCombination -> {
                    assertThat(pairCombination.pairs()).hasSize(3);
                });
    }

    @Test
    void throwsExceptionWhenNotEnoughDevelopersForStreams() { // todo should this make single pairs instead and only fail if you can't even have single devs?
        List<String> developers = List.of("a-dev", "b-dev", "c-dev");
        List<String> streams = List.of("1-stream", "2-stream", "3-stream");

        given(developers, streams);

        assertThatThrownBy(()-> underTest.getAllPairCombinations())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Not enough developers to pair on streams");
    }

    @Test
    void throwsExceptionWhenNotEnoughStreamsForDevelopers() { // todo should this make up a placeholder stream?
        List<String> developers = List.of("a-dev", "b-dev", "c-dev");
        List<String> streams = List.of("1-stream");

        given(developers, streams);

        assertThatThrownBy(()-> underTest.getAllPairCombinations())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Not enough streams for developers");
    }
}