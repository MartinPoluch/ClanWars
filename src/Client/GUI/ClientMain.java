package Client.GUI;

import Client.GUI.controllers.LogInController;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.FileInputStream;

/**
 * Trieda zabezpečuje spustenie klientskej časti aplikácie.
 */
public class ClientMain extends Application {

    private static HostServices hostServices ;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        //hostServices = getHostServices();
        primaryStage.setTitle("Log in");
        primaryStage.setMaxHeight(LogInController.STAGE_HEIGHT);
        primaryStage.setMinHeight(LogInController.STAGE_HEIGHT);
        primaryStage.setMaxWidth(LogInController.STAGE_WIDTH);
        primaryStage.setMinWidth(LogInController.STAGE_WIDTH);
        Parent root = FXMLLoader.load(getClass().getResource("/Client/GUI/fxml/LogInScene.fxml"));
        root.getStylesheets().add("/Client/GUI/css/basic.css");
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        try {
            primaryStage.getIcons().add(new Image(getClass().getResource("/resources/Icon.png").toString()));
        } catch (Exception e) {
            System.err.println("Icon not found");
        }
        primaryStage.show();
    }

    public static HostServices getGlobalHostServices() {
        return null;
        //return hostServices ;
    }

}
