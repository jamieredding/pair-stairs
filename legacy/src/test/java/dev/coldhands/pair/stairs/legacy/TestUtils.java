package dev.coldhands.pair.stairs.legacy;

import dev.coldhands.pair.stairs.legacy.domain.Pair;
import dev.coldhands.pair.stairs.legacy.domain.Pairing;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public final class TestUtils {

    public static <T> void testComparator(Comparator<T> underTest,
                                   T o1,
                                   T o2) {
        assertThat(underTest.compare(o1, o2))
                .isEqualTo(-underTest.compare(o2, o1))
                .extracting(Integer::signum)
                .isEqualTo(-1);
    }

    public static String unWindows(String s) {
        return s.replaceAll("\r\n", "\n");
    }

    public static List<Pairing> normalise(List<Pairing> badlyOrderedPairNames) {
        return badlyOrderedPairNames.stream()
                .map(pairing -> new Pairing(pairing.date(), normalise(pairing.pair())))
                .toList();
    }

    public static Pair normalise(Pair pair) {
        String first = pair.first();
        if (pair.second() != null) {
            int compare = first.compareTo(pair.second());
            if (compare > 0) {
                return new Pair(pair.second(), pair.first());
            }
        }
        return pair;
    }
}
