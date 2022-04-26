package dev.coldhands.pair.stairs;

import com.jakewharton.picnic.*;

import java.util.List;
import java.util.Set;

class PairPrinter {

    private static final CellStyle DATA_CELL_STYLE = new CellStyle.Builder()
            .setAlignment(TextAlignment.MiddleCenter)
            .build();

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

        List<PairCount> pairCounts = PairUtils.countPairs(developers, pairings);
        List<String> sortedDevelopers = developers.stream().sorted().toList();

        int numberOfDataColumnsToFill = developers.size();
        int pairCountsUpperIndex = numberOfDataColumnsToFill;
        int pairCountsIndex = 0;
        int leadingEmptyCellsToAdd = 0;

        for(String dev : sortedDevelopers) {
            Row.Builder row = new Row.Builder();
            row.addCell(dev);
            for (int j = 0; j < leadingEmptyCellsToAdd; j++) {
                row.addCell("");
            }
            leadingEmptyCellsToAdd++;

            for (; pairCountsIndex < pairCountsUpperIndex; pairCountsIndex++) {
                PairCount pairCount = pairCounts.get(pairCountsIndex);
                row.addCell(new Cell.Builder(Long.toString(pairCount.count()))
                                .setStyle(DATA_CELL_STYLE)
                        .build());
            }

            numberOfDataColumnsToFill -= 1;
            pairCountsUpperIndex += numberOfDataColumnsToFill;

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
