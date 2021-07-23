package GamePlay.Players;

import java.io.Serializable;

public class Character implements Serializable {

    private String name;
    private int damage;
    private int damageReduction;
    private int offensiveRange;
    private int defensiveRange;
    private int maxHealth;

    public Character(String name) {
        this.name = name;
    }

    public Character(String name, int maxHealth, int damage, int damageReduction, int offensiveRange, int defensiveRange) {
        this.name = name;
        this.maxHealth = maxHealth;
        this.damage = damage;
        this.damageReduction = damageReduction;
        this.offensiveRange = offensiveRange;
        this.defensiveRange = defensiveRange;
    }

    public String getName() {
        return name;
    }

    public int getDamage() {
        return damage;
    }

    public int getDamageReduction() {
        return damageReduction;
    }

    public int getOffensiveRange() {
        return offensiveRange;
    }

    public int getDefensiveRange() {
        return defensiveRange;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    @Override
    public String toString() {
        return name;
    }

}
