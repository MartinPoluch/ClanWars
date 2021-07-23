package GamePlay;

import GamePlay.Players.*;
import GamePlay.Players.Character;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

/**
 * Trieda reprezentuje spustenú hru. Hra po spustený rozdelí hráčov náhodne do tímov, náhodne im priradií postavy a
 * náhodne sa určí hráč ktorý začne hru. Trieda obsahuje všetky metódy ktoré sú potrené k priebehu hry.
 * Obsahuje metódu pre vrátenie referencie na hráča ktorý  je ako ďalší na ťahu, metódu pre nájdenie vítazného tímu, atď ...
 */
public class Game implements Serializable {

    private List<Player> players;
    private UUID id;
    private Player playerOnTurn;
    private Team winningTeam;
    private URL urlOfFolder;
    private boolean randomPositions;

    public Game(GameSetting setting, boolean randomPosition) {
        this.randomPositions = randomPosition;
        this.urlOfFolder =  getClass().getProtectionDomain().getCodeSource().getLocation();
        this.players = new ArrayList<>();
        this.id = setting.getId();
        this.winningTeam = null;
        initPlayers(setting);
        Random generator = new Random();
        int randomIndex = generator.nextInt(players.size());
        this.playerOnTurn = players.get(randomIndex);
    }

    public Game(GameSetting setting) {
        this(setting, true);
    }

    private void initPlayers(GameSetting setting) {
        List<Player> shuffled = setting.getPlayers();
        if (randomPositions) {
            Collections.shuffle(shuffled);
        }
        List<Character> characters = getCharacters(setting.getNumberOfWaitingPlayers());
        int sizeOfTeam = setting.getNumberOfWaitingPlayers() / setting.getNumberOfTeams();
        List<Team> teams = getTeams(setting.getNumberOfTeams(), sizeOfTeam);
        for (int i = 0; i < setting.getNumberOfWaitingPlayers(); i++) {
            Player player = shuffled.get(i);
            player.startPlaying(teams.get(i), characters.get(i));
            players.add(player);
        }
    }

    private List<Character> getCharacters(int numberOfPlayers) {
        List<Character> allCharacters = readCharactersFromFile();
        if (allCharacters.size() < 8) {
            allCharacters.clear(); // v pripade ze v subore nie je aspon 8 postav tak tie to postavy ignorujeme
            allCharacters.add(new Character("Hannibal", 90, 0, 0, 0, 1));
            allCharacters.add(new Character("Caesar", 100, 5, 0, 0, 0));
            allCharacters.add(new Character("Trajan", 120, -5, 5, 0, 0));
            allCharacters.add(new Character("Alexander", 90, 2, 0, 1, 0));
            allCharacters.add(new Character("Leonidas", 100, 0, 8, 0, 0));
            allCharacters.add(new Character("Attila", 100, 0, 0, 1, 0));
            allCharacters.add(new Character("Cyrus", 90, 0, 0, 2, 0));
            allCharacters.add(new Character("Antony", 90, 5, 5, 0, 0));
        }
        Collections.shuffle(allCharacters);
        return allCharacters.subList(0, numberOfPlayers);
    }

