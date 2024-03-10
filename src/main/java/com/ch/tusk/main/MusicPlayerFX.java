package com.ch.tusk.main;

import com.ch.tusk.customnodes.AlertFX;
import com.ch.tusk.json.Constants;
import com.ch.tusk.json.Json;
import com.ch.tusk.mediaListPlayer.MediaListPlayer;
import com.ch.tusk.mediametadata.ResourceExtractor;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

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

    /**
     * This method is used to set up a loading stage.
     * <p>
     * It creates a new Stage object and sets the specified Scene object to it.
     * The stage is made non-resizable and the application icon is added to it.
     * The title of the stage is set to "Music Player FX" and the stage is shown.
     *
     * @param loadingScene The Scene object that needs to be set to the loading stage.
     * @return Stage The loading stage that is set up.
     */
    public static Stage setUpLoadingStage(Scene loadingScene) {
        Stage loadingStage = new Stage();
        loadingStage.setScene(loadingScene);
        loadingStage.setResizable(false);
        loadingStage.getIcons().add(Constants.APP_ICON);
        loadingStage.setTitle("Music Player FX");
        loadingStage.show();
        return loadingStage;
    }

    /**
     * This method is used to load the necessary libraries.
     * <p>
     * It creates a new ResourceExtractor object and checks if the necessary folders and files exist.
     * If they do not exist, it extracts the necessary resources to the user directory.
     * The resources include the VLCJ plugins and the status JSON file.
     */
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
        // The following commented lines are used to add the VLCJ plugins to the native library search path and discover the native libraries.
        // NativeLibrary.addSearchPath("libvlc", System.getProperty("user.home") + File.separator + "MusicPlayerFX" + File.separator + "vlcjPlugins");
        // new NativeDiscovery().discover();
    }

    /**
     * This method is used to create a custom icon.
     * <p>
     * It creates an ImageView object and sets the image to the application icon.
     * The dimensions of the icon are set to 48x48.
     *
     * @return ImageView The ImageView object that represents the custom icon.
     */
    public static ImageView createCustomIcon() {
        // Use an ImageView to display an image as an icon
        ImageView iconImageView = new ImageView(Constants.APP_ICON);
        iconImageView.setFitWidth(48); // Set the width of the icon
        iconImageView.setFitHeight(48); // Set the height of the icon
        return iconImageView;
    }

    /**
     * This method is used to set up the stage.
     * <p>
     * It creates a new Scene object with the specified root and sets it to the stage.
     * The minimum dimensions of the stage are set to 720x700.
     * The stage is made resizable and the application icon is added to it.
     * An event handler is added to the stage's close request to handle the logout process.
     *
     * @param stage The stage that needs to be set up.
     * @param root  The root of the scene that needs to be set to the stage.
     * @throws IOException If an I/O error occurs.
     */
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

    /**
     * This method is used to handle the logout process.
     * <p>
     * It creates a confirmation alert dialog to ask the user if they really want to exit.
     * If the user confirms, it closes the stage, releases the resources of the media player, and exits the system.
     *
     * @param stage The stage that needs to be closed when the user confirms to logout.
     */
    public void logout(Stage stage) {

        // Create a new AlertFX object with the type of CONFIRMATION.
        // Set the title to "Logout", the header to "You're about to exit", and the content to "Are you sure you want to exit?".
        // Use the createCustomIcon() method to create an icon for the alert.
        var alert = new AlertFX(Alert.AlertType.CONFIRMATION, "Logout", "You're about to exit", "Are you sure you want to exit?", createCustomIcon());

        // Get the DialogPane of the alert and add the "styled-alert" style class to it.
        alert.getDialogPane().getStyleClass().add("styled-alert");

        // Show the alert and wait for the user's response.
        alert.showAndWait()
                .ifPresent(response -> {
                    // If the user's response is OK, then close the stage, release the resources of the media player, and exit the system.
                    if (response == ButtonType.OK) {
                        stage.close();
                        Constants.MEDIA_LIST_PLAYER.releaseResources();
                        System.exit(0);
                    }
                });
    }

}
