package Server;

import GamePlay.*;
import GamePlay.Players.Human;
import GamePlay.Players.Player;
import GamePlay.Players.Team;


import java.io.IOException;
import java.util.*;

/**
 * Trieda zabezpečuje samotný priebeh hry. Trieda obsahuje balíček, hru a zoznam hráčov aj z ich pripojeniami (sockets).
 */
public class GameManager extends Thread{

    private List<GameSetting> waitingGames;
    private List<Connection> clientsInLobby;

    private Deck deck;
    private Game game;
    private Map<Player, Connection> opponents;

    public GameManager(List<GameSetting> waitingGames, List<Connection> clientsInLobby, Game game, List<Connection> clientsInGame) {
        this.waitingGames = waitingGames;
        this.clientsInLobby = clientsInLobby;

        this.game = game;
        this.deck = new Deck();
        this.opponents = new HashMap<>();
        for (Player player : game.getPlayers()) {
            player.setPlayers(game.getPlayers());
            player.setOpponents(opponents);
            player.setDeck(deck);
            for (Connection connection : clientsInGame) {
                if (connection.getPlayer().equals(player)) {
                    opponents.put(player, connection);
                }
            }
        }
    }

    private void waitForAllClient() {
        try {
            sleep(3000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void dealCardsToPlayers() throws IOException{
        for (int i = 0; i < game.getPlayers().size(); i++) {
            Player player = game.getPlayerOnTurn();
            checkAllConnections();
            player.pickCard();
            checkAllConnections();
            player.pickCard();
            checkAllConnections();
            player.pickCard();
        }
    }

    @Override
    public void run() {
        super.run();
        waitForAllClient();
        try {
            dealCardsToPlayers();
            while (game.noWinner() && humanInGame()) {
                checkAllConnections();
                Player playerOnTurn = game.getPlayerOnTurn();
                checkAllConnections();
                playerOnTurn.pickCard();
                checkAllConnections();
                playerOnTurn.pickCard();
                checkAllConnections();
                playerInteraction(playerOnTurn);

//                for (Player player : game.getPlayers()) {
//                    System.out.println(player.getName()+ " has: " + player.getHealth().getActualHealth());
//                    System.out.println("num of cards: " + player.getInventory().size());
//                    System.out.println("weapon: " + player.getItem(0));
//                    System.out.println("shield: " + player.getItem(1));
//                    System.out.println("helm: " + player.getItem(2));
//                    System.out.println("cards: " + player.getInventory());
//                }
//                System.out.println("\n--------------------------------------------");
            }
            System.out.println("END OF GAME");
        }
        catch (IOException | ClassNotFoundException io) {
            System.err.println("End of game error: " + io);
            for (Connection connection : opponents.values()) {
                try {
                    connection.sendMessage(GameChange.ERROR);
                    moveConnectionToLobby(connection);
                } catch (Exception e) {
                    System.err.println("Message not send");
                }
            }
        }
    }

    private void checkAllConnections() {
        List<Player> disconnected = new ArrayList<>(8);
        for (Map.Entry<Player, Connection> entry : opponents.entrySet()) {
            if (! entry.getValue().isConnected()) {
                disconnected.add(entry.getKey());
            }
        }

        for (Player leaver : disconnected) {
            opponents.remove(leaver);
            game.replaceByBot(leaver);
        }
    }

    private void playerInteraction(Player player) throws IOException, ClassNotFoundException{
        while (true) {
            boolean playerEndHisMove = ! player.move();
            checkAllConnections();
            if (playerEndHisMove) {
                break;
            }
            else {
                Player eliminated = game.eliminatedPlayer();
                if (eliminated != null) {
                    removeFromGame(eliminated);
                    Team winner = game.winner();
                    if (winner != null) {
                        informWinners();
                        break;
                    }
                }
            }
        }
    }

    private boolean humanInGame() {
        for (Player player : game.getPlayers()) {
            if (player instanceof Human) {
                return true;
            }
        }
        return false;
    }

    private void informWinners() throws IOException{
        for (Connection winnerConn : opponents.values()) {
            winnerConn.sendMessage(GameChange.WIN); // vsetkym klientom oznamim ze hrac bol eliminovany
            moveConnectionToLobby(winnerConn);
        }

        for (Player winner : game.getPlayers()) {
            if (winner instanceof Human) {
                opponents.remove(winner);
            }
        }
    }

    /**
     * Odstrani hraca z hry a premiestniho do lobby
     * @param eliminated
     * @throws IOException
     */
    private void removeFromGame(Player eliminated) throws IOException{
        eliminated.throwAllCards();
        game.removePlayer(eliminated);
        GameChangeMsg eliminatedMsg = new GameChangeMsg(GameChange.LOSE, eliminated);
        for (Connection liveConn : opponents.values()) {
            if (liveConn.isConnected()) {
                liveConn.sendMessage(eliminatedMsg); // vsetkym klientom oznamim ze hrac bol eliminovany
            }
        }

        if (eliminated instanceof Human) {
            Connection eliminatedConn = opponents.get(eliminated);
            opponents.remove(eliminated); // zrusim spojenie z hracom, hra uz z hracom nemoze komunikovat
            moveConnectionToLobby(eliminatedConn);
        }
    }

    private void moveConnectionToLobby(Connection humanConn) {
        Lobby lobby = new Lobby(humanConn, waitingGames, clientsInLobby);
        lobby.start();
    }
}
