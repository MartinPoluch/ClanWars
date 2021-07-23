package GamePlay;


import GamePlay.Players.Player;

import java.io.Serializable;
import java.util.*;

/**
 * Nastavenie hry. Každa hra musí mať definovné meno, kapacitu, počet tímov.
 */
public class GameSetting implements Serializable{

    public static final int MAX_PLAYERS = 8;

    private final String name;
    private final int capacity;
    private final int numberOfTeams;
    private final UUID id;
    private List<Player> players;

    public GameSetting(String name, int capacity, int numberOfTeams) {
        this.name = name;
        this.capacity = capacity;
        this.numberOfTeams = numberOfTeams;
        this.players = new ArrayList<>();
        id = UUID.randomUUID();
    }

    public boolean addPlayer(Player player){
        if (players.size() < capacity) {
            players.add(player);
            return true;
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getNumberOfTeams() {
        return numberOfTeams;
    }

    public int getNumberOfWaitingPlayers() {
        return players.size();
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public UUID getId() {
        return id;
    }

    public List<Player> getPlayers() {
        return players;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        GameSetting that = (GameSetting) obj;
        return Objects.equals(id, that.id) && (name.equals(that.name)) && (capacity == that.capacity);
    }

    @Override
    public String toString() {
        return "GameSetting{" +
                "name='" + name + '\'' +
                ", capacity=" + capacity +
                ", numberOfTeams=" + numberOfTeams +
                ", players=" + players +
                '}';
    }
}
