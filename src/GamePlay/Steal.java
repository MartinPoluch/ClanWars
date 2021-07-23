package GamePlay;

import GamePlay.Cards.Card;
import GamePlay.Players.Player;

import java.io.Serializable;

public class Steal implements Serializable {

    private Card stolen;
    private Player thief;

    public Steal(Card stolen, Player thief) {
        this.stolen = stolen;
        this.thief = thief;
    }

    public Card getStolen() {
        return stolen;
    }

    public Player getThief() {
        return thief;
    }

    @Override
    public String toString() {
        return "Steal " + stolen + " thief: " + thief;
    }
}
