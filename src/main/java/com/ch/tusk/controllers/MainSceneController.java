package com.ch.tusk.controllers;

import com.ch.tusk.customnodes.AlertFX;
import com.ch.tusk.customnodes.IconImageView;
import com.ch.tusk.json.Constants;
import com.ch.tusk.json.Json;
import com.ch.tusk.main.MusicPlayerFX;
import com.ch.tusk.mediaplayerutil.FileChooserManager;
import com.ch.tusk.mediaplayerutil.FolderChooserManager;
import com.ch.tusk.mediaplayerutil.ImageUtil;
import com.ch.tusk.model.AlbumTreeItem;
import com.ch.tusk.model.MediaTreeItem;
import com.ch.tusk.model.Song;
import com.ch.tusk.model.SongTreeItem;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainSceneController implements Initializable {

    //    @FXML
//    private MenuItem mnFolderFilter, mnYearFilter, mnAlbumFilter, mnArtistFilter;
//
//    @FXML
//    private CheckBox ckbDoReverse;
//    @FXML
//    private TextArea txtSongInfo;
//
//    @FXML
//    private CheckMenuItem includeSubfolders;
    @FXML
    private BorderPane borderPane;
    private String previousAlbum = "";
    @FXML
    private TreeView<MediaTreeItem> mediaTreeView;

    @FXML
    private Button btnHide, btnMinMaxWindow, btnClose;
    @FXML
    private ListView<MediaTreeItem> tracksListView;
    //    @FXML
//    private TextField txtSongQuery, txtYoutube;
    private FileChooserManager fileChooserManager;
    private FolderChooserManager folderChooserManager;

    private Json json;

    @FXML
    private Button btnSearch, btnPlaylist, btnAlbum, btnSettings, btnTrack;
    @FXML
    private HBox menuBar;
    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initialize();
    }

    public ListView<MediaTreeItem> getTracksListView() {
        return tracksListView;
    }

    private void initialize() {
        loadPlaybackController();
        setButtonIcons();
        initializeListView();
        initializeTreeView();
        initializeGlobalVariables();

        btnClose.setOnAction(e -> {
            Stage window = (Stage) borderPane.getScene().getWindow();
            window.fireEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSE_REQUEST));
        });

        btnHide.setOnAction(e -> {
            Stage window = (Stage) borderPane.getScene().getWindow();
            window.setIconified(true);
        });

        btnMinMaxWindow.setOnAction(e -> {
            Stage window = (Stage) borderPane.getScene().getWindow();
            window.setMaximized(!window.isMaximized());
        });

        menuBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        menuBar.setOnMouseDragged(event -> {
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setX(event.getScreenX() - xOffset);
            window.setY(event.getScreenY() - yOffset);
        });

        menuBar.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
                window.setMaximized(!window.isMaximized());
            }
        });

    }

    private void initializeListView() {
        tracksListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tracksListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && oldValue != newValue) {
                handleClickOnListView(newValue);
            }
        });
    }

    private void initializeGlobalVariables() {
        fileChooserManager = new FileChooserManager();
        folderChooserManager = new FolderChooserManager();
        json = new Json();
    }

    /**
     * This method sets the icons for the buttons in the application.
     * It iterates over an array of buttons and assigns each button an icon from the /images/ directory.
     * The icons are assigned in the order of the buttons in the array.
     * The icon for each button is created as an ImageView with the preferred height and width of the button.
     * The image for the ImageView is loaded from the /images/ directory, with the file name determined by the index of the button in the array plus 16.
     * The loaded image is then set as the graphic for the button.
     */
    private void setButtonIcons() {
        String path = "/images/";
        Button[] buttons = {btnSearch, btnPlaylist, btnAlbum, btnTrack, btnSettings, btnHide, btnMinMaxWindow, btnClose};

        for (int i = 0; i < buttons.length; i++) {
            var imageView = new IconImageView(buttons[i].getPrefHeight(), buttons[i].getPrefWidth());

            imageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(path + (i + 16) + ".png"))));
            buttons[i].setGraphic(imageView);
        }
    }


    /**
     * This method is responsible for loading the playback controller.
     * It uses the FXMLLoader to load the FXML file for the playback bar.
     * The loaded FXML file is then set as the bottom component of the border pane.
     * The controller for the loaded FXML file is retrieved and stored in a constant for future use.
     * If an IOException occurs during this process, it is ignored.
     */
    private void loadPlaybackController() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/playbackBar.fxml"));

            Parent playbackBarRoot = loader.load();
            borderPane.setBottom(playbackBarRoot);

            // Now get the controller
            Constants.PLAYBACK_CONTROLLER = loader.getController();
        } catch (IOException ignored) {
        }
    }

    private void initializeTreeView() {
        TreeItem<MediaTreeItem> root = new TreeItem<>();
        mediaTreeView.setRoot(root);
        Platform.runLater(() -> root.setExpanded(true));
        mediaTreeView.getSelectionModel().selectionModeProperty().set(SelectionMode.SINGLE);
        mediaTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && oldValue != newValue) {
                Platform.runLater(() -> handleClickOnTreeView(newValue));
            }
        });

    }

    public void handleClickOnTreeView(TreeItem<MediaTreeItem> selectedItem) {
        var value = (SongTreeItem) selectedItem.getValue();
        playSelectedItem(value, selectedItem);
    }

    public Optional<TreeItem<MediaTreeItem>> getSelectedTreeViewItem() {
        var selectedItem = mediaTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem != null && selectedItem.getValue() instanceof SongTreeItem) {
            return Optional.of(selectedItem);
        } else {
            return Optional.empty();
        }
    }

    public Optional<MediaTreeItem> getSelectedListViewItem() {
        var selectedItem = (SongTreeItem) tracksListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            return Optional.of(selectedItem);
        } else {
            return Optional.empty();
        }
    }

    public void handleClickOnListView(MediaTreeItem selectedItem) {
        playSelectedItem(selectedItem);
    }


    /**
     * This method is used to play a selected item in the media player.
     * It first matches the selected item to a media reference in the media player.
     * Then, it plays the matched media reference.
     * Finally, it updates the status label in the playback controller to "Playing...".
     */
    private void playSelectedItem(SongTreeItem songTreeItem, TreeItem<MediaTreeItem> songItem) {
        Constants.MEDIA_LIST_PLAYER.play(songTreeItem.song().path());
        Platform.runLater(() -> displaySongMetadata(songItem, songTreeItem));
    }

    private void displaySongMetadata(TreeItem<MediaTreeItem> songItem, SongTreeItem value) {
        Song song = value.song();
        var album = song.album();
        var artist = song.artist();
        var title = song.title();
        Constants.PLAYBACK_CONTROLLER.setLblSongName(title);
        Constants.PLAYBACK_CONTROLLER.setLblSongArtist(artist);

        if (!album.equals(previousAlbum)) {
            Constants.PLAYBACK_CONTROLLER.setLblSongAlbum(album);
            var albumTreeItem = songItem.getParent();
            var albumWrapper = (AlbumTreeItem) albumTreeItem.getValue();
            var imageView = (ImageView) albumTreeItem.getGraphic();

            String imageURL = albumWrapper.album().imageURL();
            Constants.PLAYBACK_CONTROLLER.setCoverArt(imageURL != null
                    ? ImageUtil.createThumbnail(
                    new Image(imageURL), 80) : imageView.getImage());

//                            Constants.PLAYBACK_CONTROLLER.setCoverArt(
//                                    imageView.getImage()
//                            );
        }

        previousAlbum = album;
        Constants.PLAYBACK_CONTROLLER.setLblStatus("Playing...");
    }

    private void playSelectedItem(MediaTreeItem songItem) {
        var value = (SongTreeItem) songItem;
        Constants.MEDIA_LIST_PLAYER.play(value.song().path());
        Platform.runLater(() -> {

            Song song = value.song();
            var album = song.album();
            var artist = song.artist();
            var title = song.title();
            Constants.PLAYBACK_CONTROLLER.setLblSongName(title);
            Constants.PLAYBACK_CONTROLLER.setLblSongArtist(artist);

            if (!album.equals(previousAlbum)) {
                Constants.PLAYBACK_CONTROLLER.setLblSongAlbum(album);

                String imageURL = getAlbumCoverURL(songItem);
                if (imageURL != null) {
                    Constants.PLAYBACK_CONTROLLER.setCoverArt(ImageUtil.createThumbnail(
                            new Image(imageURL), 75));
                }

            }

            previousAlbum = album;
        });
        Constants.PLAYBACK_CONTROLLER.setLblStatus("Playing...");
    }

    public String getAlbumCoverURL(MediaTreeItem mediaTreeItem) {
        Optional<TreeItem<MediaTreeItem>> optionalTreeItem = getMediaTreeView().getRoot().getChildren().stream().filter(treeItem -> treeItem.getValue().equals(mediaTreeItem)).findAny();
        if (optionalTreeItem.isPresent()) {
            var albumTreeItem = optionalTreeItem.get().getParent();
            var albumWrapper = (AlbumTreeItem) albumTreeItem.getValue();
            return albumWrapper.album().imageURL();
        } else {
            return null;
        }
    }

    public TreeView<MediaTreeItem> getMediaTreeView() {
        return mediaTreeView;
    }

    /**
     * Opens up a FileChooser, sets the selected file's name in a label and
     * plays the selected file.
     */
    public void openFileChooser() {

        /*
        (Stage) borderPane.getScene().getWindow() is the window that owns
        the current Scene and is necessary to set up the File Chooser, as documented
        in the showOpenDialog method in the FileChooser class.
         */
        File selectedSong = fileChooserManager.openFileChooser((Stage) borderPane.getScene().getWindow());

        if (selectedSong != null) {
//            byte[] bytes = selectedSong.getBytes();
//            selectedSong = new String(bytes, StandardCharsets.UTF_8);

            String[] audioFiles = selectedSong.list(Constants.AUDIO_NAME_FILTER);
            if (audioFiles != null) {
                for (String file : audioFiles) {
                    Constants.MEDIA_LIST_PLAYER.addPaths(selectedSong.getPath() + File.separator + file);
                }
            }

            Constants.MEDIA_LIST_PLAYER.start();

//            if (!songListObservable.contains("")) {
//                songListObservable.add("");
//            }
            Constants.PLAYBACK_CONTROLLER.setLblStatus("Loaded " + Constants.MEDIA_LIST_PLAYER.getPlaylistSize() + " songs!");
        }
    }

    public void openFile() {
        File selectedSong = fileChooserManager.openFileChooser((Stage) borderPane.getScene().getWindow());

        if (selectedSong != null) {
            System.out.println(selectedSong.getPath());
            try {
                Constants.MEDIA_LIST_PLAYER.play(escapeSpecialCharacters(selectedSong.getPath()));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    // Method to handle special characters in a file path
    public static String escapeSpecialCharacters(String filePath) {
        // Replace [ with \[ and ] with \]
        filePath = filePath.replace("[", "\\[");
        filePath = filePath.replace("]", "\\]");

        // Replace { with \{ and } with \}
        filePath = filePath.replace("{", "\\{");
        filePath = filePath.replace("}", "\\}");

        // Replace ( with \( and ) with \)
        filePath = filePath.replace("(", "\\(");
        filePath = filePath.replace(")", "\\)");

        return filePath;
    }

    /**
     * TODO - This method is not yet implemented.
     */
    public void addMusicFolder() {
        addMusicFolder(folderChooserManager.showFolderChooser((Stage) borderPane.getScene().getWindow()));
    }

    /**
     * This method is used to add a music folder to the application's music directories.
     * It first loads the current status from a JSON file, then tries to add the provided folder path to the music folders.
     * If the folder path is successfully added (i.e., it was not already present), it writes the updated status back to the JSON file and displays a success alert.
     * If the folder path was not added (i.e., it was already present), it displays an error alert.
     *
     * @param folderPath The path of the folder to be added to the music directories.
     */
    public void addMusicFolder(String folderPath) {
        // Load the current status from the JSON file
        var status = json.loadStatusJson(Constants.STATUS_JSON_LOCATION);

        // Try to add the provided folder path to the music folders
        if (status.musicFolders().add(folderPath)) {
            // If the folder path was successfully added, write the updated status back to the JSON file
            json.writeStatusJSON(Constants.STATUS_JSON_LOCATION, status);

            // Display a success alert
            var success = new AlertFX(Alert.AlertType.INFORMATION, "Media Discovery", "A new folder has been successfully added to the music directories.",
                    "Subsequent library creations will include this folder.", MusicPlayerFX.createCustomIcon());
            success.show();
        } else {
            // If the folder path was not added (i.e., it was already present), display an error alert
            var failure = new AlertFX(Alert.AlertType.ERROR, "Media Discovery", "The submitted folder was already registered.",
                    "Please submit a valid folder.", MusicPlayerFX.createCustomIcon());
            failure.show();
        }
    }


    public void reloadMusicLibrary() {

        var alert = new AlertFX(Alert.AlertType.CONFIRMATION, "Media Discovery", "Reloading the entire library may take some time.",
                "Are you sure you want to proceed?", MusicPlayerFX.createCustomIcon());

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                FXMLLoader loaderLoadingScreen = new FXMLLoader(getClass().getResource("/fxml/loadingScreen.fxml"));
                Parent loadingRoot;
                try {
                    loadingRoot = loaderLoadingScreen.load();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Stage loadingStage = MusicPlayerFX.setUpLoadingStage(new Scene(loadingRoot));

                Platform.runLater(() -> {
                    alert.close();
                    borderPane.setDisable(true);
                    Constants.MEDIA_LIST_PLAYER.clearPlaylist();
                    mediaTreeView.getRoot().getChildren().clear();

                    Task<Void> backgroundTask = new Task<>() {
                        @Override
                        protected Void call() {
                            json.extractJSON(mediaTreeView, tracksListView);
                            return null;
                        }
                    };

                    backgroundTask.setOnSucceeded((WorkerStateEvent event) -> {
                        borderPane.setDisable(false);
                        loadingStage.close();
                    });

                    backgroundTask.setOnFailed((WorkerStateEvent event) -> {
                        Throwable exception = backgroundTask.getException();
                        throw new RuntimeException(exception);
                    });

                    new Thread(backgroundTask).start();
                });
            }

        });
    }

    public void switchToAlbumPane() {
        mediaTreeView.toFront();
    }

    public void switchToTracksPane() {
        tracksListView.toFront();
    }


    public void orderByYear() {
    }

    public void filterSongsByName(ActionEvent e) {
//        if (!allSongs.isEmpty()) {
//            String text = txtSongQuery.getText().toLowerCase();
//            List<String> filteredSongs;
//
//            if (filteringBackup == null) {
//                filteredSongs = filterSongs(allSongs, text);
//            } else {
//                filteredSongs = filterSongs(filteringBackup, text);
//            }
//
//            setFilteredSongsToUI(filteredSongs);
//        }
    }

    private List<String> filterSongs(List<String> songs, String text) {
        return songs.stream()
                .filter(song -> song.toLowerCase().contains(text))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private void setFilteredSongsToUI(List<String> filteredSongs) {
    }

    public void filterByArtist(ActionEvent e) {
    }

    public void filterByYear(ActionEvent e) {

    }


}
