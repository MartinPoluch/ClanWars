package GamePlay.Cards;

public class Weapon extends Item{

    private int range;
    private int damage;

    public Weapon(String name, String path, int id, int value, int range, int damage) {
        super(name, path, id, value);
        this.range = range;
        this.damage = damage;
    }

    @Override
    public String getInfo() {
        return "Weapon increases your damage (" + damage + ") and range (" + range + ")";
    }

    public int getRange() {
        return range;
    }

    public int getDamage() {
        return damage;
    }

    @Override
    public int positionIndex() {
        return 0;
    }
}
