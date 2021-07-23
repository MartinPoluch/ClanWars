package Client.GUI.controllers;

import Client.GUI.Animator;
import Client.GUI.CardFrame;
import GamePlay.Players.Health;
import GamePlay.Players.Player;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * PlayerBox predstavuje VBox na ktorom je zobrazený vybraný hráč. Pre každého hráča sú tu zobrazené všetky potrebné informácie.
 * PlayerBox môžme rozdeliť na dve časti:
 *  - prvá časť tzv. infoBox obsahuje podrobné informácie o každom hráčovi. Ide hlavne o meno hráča, názov postavy,
 *    obrázok tímu, počet životov.
 *  - druhá časť obsahuje karty ktorými daný hráč disponuje. Karty sú logicky rozdelené medzi itemy a karty s
 *    jednorázovým efektom
 *  Táto trieda zapuzdruje všetky metódy pomocou ktorých je možné zmeniť štatisktiky a karty daného hráča.
 */
public class PlayerBoxController {

    @FXML private HBox playerBox;

    @FXML private VBox infoBox;

    @FXML private VBox cardsBox;
    @FXML private HBox inventory;

    @FXML private HBox itemsHBox;
    @FXML private CardFrame[] items;

    @FXML private VBox detailedInfo;
    @FXML private Label name;
    @FXML private Label healthInfo;
    @FXML private ProgressBar healthBar;
    @FXML private Label character;
    @FXML private Label teamName;

    @FXML private ImageView teamImage;
    @FXML private Button endMoveBtn;
    @FXML private Button ignoreBtn;

    private Player player;

    @FXML private VBox statsInfo;
    private StatsInfoController statsController;

    public void initialize() {
        items = new CardFrame[3];
        for (int i = 0; i < 3; i++) {
            CardFrame cardFrame = new CardFrame();
            items[i] = cardFrame;
            itemsHBox.getChildren().add(cardFrame);
        }
    }

    public void borderChange(Player playerOnTurn) {
        if (player.equals(playerOnTurn)) {
            playerBox.setStyle("-fx-border-color: #C50000; " +
                    "-fx-border-width: 8px;");
            infoBox.setStyle("-fx-border-color: #C50000;");
        }
        else {
            playerBox.setStyle("-fx-border-color: black; " +
                    "-fx-border-width: 4px;");
            infoBox.setStyle("-fx-border-color: black");
        }
    }

    public void loadPlayerStatsInfoBox() {
        FXMLLoader playerLoader = new FXMLLoader(getClass().getResource("/Client/GUI/fxml/statsInfoBox.fxml"));
        try {
            statsInfo = playerLoader.load();
            statsController = playerLoader.getController();
            statsController.refreshStats(player);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public void hide() {
        playerBox.setVisible(false);

    }

    public void initPlayer(Player player, Animator animator) {
        this.player = player;
        name.setText(player.getName());
        character.setText(player.getCharacter().getName());
        try {
            Image image = new Image(getClass().getResource(player.getTeam().getImagePath()).toString());
            final int sizeOfImage = GameController.isFullHD() ? 90 : 60;
            teamImage.setImage(image);
            teamImage.setFitHeight(sizeOfImage);
            teamImage.setFitWidth(sizeOfImage);
            teamImage.setOnMouseClicked(event -> showPlayerStats());

        } catch (Exception io) {
            System.err.println("Image " + player.getTeam().getImagePath() + " not found");
        }
        loadPlayerStatsInfoBox();
        setHealth(player.getHealth());
    }

    private void showPlayerStats() {
        System.out.println("show stats");
        statsController.refreshStats(player);
        infoBox.getChildren().remove(0);
        infoBox.getChildren().add(0, statsInfo);
        teamImage.setOnMouseClicked(event -> hidePlayerStats());
    }

    private void hidePlayerStats() {
        System.out.println("hide stats");
        infoBox.getChildren().remove(0);
        infoBox.getChildren().add(0, detailedInfo);
        teamImage.setOnMouseClicked(event -> showPlayerStats());
    }

    public void setHealth(Health newHealth) {
        healthBar.setProgress(newHealth.percentage());
        healthInfo.setText(newHealth.getActualHealth() + "/" + newHealth.getMaxHealth());
        player.setHealth(newHealth);
    }

    public void addHealth(int addHealth) {
        player.getHealth().changeHealth(addHealth);
        healthBar.setProgress(player.getHealth().percentage());
        healthInfo.setText(player.getHealth().getActualHealth() + "/" + player.getHealth().getMaxHealth());
    }

    public Player getPlayer() {
        return player;
    }

    public void addBlankCard() {
        CardFrame pickedCard = new CardFrame();
        pickedCard.setBackSide();
        ObservableList<Node> cards = inventory.getChildren();
        if (cards.size() < 5) {
            cards.add(pickedCard);
        }
        else {
            System.err.println("cannot add card");
        }
    }

    public void removeBlankCard() {
        int numberOfCards = getInventory().getChildren().size();
        if (numberOfCards != 0) {
            getInventory().getChildren().remove(numberOfCards - 1);
        }
        else {
            System.err.println("Cannot remove last card because " + player.getName() + " has no card in getInventory");
        }
    }

    public Button getEndMoveBtn() {
        return endMoveBtn;
    }

    public Button getIgnoreBtn() {
        return ignoreBtn;
    }

    public HBox getInventory() {
        return inventory;
    }

    public CardFrame[] getItems () {
        return items;
    }

    public HBox getPlayerBox() {
        return playerBox;
    }
}
