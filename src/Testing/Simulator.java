package Testing;

import GamePlay.Deck;
import GamePlay.Game;
import GamePlay.GameSetting;
import GamePlay.Players.*;
import Server.Connection;

import java.io.IOException;
import java.util.*;

public class Simulator {

    private Game game;
    private Deck deck;
    private int[] wins;
    private Difficulty[] difficulties;
    private List<TestFailed> failedTests;
    private int sizeOfTeam;

    private final int TEAM_SHIFT = 10;

    public Simulator(Difficulty team1, Difficulty team2, int sizeOfTeam) {
        this.sizeOfTeam = sizeOfTeam;
        this.wins = new int[2];
        this.difficulties = new Difficulty[2];
        this.difficulties[0] = team1;
        this.difficulties[1] = team2;
        this.failedTests = new LinkedList<>();
    }

    private void createGame() throws IOException {
        final int NUM_OF_TEAMS = 2;
        GameSetting setting = new GameSetting("simulation game", sizeOfTeam * NUM_OF_TEAMS, NUM_OF_TEAMS);
        for (int botId = 0; botId < sizeOfTeam; botId++) {
            Bot bot1 = createBot(difficulties[0], botId);
            setting.addPlayer(bot1);
            Bot bot2 = createBot(difficulties[1], botId + TEAM_SHIFT);
            setting.addPlayer(bot2);
        }

        this.game = new Game(setting, false);
        this.deck = new Deck();
        Map<Player, Connection> opponents = new HashMap<>();
        for (Player player : game.getPlayers()) {
            player.setPlayers(game.getPlayers());
            player.setDeck(deck);
            player.setOpponents(opponents); // len kvoli kontabilite
            player.pickCard();
            player.pickCard();
            player.pickCard();
        }
    }

    private Bot createBot(Difficulty difficulty, int id) {
        switch (difficulty) {
            case EASY:{
                return new EasyBot(id, true);
            }
            case MEDIUM:{
                return new MediumBot(id, true);
            }
            case HARD:{
                return new HardBot(id, true);
            }
            default: {
                return null;
            }
        }
    }

    public void simulate(int numOfReplications) {
        for (int replication = 0; replication < numOfReplications; replication++) {
            try {
                createGame();
                int turns = 0;
                while (game.noWinner()) {
                    turns++;
                    Player playerOnTurn = game.getPlayerOnTurn();
                    playerOnTurn.pickCard();
                    playerOnTurn.pickCard();
                    playerTurn(playerOnTurn);
                }
                if (replication % 1000 == 0) {
                    System.out.println(replication);
                }
                //System.out.print(replication + ". " + " turns: " + turns);
                updateWinner();
            } catch (Exception exception) {
                TestFailed fail = new TestFailed(exception.getMessage(), exception.getCause(),  game, replication);
                failedTests.add(fail);
            }
        }
    }

    private void playerTurn(Player playerOnTurn) throws Exception {
        while (true) {
            boolean playerEndHisMove = ! playerOnTurn.move();
            if (playerEndHisMove) {
                return;
            }
            else {
                Player eliminated = game.eliminatedPlayer();
                if (eliminated != null) {
                    eliminated.throwAllCards();
                    game.removePlayer(eliminated);
                    Team winner = game.winner();
                    if (winner != null) {
                        return;
                    }
                }
            }
        }
    }

    private void updateWinner() {
        Team winner = game.winner();
        if (winner != null) {
            Player player = game.getPlayerOnTurn();
            if (player.getId() < TEAM_SHIFT) {
                wins[0] = wins[0] + 1;
                //System.out.println(" winner: " + difficulties[0]);
            }
            else {
                wins[1] = wins[1] + 1;
                //System.out.println(" winner: " + difficulties[1]);
            }
        }
    }

    public void printResult() {
        System.out.println("\n----------------------------------------------------------");
        for (int team = 0; team < difficulties.length; team++) {
            System.out.println(difficulties[team] + " wins: " + wins[team]);
        }
        System.out.println("first team win percentage: " + (((double)wins[0]/(double)(wins[0]+wins[1])) * 100 ) + " %");

        System.out.println("----------------------------------------------------------");
        System.out.println("Errors: ");
        for (TestFailed testFailed : failedTests) {
            System.out.println(testFailed);
        }
    }
}
