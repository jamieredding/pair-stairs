package dev.coldhands.pair.stairs;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class PairTest {

    @ParameterizedTest
    @CsvSource({
            "jamie, jamie, jorge, true",
            "jamie, jorge, jamie, true",
            "jamie, jamie,, true",
            "jamie, jorge, reece, false",
            "jamie,,, false"
    })
    void contains(String singleDev, String first, String second, boolean contains) {
        assertThat(new Pair(first, second).contains(singleDev)).isEqualTo(contains);
    }

    @ParameterizedTest
    @CsvSource({
            "jamie, jorge, jamie, jorge, true",
            "jamie, jorge, jorge, jamie, true",
            "jamie,, jamie,, true",
            "jamie,, jorge, reece, false",
            "jamie,,jorge,, false"
    })
    void equivalentTo(String firstFirst, String firstSecond, String secondFirst, String secondSecond, boolean equivalentTo) {
        assertThat(new Pair(firstFirst, firstSecond).equivalentTo(new Pair(secondFirst, secondSecond))).isEqualTo(equivalentTo);
    }
}