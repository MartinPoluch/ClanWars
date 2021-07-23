package Client.GUI;


import Client.GUI.controllers.GameController;
import GamePlay.Cards.Card;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Screen;

/**
 * Grafická reprezentácia karty.
 */
public class CardFrame extends ImageView {

    private Card card;

    public CardFrame() {
        setFitHeight(getCardHeight());
        setFitWidth(getCardWidth());
    }

    public void setBackSide() {
        Image back = new Image(getClass().getResource("/resources/cards/back.png").toString());
        setImage(back);
    }

    public static int getCardHeight() {
        return GameController.isFullHD() ? 126 : 87;
    }

    public static int getCardWidth() {

        return GameController.isFullHD() ? 84 : 58;
    }

    public void setCard(Card card) {
        this.card = card;
        Image image = new Image(getClass().getResource(card.getImagePath()).toString());
        setImage(image);
        setFitHeight(getCardHeight());
        setFitWidth(getCardWidth());
    }

    public void removeCard() {
        card = null;
        setImage(null);
    }

    public Card getCard() {
        return card;
    }

}
