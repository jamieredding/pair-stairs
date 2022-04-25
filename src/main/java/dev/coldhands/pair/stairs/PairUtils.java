package dev.coldhands.pair.stairs;

import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class PairUtils {
    static Set<Pair> allPairs(Set<String> developers) {
        var pairs = new HashSet<Pair>();
        Sets.combinations(developers, 2).stream()
                .map(ArrayList::new)
                .peek(Collections::sort)
                .map(list -> new Pair(list.get(0), list.get(1)))
                .forEach(pairs::add);
        developers.forEach(dev -> pairs.add(new Pair(dev)));

        return pairs;
    }
}
