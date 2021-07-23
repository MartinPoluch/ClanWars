package GamePlay.Cards;

public class Shield extends Item{

    private int protection;

    public Shield(String name, String path, int id, int value, int protection) {
        super(name, path, id, value);
        this.protection = protection;
    }

    public int getProtection() {
        return protection;
    }

    @Override
    public String getInfo() {
        return "Shield reduces damage of enemies attacks.";
    }

    @Override
    public int positionIndex() {
        return 1;
    }
}
