package dev.coldhands.pair.stairs.core;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StreamsCombinationServiceTest {

    private CombinationService<PairStreamCombination> underTest;

    private void given(Collection<String> developers, Collection<String> streams) {
        underTest = new StreamsCombinationService(developers, streams);
    }

    @Test
    void calculateAllCombinationsForOddNumberOfDevelopers() {
        List<String> developers = List.of("a-dev", "b-dev", "c-dev");
        List<String> streams = List.of("1-stream", "2-stream");

        given(developers, streams);

        Set<PairStreamCombination> allCombinations = underTest.getAllCombinations();

        assertThat(allCombinations)
                .containsOnly(
                        new PairStreamCombination(Set.of(new Pair(Set.of("a-dev", "b-dev"), "1-stream"), new Pair(Set.of("c-dev"), "2-stream"))),
                        new PairStreamCombination(Set.of(new Pair(Set.of("a-dev", "b-dev"), "2-stream"), new Pair(Set.of("c-dev"), "1-stream"))),

                        new PairStreamCombination(Set.of(new Pair(Set.of("a-dev", "c-dev"), "1-stream"), new Pair(Set.of("b-dev"), "2-stream"))),
                        new PairStreamCombination(Set.of(new Pair(Set.of("a-dev", "c-dev"), "2-stream"), new Pair(Set.of("b-dev"), "1-stream"))),

                        new PairStreamCombination(Set.of(new Pair(Set.of("b-dev", "c-dev"), "1-stream"), new Pair(Set.of("a-dev"), "2-stream"))),
                        new PairStreamCombination(Set.of(new Pair(Set.of("b-dev", "c-dev"), "2-stream"), new Pair(Set.of("a-dev"), "1-stream")))
                );
    }

    @Test
    void calculateAllCombinationsForEvenNumberOfDevelopers() {
        List<String> developers = List.of("a-dev", "b-dev", "c-dev", "d-dev");
        List<String> streams = List.of("1-stream", "2-stream");

        given(developers, streams);

        Set<PairStreamCombination> allCombinations = underTest.getAllCombinations();

        assertThat(allCombinations)
                .containsOnly(
                        new PairStreamCombination(Set.of(new Pair(Set.of("a-dev", "b-dev"), "1-stream"), new Pair(Set.of("c-dev", "d-dev"), "2-stream"))),
                        new PairStreamCombination(Set.of(new Pair(Set.of("a-dev", "b-dev"), "2-stream"), new Pair(Set.of("c-dev", "d-dev"), "1-stream"))),

                        new PairStreamCombination(Set.of(new Pair(Set.of("a-dev", "c-dev"), "1-stream"), new Pair(Set.of("b-dev", "d-dev"), "2-stream"))),
                        new PairStreamCombination(Set.of(new Pair(Set.of("a-dev", "c-dev"), "2-stream"), new Pair(Set.of("b-dev", "d-dev"), "1-stream"))),

                        new PairStreamCombination(Set.of(new Pair(Set.of("b-dev", "c-dev"), "1-stream"), new Pair(Set.of("a-dev", "d-dev"), "2-stream"))),
                        new PairStreamCombination(Set.of(new Pair(Set.of("b-dev", "c-dev"), "2-stream"), new Pair(Set.of("a-dev", "d-dev"), "1-stream")))
                );
    }

    @Test
    void calculateAllCombinationsWhereThereShouldBeThreePairsInEachCombination() {
        List<String> developers = List.of("a-dev", "b-dev", "c-dev", "d-dev", "e-dev", "f-dev");
        List<String> streams = List.of("1-stream", "2-stream", "3-stream");

        given(developers, streams);

        Set<PairStreamCombination> allCombinations = underTest.getAllCombinations();

        assertThat(allCombinations)
                .hasSize(90) // 15 pair combinations X 3! ways of permuting with streams
                .allSatisfy(combination -> {
                    assertThat(combination.pairs()).hasSize(3);
                });
    }

    @Test
    void throwsExceptionWhenNotEnoughDevelopersForStreams() { // todo should this make single pairs instead and only fail if you can't even have single devs?
        List<String> developers = List.of("a-dev", "b-dev", "c-dev");
        List<String> streams = List.of("1-stream", "2-stream", "3-stream");

        given(developers, streams);

        assertThatThrownBy(()-> underTest.getAllCombinations())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Not enough developers to pair on streams");
    }

    @Test
    void throwsExceptionWhenNotEnoughStreamsForDevelopers() { // todo should this make up a placeholder stream?
        List<String> developers = List.of("a-dev", "b-dev", "c-dev");
        List<String> streams = List.of("1-stream");

        given(developers, streams);

        assertThatThrownBy(()-> underTest.getAllCombinations())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Not enough streams for developers");
    }
}