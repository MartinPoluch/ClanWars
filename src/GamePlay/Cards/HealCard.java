package GamePlay.Cards;

import GamePlay.Players.Player;

public class HealCard extends Card{

    private int heal;
    private TypeOfHeal type;
    private Player target;

    public HealCard(String name, String path, int id, int value, int heal, TypeOfHeal type) {
        super(name, path, id, value);
        this.heal = heal;
        this.type = type;
    }

    @Override
    public String getInfo() {
        switch (type) {
            case SELF_HEAL: {
                return "This card will add you " + heal + " healths.";
            }
            case TEAM_HEAL: {
                return "This card will add you or your teammate " + heal + " healths.";
            }
            case KETTLE: {
                return "This card will add " + heal + " healths to whole your team.";
            }
            default: {
                return "";
            }
        }
    }

    public int getHeal() {
        return heal;
    }

    public TypeOfHeal getType() {
        return type;
    }

    public Player getTarget() {
        return target;
    }

    public void setTarget(Player target) {
        this.target = target;
    }

    @Override
    public void reset() {
        super.reset();
        target = null;
    }

    @Override
    public String toString() {
        if (type == TypeOfHeal.TEAM_HEAL && target != null) {
            return super.toString() + " target: " + target.getName();
        }
        else {
            return super.toString();
        }

    }
}
