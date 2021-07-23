package GamePlay.Players;

import GamePlay.Players.Player;

import java.io.Serializable;

public class Health implements Serializable {

    private int maxHealth;
    private int actualHealth;
    private Player player;

    public Health(Player player) {
        this.maxHealth = 0;
        this.actualHealth = 0;
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setActualHealth(int actualHealth) {
        this.actualHealth = actualHealth;
    }

    public int getActualHealth() {
        return actualHealth;
    }

    public int changeHealth(int change) {
        actualHealth += change;
        if (actualHealth > maxHealth) {
            actualHealth = maxHealth;
        }
        else if (actualHealth < 0){
            actualHealth = 0;
        }
        return actualHealth;
    }

    public double percentage() {
        return (double)actualHealth / (double) maxHealth;
    }

    @Override
    public String toString() {
        return "Health actual= " +  actualHealth + " owner= " + player.getName();
    }
}
