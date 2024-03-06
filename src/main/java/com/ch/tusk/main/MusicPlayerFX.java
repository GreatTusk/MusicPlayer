package com.ch.tusk.main;

import com.ch.tusk.json.Constants;
import com.ch.tusk.json.Json;
import com.ch.tusk.mediaListPlayer.MediaListPlayer;
import com.ch.tusk.mediametadata.ResourceExtractor;
import com.sun.jna.NativeLibrary;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * @author f_776
 */
public class MusicPlayerFX extends Application {


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        /*Static method that we inherit from the parent class, Application. Behind the scenes, the
        start method will be called
         */
//        NativeLibrary.addSearchPath("libvlc", JavaFXTesting.class.getResource("/org/openjfx/resources").toString());
//        System.setProperty("VLC_PLUGIN_PATH",  "C:\\Users\\f_776\\Documents\\vlcstuff\\vlcstuff\\win64\\plugins");
//        NativeDiscovery nativeDiscovery = new NativeDiscovery();
//        nativeDiscovery.discover();
//        System.out.println(nativeDiscovery.discoveredPath());
        launch(args);

    }

    public static Stage setUpLoadingStage(Scene loadingScene) {
        Stage loadingStage = new Stage();
        loadingStage.setScene(loadingScene);
        loadingStage.setResizable(false);
        loadingStage.getIcons().add(Constants.APP_ICON);
        loadingStage.setTitle("Music Player FX");
        loadingStage.show();
        return loadingStage;
    }

    private static void loadLibraries() {
        var e = new ResourceExtractor();

        if (!(e.doesFolderExist("MusicPlayerFX" + File.separatorChar + "vlcjPlugins") || e.doesFileExist("MusicPlayerFX", "status.json"))) {

            try {
                e.extractResourceToUserDir("MusicPlayerFX/vlcjPlugins",
                        "/vlc/vlcjLibWin64.zip", "vlclib.zip");
                e.extractFileToUserDir("MusicPlayerFX",
                        "/json/status.json",
                        "status.json");
            } catch (IOException ignored) {
            }


        }
        NativeLibrary.addSearchPath("libvlc", System.getProperty("user.home") + File.separator + "MusicPlayerFX" + File.separator + "vlcjPlugins");
//        new NativeDiscovery().discover();
    }

    public static ImageView createCustomIcon() {
        // Use an ImageView to display an image as an icon

        ImageView iconImageView = new ImageView(Constants.APP_ICON);
        iconImageView.setFitWidth(48); // Set the width of the icon
        iconImageView.setFitHeight(48); // Set the height of the icon
        return iconImageView;
    }

    private void setUpStage(Stage stage, Parent root) throws IOException {

        Scene scene = new Scene(root);
        stage.setMinHeight(720);
        stage.setMinWidth(700);

        // Adding CSS styling
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm());
        stage.setScene(scene);
        // Setting a title for the stage
        stage.setTitle("Music Player FX");

        stage.setResizable(true);
        //stage.setX(0);
        //stage.setY(0);

        // Adding the scene to the stage
        stage.getIcons().add(Constants.APP_ICON);
        // Set OnCloseRequestConfirmation using a lambda expresion
        stage.setOnCloseRequest(event -> {
            // The event is consumed so the cancel button functions accordingly. Otherwise, the program will close regardless
            // of the user input.
            event.consume();


            logout(stage);
        });
        stage.show();
    }

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/mainScene.fxml"));
        // Adding the root to the scene
        Parent root = loader.load();
        // Check if the resource is found
        Constants.MAIN_SCENE_CONTROLLER = loader.getController();

        FXMLLoader loaderLoadingScreen = new FXMLLoader(getClass().getResource("/fxml/loadingScreen.fxml"));

        Parent loadingRoot = loaderLoadingScreen.load();

        // The scene's constructor can also receive a color
        Scene loadingScene = new Scene(loadingRoot);
        //   loadingScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        Constants.MEDIA_LIST_PLAYER = new MediaListPlayer();
        Platform.runLater(() -> {

            Stage loadingStage = setUpLoadingStage(loadingScene);

            Task<Void> backgroundTask = new Task<>() {
                @Override
                protected Void call() {
                    loadLibraries();
                    try {
                        new Json().extractJSON(Constants.MEDIA_LIST_PLAYER, Constants.MAIN_SCENE_CONTROLLER.getMediaTreeView(), loaderLoadingScreen.getController());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return null;
                }
            };

            backgroundTask.setOnSucceeded((WorkerStateEvent event) -> {
                loadingStage.close();
                // Shows the scene
                try {
                    setUpStage(stage, root);
                } catch (IOException ignored) {
                }

            });

            backgroundTask.setOnFailed((WorkerStateEvent event) -> {
                Throwable exception = backgroundTask.getException();
                if (exception != null) {
                    System.out.println(exception);
                }
            });

            new Thread(backgroundTask).start();

        });

    }

    public void logout(Stage stage) {
        // Add a confirmation Alert
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("You're about to exit");
        alert.setContentText("Are you sure you want to exit?");

        Node iconG = createCustomIcon(); // Define a method to create a custom icon
        alert.getDialogPane().setGraphic(iconG);

        // Get the DialogPane and set its styles
        alert.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm());
        alert.getDialogPane().getStyleClass().add("styled-alert");

        if (alert.showAndWait().orElseThrow() == ButtonType.OK) {
            stage.close();
            Constants.MEDIA_LIST_PLAYER.releaseResources();
            System.exit(0);
        }
    }

}
