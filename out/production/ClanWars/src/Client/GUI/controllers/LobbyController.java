package Client.GUI.controllers;

import GamePlay.GameSetting;
import GamePlay.Players.*;
import GamePlay.LobbyChange;
import GamePlay.GSChange;
import Client.Communication.Server;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;

/**
 * Trieda LobbyController
 */
public class LobbyController {

    @FXML private MenuItem newGameItem;
    @FXML private MenuItem tutorialItem;
    @FXML private MenuItem aboutItem;
    @FXML private MenuItem exitItem;

    @FXML private Button gameTransitionBtn; // button na zmenu hry (join/leave)
    @FXML private Button newGameBtn;
    @FXML private Button tutorialBtn;

    @FXML private TableColumn<GameSetting, String> gameNameCol;
    @FXML private TableColumn<GameSetting, Integer> waitingPlayersCol;
    @FXML private TableColumn<GameSetting, Integer> capacityCol;
    @FXML private TableColumn<GameSetting, Integer> teamsCol;
    @FXML private TableView<GameSetting> gamesTab;


    @FXML private Label nameInfoVal;
    @FXML private Label capacityInfoVal;
    @FXML private Label teamsInfoVal;
    @FXML private Label botLevelInfoVal;

    @FXML private TableView<Player> playersTab;
    @FXML private TableColumn<Player, String> playerNameCol;
    @FXML private TableColumn<Player, String> playerTypeCol;

    @FXML private Label userName;

    private ObservableList<GameSetting> waitingGames;

    private GameSetting myGame;
    private Human me;
    private Server server;

    // tie to atributy (static) sluzia len ako idcka pre css, v kode sa nepouzivaju
    @FXML private Label nameInfoStaticText;
    @FXML private Label capacityInfoStaticText;
    @FXML private Label teamsInfoStaticText;
    @FXML private Label botLevelInfoStaticText;
    @FXML private Label gameInfoStaticText;
    @FXML private Label userNameStaticText;


    /**
     * Inicializ??cia sc??ny. Nastavenie akci?? jednotliv??mi buttonom. Zavolanie ??al????ch inicializa??n??ch met??d.
     */
    public void initialize(){
        playersTab.setSelectionModel(null);
        initGamesTab();
        playerNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        playerNameCol.setSortable(false);
        playerTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        playerTypeCol.setSortable(false);
        myGame = null;
        gameTransitionBtn.setOnAction(event -> joinGameOnServer());
        newGameBtn.setOnAction((event) ->  createNewGame());
        tutorialBtn.setOnAction((event) -> openTutorial());

        newGameItem.setOnAction((event) -> createNewGame());
        tutorialItem.setOnAction((event) -> openTutorial());
        aboutItem.setOnAction((event) -> openAbout());
    }

