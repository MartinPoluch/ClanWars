package Client.GUI;

import Client.GUI.controllers.PlayerBoxController;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Bounds;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Trieda zabezpečuje všetky animácie ktoré sa v hre nachádzajú.
 */
public class Animator {

    private TranslateTransition cardMoving;
    private FadeTransition eliminating;
    private boolean isAnimating;
    public static final int speedOfCardMoving = 650;

    public Animator() {
        isAnimating = false;

        final int speedOfEliminating = 1000;
        eliminating = new FadeTransition();
        eliminating.setDuration(Duration.millis(speedOfEliminating));

        cardMoving = new TranslateTransition();
        cardMoving.setDuration(Duration.millis(speedOfCardMoving));
    }

    /**
     * @return true = prebieha animácia
     */
    public boolean isAnimating() {
        return isAnimating;
    }

    public void setAnimating(boolean animating) {
        isAnimating = animating;
    }

    /**
     * Animácia karty ktorá práve bola použitá. Karta sa presunie z inventára hráča na vrchol balíčka.
     * @param source použitá karta
     * @param destination vrchol balíčka
     */
    public void moveToLastUsedCard(CardFrame source, ImageView destination) {
        isAnimating = true;
        cardMoving.setNode(source);
        Bounds boundCard = source.localToScene(source.getBoundsInLocal());
        Bounds boundsLastUsed = destination.localToScene(destination.getBoundsInLocal());
        double travelX = boundsLastUsed.getMaxX() - boundCard.getMaxX();
        cardMoving.setByX(travelX);
        double travelY = boundsLastUsed.getMaxY() - boundCard.getMaxY();
        cardMoving.setByY(travelY);
        cardMoving.play();
    }

    public TranslateTransition getCardMoving() {
        return cardMoving;
    }

    /**
     * Animácia postupného miznutia hráča ktorý bol emilinovaný z hry.
     * @param playerBoxController controller k eliminovanému hráčovi
     */
    public void eliminateAnimation(PlayerBoxController playerBoxController) {
        HBox box = playerBoxController.getPlayerBox();
        eliminating.setNode(box);
        eliminating.setFromValue(1.0);
        eliminating.setToValue(0);
        eliminating.play();
    }

    public FadeTransition getEliminating() {
        return eliminating;
    }
}
