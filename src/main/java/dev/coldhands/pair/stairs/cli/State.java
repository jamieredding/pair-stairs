package dev.coldhands.pair.stairs.cli;

enum State {
    INITIAL_OUTPUT,
    OFFER_USER_CHOICE,
    SHOW_NUMBERED_DEVELOPERS_TO_PICK,
    PROCESS_INPUT_FOR_PICKING_A_PAIR,
    SHOW_SELECTION,
    SAVE_DATA_FILE,
    COMPLETE
}
