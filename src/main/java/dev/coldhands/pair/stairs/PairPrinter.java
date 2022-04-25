package dev.coldhands.pair.stairs;

import com.jakewharton.picnic.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.Comparator.*;

class PairPrinter {

    static String draw(Set<String> developers, List<Pairing> pairings) {
        Table.Builder builder = new Table.Builder()
                .setTableStyle(new TableStyle.Builder()
                        .setBorderStyle(BorderStyle.Hidden)
                        .build())
                .setCellStyle(new CellStyle.Builder()
                        .setPaddingLeft(1)
                        .setPaddingRight(1)
                        .build())

                .setHeader(buildHeader(developers));

        builder.setBody(buildBody(developers, pairings));

        return builder.build().toString();
    }

    private static TableSection buildBody(Set<String> developers, List<Pairing> pairings) {
        TableSection.Builder builder = new TableSection.Builder();

        Set<Pair> allPairs = PairUtils.allPairs(developers);

        int pad = 0;
        var sorted = new ArrayList<>(developers);
        Collections.sort(sorted);

        for (String dev : sorted) {
            Row.Builder row = new Row.Builder();
            row.addCell(dev);

            // ensure leading cells that have their data already printed
            // are replaced with empty cells
            for (int i = 0; i < pad; i++) {
                row.addCell("");
            }
            pad++;

            allPairs.stream()
                    // find pairs where current dev is first,
                    // where you aren't first will already have been printed
                    .filter(pair -> pair.first().equals(dev))
                    .sorted(comparing(Pair::first)
                            .thenComparing(Pair::second, nullsFirst(naturalOrder())))
                    .forEach(pair -> {
                        long count = pairings.stream()
                                .filter(pairing -> pair.equivalentTo(pairing.pair()))
                                .count();

                        row.addCell(Long.toString(count));
                    });


            builder.addRow(row.build());
        }

        return builder.build();
    }

    private static TableSection buildHeader(Set<String> developers) {
        Row.Builder row = new Row.Builder();
        row.addCell("");
        developers.stream()
                .sorted()
                .forEach(row::addCell);

        return new TableSection.Builder()
                .addRow(row.build())
                .build();
    }

}
