package Client.GUI.controllers;

import Client.GUI.ClientMain;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;

public class AboutController {

    @FXML private Hyperlink itemLink;
    @FXML private Hyperlink cardBackLink;
    @FXML private Hyperlink potionsLink;
    @FXML private Hyperlink otherCardsLink;
    @FXML private Hyperlink iconsLink;

    public void initialize() {
        itemLink.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ClientMain.getGlobalHostServices().showDocument("https://www.dreamstime.com/stock-illustration-ancient-battle-weapons-set-icons-stock-vector-illustration-isolated-white-background-image84998856");
            }
        });

        cardBackLink.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ClientMain.getGlobalHostServices().showDocument("http://www.sclance.com/pngs/playing-card-back-png/download.php?file=./playing_card_back_png_1044899.png");
            }
        });

        potionsLink.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ClientMain.getGlobalHostServices().showDocument("https://www.freepik.com/free-vector/realistic-pack-witch-elements_1315349.htm");
            }
        });

        otherCardsLink.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ClientMain.getGlobalHostServices().showDocument("https://www.dreamstime.com/bazzier_latest-illustrations-vectors-clipart_pg1");
            }
        });

        iconsLink.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ClientMain.getGlobalHostServices().showDocument("https://www.flaticon.com/packs/medieval-icon-collection");
            }
        });


    }
}
