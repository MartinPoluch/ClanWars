package GamePlay.Players;

import Client.GUI.Animator;
import Server.Connection;
import GamePlay.*;
import GamePlay.Cards.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public abstract class Player implements Serializable {

    private String name;
    private long id;
    private Team team;
    private Character character;
    private Health health;
    private List<Card> inventory;
    private Item[] items;
    private boolean testing;

    transient protected Map<Player, Connection> opponents;
    transient protected List<Player> players;
    transient protected Deck deck;
    protected final static int MAX_CARDS = 3;

    public Player(String name) {
        this.name = name;
        this.id = -1;
        this.team = null;
        this.character = null;
        this.health = new Health(this);
        this.inventory = new ArrayList<>();
        this.items = new Item[3];
        this.opponents = new HashMap<>();
        this.players = new ArrayList<>();
        this.deck = null;
        this.testing = false;
    }

    public Player(String name, boolean testing) {
        this(name);
        this.testing = true;
    }

    public boolean isTesting() {
        return testing;
    }

    public Player(Player player) {
        this.name = player.getName();
        this.id = player.getId();
        this.team = player.getTeam();
        this.character = player.getCharacter();
        this.health = new Health(this);
        Health health = player.getHealth();
        this.health.setMaxHealth(health.getMaxHealth());
        this.health.setActualHealth(health.getActualHealth());
        this.inventory = new ArrayList<>();
        for (Card card : player.getInventory()) {
            addToInventory(card);
        }
        this.items = new Item[3];
        for (int itemIndex = 0; itemIndex < 3; itemIndex++) {
            Item item = player.getItem(itemIndex);
            if (item != null) {
                addItem(item);
            }
        }
        this.opponents = player.opponents;
        this.players = player.players;
        this.deck = player.deck;
    }


    public Item addItem(Item newItem) {
        Item oldItem = items[newItem.positionIndex()];
        items[newItem.positionIndex()] = newItem;
        return oldItem;
    }

    public Item removeItem(int index) {
        Item removed = items[index];
        items[index] = null;
        return removed;
    }

    public Item getItem(int index) {
        return items[index];
    }

    public Item getFirstActiveItem(){
        for (int indexItem = 0; indexItem < 3; indexItem++) {
            Item item = items[indexItem];
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    public int getOffensiveRange() {
        int range = 1 + character.getOffensiveRange();
        for (Item item : items) {
            if (item instanceof Weapon) {
                Weapon myWeapon = (Weapon) item;
                range += myWeapon.getRange();
            }
            else if (item instanceof Helm) {
                Helm myHelm = (Helm) item;
                range += myHelm.getRangePlus();
            }
        }
        return range;
    }

    public int getDefensiveRange() {
        int range = character.getDefensiveRange();
        for (Item item : items) {
            if (item instanceof Helm) {
                Helm myHelm = (Helm) item;
                range += myHelm.getRangeMinus();
            }
        }
        return range;
    }

    public int getDamage() {
        int damage = character.getDamage();
        for (Item item : items) {
            if (item instanceof Weapon) {
                Weapon myWeapon = (Weapon) item;
                return (damage + myWeapon.getDamage());
            }
        }
        final int DEFAULT_DAMAGE = 20;
        return damage + DEFAULT_DAMAGE;
    }

    public int getProtection() {
        int protection = character.getDamageReduction();
        for (Item item : items) {
            if (item instanceof Shield) {
                Shield myShield = (Shield) item;
                protection += myShield.getProtection();
            }
        }
        return protection;
    }

    public void addToInventory(Card card) {
        card.setOwner(this);
        if (card instanceof Item) {
            Item item = (Item) card;
            item.setActive(false);
            inventory.add(item);
        }
        else {
            inventory.add(card);
        }
    }

    public boolean removeFromInventory(Card card) {
        return inventory.remove(card);
    }

    public Card removeFromInventory(int index) {
        return inventory.remove(index);
    }

    public List<Card> getInventory() {
        return inventory;
    }

    /**
     * Id môže byť nastavené iba raz.
     * @param id unikátny identifikátor na základe ktorého server dokáže rozpoznať klient
     */
    public void setId(long id) {
        if (this.id == -1) { // este nebolo definovane
            this.id = id;
        }
    }

    public void setHealth(Health health) {
        this.health = health;
    }

    public long getId() {
        return id;
    }

    public Team getTeam() {
        return team;
    }

    public Health getHealth() {
        return health;
    }

    public Character getCharacter() {
        return character;
    }

    public void startPlaying(Team team, Character character) {
        this.team = team;
        this.character = character;
        int maxHealth = character.getMaxHealth();
        if (this instanceof Bot) {
            maxHealth += ((Bot) this).getAddedHealth();
        }
        this.health.setMaxHealth(maxHealth);
        this.health.changeHealth(maxHealth);
        this.inventory.clear();
    }



    public void setOpponents(Map<Player, Connection> opponents) {
        this.opponents = opponents;
    }

    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        String info = name;
//        info += name + " health: " + health.getActualHealth() + "\n";
//        info += inventory + "\"";
//        info += "\nweapon: " +items[0];
//        info += "\nshield: " + items[1];
//        info += "\nhorse: " + items[2];
        return info;
    }

    public abstract String getType();

    @Override
    public int hashCode() {
        return Objects.hash(name, id);
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    /**
     *
     * @return Ak vrati true tak tah ešte pokračuje. Ak vráti false tak ťah skončil.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public abstract boolean move() throws IOException, ClassNotFoundException;

    protected abstract boolean offense() throws IOException, ClassNotFoundException ;

    public abstract void defense(OffensiveCard offensiveCard) throws IOException, ClassNotFoundException ;

    public abstract void pickCard() throws IOException;

    protected abstract void healing() throws IOException;

    protected abstract void equip() throws IOException;

    /**
     * Vráti zoznam všetkých mojích spojencov vrátane mňa.
     * @return spojenci a ja
     */
    protected List<Player> getTeammates() {
        List<Player> teammates = new ArrayList<>();
        for (Player player : players) {
            boolean heIsMyTeammate = player.getTeam().equals(this.getTeam());
            if (heIsMyTeammate) {
                teammates.add(player);
            }
        }
        return teammates;
    }

    public void throwUsedCard(Card card) {
        if (removeFromInventory(card)) {
            deck.insert(card);
        }
        animationDelay();
    }

    public void throwAllCards() throws IOException{
        for (int itemIndex = 0; itemIndex < 3; itemIndex++) {
            if (getItem(itemIndex) != null) {
                Item removed = removeItem(itemIndex);
                removed.setActive(false);
                removed.setThrown(true);
                deck.insert(removed);
            }
        }

        for (Card card : getInventory()) {
            card.setThrown(true);
            deck.insert(card);
        }
        inventory.clear();
    }

    protected void broadcastToOpponents(Object message, Player ... exceptions) throws IOException {
        for (Map.Entry<Player, Connection> client : opponents.entrySet()) {
            if (! isException(client.getKey(), exceptions)) {
                Connection connection = client.getValue();
                if (connection.isConnected()) {
                    connection.sendMessage(message);
                }
            }

        }
    }

    private boolean isException(Player client, Player[] exceptions) {
        for (Player exception : exceptions) {
            if (client.equals(exception)) {
                return true;
            }
        }
        return false;
    }

    protected Player serverPlayer(Player clientPlayer) {
        int indexOfServerPlayer = players.indexOf(clientPlayer);
        return players.get(indexOfServerPlayer);
    }

    protected void animationDelay() {
        animationDelay(Animator.speedOfCardMoving + 20);
    }


    protected void animationDelay(int duration) {
        if (! testing) {
            try {
                Thread.sleep(duration);
            } catch (InterruptedException i) {
                i.printStackTrace();
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Player) {
            Player player = (Player) obj;
            return  (player.getId() == this.getId()) &&
                    (player.getId() != -1) &&
                    (player.getName().equals(this.getName()));
        }
        return false;
    }
}
