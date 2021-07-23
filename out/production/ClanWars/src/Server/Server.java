package Server;

import GamePlay.*;
import GamePlay.Players.EasyBot;
import GamePlay.Players.HardBot;
import GamePlay.Players.MediumBot;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

/**
 * Trieda zabezpečuje pripájanie jednotlivých klientov k serveru. V hlavnom vlákne sa nachádza nekonečný cyklus ktorý
 * pripaja pomocou metódy accept nových klientov. Pre každého klienta je vytvorené nové vlákno pomocou ktorého
 * server komunikuje s klientom.
 */
public class Server {

    private ServerSocket serverSocket;
    private final List<GameSetting> waitingGames;
    private final List<Connection> clientInLobbies;

    public Server() {
        int port = 15000;
        try {
            URL urlOfFolder =  getClass().getProtectionDomain().getCodeSource().getLocation();
            URL fileURL = new URL(urlOfFolder, "Files/port.txt", null);
            File fileWithPort = Paths.get(fileURL.toURI()).toFile();
            Scanner scanner = new Scanner(fileWithPort);
            if (scanner.hasNextLine()) {
                port = scanner.nextInt();
            }
        } catch (Exception e) {
            final int defaultPort = 15000;
            port = defaultPort;
        }

        this.waitingGames = new ArrayList<>();
        this.clientInLobbies = new ArrayList<>();
        createDefaultGames();
        try{
            serverSocket = new ServerSocket(port);
            boolean serverIsRunning = true;
            while (serverIsRunning) {
                System.out.println("Waiting for client...");
                Connection connection = new Connection(serverSocket.accept());
                System.out.println("Connecting new client");
                Lobby lobby = new Lobby(connection, waitingGames, clientInLobbies);
                lobby.start();
            }
        } catch (IOException io) {
            System.out.println("Catch exception: " + io);
        }
    }

    private void createDefaultGames() {
        GameSetting g1 = new GameSetting("game 01", 8, 4);
        g1.addPlayer(new EasyBot(1));
        g1.addPlayer(new EasyBot(2));
        g1.addPlayer(new EasyBot(3));
        g1.addPlayer(new EasyBot(4));
        g1.addPlayer(new EasyBot(5));
        g1.addPlayer(new EasyBot(6));
        g1.addPlayer(new EasyBot(7));
        waitingGames.add(g1);
        GameSetting g2 = new GameSetting("test game", 4, 2);
        waitingGames.add(g2);
        waitingGames.add(new GameSetting("best game", 2, 2));
        GameSetting g3 = new GameSetting("test bots", 4, 2);
        g3.addPlayer(new EasyBot(1));
        g3.addPlayer(new EasyBot(2));
        waitingGames.add(g3);

        GameSetting g4 = new GameSetting("medium 4", 4, 2);
        g4.addPlayer(new MediumBot(1));
        g4.addPlayer(new MediumBot(2));
        g4.addPlayer(new MediumBot(3));
        waitingGames.add(g4);

        GameSetting g5 = new GameSetting("hard 4", 4, 2);
        g5.addPlayer(new HardBot(1));
        g5.addPlayer(new HardBot(2));
        g5.addPlayer(new HardBot(3));
        waitingGames.add(g5);

        GameSetting g6 = new GameSetting("hard 2", 2, 2);
        g6.addPlayer(new HardBot(1));
        waitingGames.add(g6);

        GameSetting g7 = new GameSetting("battle royal", 4, 4);
        g7.addPlayer(new MediumBot(1));
        g7.addPlayer(new MediumBot(2));
        g7.addPlayer(new MediumBot(3));
        waitingGames.add(g7);
    }
}
