package Server;

import GamePlay.*;
import GamePlay.Players.Human;
import GamePlay.Players.Player;


import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 * Vlákno ktoré reprezentuje hráča ktorý čaká na spustenie hry. Toto vlákno zabezpečuje komunikáciu medzi klientom a serverom.
 */
public class Lobby extends Thread{

    private final List<GameSetting> waitingGames;
    private final List<Connection> clientsInLobby;
    private Connection connection;

    private List<Connection> clientsInStartedGame;

    /**
     * @param connection pripojenie (socket) na konkrétneho hráča (klienta)
     * @param waitingGames zoznam všetkých hier ktoré čakajú na spustenie
     * @param clientsInLobby zoznam všetkých hráčov ktorý čakajú v lobby
     */
    public Lobby(Connection connection, List<GameSetting> waitingGames, List<Connection> clientsInLobby) {
        this.connection = connection;
        this.waitingGames = waitingGames;
        this.clientsInLobby = clientsInLobby;
        this.clientsInStartedGame = new ArrayList<>();
    }

    /**
     * Pripojenie nového hráča do lobby.
     */
    private void joinLobby() throws IOException{
        synchronized (clientsInLobby) {
            clientsInLobby.add(connection);
        }

        synchronized (waitingGames) {
            connection.sendMessage(waitingGames);
        }
    }

