package Client.GUI.controllers;

import GamePlay.Players.Player;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

/**
 * Trieda reprezentuje kontajner VBox na ktorom sa zobrazujú štatistiky zvoleného hráča.
 * Každý hráč má presne 4 štatistiky damage, protection, offensiveRange, defensiveRange
 */
public class StatsInfoController {

    @FXML private ImageView damageIcon;
    @FXML private Label damage;

    @FXML private ImageView protectionIcon;
    @FXML private Label protection;

    @FXML private ImageView offensiveRangeIcon;
    @FXML private Label offensiveRange;

    @FXML private ImageView defensiveRangeIcon;
    @FXML private Label defensiveRange;

    public void initialize() {
        final double WIDTH = GameController.isFullHD() ? 22 : 14;
        final double HEIGHT = GameController.isFullHD() ? 22 : 14;

        if (! GameController.isFullHD()) {
            damage.setStyle("-fx-font-size: 13px");
            protection.setStyle("-fx-font-size: 13px");
            offensiveRange.setStyle("-fx-font-size: 13px");
            defensiveRange.setStyle("-fx-font-size: 13px");
            defensiveRangeIcon.setVisible(false);
        }

        damageIcon.setFitWidth(WIDTH);
        damageIcon.setFitHeight(HEIGHT);

        protectionIcon.setFitWidth(WIDTH);
        protectionIcon.setFitHeight(HEIGHT);

        offensiveRangeIcon.setFitWidth(WIDTH);
        offensiveRangeIcon.setFitHeight(HEIGHT);

        defensiveRangeIcon.setFitWidth(WIDTH);
        defensiveRangeIcon.setFitHeight(HEIGHT);
    }

    public void refreshStats(Player player) {
        damage.setText(Integer.toString(player.getDamage()));
        protection.setText(Integer.toString(player.getProtection()));
        offensiveRange.setText(Integer.toString(player.getOffensiveRange()));
        defensiveRange.setText(Integer.toString(player.getDefensiveRange()));
    }
}
