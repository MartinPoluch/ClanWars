package Client.Communication;

import Client.GUI.controllers.LobbyController;
import GamePlay.GSChange;
import GamePlay.GameChange;
import GamePlay.GameSetting;
import GamePlay.Players.Human;
import GamePlay.Players.Player;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 * Trieda Lobby prijma správy od servera. Tieto správy sú následne poslané triede LobbyController ktorá zabezpčuje zmenu GUI.
 *
 */
public class Lobby implements  Runnable{

    private static final int STAGE_HEIGHT = 620;
    private static final int STAGE_WIDTH = 850;

    private Server server;
    private LobbyController lobbyController;
    private Stage primaryStage;
    private Human me;
    private List<GameSetting> games;


    public Lobby(Human playerWithoutID, Stage primaryStage, Server server) {
        this.server = server;
        this.primaryStage = primaryStage;
        this.games = new ArrayList<>();
        introducing(playerWithoutID);
        loadAllGames();
        showLobbyScene();
    }

    private void showLobbyScene() {
        primaryStage.setOnCloseRequest(close -> server.endCommunication());
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Client/GUI/fxml/LobbyScene.fxml"));
            Parent root = fxmlLoader.load();
            primaryStage.setTitle("Lobby");
            primaryStage.setHeight(STAGE_HEIGHT);
            primaryStage.setMaxHeight(STAGE_HEIGHT);
            primaryStage.setMinHeight(STAGE_HEIGHT);
            primaryStage.setMaxWidth(STAGE_WIDTH);
            primaryStage.setMinWidth(STAGE_WIDTH);
            primaryStage.setWidth(STAGE_WIDTH);
            Scene mainScene = new Scene(root);
            mainScene.getStylesheets().add("Client/GUI/css/basic.css");
            mainScene.getStylesheets().add("Client/GUI/css/LobbyStyle.css");
            this.lobbyController = fxmlLoader.getController();
            lobbyController.setPlayer(me);
            lobbyController.setServer(server);
            lobbyController.setGames(games);
            primaryStage.setScene(mainScene);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    /**
     * Poslanie inštancie hráča serveru. Server priradí k inštancii unikátne ID a pošle inštanciu späť.
     * @param playerWithoutID inštancia daného hráča
     */
    private void introducing(Human playerWithoutID) {
        try{
            server.send(playerWithoutID);
            me = (Human) server.receive();
        } catch (SocketException closedSocket) {
            closedSocket.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Prijatie zoznamu hier od servera.
     */
    private void loadAllGames() {
        try {
            Object message = server.receive();
            if (message instanceof ArrayList) {
                games.clear();
                games = (ArrayList<GameSetting>) message;
            }
            else {
                System.out.println("Unknown message: " + message);
            }
        } catch (SocketException closedSocket) {
            System.out.println("Lobby was disconnected.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Táto metóda zabezpečuje prijatie a spracovanie správ zo servera.
     * Metóda sa spustí po prihlásení do Lobby a končí odchodom z lobby.
     */
    @Override
    public void run() {
        while (true) {
            Object receivedMsg = null;
            try {
                receivedMsg = server.receive();
                System.out.println("received: " + receivedMsg);

                if (receivedMsg instanceof GSChange) {
                    GSChange changedGame = (GSChange) receivedMsg;
                    System.out.println("received GSChange message" + changedGame);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            lobbyController.changeOfWaitingGames(changedGame);
                        }
                    });
                } else if (receivedMsg instanceof List) {
                    List<Player> players = (List<Player>) receivedMsg;
                    server.send(GameChange.START_OF_GAME);
                    server.setStopSending(true);
                    GameCommunicator gameCommunicator = new GameCommunicator(server, primaryStage, me, players);
                    GameChange gameResult = gameCommunicator.communication();
                    recreateLobbyAfterGame(gameResult);
                } else {
                    System.out.println("Unknown message: " + receivedMsg);
                }
            } catch (SocketException closedSocket) {
                System.out.println("Lobby was disconnected.");
                break;
            } catch (Exception e) {
                server.send("end");
                System.out.println("end");
                break;
            }
        }
    }

    /**
     * Po ukončení hry je potrebné znova otvoriť Lobby okno. Toto zabezpečuje táto metóda.
     * @param gameResult
     */
    private void recreateLobbyAfterGame(GameChange gameResult) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {

            }
        });

        Human newMe = new Human(me.getName());
        server.setStopSending(false);
        introducing(newMe);
        loadAllGames();

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Alert endOfGameInfo = new Alert(Alert.AlertType.INFORMATION);
                try {
                    DialogPane dialogPane = endOfGameInfo.getDialogPane();
                    dialogPane.getStylesheets().add(getClass().getResource("/Client/GUI/css/basic.css").toExternalForm());
                    dialogPane.getStylesheets().add(getClass().getResource("/Client/GUI/css/alertStyles.css").toExternalForm());
                    Stage stage = (Stage) dialogPane.getScene().getWindow();
                    stage.getIcons().add(new Image(getClass().getResource("/resources/Icon.png").toString()));
                } catch (Exception e) {
                    System.err.println("CSS not found");
                }

                if (gameResult == GameChange.WIN) {
                    endOfGameInfo.setTitle("You won");
                    endOfGameInfo.setHeaderText("Congratulation your team won the game");
                }
                else if (gameResult == GameChange.LOSE){
                    endOfGameInfo.setTitle("You lost");
                    endOfGameInfo.setHeaderText("You were eliminate from the game");
                }
                else if (gameResult == GameChange.ERROR) {
                    endOfGameInfo.setTitle("Error");
                    endOfGameInfo.setHeaderText("Error occured.");
                }
                endOfGameInfo.showAndWait();
                showLobbyScene();
            }
        });
    }
}
