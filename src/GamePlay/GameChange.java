package GamePlay;

/**
 * Zmena hry. Ťah ktorý sa nedá vyjadriť žiadnou kartou.
 */
public enum GameChange {

    START_COMMUNICATION,
    STOP_COMMUNICATION,

    START_OF_GAME,
    START_MOVE,
    PICK_CARD,
    END_MOVE,
    LOSE,
    WIN,
    ERROR,
    CHECK;
}
