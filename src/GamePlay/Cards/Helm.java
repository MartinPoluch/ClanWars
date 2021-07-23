package GamePlay.Cards;

public class Helm extends Item{

    private int rangePlus;
    private int rangeMinus;

    public Helm(String name, String path, int id, int value, int rangePlus, int rangeMinus) {
        super(name, path, id, value);
        this.rangePlus = rangePlus;
        this.rangeMinus = rangeMinus;
    }

    public int getRangePlus() {
        return rangePlus;
    }

    public int getRangeMinus() {
        return rangeMinus;
    }

    @Override
    public String getInfo() {
        return "Helm will change range between you and your enemies";
    }

    @Override
    public int positionIndex() {
        return 2;
    }
}
