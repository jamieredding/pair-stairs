package dev.coldhands.pair.stairs.cli;

enum State {
    INITIALISE,
    SHOW_PREVIOUS_PAIR_STAIR,
    CALCULATE_PAIRS,
    SHOW_RESULTS,
    SHOW_NEXT_PAIR,
    SHOW_NEXT_PAIR_OPTIONS,
    PROCESS_INPUT_AFTER_NEXT_PAIR,
    SHOW_NUMBERED_DEVELOPERS_TO_PICK,
    PROCESS_INPUT_FOR_PICKING_A_PAIR,
    SHOW_OUT_OF_PAIRS,
    ASK_FOR_A_PAIR,
    PROCESS_SELECTION,
    SHOW_SELECTION,
    SAVE_DATA_FILE,
    COMPLETE,
    FAILED
}