    private void openTutorial() {
        try {
            URL urlOfFolder =  getClass().getProtectionDomain().getCodeSource().getLocation();
            URL fileUrl = new URL(urlOfFolder, "Tutorial.pdf", null);
            File tutorial = Paths.get(fileUrl.toURI()).toFile();
            if (Desktop.isDesktopSupported()) {
                new Thread(() -> {
                    try {
                        Desktop.getDesktop().open(tutorial);
                    } catch (Exception e) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        try {
                            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                            stage.getIcons().add(new Image(getClass().getResource("/resources/Icon.png").toString()));
                        } catch (Exception i) {
                            System.err.println("Icon not found");
                        }
                        alert.setTitle("Error");
                        alert.setHeaderText("Tutorial cannot be open.");
                        alert.setContentText(e.getMessage());
                        alert.showAndWait();
                    }
                }).start();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Tutorial cannot be open.");
                alert.showAndWait();
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Tutorial file not found.");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void openAbout() {
        Stage about = new Stage();
        about.setTitle("About");
        try {
            about.getIcons().add(new Image(getClass().getResource("/resources/Icon.png").toString()));
        } catch (Exception e) {
            System.err.println("Icon not found");
        }
        final int HEIGHT = 550;
        final int WIDTH = 650;
        about.setMinHeight(HEIGHT);
        about.setMinWidth(WIDTH);
        about.setHeight(HEIGHT);
        about.setWidth(WIDTH);
        about.setMaxHeight(HEIGHT);
        about.setMaxWidth(WIDTH);
        about.initModality(Modality.APPLICATION_MODAL);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client/GUI/fxml/aboutScene.fxml"));
        try {
            VBox vBox = loader.load();
            Scene scene = new Scene(vBox);
            scene.getStylesheets().add("/Client/GUI/css/basic.css");
            about.setScene(scene);
            about.show();        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPlayer(Human me) {
        this.me = me;
        userName.setText(me.getName());
    }

    public void setServer(Server server) {
        this.server = server;
        exitItem.setOnAction((event) -> {
            server.endCommunication();
            Stage primaryStage = (Stage) (newGameBtn.getScene().getWindow());
            primaryStage.close();
        });
    }

    /**
     * Met??da spracuv??va prijat?? spr??vy. Jednotliv?? spr??vy vykon??vaj?? zmeny v zozname hier.
     * @param change prijat?? spr??va
     */
    public void changeOfWaitingGames(GSChange change) {
        switch (change.getLobbyChange()) {
            case CREATE:
                waitingGames.add(change.getGameSetting());
                System.out.println("Created new game by " + change.getInitiator().getName());
                break;
            case JOIN:
                joinGameOnClient(change);
                break;
            case LEAVE:
                leaveGameOnClient(change);
                break;
            case DESTROY:
                waitingGames.remove(change.getGameSetting());
                System.out.println("Removed game " + change.getGameSetting());
                break;
            default:
                 System.out.println("Unknown request");
        }
    }

    private void joinGameOnClient(GSChange change) {
        int indexOfGame = waitingGames.indexOf(change.getGameSetting());
        if (indexOfGame != -1) {

            GameSetting clientGame = waitingGames.get(indexOfGame);
            clientGame.addPlayer(change.getInitiator());

            if (change.getInitiator().equals(me)) {
                myGame = clientGame;
                System.out.println("You joined game");
                gameTransitionBtn.setText("Leave game");
                gameTransitionBtn.setOnAction(leave -> leaveGameOnServer());

            }
            refreshTables();
        }
        else {
            System.out.println("game " + change.getGameSetting() + " is unknown");
        }
    }

    private void leaveGameOnClient(GSChange change) {
        int indexOfGame = waitingGames.indexOf(change.getGameSetting());
        if (indexOfGame != -1) {

            GameSetting clientGame = waitingGames.get(indexOfGame);
            clientGame.removePlayer(change.getInitiator());

            if (change.getInitiator().equals(me)) {
                myGame = null;
                System.out.println("You left game " + clientGame);
                gameTransitionBtn.setText("Join game");
                gameTransitionBtn.setOnAction((join) -> joinGameOnServer());
            }

            System.out.println("Player " + change.getInitiator().getName() + " left game " + clientGame.getName());
            refreshTables();
        }
        else {
            System.out.println("game " + change.getGameSetting() + " is unknown");
        }
    }

    private void refreshTables() {
        GameSetting selectedGame =  gamesTab.getSelectionModel().getSelectedItem();
        updateInfoOfSelectedGame(selectedGame);
        gamesTab.setItems(waitingGames);
        gamesTab.refresh();
    }

    /**
     * Ininicializ??cia tabu??ky hier, jednotliv?? st??pce sa inicializuj?? na z??klade triedy GameSetting.
     * Nastavenie listenera ktor?? bude sledova?? aktu??lne zvolen?? riadok tabu??ky a na z??klade tohto riadku bude meni??
     * zobrazovanie podrobn?? inform??cie o hre a bud?? dynamicky meni?? n??zov a akciu gameTranstitionBtn buttonu.
     */
    private void initGamesTab() {
        waitingGames = FXCollections.observableArrayList();
        gameNameCol.setCellValueFactory(new PropertyValueFactory<GameSetting, String>("name"));
        waitingPlayersCol.setCellValueFactory(new PropertyValueFactory<GameSetting, Integer>("numberOfWaitingPlayers"));
        capacityCol.setCellValueFactory(new PropertyValueFactory<GameSetting, Integer>("capacity"));
        teamsCol.setCellValueFactory(new PropertyValueFactory<GameSetting, Integer>("numberOfTeams"));
        gamesTab.setItems(waitingGames);

        gamesTab.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            updateInfoOfSelectedGame(newSelection);
            if (newSelection.equals(myGame)) {
                gameTransitionBtn.setText("Leave game");
                gameTransitionBtn.setOnAction(leave -> leaveGameOnServer());
            } else {
                gameTransitionBtn.setText("Join game");
                gameTransitionBtn.setOnAction(join -> joinGameOnServer());
            }


        });
    }

    /**
     * Zobrazuje podrobn?? inform??cie o aktu??lne zvolenej hre (meno, kapacita, po??et t??mov, ??rove?? botov).
     * Zobrazuje zoznam hr????ov ktor?? ??akaj?? vo zvolenej hre.
     * Ku ka??d??mu hr????ovi zobrazuje jeho meno a inform??ciu o tom ??i je to ??lovek alebo bot.
     * @param selectedGame aktu??lne zvolen?? hra v tabu??ke hier
     */
    private void updateInfoOfSelectedGame(GameSetting selectedGame) {
        if (selectedGame != null) {
            nameInfoVal.setText(selectedGame.getName());
            capacityInfoVal.setText(Integer.toString(selectedGame.getCapacity()));
            teamsInfoVal.setText(Integer.toString(selectedGame.getNumberOfTeams()));
            botLevelInfoVal.setText(findBotsLevel(selectedGame.getPlayers()));
            ObservableList<Player> players = FXCollections.observableArrayList();
            playersTab.setItems(players);
            players.addAll(selectedGame.getPlayers());
            playersTab.refresh();
        }
    }

    /**
     * Na z??klade zoznamu hr????ov zist?? na akej ??rovni (obtia??nos??) hraj?? botovia.
     * Pracuje s predpokladom ??e v??etci botovia hraj?? na rovnakej ??rovni.
     * @param players zoznam hr????ov danej hry (??udia + botovia)
     * @return obtia??nos?? ktor?? je nastaven?? botom. Ak v hre nie s?? ??iadny botovia tak vr??ti "no bots"
     */
    private String findBotsLevel(List<Player> players) {
        String botLevel = "no bots";
        for (Player player : players) {
            if (player instanceof EasyBot) {
                botLevel = "easy";
                break;
            }
            else if (player instanceof MediumBot) {
                botLevel = "medium";
                break;
            }
            else if (player instanceof HardBot) {
                botLevel = "hard";
                break;
            }
        }
        return botLevel;
    }

    /**
     * Otvor?? nov?? okno (Stage) ktor?? obsahuje formul??r pre vytvorenie novej hry.
     * Ak u????vate?? ??spe??ne vyplnil formul??r tak po zatvoren?? okna sa z formul??ra z??ska in??tancia triedy GameSetting
     * na z??klade ktorej sa vytvor??nov?? riadok v tabu??ke hier.
     * Ak u????vate?? zru??il formul??r (cancel) tak sa ni?? nezmen??.
     */
    public void createNewGame() {
        Stage newGameStage = new Stage();
        newGameStage.setTitle("Create new game");
        try {
            newGameStage.getIcons().add(new Image(getClass().getResource("/resources/Icon.png").toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        newGameStage.setHeight(CreateGameController.STAGE_HEIGHT);
        newGameStage.setMaxHeight(CreateGameController.STAGE_HEIGHT);
        newGameStage.setMinHeight(CreateGameController.STAGE_HEIGHT);
        newGameStage.setMaxWidth(CreateGameController.STAGE_WIDTH);
        newGameStage.setMinWidth(CreateGameController.STAGE_WIDTH);
        newGameStage.setWidth(CreateGameController.STAGE_WIDTH);
        newGameStage.initModality(Modality.APPLICATION_MODAL); // ak je otvorene nove okno tak nie je mozne pouzivat okno
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Client/GUI/fxml/CreateGameForm.fxml"));
            Parent root = fxmlLoader.load();
            root.getStylesheets().add("/Client/GUI/css/basic.css");
            root.getStylesheets().add("/Client/GUI/css/CreateGameStyles.css");
            newGameStage.setScene(new Scene(root));
            CreateGameController createGame = fxmlLoader.getController();
            newGameStage.showAndWait();

            GameSetting newGame = createGame.getGameSetting();
            if (newGame != null) {
                GSChange gsChange = new GSChange(newGame, me, LobbyChange.CREATE);
                server.send(gsChange);
            }

        } catch (IOException io) {
            System.err.println("Own Exception: " + io);
        }
    }

    public void setGames(List<GameSetting> games) {
        waitingGames.clear();
        waitingGames.addAll(games);
    }

    public void joinGameOnServer() {
        GameSetting selectedGame = gamesTab.getSelectionModel().getSelectedItem();
        if (selectedGame != null) {
            if (myGame != null) {
                leaveGameOnServer();
            }
            GSChange joinedGame = new GSChange(selectedGame, me, LobbyChange.JOIN);
            server.send(joinedGame);
        }
    }

    public void leaveGameOnServer() {
        if (myGame != null) {
            GSChange leftGame = new GSChange(myGame, me, LobbyChange.LEAVE);
            server.send(leftGame);
        }
    }
}
