package Client.GUI.controllers;


import Client.Communication.Lobby;
import Client.Communication.Server;
import GamePlay.Players.Human;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Scanner;

/**
 * Trieda reprezentuje logiku prihlasovacieho okna. Toto okno sa užívateľovi automatický otvorí vždy po spustení aplikácie.
 * Umožňuje užívateľovi prihlásiť sa na server.
 * IP adresa servera a číslo portu sa načítavajú z externého textového súboru. Užívateľ  môže tento súbor prepísať za
 * použitia GUI. Cez voľbu setting si užívateľ môže nastaviť server na ktorý sa chce prihlásiť (hostname a číslo port).
 *
 */
public class LogInController {

    @FXML private Button playBtn;
    @FXML private TextField nameInput;
    @FXML private MenuItem setting;

    public static final int STAGE_HEIGHT = 450;
    public static final int STAGE_WIDTH = 600;

    private String ipAddress;
    private int port;

    /**
     * Načítanie údajov zo súboru a priradenie eventov k buttonom
     */
    public void initialize() {
        Pair<String, Integer> settings = readSettingsFromFile();
        ipAddress = settings.getKey();
        port = settings.getValue();
        playBtn.setOnAction(action -> {
            if (checkName()) {
                if (ipAddress.equals("")) {
                    showSettings();
                }
                else {
                    showLobby();
                }
            }
        });
        setting.setOnAction(e -> showSettings());
    }

    /**
     * Modálne okno ktorú umožní prepísať nastavenia servera v textovom súbore "server_settings.txt"
     */
    private void showSettings() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        try {
            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.getStylesheets().add(getClass().getResource("/Client/GUI/css/basic.css").toExternalForm());
            dialogPane.getStylesheets().add(getClass().getResource("/Client/GUI/css/alertStyles.css").toExternalForm());
            Stage stage = (Stage) dialogPane.getScene().getWindow();
            stage.getIcons().add(new Image(getClass().getResource("/resources/Icon.png").toString()));
        } catch (Exception e) {
            System.err.println("CSS not found ");
        }
        dialog.setTitle("Server settings");
        dialog.setHeaderText("Enter IP address or hostname of server \nand number of port.");

        ButtonType loginButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        Pair<String, Integer> defaults = readSettingsFromFile();
        TextField ipInput = new TextField();
        ipInput.setText(defaults.getKey());
        TextField portInput = new TextField();
        portInput.setText(defaults.getValue().toString());

        gridPane.add(new Label("IP address"), 0, 0);
        gridPane.add(new Label("port"), 0, 1);
        gridPane.add(ipInput, 1, 0);
        gridPane.add(portInput, 1, 1);

        dialog.getDialogPane().setContent(gridPane);


        // Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(ipInput.getText(), portInput.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();
        result.ifPresent( e -> {
            try {
                System.out.println("SAVE");
                Pair<String, String> pair = result.get();
                String ipAddress = pair.getKey();
                int port = Integer.parseInt(pair.getValue());
                this.ipAddress = ipAddress;
                this.port = port;
                saveSettings();
            } catch (Exception badPort) {
                System.err.println("Bad settings.");
            }
        });
    }

    /**
     * Načítanie IP adresy servera a číslo portu na ktrom bude prebiehať komunikácia. Údaje sa načítavajú z externého
     * textového súboru "server_settings.txt"
     * @return IP adresa a číslo portu
     */
    private Pair<String, Integer> readSettingsFromFile() {
        String ipAddress = "";
        int port = 0;
        try {
            URL urlOfFolder =  getClass().getProtectionDomain().getCodeSource().getLocation();
            URL fileUrl = new URL(urlOfFolder, "server_settings.txt", null);
            File settingsFile = Paths.get(fileUrl.toURI()).toFile();
            Scanner scanner = new Scanner(settingsFile);
            while (scanner.hasNextLine()) {
                String[] line = scanner.nextLine().split(" : ");
                if (line[0].equals("ipAddress")) {
                    ipAddress = line[1];
                }
                else if (line[0].equals("port")) {
                    port = Integer.parseInt(line[1]);
                }
                else {
                    System.err.println("Bad input in file " + settingsFile.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Pair<String, Integer>(ipAddress, port);
    }

    /**
     * Prepísanie dát (ip adresa, port) v súbore "server_settings.txt"
     */
    private void saveSettings() {
        try {
            URL urlOfFolder =  getClass().getProtectionDomain().getCodeSource().getLocation();
            URL fileUrl = new URL(urlOfFolder, "server_settings.txt", null);
            File settingsFile = Paths.get(fileUrl.toURI()).toFile();
            FileWriter fileWriter = new FileWriter(settingsFile);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println("ipAddress : " + ipAddress);
            printWriter.println("port : " + port);
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Zaháji komunikáciu so serverom. Pre komunikáciu vytvorí nové vlákno. Toto vlákno už bude existovať po celú dobu
     * fungovania aplikácie.
     * Zatvorí prihlasovacie okno a zobrazí okno Lobby.
     * Ak nastala chyba nič z vyššie evedeného sa nevykoná, a zobrazí sa užívateľovi informácia o chybe  v podobe alertu.
     */
    private void showLobby() {
        Stage primaryStage = (Stage) playBtn.getScene().getWindow();
        Human me = new Human(nameInput.getText());
        Server server = null;
        try {
//            server = new Server(ipAddress, port);
            server = new Server("127.0.0.1", port);
            if (server.connected()) {
                Lobby lobby = new Lobby(me, primaryStage, server);
                Thread thread = new Thread(lobby);
                thread.setDaemon(true); // ak sa ukonci appka tak sa ukonci aj komunikacia
                thread.start();
            }
            else {
                throw new Exception("Server doesn't respond.");
            }

        } catch (Exception serverIsDown) {
            // ak server momentalne nefunguje tak sa vypise klientovi chybova hlaska
            Alert serverDown = new Alert(Alert.AlertType.ERROR);
            try {
                Stage stage = (Stage) serverDown.getDialogPane().getScene().getWindow();
                stage.getIcons().add(new Image(getClass().getResource("/resources/Icon.png").toString()));
            } catch (Exception e) {
                System.err.println("Icon not found");
            }

            serverDown.setTitle("Connect failed");
            serverDown.setHeaderText("Server is not running");
            if (server != null) {
                serverDown.setContentText(server.getError());
            }
            showAlert(serverDown);
        }
    }

    /**
     * Skontroluje či užívateš zadal valídne meno.
     * @return true = valídne meno
     */
    private boolean checkName() {
        final int MIN_LENGTH = 3;
        final int MAX_LENGTH = 15;
        if (nameInput.getText().length() >= MIN_LENGTH && nameInput.getText().length() <= MAX_LENGTH) {
            return true;
        }
        else {
            Alert shortName = new Alert(Alert.AlertType.ERROR);
            try {
                Stage stage = (Stage) shortName.getDialogPane().getScene().getWindow();
                stage.getIcons().add(new Image(getClass().getResource("/resources/Icon.png").toString()));
            } catch (Exception e) {
                System.err.println("Icon not found");
            }
            shortName.setTitle("Failed");
            shortName.setHeaderText("Name must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters");
            showAlert(shortName);
            return false;
        }
    }

    /**
     * Oznámenie užívatelovi informáciu o chybe. Chyba môže nastať v prípade zle zvoleného užívateľského mena alebo
     * zlých údajov o serveri.
     * @param alert Refferencia na okno
     */
    private void showAlert(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();
        try {
            Stage alertStage = (Stage) dialogPane.getScene().getWindow();
            alertStage.getIcons().add(new Image(getClass().getResource("/resources/Icon.png").toString()));
            dialogPane.getStylesheets().add("/Client/GUI/css/basic.css");
            dialogPane.getStylesheets().add("/Client/GUI/css/alertStyles.css");
        } catch (Exception e) {
            System.err.println("Icon not found");
        }

        alert.showAndWait();
    }










}
