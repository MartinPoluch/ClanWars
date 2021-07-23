package Client.GUI.controllers;

import Client.Communication.Server;
import Client.GUI.Animator;
import Client.GUI.CardFrame;
import Client.GUI.Point;
import GamePlay.GameChange;
import GamePlay.Cards.*;
import GamePlay.GameChangeMsg;
import GamePlay.Steal;
import GamePlay.Players.Health;
import GamePlay.Players.Human;
import GamePlay.Players.Player;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Screen;


import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Najdôležitejšia trieda celej klientskej časti. Trieda vykonáva všetky zmeny GUI počas priebehu hry.
 */
public class GameController {

    @FXML private GridPane root;
    @FXML private ImageView lastUsedCard;
    @FXML private StackPane pile;
    @FXML private ImageView topOfDeck;
    @FXML private Label primaryGameInfo;
    @FXML private Label secondaryGameInfo;
    @FXML private Label selectedCardName;
    @FXML private Label selectedCardInfo;

    private List<Player> players;
    private Human me;
    private Map<Player, PlayerBoxController> controllers;
    private PlayerBoxController myController;
    private Server server;
    private Animator animator;

    private CardFrame selectedFrame;
    private OffensiveCard attackingCard;

    public static final int MAX_NUMBER_OF_CARDS = 3;

    private boolean discardMode;

