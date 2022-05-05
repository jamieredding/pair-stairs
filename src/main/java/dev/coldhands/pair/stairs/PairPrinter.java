package dev.coldhands.pair.stairs;

import com.jakewharton.picnic.*;

import java.util.List;
import java.util.Set;

import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;

public final class PairPrinter {

    private static final CellStyle DATA_CELL_STYLE = new CellStyle.Builder()
            .setAlignment(TextAlignment.MiddleCenter)
            .build();

    public static String drawPairStairs(Set<String> developers, List<Pairing> pairings) {
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

        for (String dev : sortedDevelopers) {
            Row.Builder row = new Row.Builder();
            row.addCell(dev);
            for (int j = 0; j < leadingEmptyCellsToAdd; j++) {
                row.addCell("");
            }
            leadingEmptyCellsToAdd++;

            for (; pairCountsIndex < pairCountsUpperIndex; pairCountsIndex++) {
                PairCount pairCount = pairCounts.get(pairCountsIndex);
                String content = Long.toString(pairCount.count()) +
                                 (pairCount.wasRecent() ? " *" : "");
                row.addCell(new Cell.Builder(content)
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

    public static String drawPairChoices(List<ScoredPairCombination> scoredPairCombinations, int optionsToShow) {
        Table.Builder builder = new Table.Builder()
                .setTableStyle(new TableStyle.Builder()
                        .setBorderStyle(BorderStyle.Hidden)
                        .build())
                .setCellStyle(new CellStyle.Builder()
                        .setPaddingLeft(1)
                        .setPaddingRight(1)
                        .setAlignment(TextAlignment.MiddleCenter)
                        .build());

        int numberOfPairs = scoredPairCombinations.stream()
                .mapToInt(combination -> combination.pairCombination().size())
                .findFirst()
                .getAsInt();

        record PrintableScoredPairCombination(List<Pair> pairs,
                                              int score){}

        List<PrintableScoredPairCombination> toPrint = scoredPairCombinations.stream()
                .map(spc -> new PrintableScoredPairCombination(
                        spc.pairCombination().stream()
                                .sorted(comparing(Pair::second, nullsLast((o1, o2) -> 0))
                                        .thenComparing(Pair::first))
                                .toList(),
                        spc.score()
                ))
                .toList();

        final TableSection.Builder body = new TableSection.Builder();

        addPairIndexRow(body, optionsToShow);

        for (int i = 0; i < numberOfPairs; i++) {
            Row.Builder row = new Row.Builder();
            row.addCell("Pair " + toLetter(i));
            for (int optionIndex = 0; optionIndex < optionsToShow; optionIndex++) {
                PrintableScoredPairCombination option = toPrint.get(optionIndex);
                Pair pair = option.pairs().get(i);
                if (pair.second() == null) {
                    row.addCell(twoColumnCell(pair.first()));
                }else {
                    row.addCell(pair.first());
                    row.addCell(pair.second());
                }
            }
            body.addRow(row.build());
        }

        Row.Builder bottomRow = new Row.Builder();
        bottomRow.addCell("");
        for (int i = 0; i < optionsToShow; i++) {
            PrintableScoredPairCombination option = toPrint.get(i);
            bottomRow.addCell(twoColumnCell(String.valueOf(option.score)));
        }

        body.addRow(bottomRow.build());
        builder.setBody(body.build());

        return builder.build().toString();
    }

    private static String toLetter(int i) {
        return String.valueOf((char) (i + 'a'));
    }

    private static Cell twoColumnCell(String value) {
        return new Cell.Builder(value)
                .setColumnSpan(2)
                .build();
    }

    private static void addPairIndexRow(TableSection.Builder builder, int numberOfPairs) {
        Row.Builder row = new Row.Builder();

        row.addCell("");

        for (int i = 0; i < numberOfPairs; i++) {
            row.addCell(twoColumnCell(String.valueOf(i + 1)));
        }

        builder.addRow(row.build());
    }
}