    private List<Character> readCharactersFromFile() {
        List<Character> characters = new ArrayList<>();
        try {
            URL fileUrl = new URL(urlOfFolder, "Files/characters.txt", null);
            File file = Paths.get(fileUrl.toURI()).toFile();
            Scanner scanner = new Scanner(file);

            for (int ignoreLine = 0; ignoreLine < 4; ignoreLine++) {
                scanner.nextLine();
            }

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                try {
                    final String separator = " : ";
                    String[] input = line.split(separator);
                    if (input.length == 6) {
                        String name = input[0];
                        int maxHealth = Integer.parseInt(input[1]);
                        int damage = Integer.parseInt(input[2]);
                        int protection = Integer.parseInt(input[3]);
                        int offensiveRange = Integer.parseInt(input[4]);
                        int defensiveRange = Integer.parseInt(input[5]);
                        Character character = new Character(name, maxHealth, damage, protection, offensiveRange, defensiveRange);
                        characters.add(character);
                    }
                    else {
                        throw new IllegalArgumentException("Wrong input");
                    }
                } catch (Exception e) {
                    System.err.println("Wrong input in file with characters, input: " + line);
                }

            }
        } catch (Exception io) {
            System.err.println("File with characters does not exist");
        }
        return characters;
    }


    private List<Team> getTeams(int numberOfTeams, int sizeOfTeam) {
        List<Team> allTeams = new ArrayList<>();
        allTeams.add(new Team("Rome", "/resources/teams/Rome.png"));
        allTeams.add(new Team("Carthage", "/resources/teams/Carthage.png"));
        allTeams.add(new Team("Sparta", "/resources/teams/Sparta.png"));
        allTeams.add(new Team("Athens", "/resources/teams/Athens.png"));
        allTeams.add(new Team("Egypt", "/resources/teams/Egypt.png"));
        allTeams.add(new Team("Macedon", "/resources/teams/Macedon.png"));
        allTeams.add(new Team("Galatia", "/resources/teams/Galatia.png"));
        allTeams.add(new Team("Suebi", "/resources/teams/Suebi.png"));
        Collections.shuffle(allTeams);
        List<Team> uniqueTeam = allTeams.subList(0, numberOfTeams);
        List<Team> duplicatedTeams = new ArrayList<>();
        for (int duplicate = 0; duplicate < sizeOfTeam; duplicate++) {
            duplicatedTeams.addAll(uniqueTeam);
        }
        return duplicatedTeams;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    /**
     * Vráti hráča ktorý je ako ďalší na ťahu.
     * @return ďalší na ťahu
     */
    public Player getPlayerOnTurn() {
        shiftPlayerOnTurn();
        return playerOnTurn;
    }

    /**
     * Nahradí hráča MediumBotom. Využíva sa keď, hráč predčasne opustí hru.
     * @param player hráč ktorý má byť nahradený botom
     */
    public void replaceByBot(Player player) {
        int position = players.indexOf(player);
        if (position != -1) {
            players.remove(player);
            Player bot = new MediumBot(player);
            players.add(position, bot);
        }
    }

    private void shiftPlayerOnTurn() {
        if ((playerOnTurn == null) || (players.indexOf(playerOnTurn) == -1) || players.isEmpty()) {
            // defenzivne programovanie
            System.err.println("Error, cannot shift playerOnTurn: " + playerOnTurn + " \nnumber of players: " + players.size());
            return;
        }
        int indexOfPlayer = players.indexOf(playerOnTurn);
        if (indexOfPlayer == 0) {
            indexOfPlayer = players.size() - 1;
        }
        else {
            indexOfPlayer--;
        }
        playerOnTurn = players.get(indexOfPlayer);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Game game = (Game) o;
        return Objects.equals(id, game.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * @return vráti hráča ktorý stratil včetky svoje životy.
     */
    public Player eliminatedPlayer() {
        for (Player player : players) {
            Health health = player.getHealth();
            if (health.getActualHealth() <= 0) {
                return player;
            }
        }
        return null;
    }

    /**
     * Zistí či je hra už ukončená, teda či sa v hre nachádza už len jeden tím.
     * Upraví atribút winningTeam.
     * @return ak existuje vítaz vráti vítazný tím, iinak null
     */
    public Team winner() {
        winningTeam = null;
        for (Player player : players) {
            if (winningTeam == null) {
                // inicializacia
                winningTeam = player.getTeam();
            }
            else if (! winningTeam.equals(player.getTeam())){
                // v hre sa este nachadzaju dva timy, hra este neskoncila
                winningTeam = null;
                return null;
            }
        }
        return winningTeam;
    }

    public boolean noWinner() {
        return (winningTeam == null);
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        output.append("\n");
        for (Player player : players) {
            output.append(player);
            output.append("\n");
        }
        return output.toString();
    }
}
