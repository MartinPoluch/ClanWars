package Client.GUI.controllers;

import GamePlay.GameSetting;
import GamePlay.Players.Bot;
import GamePlay.Players.EasyBot;
import GamePlay.Players.HardBot;
import GamePlay.Players.MediumBot;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Trieda reprezentuje logiku formulára, pre vytvorenie novej hry.
 */
public class CreateGameController {

    public static final int STAGE_HEIGHT = 500;
    public static final int STAGE_WIDTH = 700;

    @FXML private Button cancelBtn;
    @FXML private Button createBtn;
    @FXML private TextField nameInput;
    @FXML private Spinner<Integer> capacityInput;
    @FXML private Spinner<Integer> teamsInput;
    @FXML private Spinner<Integer> NPCInput;
    @FXML private Toggle easy;
    @FXML private Toggle medium;
    @FXML private Toggle hard;

    @FXML private Label header;

    private GameSetting gameSetting;

    /**
     * Nastavenie defaultných hodnôt vo folmulári pre vytvorenie novej hry.
     * Nastavenie rozsahu povolených hodnôt pre spinneri.
     */
    public void initialize(){
        nameInput.setText("game");
        capacityInput.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2, GameSetting.MAX_PLAYERS, 4));
        capacityInput.setEditable(false);
        teamsInput.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2, GameSetting.MAX_PLAYERS, 2));
        teamsInput.setEditable(false);
        NPCInput.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, GameSetting.MAX_PLAYERS - 1, 0));
        NPCInput.setEditable(false);
        medium.setSelected(true);
        gameSetting = null;
    }

    /**
     * Zavrie formulár pre nastavenie novej hry. Nebudú vykonané žíadne zmeny
     */
    public void closeStage() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }

    /**
     * Kontrola či kombinácia zadaných hodnôt je valídna a je možné z daných nastavení vytvoriť novú hru.
     * Ak nie je možné vytvoriť hru tak zobrazí dialógové okno kde užívateľovi napíše dôvod prečo nie je možné z danými
     * nastaveniami vytvoriť novú hru.
     * @return TRUE ak je možné vytvoriť hru
     */
    private boolean checkUserInput() {
        String errorMsg = "";
        final int MAX_NAME_LENGTH = 15;
        boolean nameIsValid = (1 <= nameInput.getText().length()) && (nameInput.getText().length() <= MAX_NAME_LENGTH);
        if (! nameIsValid) {
            errorMsg = "Name length must be between 1 and " + MAX_NAME_LENGTH + " characters";
        }
        boolean sufficientCapacity = (capacityInput.getValue() > NPCInput.getValue()); // nie je privela botov
        if (! sufficientCapacity) {
            errorMsg = "Number of selected bot's is not suitable for selected capacity";
        }
        boolean teamEquality = (capacityInput.getValue() % teamsInput.getValue() == 0); // rovnako velke timy
        if (! teamEquality) {
            errorMsg = "Each team must have same size.\n"
                    + capacityInput.getValue() + " player/s cannot be equally divided into " +  teamsInput.getValue() + " teams.";
        }
        boolean validNumberOfTeams = (teamsInput.getValue() <= capacityInput.getValue()); // timov nemoze byt viacej ako hracov
        if (! validNumberOfTeams) {
            errorMsg = "Number of teams cannot be bigger then number of all players.";
        }
        if (nameIsValid && sufficientCapacity && teamEquality && validNumberOfTeams) {
            return true;
        }
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            DialogPane dialogPane = alert.getDialogPane();
            Stage alertStage = (Stage) dialogPane.getScene().getWindow();
            try {
                alertStage.getIcons().add(new Image(getClass().getResource("/resources/Icon.png").toString()));
                dialogPane.getStylesheets().add(getClass().getResource("/Client/GUI/css/basic.css").toExternalForm());
                dialogPane.getStylesheets().add(getClass().getResource("/Client/GUI/css/alertStyles.css").toExternalForm());
            } catch (Exception e) {
                System.err.println("Icon not found");
            }
            alert.setTitle("Cannot create new game");
            alert.setHeaderText(errorMsg);
            alert.showAndWait();
            return false;
        }
    }

    /**
     * Ak užívateľ zadal do formulára korektné nastavenia hry
     *     tak na základe týchto nastaveníinicializuje hodnotu atribútu gameSetting.
     *     Do hry automaticky pridá botov (ak ich užívateľ požadoval) ktorý budú hrať požadovanej úrovni (úroveň bola zadaná).
     *     Zatvorí formulár.
     * Ak užívateľ nezadal korektné nastavenia hry, tak metóda nevykoná žiadnu akciu.
     */
    public void create() {
        if (checkUserInput()) {
            gameSetting = new GameSetting(nameInput.getText(), capacityInput.getValue(), teamsInput.getValue());
            for (int id = 0; id < NPCInput.getValue(); id++) {
                Bot bot;
                if (easy.isSelected()) {
                   bot = new EasyBot(id);
                }
                else if (medium.isSelected()) {
                    bot = new MediumBot(id);
                }
                else { // hard.isSelected()
                    bot = new HardBot(id);
                }
                gameSetting.addPlayer(bot);
            }
            closeStage();
        }
    }

    /**
     * @return Vráti informácie získané z korektne vyplneného formulára. Ak nebol korektne vyplnený formulár tak NULL.
     */
    public GameSetting getGameSetting() {
        return gameSetting;
    }

}
