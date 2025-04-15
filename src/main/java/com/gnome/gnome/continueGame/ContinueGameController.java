package com.gnome.gnome.continueGame;

import com.gnome.gnome.camera.Camera;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import static javafx.scene.input.KeyCode.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class ContinueGameController implements Initializable {

    @FXML private BorderPane rootBorder;
    @FXML private StackPane centerStack;
    @FXML private Button centerMenuButton;

    // The full 30x30 map.
    private int[][] field30x30;

    // The camera instance.
    private Camera camera;

    // Popup for center menu.
    private Popup centerMenuPopup;

    private static final Logger logger = Logger.getLogger(ContinueGameController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        field30x30 = initMap(30, 30);

        camera = new Camera(field30x30, 15, 15, 15, 15);

        centerStack.setAlignment(Pos.CENTER);
        updateCameraViewport();

        centerMenuButton.setOnAction(e -> showCenterMenu());

        rootBorder.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                registerKeyHandlers(newScene);
            }
        });
    }

    /**
     * Initializes a rows x cols map filled with 0s and a border (value 1) around the edge.
     */
    private int[][] initMap(int rows, int cols) {
        int[][] map = new int[rows][cols];
        // Fill map with 0.
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                map[row][col] = 0;
            }
        }
        // Build border.
        for (int i = 0; i < rows; i++) {
            map[i][0] = 1;
            map[i][cols - 1] = 1;
            map[0][i] = 1;
            map[rows - 1][i] = 1;
        }
        return map;
    }

    /**
     * Shows the center menu popup at the center of the scene.
     * The popup displays a title and two buttons:
     * "Option 1" performs an action (currently logs a message),
     * "Go Back" returns the user to the main page.
     */
    private void showCenterMenu() {
        if (centerMenuPopup == null) {
            centerMenuPopup = new Popup();
            centerMenuPopup.setAutoHide(true);

            VBox menuBox = new VBox(20);
            menuBox.setAlignment(Pos.CENTER);
            menuBox.getStyleClass().add("menu-popup");
            menuBox.setStyle("-fx-background-color: #C0C0C0; -fx-padding: 20; -fx-background-radius: 20;");

            Label title = new Label("MENU");
            title.getStyleClass().add("menu-title");

            Button option1 = new Button("Option 1");
            option1.getStyleClass().add("menu-button");
            Button goBackButton = new Button("Go Back");
            goBackButton.getStyleClass().add("menu-button");

            option1.setOnAction(e -> {
                logger.info("Option 1 clicked.");
                centerMenuPopup.hide();
            });

            goBackButton.setOnAction(e -> {
                logger.info("Go Back clicked. Redirecting to main page.");
                try {
                    URL fxmlUrl = getClass().getResource("/com/gnome/gnome/pages/hello-view.fxml");
                    Objects.requireNonNull(fxmlUrl, "FXML file not found!");
                    Parent mainRoot = FXMLLoader.load(fxmlUrl);
                    Stage stage = (Stage) centerMenuButton.getScene().getWindow();
                    stage.getScene().setRoot(mainRoot);
                } catch (IOException ex) {
                    logger.severe("Failed to load main page: " + ex.getMessage());
                    ex.printStackTrace();
                }
                centerMenuPopup.hide();
            });

            menuBox.getChildren().addAll(title, option1, goBackButton);
            centerMenuPopup.getContent().add(menuBox);
        }
        Scene scene = centerMenuButton.getScene();
        if (scene != null) {
            Bounds bounds = scene.getRoot().localToScreen(scene.getRoot().getBoundsInLocal());
            double centerX = bounds.getMinX() + bounds.getWidth() / 2;
            double centerY = bounds.getMinY() + bounds.getHeight() / 2;
            centerMenuPopup.show(scene.getWindow(), centerX - 100, centerY - 75);
        }
    }
    /**
     * Registers key event handlers to move the player. If the center menu popup is open, movement is ignored.
     */
    private void registerKeyHandlers(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (centerMenuPopup != null && centerMenuPopup.isShowing()) {
                return;
            }
            KeyCode code = event.getCode();
            switch (code) {
                case LEFT:
                case A:
                    camera.movePlayerLeft();
                    logger.info("Moved player left.");
                    break;
                case RIGHT:
                case D:
                    camera.movePlayerRight();
                    logger.info("Moved player right.");
                    break;
                case UP:
                case W:
                    camera.movePlayerUp();
                    logger.info("Moved player up.");
                    break;
                case DOWN:
                case S:
                    camera.movePlayerDown();
                    logger.info("Moved player down.");
                    break;
                default:
                    break;
            }
            updateCameraViewport();
        });
        scene.getRoot().requestFocus();
    }

    /**
     * Refreshes the camera viewport by clearing and re-adding the GridPane from the camera.
     */
    private void updateCameraViewport() {
        GridPane cameraViewport = camera.getViewport();
        centerStack.getChildren().clear();
        cameraViewport.setAlignment(Pos.CENTER);
        StackPane.setAlignment(cameraViewport, Pos.CENTER);
        centerStack.setAlignment(Pos.CENTER);
        centerStack.getChildren().add(cameraViewport);
    }
}
