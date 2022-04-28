package dev.coldhands.pair.stairs.cli;

enum State {
    BEGIN,
    CALCULATE_PAIRS,
    SHOW_RESULTS,
    SHOW_NEXT_PAIR,
    PROCESS_INPUT_AFTER_NEXT_PAIR,
    ASK_FOR_A_PAIR,
    PROCESS_SELECTION,
    SHOW_SELECTION,
    COMPLETE
}
