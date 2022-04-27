package dev.coldhands.pair.stairs;

import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;

final class TestUtils {

    static <T> void testComparator(Comparator<T> underTest,
                                   T o1,
                                   T o2) {
        assertThat(underTest.compare(o1, o2))
                .isEqualTo(-underTest.compare(o2, o1))
                .extracting(Integer::signum)
                .isEqualTo(-1);
    }
}
