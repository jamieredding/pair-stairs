package dev.coldhands.pair.stairs.core.usecases.pairstream;

import dev.coldhands.pair.stairs.core.domain.Combination;
import dev.coldhands.pair.stairs.core.domain.CombinationService;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStream;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PairStreamCombinationServiceTest {

    private CombinationService<PairStream> underTest;

    private void given(Collection<String> developers, Collection<String> streams) {
        underTest = new PairStreamCombinationService(developers, streams);
    }

    @Test
    void calculateAllCombinationsForOddNumberOfDevelopers() {
        List<String> developers = List.of("a-dev", "b-dev", "c-dev");
        List<String> streams = List.of("1-stream", "2-stream");

        given(developers, streams);

        Set<Combination<PairStream>> allCombinations = underTest.getAllCombinations();

        assertThat(allCombinations)
                .containsOnly(
                        new Combination<>(Set.of(new PairStream(Set.of("a-dev", "b-dev"), "1-stream"), new PairStream(Set.of("c-dev"), "2-stream"))),
                        new Combination<>(Set.of(new PairStream(Set.of("a-dev", "b-dev"), "2-stream"), new PairStream(Set.of("c-dev"), "1-stream"))),

                        new Combination<>(Set.of(new PairStream(Set.of("a-dev", "c-dev"), "1-stream"), new PairStream(Set.of("b-dev"), "2-stream"))),
                        new Combination<>(Set.of(new PairStream(Set.of("a-dev", "c-dev"), "2-stream"), new PairStream(Set.of("b-dev"), "1-stream"))),

                        new Combination<>(Set.of(new PairStream(Set.of("b-dev", "c-dev"), "1-stream"), new PairStream(Set.of("a-dev"), "2-stream"))),
                        new Combination<>(Set.of(new PairStream(Set.of("b-dev", "c-dev"), "2-stream"), new PairStream(Set.of("a-dev"), "1-stream")))
                );
    }

    @Test
    void calculateAllCombinationsForEvenNumberOfDevelopers() {
        List<String> developers = List.of("a-dev", "b-dev", "c-dev", "d-dev");
        List<String> streams = List.of("1-stream", "2-stream");

        given(developers, streams);

        Set<Combination<PairStream>> allCombinations = underTest.getAllCombinations();

        assertThat(allCombinations)
                .containsOnly(
                        new Combination<>(Set.of(new PairStream(Set.of("a-dev", "b-dev"), "1-stream"), new PairStream(Set.of("c-dev", "d-dev"), "2-stream"))),
                        new Combination<>(Set.of(new PairStream(Set.of("a-dev", "b-dev"), "2-stream"), new PairStream(Set.of("c-dev", "d-dev"), "1-stream"))),

                        new Combination<>(Set.of(new PairStream(Set.of("a-dev", "c-dev"), "1-stream"), new PairStream(Set.of("b-dev", "d-dev"), "2-stream"))),
                        new Combination<>(Set.of(new PairStream(Set.of("a-dev", "c-dev"), "2-stream"), new PairStream(Set.of("b-dev", "d-dev"), "1-stream"))),

                        new Combination<>(Set.of(new PairStream(Set.of("b-dev", "c-dev"), "1-stream"), new PairStream(Set.of("a-dev", "d-dev"), "2-stream"))),
                        new Combination<>(Set.of(new PairStream(Set.of("b-dev", "c-dev"), "2-stream"), new PairStream(Set.of("a-dev", "d-dev"), "1-stream")))
                );
    }

    @Test
    void calculateAllCombinationsWhereThereShouldBeThreePairsInEachCombination() {
        List<String> developers = List.of("a-dev", "b-dev", "c-dev", "d-dev", "e-dev", "f-dev");
        List<String> streams = List.of("1-stream", "2-stream", "3-stream");

        given(developers, streams);

        Set<Combination<PairStream>> allCombinations = underTest.getAllCombinations();

        assertThat(allCombinations)
                .hasSize(90) // 15 pair combinations X 3! ways of permuting with streams
                .allSatisfy(combination -> {
                    assertThat(combination.pairs()).hasSize(3);

                    record AllParts(Set<String> developers, Set<String> streams) {

                    }

                    final AllParts allParts = combination.pairs().stream()
                            .collect(teeing(
                                    flatMapping(pair -> pair.developers().stream(), toSet()),
                                    mapping(PairStream::stream, toSet()),
                                    AllParts::new
                            ));

                    assertThat(allParts.developers)
                            .containsExactlyInAnyOrderElementsOf(developers);
                    assertThat(allParts.streams)
                            .containsExactlyInAnyOrderElementsOf(streams);

                });
    }

    @Test
    void allowACombinationOfASingleDeveloperAndStream() {
        List<String> developers = List.of("a-dev");
        List<String> streams = List.of("1-stream");

        given(developers, streams);

        Set<Combination<PairStream>> allCombinations = underTest.getAllCombinations();

        assertThat(allCombinations)
                .containsOnly(
                        new Combination<>(Set.of(new PairStream(Set.of("a-dev"), "1-stream")))
                );
    }


    @Test
    void throwsExceptionWhenNotEnoughDevelopersForStreams() { // todo should this make single pairs instead and only fail if you can't even have single devs?
        List<String> developers = List.of("a-dev", "b-dev", "c-dev");
        List<String> streams = List.of("1-stream", "2-stream", "3-stream");

        given(developers, streams);

        assertThatThrownBy(() -> underTest.getAllCombinations())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Not enough developers to pair on streams");
    }

    @Test
    void throwsExceptionWhenNotEnoughStreamsForDevelopers() { // todo should this make up a placeholder stream?
        List<String> developers = List.of("a-dev", "b-dev", "c-dev");
        List<String> streams = List.of("1-stream");

        given(developers, streams);

        assertThatThrownBy(() -> underTest.getAllCombinations())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Not enough streams for developers");
    }
}