    @FXML
    public void initialize() {
        showDeck();
        discardMode = false;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    private Point[] playerPositions() {
        Point[] positions;
        switch (players.size()) {
            case 2:
                positions = new Point[]{new Point(2, 1), new Point(0, 1)};
                break;
            case 3:
                positions = new Point[]{new Point(2, 1), new Point(0, 0), new Point(0, 1)};
                break;
            case 4:
                positions = new Point[]{new Point(2, 1), new Point(0, 0),
                        new Point(0, 1), new Point(0, 2)};
                break;
            case 5:
                positions = new Point[]{new Point(2, 1), new Point(1, 0), new Point(0, 0),
                        new Point(0, 1), new Point(0, 2)};
                break;
            case 6:
                positions = new Point[]{new Point(2, 1), new Point(1, 0), new Point(0, 0),
                        new Point(0, 1), new Point(0, 2), new Point(1, 2)};
                break;
            case 7:
                positions = new Point[]{new Point(2, 1), new Point(2, 0), new Point(1, 0),
                        new Point(0, 0), new Point(0, 1), new Point(0, 2), new Point(1, 2)};
                break;
            case 8:
                positions = new Point[]{new Point(2, 1), new Point(2, 0), new Point(1, 0), new Point(0, 0),
                        new Point(0, 1), new Point(0, 2), new Point(1, 2), new Point(2, 2)};
                break;
            default:
                positions = new Point[]{};
        }
        return positions;
    }

    public void equipOpponentWithItem(Item item) {
        int ownerIndex = players.indexOf(item.getOwner());
        Player clientOwner = players.get(ownerIndex);
        Item oldItem = clientOwner.addItem(item);
        if (oldItem == null) {
            controllers.get(clientOwner).removeBlankCard();
        }
        PlayerBoxController ownerController = controllers.get(item.getOwner());
        CardFrame[] items = ownerController.getItems();
        items[item.positionIndex()].setCard(item);
    }

    public void eliminatePlayer(Player player) {
        PlayerBoxController playerBoxController = controllers.get(player);
        animator.eliminateAnimation(playerBoxController);
        animator.getEliminating().setOnFinished(e -> {
            playerBoxController.hide();
            controllers.remove(player);
            players.remove(player);
            animator.setAnimating(false);
        });

    }

    private void equipYourselfWithItem(Item item) {
        boolean removedFromInventory = (myController.getInventory().getChildren().remove(selectedFrame));
        if (removedFromInventory) { // defenzivne programovanie, ci je mam vobec dany item (vzdy by to to malo byt true)
            selectedFrame = null;
            item.setActive(true); // nastavim item ako aktiny (nasadeny)
            server.send(item); // poslem ostatnym hracom informaciu o zmene
            myController.getItems()[item.positionIndex()].setCard(item); // graficke zobrazenie aktualneho itemu, prepise stary item

            Item oldItem = me.addItem(item); // ulozim si referenciu na danu kartu, a metoda mi vrati predchadzajuci item
            if (oldItem != null) {
                oldItem.setActive(false); // stary item sa uz nepouziva
                gainNewCard(oldItem); // vlozim si do svojho inventara
            }
        }
        else {
            System.err.println("You dont have item " + item + " in your inventary.");
        }
    }

    public void initGame(List<Player> players, Human myReference) {
        animator = new Animator();
        this.controllers = new HashMap<Player, PlayerBoxController>();
        this.players = players;
        int myIndex  = players.indexOf(myReference);
        this.me = (Human) players.get(myIndex);
        Point[] playerPositions = playerPositions();
        int actualIndex = myIndex;
        for (int playerCount = 0; playerCount < players.size(); playerCount++) {
            FXMLLoader playerLoader = new FXMLLoader(getClass().getResource("/Client/GUI/fxml/PlayerBox.fxml"));
            try {
                Pane playerBox = playerLoader.load();
                addDesign(playerBox);
                Point position = playerPositions[playerCount];
                root.add(playerBox, position.ROW, position.COLUMN);
                PlayerBoxController playerController = playerLoader.getController();
                initItemInfo(playerController.getItems());
                Player player = players.get(actualIndex);
                controllers.put(player, playerController);
                if (player.equals(me)) {
                    this.myController = playerController;
                }
                else if (! player.getTeam().equals(me.getTeam())){ // hrac je z nepriatelskeho timu
                    initItemsEvents(playerController);
                    addEventToEnemy(playerBox, player, playerController);
                }
                else if (player.getTeam().equals(me.getTeam())){
                    addEventToTeammate(playerBox, player, playerController);
                }
                playerController.initPlayer(player, animator);
                if (actualIndex > 0) {
                    actualIndex--;
                }
                else {
                    actualIndex = players.size() - 1;
                }
            } catch (IOException io) {
                io.printStackTrace();
            }
        }
    }

    private void initItemInfo(CardFrame[] items) {
        if (items != null) {
            for (CardFrame itemFrame : items) {
                itemFrame.addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        if (itemFrame != null && itemFrame.getCard() != null) {
                            Card item = itemFrame.getCard();
                            selectedCardName.setText(item.getName() + ":");
                            selectedCardInfo.setText(item.getInfo());
                        }
                    }
                });

                itemFrame.addEventHandler(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        if (itemFrame != null && itemFrame.getCard() != null) {
                            Card item = itemFrame.getCard();
                            selectedCardName.setText("");
                            selectedCardInfo.setText("");
                        }
                    }
                });

            }
        }
    }

    public static boolean isFullHD() {
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getBounds();
        return (primaryScreenBounds.getWidth() >= 1920) && (primaryScreenBounds.getHeight() >= 1080);
    }

    private void addDesign(Pane playerBox) {
        if (isFullHD()) {
            playerBox.getStylesheets().add("/Client/GUI/css/PlayerBoxStylesRegular.css");
        }
        else {
            playerBox.getStylesheets().add("/Client/GUI/css/PlayerBoxStylesSmall.css");
        }
    }

    private void addEventToEnemy(Pane playerBox, Player player, PlayerBoxController playerController) {
        playerBox.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                boolean iAmUnderAttack = myController.getIgnoreBtn().isVisible();
                if (server.canSending() && ! discardMode && ! animator.isAnimating() && ! iAmUnderAttack) {
                    if (selectedFrame != null && selectedFrame.getCard() instanceof OffensiveCard ) {
                        OffensiveCard offensiveCard = (OffensiveCard) selectedFrame.getCard();
                        Player defender = player;
                        boolean playerHasCards = !playerController.getInventory().getChildren().isEmpty();
                        if (defender.getTeam().equals(me.getTeam())) {
                            writeSecondaryInfo("You cannot attack your teammate");
                        }
                        else if (offensiveCard.getType() == TypeOfOffensive.DISARM) {
                            // pri DISARM vzdialenost nehra rolu
                            if (playerHasCards) {
                                playOffensiveCard(player, offensiveCard);
                            }
                        }
                        else if (offensiveCard.getType() == TypeOfOffensive.HIT){
                            if (playerInRange(me, defender)) {
                                offensiveCard.setDamage(me.getDamage());
                                playOffensiveCard(player, offensiveCard);
                            }
                            else {
                                writeSecondaryInfo(defender.getName() + " is not in you range, you cannot attack him.");
                            }
                        }
                        else if (offensiveCard.getType() == TypeOfOffensive.THEFT){
                            if (playerInRange(me, defender) && playerHasCards) {
                                playOffensiveCard(player, offensiveCard);
                            }
                            else {
                                writeSecondaryInfo(defender.getName() + " is not in you range, you cannot attack him.");
                            }
                        }
                        else {
                            System.err.println("Try to use unknown offensive card " + offensiveCard + " at " + defender.getName());
                        }
                    }
                }
            }
        });
    }

    private void addEventToTeammate(Pane playerBox, Player teammate, PlayerBoxController teamController) {
        playerBox.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                boolean iAmUnderAttack = myController.getIgnoreBtn().isVisible();
                boolean selectedHeal = (selectedFrame != null && selectedFrame.getCard() instanceof HealCard);
                if (server.canSending() && ! discardMode && selectedHeal && ! animator.isAnimating() && ! iAmUnderAttack && ! animator.isAnimating()) {
                    HealCard heal = (HealCard) selectedFrame.getCard();
                    if (heal.getType() == TypeOfHeal.TEAM_HEAL) {
                        heal.setTarget(teammate);
                        writePrimaryInfo("You healed " + teammate.getName());
                        server.send(heal);
                        throwCard(selectedFrame);
                        teamController.addHealth(heal.getHeal());
                    }
                    else {
                        writeSecondaryInfo("You cannot use this card to heal your teammate.");
                    }
                }
            }
        });
    }

    /**
     * Pridá eventy k pozíciam daných itemov. Ak hráč nemá aktívny item na danej pozícii tak sa event nevykoná.
     * @param playerBoxController
     */
    private void initItemsEvents(PlayerBoxController playerBoxController) {
        CardFrame[] items = playerBoxController.getItems();
        for (CardFrame item : items) {
            item.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    boolean clickedAtItem = (item != null) && (item.getCard() != null);
                    boolean selectedOffensiveCard = selectedFrame != null && selectedFrame.getCard() instanceof OffensiveCard;
                    boolean iAmUnderAttack = myController.getIgnoreBtn().isVisible();
                    if (clickedAtItem && selectedOffensiveCard && ! iAmUnderAttack && server.canSending() && ! discardMode) {
                        OffensiveCard offensiveCard = (OffensiveCard) selectedFrame.getCard();
                        if (offensiveCard.getType() == TypeOfOffensive.THEFT || offensiveCard.getType() == TypeOfOffensive.DISARM) {
                            offenseAtItem((Item) item.getCard(), offensiveCard);
                        }
                    }
                }
            });
        }
    }

    private void offenseAtItem(Item item, OffensiveCard offensiveCard) {
        Player defender = item.getOwner();
        if (offensiveCard.getType() == TypeOfOffensive.THEFT) {
            if (playerInRange(me, defender)) {
                offensiveCard.setTarget(item);
                playOffensiveCard(defender, offensiveCard);
            }
            else {
                writeSecondaryInfo(defender.getName() + " is not in your range.");
            }
        }
        else if (offensiveCard.getType() == TypeOfOffensive.DISARM) {
            offensiveCard.setTarget(item);
            playOffensiveCard(defender, offensiveCard);
        }
    }

    private boolean playerInRange(Player attacker, Player defender) {
        int attackerPosition = players.indexOf(attacker);
        int defenderPosition = players.indexOf(defender);
        int distanceLeft = 0;
        while (attackerPosition != defenderPosition) { // posupne iterujem jednotlive pozicie
            distanceLeft++;
            attackerPosition--;
            if (attackerPosition < 0) {
                attackerPosition = players.size() -1;
            }
        }
        int distanceRight = players.size() - distanceLeft;
        int shortestDistance = Math.min(distanceLeft, distanceRight);
        int myRange = attacker.getOffensiveRange() - defender.getDefensiveRange();
        return (myRange >= shortestDistance);
    }

    private void playOffensiveCard(Player defender, OffensiveCard offensiveCard) {
        myController.getEndMoveBtn().setVisible(false);
        primaryGameInfo.setText("You are attacking player " + defender.getName());
        System.out.println("You are attacking player " + defender.getName());
        secondaryGameInfo.setText("");
        offensiveCard.setDefender(defender);
        server.setStopSending(false);
        server.send(offensiveCard);
        server.setStopSending(true);
        throwCard(selectedFrame);
    }

    private void throwCard(CardFrame cardFrame) {
        animator.moveToLastUsedCard(cardFrame, lastUsedCard);
        animator.getCardMoving().setOnFinished(e -> {
            lastUsedCard.setImage(cardFrame.getImage());
            myController.getInventory().getChildren().remove(cardFrame);
            if (cardFrame == selectedFrame) {
                selectedFrame = null;
            }
            animator.setAnimating(false);
        });
    }

    public void useKettle(HealCard healCard) {
        int indexOfHealer = players.indexOf(healCard.getOwner());
        Player healer = players.get(indexOfHealer);
        for (Map.Entry<Player, PlayerBoxController> entry : controllers.entrySet()) {
            Player player = entry.getKey();
            if (healer.getTeam().equals(player.getTeam())) {
                PlayerBoxController controller = entry.getValue();
                controller.addHealth(healCard.getHeal());
                System.out.println("Player " + player.getName() + " was healed.");
            }
        }
    }

    public PlayerBoxController getPlayerController(Player player) {
        return controllers.get(player);
    }

    public void throwOpponentCardAway(Card card) {
        throwOpponentCardAway(card, true);
    }

    public void throwOpponentCardAway(Card card, boolean updateLastCard){

        if (card instanceof Item && ((Item) card).isActive()) {
            Item item = (Item) card;
            int clientOwnerIndex = players.indexOf(item.getOwner());
            Player clientOwner = players.get(clientOwnerIndex);
            clientOwner.removeItem(item.positionIndex());
            PlayerBoxController playerController = controllers.get(clientOwner);
            CardFrame itemFrame = playerController.getItems()[item.positionIndex()];
            if (updateLastCard) {
                lastUsedCard.setImage(itemFrame.getImage());
                itemFrame.removeCard();
            }
            else {
                itemFrame.removeCard();
            }
        }
        else {
            int clientOwnerIndex = players.indexOf(card.getOwner());
            Player clientOwner = players.get(clientOwnerIndex);
            Image usedCard = new Image(getClass().getResource(card.getImagePath()).toString());
            PlayerBoxController ownerController = controllers.get(clientOwner);
            if (updateLastCard) {
                ObservableList<Node> cards = ownerController.getInventory().getChildren();
                if (! cards.isEmpty()) {
                    CardFrame removedCard = (CardFrame) cards.get(cards.size() - 1);
                    animator.moveToLastUsedCard(removedCard, lastUsedCard);
                    animator.getCardMoving().setOnFinished(e -> {
                        lastUsedCard.setImage(usedCard);
                        ownerController.removeBlankCard();
                        animator.setAnimating(false);
                    });
                }
                else {
                    System.err.println("Cannot remove card player doesnt have any cards");
                }
            }
            else {
                ownerController.removeBlankCard();
            }
        }
    }

    public void showPlayerOnTurn(Player playerOnTurn) {
        for (Map.Entry<Player, PlayerBoxController> entry : controllers.entrySet()) {
            PlayerBoxController playerBoxController = entry.getValue();
            playerBoxController.borderChange(playerOnTurn);
        }
    }

    public void showIgnoreMode(OffensiveCard card) {
        attackingCard = card;
        Button ignoreBtn = myController.getIgnoreBtn();
        ignoreBtn.setVisible(true);
        ignoreBtn.setOnAction(e -> {
            ignoreAttack();
        });
    }

    private void ignoreAttack() {
        if (attackingCard.getType() == TypeOfOffensive.HIT) {
            Health myHealth = me.getHealth();
            int takenDamage = attackingCard.getDamage() - me.getProtection();
            if (takenDamage < 0) {
                takenDamage = 0;
            }
            myHealth.changeHealth(- takenDamage);
            myController.setHealth(myHealth);
            server.setStopSending(false);
            server.send(myHealth);
            server.setStopSending(true);
            myController.getIgnoreBtn().setVisible(false);
            int lostHealth = (attackingCard.getDamage() - me.getProtection());
            if (lostHealth < 0) {
                lostHealth = 0;
            }
            primaryGameInfo.setText("You lost " + lostHealth + " health.");
            System.out.println("You decided ignore attack");
            attackingCard = null;
        }
        else if ((attackingCard.getType() == TypeOfOffensive.DISARM) || (attackingCard.getType() == TypeOfOffensive.THEFT)) {
            Card cardMove;
            if (attackingCard.getTarget() == null) {
                CardFrame cardMoveFrame = removeRandomCardFrame();
                cardMove = cardMoveFrame.getCard();
                if (attackingCard.getType() == TypeOfOffensive.DISARM) {
                    lastUsedCard.setImage(cardMoveFrame.getImage());
                }
            }
            else {
                int clientDefenderIndex = players.indexOf(attackingCard.getDefender());
                Player clientDefender = players.get(clientDefenderIndex);
                Item target = attackingCard.getTarget();
                clientDefender.removeItem(target.positionIndex()); // odoberiem kartu konkretnemu hracovi (aplikacna logika)
                PlayerBoxController defenderController = controllers.get(attackingCard.getDefender());
                CardFrame frameItem = defenderController.getItems()[target.positionIndex()];
                if (attackingCard.getType() == TypeOfOffensive.DISARM) {
                    lastUsedCard.setImage(frameItem.getImage());
                }
                cardMove = frameItem.getCard();
                frameItem.removeCard();
            }
            server.setStopSending(false);
            if (attackingCard.getType() == TypeOfOffensive.DISARM) {
                cardMove.setThrown(true);
                server.send(cardMove);
            }
            else {
                Steal steal = new Steal(cardMove, attackingCard.getOwner());
                server.send(steal);
            }
            server.setStopSending(true);
            myController.getIgnoreBtn().setVisible(false);
            attackingCard = null;
        }
        writeSecondaryInfo("");
    }

    private CardFrame removeRandomCardFrame() {
        ObservableList<Node> myInventory = myController.getInventory().getChildren();
        Random generator = new Random();
        int randomIndex = generator.nextInt(myInventory.size());
        return (CardFrame) myInventory.remove(randomIndex);
    }

    public PlayerBoxController myController() {
        return controllers.get(me);
    }

    public void gainNewCard(Card card) {
        CardFrame pickedCard = new CardFrame();
        pickedCard.setCard(card);

        pickedCard.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (server.canSending() && ! animator.isAnimating()) {
                    if (selectedFrame != null && selectedFrame.getCard() == card) {
                        boolean iAmUnderAttack = myController.getIgnoreBtn().isVisible();
                        if (discardMode) { // ak som uz zvolil ukoncit tah ale mam prilis vela kariet
                            selectedFrame.getCard().setThrown(true);
                            server.send(selectedFrame.getCard());
                            writePrimaryInfo("You thrown card away");
                            animator.moveToLastUsedCard(selectedFrame, lastUsedCard);
                            animator.getCardMoving().setOnFinished(e -> {
                                lastUsedCard.setImage(selectedFrame.getImage());
                                myController.getInventory().getChildren().remove(selectedFrame);
                                animator.setAnimating(false);
                                if (!tooManyCards()) { // zistujem ci uz je pocet kariet ok
                                    endYourMoveAction();
                                }
                            });
                        }
                        else if (iAmUnderAttack && selectedFrame.getCard() instanceof DefensiveCard) {
                            DefensiveCard myDefense = (DefensiveCard) selectedFrame.getCard();
                            playDefensiveCard(myDefense);
                        }
                        else if (! iAmUnderAttack && selectedFrame.getCard() instanceof HealCard) {
                            HealCard heal = (HealCard) card;
                            playHealCard(heal);
                        }
                        else if (! iAmUnderAttack && selectedFrame.getCard() instanceof Item) {
                            Item item = (Item) selectedFrame.getCard();
                            equipYourselfWithItem(item);
                        }
                        else {
                            secondaryGameInfo.setText("You cannot use this card");
                        }
                    }
                    else {
                        selectedFrame = pickedCard;
                    }
                    writeSecondaryInfo("");
                }
                else {
                    secondaryGameInfo.setText("You can't play right now. Wait for your turn.");
                }
            }
        });

        pickedCard.addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                selectedCardName.setText(card.getName() + ":");
                selectedCardInfo.setText(card.getInfo());
            }
        });

        pickedCard.addEventHandler(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                selectedCardName.setText("");
                selectedCardInfo.setText("");
            }
        });

        myController.getInventory().getChildren().add(pickedCard);
    }

    private void playHealCard(HealCard heal) {
        if (heal.getType() == TypeOfHeal.TEAM_HEAL) {
            heal.setTarget(me);
            myController.addHealth(heal.getHeal());
            writePrimaryInfo("You healed yourself.");
        }
        else if (heal.getType() == TypeOfHeal.SELF_HEAL) {
            myController.addHealth(heal.getHeal());
            writePrimaryInfo("You healed yourself.");
        }
        else if (heal.getType() == TypeOfHeal.KETTLE) {
            useKettle(heal);
            writePrimaryInfo("You healed all your teammates");
        }
        server.send(heal);
        throwCard(selectedFrame);
    }

    private void playDefensiveCard(DefensiveCard myDefense) {
        if (myDefense.against(attackingCard.getType())) {
            server.send(myDefense);
            server.setStopSending(true);
            primaryGameInfo.setText("You defended yourself");
            myController.getIgnoreBtn().setVisible(false);
            attackingCard = null;
            throwCard(selectedFrame);
            System.out.println("You defended yourself");
        }
        else {
            writeSecondaryInfo("You cannot defend yourself with this card");
        }
    }

    public void setUpEndMoveBtn() {
        if (discardMode) { // ak zahadzujem karty tak nechcem zobrazit endMoveBtn
            return;
        }
        Button endMoveBtn = myController.getEndMoveBtn();
        endMoveBtn.setVisible(true);
        endMoveBtn.setOnAction(e -> {
            // defenzivne programovanie, ak dojde k chybe a zobrazi sa endMove button vtedy ked nema tak po jeho
            // kliknuti zmizne a nebude mat ziadny dalsi efekt
            if (server.canSending() && ! animator.isAnimating()) {
                if (tooManyCards()) {
                    writeSecondaryInfo("You cannot have more that 3 cards.\nChoose cards which you want throw away.");
                    discardMode = true;
                }
                else {
                    endYourMoveAction();
                }
            }
            endMoveBtn.setVisible(false);
        });
    }

    private boolean tooManyCards() {
        return (myController.getInventory().getChildren().size() > MAX_NUMBER_OF_CARDS);
    }

    private void endYourMoveAction() {
        discardMode = false;
        server.send(new GameChangeMsg(GameChange.END_MOVE, me));
        server.setStopSending(true);
        reset();
    }

    public void writePrimaryInfo(String info) {
        primaryGameInfo.setText(info);
    }

    public void writeSecondaryInfo(String info) {
        secondaryGameInfo.setText(info);
    }

    private void showDeck() {
        try {
            Image lastCardImg = new Image(getClass().getResource("/resources/cards/axe.png").toString());
            Image deckImg = new Image(getClass().getResource("/resources/cards/back.png").toString());
            lastUsedCard.setFitHeight(CardFrame.getCardHeight());
            lastUsedCard.setFitWidth(CardFrame.getCardWidth());
            lastUsedCard.setImage(lastCardImg);
            topOfDeck.setFitHeight(CardFrame.getCardHeight());
            topOfDeck.setFitWidth(CardFrame.getCardWidth());
            topOfDeck.setImage(deckImg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reset() {
        primaryGameInfo.setText("");
        secondaryGameInfo.setText("");
        selectedFrame = null;
        attackingCard = null;
    }
}