    @Override
    public void run() {
        boolean endOfProgram = true;
        try{
            connection.initConnection(this.getId() + 10); // prve id-cka budeu vyhradene pre botov
            joinLobby();
            while (true) {
                Object message = connection.receiveMessage();
                if (message instanceof GSChange) {
                    GSChange changedGame = (GSChange) message;
                    if (doChangeOfGame(changedGame)) {
                        checkIfGameIsFull(changedGame.getGameSetting());
                    }
                }
                else if (message instanceof GameChange) {
                    GameChange change = (GameChange) message;
                    if (change == GameChange.START_OF_GAME) {
                        endOfProgram = false;
                        break;
                    }
                }
                else if (message instanceof String) {
                    String strMessage = (String) message;
                    if (strMessage.equals("end")) {
                        System.out.println("LobbyCommunication " + connection.getPlayer().getName() + " id: " + connection.getPlayer().getId() + " left server");
                        endOfProgram = true;
                        break;
                    }
                }
                else {
                    System.out.println("Unknown message " + message);
                }
            }
        } catch (SocketException disconnectedClient) {
            disconnectedClient.printStackTrace();
            //System.out.println("LobbyCommunication " + connection.getPlayer().getName() + " id: " + connection.getPlayer().getId() + " was unexpectedly disconnected");
        } catch (Exception io) {
            io.printStackTrace();
           // System.out.println("Caught exception client " + connection.getPlayer().getName() + " id: " + getId() + " :" + io);
        }
        finally {
            try {
                if (endOfProgram) {
                    removeClientFromLobby();
                    removePlayerFromWaitingGames();
                    connection.closeConnection();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkIfGameIsFull(GameSetting changedGame) throws IOException{
        GameSetting serverGame;
        boolean gameIsFull = false;
        synchronized (waitingGames) {
            int indexOfGame = waitingGames.indexOf(changedGame);
            serverGame = waitingGames.get(indexOfGame);
            if (serverGame.getNumberOfWaitingPlayers() == serverGame.getCapacity()) {
                gameIsFull = removeGameFromWaitingGames(serverGame);
            }
        }
        if (gameIsFull) {
            movePlayersFromLobbyToSameGame(serverGame);
            showGameToPlayersOfGame(serverGame);
        }
    }

    private boolean removeGameFromWaitingGames(GameSetting serverGame) throws IOException{
        boolean removed = false;
        synchronized (waitingGames) {
            removed = waitingGames.remove(serverGame);
        }
        if (removed) {
            GSChange destroyedGame = new GSChange(serverGame, null, LobbyChange.DESTROY);
            sendChangeToClients(destroyedGame);
        }
        return removed;
    }

    private void movePlayersFromLobbyToSameGame(GameSetting fullGame) {
        clientsInStartedGame = new ArrayList<>();
        for (Player player : fullGame.getPlayers()) {
            if (player instanceof Human) {
                Human human = (Human) player;
                synchronized (clientsInLobby) {
                    for (Connection client : clientsInLobby) {
                        if (client.getPlayer().getId() == human.getId()) {
                            clientsInStartedGame.add(client);
                            //client.getClientInGame().setOpponents(clientsInSameGame);
                            break;
                        }
                    }
                }
            }
        }
        synchronized (clientsInLobby) {
            for (Connection clientInLobbyGame : clientsInStartedGame) {
                clientsInLobby.remove(clientInLobbyGame);
            }
        }
    }

    private void showGameToPlayersOfGame(GameSetting setting) {
        Game game = new Game(setting);
        for (Connection client : clientsInStartedGame) {
            try {
                //client.getClientInGame().setGame(game);
                client.sendMessage(game.getPlayers());
            } catch (IOException io) {
                io.printStackTrace();
            }
        }
        GameManager gameManager = new GameManager(waitingGames, clientsInLobby, game, clientsInStartedGame);
        gameManager.start();
    }

    private void removeClientFromLobby() throws IOException{
        synchronized (clientsInLobby) {
            clientsInLobby.remove(connection);
        }
    }

    private void removePlayerFromWaitingGames() throws IOException{
        GameSetting leftGame = null;
        synchronized (waitingGames) {
            for (GameSetting gameSetting : waitingGames) {
                if (gameSetting.getPlayers().contains(connection.getPlayer())) {
                    gameSetting.removePlayer(connection.getPlayer());
                    leftGame = gameSetting;
                    break; // hrac moze byt iba v jednej hre
                }
            }
        }
        if (leftGame != null) {
            GSChange clientDisconnected = new GSChange(leftGame, connection.getPlayer(), LobbyChange.LEAVE);
            sendChangeToClients(clientDisconnected);
        }
    }

    private boolean doChangeOfGame(GSChange changedGame) throws IOException{
        boolean canChange = true;
        synchronized (waitingGames) {
            switch (changedGame.getLobbyChange()) {
                case CREATE:
                    waitingGames.add(changedGame.getGameSetting());
                    System.out.println("New game was created by " + changedGame.getInitiator().getName());
                    break;
                case JOIN:
                {
                    int indexOfGame = waitingGames.indexOf(changedGame.getGameSetting());
                    boolean gameExist = (indexOfGame != -1);
                    if (gameExist) {
                        GameSetting serverGame = waitingGames.get(indexOfGame);
                        canChange = serverGame.addPlayer(changedGame.getInitiator());
                        System.out.println("Player " + changedGame.getInitiator().getName() + " joined " + " game: " + serverGame.getName());
                    }
                    else {
                        canChange = false;
                        System.out.println("GamePlay " + changedGame.getGameSetting() + " does not exist");
                    }
                    break;
                }
                case LEAVE:
                {
                    int indexOfGame = waitingGames.indexOf(changedGame.getGameSetting());
                    boolean gameExist = (indexOfGame != -1);
                    if (gameExist) {
                        GameSetting serverGame = waitingGames.get(indexOfGame);
                        serverGame.removePlayer(changedGame.getInitiator());
                        System.out.println("Player " + changedGame.getInitiator().getName() + " left " + " game: " + serverGame.getName());
                    }
                    else {
                        canChange = false; // nemozem zmenit
                        System.out.println("GamePlay " + changedGame.getGameSetting() + " does not exist");
                    }
                    break;
                }
                default:
                    System.out.println("Unknown game change request");
            }
        }
        if (canChange) {
            sendChangeToClients(changedGame);
        }
        return canChange;
    }

    private void sendChangeToClients(GSChange changedGame) throws IOException{
        synchronized (clientsInLobby) {
            for (Connection clientInLobby : clientsInLobby) {
                clientInLobby.sendMessage(changedGame);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lobby lobby = (Lobby) o;
        return lobby.getId() == this.getId();
    }
}
