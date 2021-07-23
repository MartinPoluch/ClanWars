package GamePlay.Cards;

public abstract class Item extends Card{

    private boolean active;

    public Item(String name, String path, int id, int value) {
        super(name, path, id, value);
        this.reset();
    }

    @Override
    public void reset() {
        super.reset();
        active = false;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public abstract int positionIndex();

}
