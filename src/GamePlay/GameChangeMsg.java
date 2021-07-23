package GamePlay;

import GamePlay.Players.Player;

import java.io.Serializable;

/**
 * Zapúzdrenie zmeny do samostatného objektu a priradie referenciu na hráč ktorý danú zmenu vykonal.
 */
public class GameChangeMsg implements Serializable {

    private GameChange change;
    private Player initiator;

    public GameChangeMsg(GameChange change, Player initiator) {
        this.change = change;
        this.initiator = initiator;
    }

    public GameChange getChange() {
        return change;
    }

    public Player getInitiator() {
        return initiator;
    }

    @Override
    public String toString() {
        return "player: " + initiator.getName() + " change: " + change;
    }
}
