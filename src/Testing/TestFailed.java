package Testing;

import GamePlay.Game;

public class TestFailed extends Exception {

    private Game failedGame;
    private int replication;

    public TestFailed(String message, Throwable cause, Game failedGame, int replication) {
        super(message, cause);
        this.failedGame = failedGame;
        this.replication = replication;
    }

    public Game getFailedGame() {
        return failedGame;
    }

    @Override
    public String toString() {
        return "rep: " + replication + " message: " + getMessage();
    }
}
