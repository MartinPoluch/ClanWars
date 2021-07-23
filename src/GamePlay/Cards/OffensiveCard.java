package GamePlay.Cards;

import GamePlay.Players.Player;

public class OffensiveCard extends Card{

    private Player defender;
    private int damage;
    private Item target;
    private TypeOfOffensive type;

    public OffensiveCard(String name, String path, int id, int value, TypeOfOffensive type) {
        super(name, path, id, value);
        this.damage = 0;
        this.type = type;
    }

    @Override
    public void reset() {
        super.reset();
        target = null;
        defender = null;
        damage = 0;
    }

    public TypeOfOffensive getType() {
        return type;
    }

    @Override
    public String getInfo() {
        switch (type) {
            case HIT: {
                return "This card reduces opponent health";
            }
            case THEFT: {
                return "This card steals opponent card";
            }
            case DISARM: {
                return "This card throws opponent card";
            }
            default: {
                return "";
            }
        }
    }

    public void setDefender(Player defender) {
        this.defender = defender;
    }

    public Player getDefender() {
        return defender;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getDamage() {
        return damage;
    }

    public void setTarget(Item target) {
        this.target = target;
    }

    public Item getTarget() {
        return target;
    }

    @Override
    public String toString() {
        String defender = (getDefender() != null) ? getDefender().getName() : "null";
        String target = (getTarget() != null) ? getTarget().getName() : "null";
        return "Offensive" + super.toString() + " type: " + type + " defender: " + defender + " target: " + target;
    }
}
