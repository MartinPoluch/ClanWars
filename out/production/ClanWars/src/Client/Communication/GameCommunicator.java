package Client.Communication;


import Client.GUI.controllers.GameController;
import Client.GUI.controllers.PlayerBoxController;
import GamePlay.Cards.*;
import GamePlay.GameChange;
import GamePlay.GameChangeMsg;
import GamePlay.Players.Health;
import GamePlay.Players.Human;
import GamePlay.Players.Player;
import GamePlay.Steal;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Jedna z klučových tried. Keď sa hráč nachádza v hre tak táto trieda príjma všetky správy zo servera a spracuváva ich.
 * Správy sú posielané triede GameController ktorá obsahuje všetku logiku hry a mení GUI.
 */
public class GameCommunicator {

    private Server server;
    private GameController controller;
    private Stage primaryStage;
    private Human me;

    public GameCommunicator(Server server, Stage primaryStage, Human me, List<Player> players) {
        this.server = server;
        this.primaryStage = primaryStage;
        this.me = me;
        showGameScene(players);
        primaryStage.setOnCloseRequest(close -> {
            if (! showLeaveWarning()) {
                close.consume();
            }
        } );
    }

    /**
     * Ak hráč bude chcieť hru predčasne opustit, tak sa mu zobrazí varovanie v podobe vyskakovacieho okna.
     * Hráč mmá ešte možnosť zrušiť proces opustenia hry, a v hre ostať.
     * @return true = hráč sa rozhodol opustiť hru
     */
    private boolean showLeaveWarning() {
        Alert leaveWarning = new Alert(Alert.AlertType.CONFIRMATION);
        try {
            DialogPane dialogPane = leaveWarning.getDialogPane();
            dialogPane.getStylesheets().add(getClass().getResource("/Client/GUI/css/basic.css").toExternalForm());
            dialogPane.getStylesheets().add(getClass().getResource("/Client/GUI/css/alertStyles.css").toExternalForm());
            Stage stage = (Stage) dialogPane.getScene().getWindow();
            stage.getIcons().add(new Image(getClass().getResource("/resources/Icon.png").toString()));
        } catch (Exception e) {
            System.err.println("CSS not found ");
        }
        leaveWarning.setTitle("Leave warning");
        leaveWarning.setHeaderText("Are you sure that you want to leave this game?\n" +
                "If you will leave, you cannot join this game again.");
        leaveWarning.getButtonTypes().clear();
        ButtonType stayBtn = new ButtonType("Stay", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType leaveBtn = new ButtonType("Leave", ButtonBar.ButtonData.OK_DONE);
        leaveWarning.getButtonTypes().addAll(stayBtn, leaveBtn);
        Optional<ButtonType> selectedOption = leaveWarning.showAndWait();
        return (selectedOption.orElse(stayBtn) == leaveBtn);
    }

    private void showGameScene(List<Player> players) {
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Client/GUI/fxml/GameScene.fxml"));
            Parent root = fxmlLoader.load();
            if (GameController.isFullHD()) {
                root.getStylesheets().add("/Client/GUI/css/GameSceneRegular.css");
            }
            else {
                root.getStylesheets().add("/Client/GUI/css/GameSceneSmall.css");
            }

            Scene gameScene = new Scene(root);
            this.controller = fxmlLoader.getController();
            this.controller.setServer(server);
            //mainScene.getStylesheets().add(".css");
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    controller.initGame(players, me);
                    primaryStage.setTitle("Game");
                    primaryStage.setScene(gameScene);
                    Rectangle2D screenBounds = Screen.getPrimary().getBounds();
                    primaryStage.setMinHeight(screenBounds.getHeight());
                    primaryStage.setMinWidth(screenBounds.getWidth());
                    primaryStage.setMaximized(true);
                }
            });

        } catch (IOException io) {
            System.out.println("CE: Cannot create game " + io);
        }
    }


    public GameChange communication() {
        while (true) {
            Object message = null;
            try {
                message = server.receive();
                System.out.println("received: " + message);
                if (message instanceof GameChangeMsg) {
                    GameChangeMsg gameChangeMsg = (GameChangeMsg) message;
                    handleGameChangeMsg(gameChangeMsg);
                    boolean iAmEliminated = (gameChangeMsg.getChange() == GameChange.LOSE) &&
                                             gameChangeMsg.getInitiator().equals(me);
                    if (iAmEliminated) {
                        // hrac ktory skonci hru este bude vidiet ako zmizne jeho playerBox
                        // to bude zaroven to posledne co uvidi
                        return GameChange.LOSE;
                    }
                }
                else if (message instanceof GameChange) {
                    GameChange change = (GameChange) message;
                    if (change == GameChange.WIN || change == GameChange.ERROR) {
                        return change;
                    }
                    handleGameChange(change);
                }
                else if (message instanceof Card) {
                    Card card = (Card) message;
                    handleCard(card);
                }

                else if (message instanceof Health) {
                    Health health = (Health) message;
                    handleHealthChange(health);
                }
                else if (message instanceof Steal) {
                    Steal steal = (Steal) message;
                    handleSteal(steal);
                }
                else {
                    System.out.println("Unknown message");
                }
            }
            catch (IOException io) {
                System.out.println("end");
                return GameChange.ERROR;
            }
            catch (Exception e) {
                System.out.println("end");
                return GameChange.ERROR;
            }
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    controller.writeSecondaryInfo("");
                }
            });

        }
    }

    /**
     * Treba vykonat pre vsetkych klientov
     * @param steal
     */
    private void handleSteal(Steal steal) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Card stolenCard = steal.getStolen();
                Player robbed = stolenCard.getOwner();
                Player thief = steal.getThief();
                if (thief.equals(me)) {
                    controller.throwOpponentCardAway(stolenCard, false);
                    controller.gainNewCard(stolenCard);
                    controller.writePrimaryInfo("You stolen card from " + steal.getStolen().getOwner());
                    System.out.println("You stolen card from " + steal.getStolen().getOwner());
                    stolenCard.setOwner(thief);
                }
                else if (robbed.equals(me)) {
                    controller.getPlayerController(thief).addBlankCard();
                    controller.writePrimaryInfo(thief.getName() + " stolen your card ");
                    System.out.println(thief.getName() + " stolen your card ");
                }
                else {
                    controller.throwOpponentCardAway(stolenCard, false);
                    controller.getPlayerController(thief).addBlankCard();
                    controller.writePrimaryInfo(steal.getThief().getName() + " stolen card from " + steal.getStolen().getOwner().getName());
                    System.out.println(steal.getThief().getName() + " stolen card from " + steal.getStolen().getOwner().getName());
                }
            }
        });
    }


    private void handleGameChangeMsg(GameChangeMsg changeMsg) {
        switch (changeMsg.getChange()) {
            case PICK_CARD: {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Player picker = changeMsg.getInitiator();
                        PlayerBoxController playerController = controller.getPlayerController(picker);
//                        if (playerController == null) {
//                            Player tester = changeMsg.getInitiator();
//                            PlayerBoxController testerController = controller.getPlayerController(picker);
//                        }
                        playerController.addBlankCard();
                    }
                });
                break;
            }
            case START_MOVE:
            {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (changeMsg.getInitiator().equals(me)) {
                            server.setStopSending(false);
                            controller.setUpEndMoveBtn();
//                            controller.writePrimaryInfo("You are on turn");
                            controller.showPlayerOnTurn(me);
                            System.out.println();
                            System.out.println("You started your move");
                        }
                        else {
                            System.out.println();
                            System.out.println("Player " + changeMsg.getInitiator().getName() + " is on turn");
                            controller.showPlayerOnTurn(changeMsg.getInitiator());
//                            controller.writePrimaryInfo("Player " + changeMsg.getInitiator().getName() + " is on turn");
                        }
                        System.out.println("\n");
                    }
                });
                break;
            }
            case LOSE:
            {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Player eliminated = changeMsg.getInitiator();
                        controller.eliminatePlayer(eliminated);
                        controller.writePrimaryInfo(eliminated.getName() + " was eliminated");
                    }
                });
                break;
            }
        }
    }

    private void handleGameChange(GameChange change) {
        switch (change) {
            case START_COMMUNICATION:
            {
                server.setStopSending(false);
                break;
            }
            case STOP_COMMUNICATION:
            {
                server.setStopSending(true);
                break;
            }
            case END_MOVE:
            {
                server.setStopSending(true);
                controller.myController().getEndMoveBtn().setVisible(false);
                break;
            }
            case CHECK:
            {
                server.setStopSending(false);
                server.send(GameChange.CHECK);
                server.setStopSending(true);
            }
        }
    }

    private void handleCard(Card card) {
        if (card.getOwner() == null) {
            System.err.println("Received card with no owner");
        }
        else if (card instanceof Item && ((Item) card).isActive() && card.isThrown()) {
            // niekto pouzil kartu disarm aby niekomu zahodil item
            Item item = (Item) card;
            controller.throwOpponentCardAway(item);
        }
        else if (card instanceof Item && ((Item) card).isActive()) { // ak mi prisla sprava ze si niekto nasadil item
            Item activeItem = (Item) card;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Player owner = activeItem.getOwner(); // zistim kto si nasadil ten item
                    //PlayerBoxController playerController = controller.getPlayerController(owner);
                    controller.writePrimaryInfo("Player " + owner.getName() + " equipped himself with " + activeItem.getName());
                    System.out.println("Player " + owner.getName() + " equipped himself with " + activeItem.getName());
                    controller.equipOpponentWithItem(activeItem); // graficke zobrazenie itemu
                }
            });
        }
        else if (card.getOwner().equals(me)) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    controller.gainNewCard(card);
                    System.out.println("You gained new card");
                }
            });
        }
        else if (! card.getOwner().equals(me)) {
            notMyChange(card);
        }

    }


    private void notMyChange(Card card) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                controller.throwOpponentCardAway(card);
            }
        });
        if (card.isThrown()) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    controller.writePrimaryInfo(card.getOwner().getName() + " threw card");
                    System.out.println(card.getOwner().getName() + " threw card");
                }
            });
        }
        else if (card instanceof OffensiveCard) {
            OffensiveCard offensiveCard = (OffensiveCard) card;
            if (offensiveCard.getDefender().equals(me)) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("You are under attack by " + offensiveCard.getOwner().getName());
                        controller.writePrimaryInfo("You are under attack by " + offensiveCard.getOwner().getName());
                        controller.showIgnoreMode(offensiveCard);
                    }
                });
            }
            else {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        controller.writePrimaryInfo("Player " + offensiveCard.getDefender().getName() + " is under attack by " + offensiveCard.getOwner().getName());
                    }
                });
            }
        }
        else if (card instanceof DefensiveCard){
            DefensiveCard defensiveCard = (DefensiveCard) card;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    controller.writePrimaryInfo("Player " + defensiveCard.getOwner() + " defended himself");
                    System.out.println("Player " + defensiveCard.getOwner().getName() + " defended himself");
                }
            });
        }
        else if (card instanceof HealCard) {
            HealCard healCard = (HealCard) card;
            handleHealCard(healCard);
        }
    }

    private void handleHealCard(HealCard healCard) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Player healer = healCard.getOwner();
                int powerOfHeal = healCard.getHeal();
                if (healCard.getType() == TypeOfHeal.SELF_HEAL) {
                    controller.writePrimaryInfo("Player " + healer + " healed himself. ");
                    System.out.println("Player " + healer.getName() + " healed himself. ");
                    controller.getPlayerController(healer).addHealth(powerOfHeal);
                }
                else if (healCard.getType() == TypeOfHeal.TEAM_HEAL) {
                    Player healed = healCard.getTarget();
                    controller.writePrimaryInfo("Player " + healer + " healed " + healCard.getTarget().getName());
                    System.out.println("Player " + healer.getName() + " healed himself. ");
                    controller.getPlayerController(healed).addHealth(powerOfHeal);
                }
                else if (healCard.getType() == TypeOfHeal.KETTLE) {
                    controller.writePrimaryInfo("Player " + healer + " healed all his teammates.");
                    controller.useKettle(healCard);
                }

            }
        });
    }





    private void handleHealthChange(Health health) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                PlayerBoxController playerController =  controller.getPlayerController(health.getPlayer());
                playerController.setHealth(health);
                controller.writePrimaryInfo("Player " + health.getPlayer().getName() + " lost health");
                System.out.println("Player " + health.getPlayer().getName() + " lost health");

            }
        });
    }

}
