package GamePlay.Cards;


import GamePlay.Players.Player;

import java.io.Serializable;

public abstract class Card implements Serializable {

    private final String name;
    private String imagePath;
    private Player owner;
    private int value;
    private boolean thrown;
    private int id;

    public Card(String name, String path, int id, int value) {
        this.name = name;
        this.imagePath = path;
        this.reset();
        this.id = id;
        this.value = value;
    }

    public void reset() {
        owner = null;
        thrown = false;
    }

    public boolean isThrown() {
        return thrown;
    }

    public void setThrown(boolean thrown) {
        this.thrown = thrown;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    public String getImagePath() {
        return imagePath;
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public abstract String getInfo();

    @Override
    public String toString() {
        String owner = (getOwner() != null) ? getOwner().getName() : " not owned";
        return "Card " + getName() + " owner: " + owner + " id: " + id;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Card card = (Card) obj;
        return (this.getId() == card.getId());
    }
}
