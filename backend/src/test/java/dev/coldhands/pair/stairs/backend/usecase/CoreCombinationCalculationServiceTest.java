package dev.coldhands.pair.stairs.backend.usecase;

import dev.coldhands.pair.stairs.backend.domain.DeveloperInfo;
import dev.coldhands.pair.stairs.backend.domain.PairStream;
import dev.coldhands.pair.stairs.backend.domain.ScoredCombination;
import dev.coldhands.pair.stairs.backend.domain.StreamInfo;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.DeveloperEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.StreamEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.DeveloperRepository;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.StreamRepository;
import dev.coldhands.pair.stairs.core.domain.Combination;
import dev.coldhands.pair.stairs.core.usecases.pairstream.PairStreamEntryPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoreCombinationCalculationServiceTest {

    @Mock
    private EntryPointFactory entryPointFactory;
    @Mock
    private PairStreamEntryPoint entryPoint;
    @Mock
    private DeveloperRepository developerRepository;
    @Mock
    private StreamRepository streamRepository;

    private CoreCombinationCalculationService underTest;

    @BeforeEach
    void setUp() {
        underTest = new CoreCombinationCalculationService(developerRepository, streamRepository, entryPointFactory);
    }

    @Test
    void runCoreEntryPointUsingInput() {
        when(developerRepository.findAllById(List.of(0L, 1L, 2L)))
                .thenReturn(List.of(
                        new DeveloperEntity(0L, "dev-0"),
                        new DeveloperEntity(1L, "dev-1"),
                        new DeveloperEntity(2L, "dev-2")
                ));
        when(streamRepository.findAllById(List.of(0L, 1L)))
                .thenReturn(List.of(
                        new StreamEntity(0L, "stream-a"),
                        new StreamEntity(1L, "stream-b")
                ));
        when(entryPointFactory.create(List.of("0", "1", "2"), List.of("0", "1"))).thenReturn(entryPoint);

        when(entryPoint.computeScoredCombinations()).thenReturn(List.of(
                new dev.coldhands.pair.stairs.core.domain.ScoredCombination<>(
                        new Combination<>(Set.of(
                                new dev.coldhands.pair.stairs.core.domain.pairstream.PairStream(Set.of("0", "1"), "0"),
                                new dev.coldhands.pair.stairs.core.domain.pairstream.PairStream(Set.of("2"), "1"))),
                        10,
                        List.of()
                ),
                new dev.coldhands.pair.stairs.core.domain.ScoredCombination<>(
                        new Combination<>(Set.of(
                                new dev.coldhands.pair.stairs.core.domain.pairstream.PairStream(Set.of("0", "2"), "0"),
                                new dev.coldhands.pair.stairs.core.domain.pairstream.PairStream(Set.of("1"), "1"))),
                        20,
                        List.of()
                )
        ));

        final List<ScoredCombination> scoredCombinations = underTest.calculate(List.of(0L, 1L, 2L), List.of(0L, 1L), 0, 10);

        assertThat(scoredCombinations).containsExactly(
                new ScoredCombination(10,
                        List.of(new PairStream(
                                        List.of(
                                                new DeveloperInfo(0, "dev-0"),
                                                new DeveloperInfo(1, "dev-1")
                                        ),
                                        new StreamInfo(0, "stream-a")
                                ),
                                new PairStream(
                                        List.of(
                                                new DeveloperInfo(2, "dev-2")
                                        ),
                                        new StreamInfo(1, "stream-b")
                                ))
                ),
                new ScoredCombination(20,
                        List.of(new PairStream(
                                        List.of(
                                                new DeveloperInfo(0, "dev-0"),
                                                new DeveloperInfo(2, "dev-2")
                                        ),
                                        new StreamInfo(0, "stream-a")
                                ),
                                new PairStream(
                                        List.of(
                                                new DeveloperInfo(1, "dev-1")
                                        ),
                                        new StreamInfo(1, "stream-b")
                                ))
                ));
    }

    @Nested
    class Pagination {

        @Test
        void onlyReturnFirstPageOfScoredCombinationsIfMoreThanPageSizeAreReturned() {
            when(developerRepository.findAllById(List.of(0L)))
                    .thenReturn(List.of(
                            new DeveloperEntity(0L, "dev-0")
                    ));
            when(streamRepository.findAllById(List.of(0L)))
                    .thenReturn(List.of(
                            new StreamEntity(0L, "stream-a")
                    ));
            when(entryPointFactory.create(List.of("0"), List.of("0"))).thenReturn(entryPoint);

            when(entryPoint.computeScoredCombinations()).thenReturn(List.of(
                    coreScoredCombinationWithScore(10),
                    coreScoredCombinationWithScore(20),
                    coreScoredCombinationWithScore(30)
            ));

            final int pageSize = 2;
            final int requestedPage = 0;
            final CoreCombinationCalculationService underTest = new CoreCombinationCalculationService(developerRepository, streamRepository, entryPointFactory);

            final List<ScoredCombination> scoredCombinations = underTest.calculate(List.of(0L), List.of(0L), requestedPage, pageSize);
            final List<Integer> scores = scoredCombinations.stream()
                    .map(ScoredCombination::score)
                    .toList();

            assertThat(scores).containsExactly(10, 20);
        }

        @Test
        void returnSecondPageOfScoredCombinationsWhenRequested() {
            when(developerRepository.findAllById(List.of(0L)))
                    .thenReturn(List.of(
                            new DeveloperEntity(0L, "dev-0")
                    ));
            when(streamRepository.findAllById(List.of(0L)))
                    .thenReturn(List.of(
                            new StreamEntity(0L, "stream-a")
                    ));
            when(entryPointFactory.create(List.of("0"), List.of("0"))).thenReturn(entryPoint);

            when(entryPoint.computeScoredCombinations()).thenReturn(List.of(
                    coreScoredCombinationWithScore(10),
                    coreScoredCombinationWithScore(20),
                    coreScoredCombinationWithScore(30)
            ));

            final int pageSize = 2;
            final int requestedPage = 1;
            final CoreCombinationCalculationService underTest = new CoreCombinationCalculationService(developerRepository, streamRepository, entryPointFactory);

            final List<ScoredCombination> scoredCombinations = underTest.calculate(List.of(0L), List.of(0L), requestedPage, pageSize);
            final List<Integer> scores = scoredCombinations.stream()
                    .map(ScoredCombination::score)
                    .toList();

            assertThat(scores).containsExactly(30);
        }

        private static dev.coldhands.pair.stairs.core.domain.ScoredCombination<dev.coldhands.pair.stairs.core.domain.pairstream.PairStream> coreScoredCombinationWithScore(int totalScore) {
            return new dev.coldhands.pair.stairs.core.domain.ScoredCombination<>(
                    new Combination<>(Set.of(
                            new dev.coldhands.pair.stairs.core.domain.pairstream.PairStream(Set.of("0"), "0"))),
                    totalScore,
                    List.of()
            );
        }
    }
}