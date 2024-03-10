package com.ch.tusk.controllers;

import com.ch.tusk.customnodes.AlertFX;
import com.ch.tusk.customnodes.IconImageView;
import com.ch.tusk.json.Constants;
import com.ch.tusk.json.Json;
import com.ch.tusk.main.MusicPlayerFX;
import com.ch.tusk.mediaplayerutil.FileChooserManager;
import com.ch.tusk.mediaplayerutil.FolderChooserManager;
import com.ch.tusk.mediaplayerutil.StringFormatter;
import com.ch.tusk.model.Album;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import uk.co.caprica.vlcj.medialist.MediaList;
import uk.co.caprica.vlcj.medialist.MediaListRef;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

    @FXML
    private TreeView<String> mediaTreeView;

    //    @FXML
//    private ImageView coverAlbum;
//    @FXML
//    private VBox treeViewVBox;
//    @FXML
//    private TextField txtSongQuery, txtYoutube;
    private FileChooserManager fileChooserManager;
    private FolderChooserManager folderChooserManager;

    private Json json;

    @FXML
    private Button btnSearch, btnPlaylist, btnAlbum, btnSettings, btnTrack;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initialize();
    }

    private void initialize() {
        loadPlaybackController();
        setButtonIcons();
        initializeTreeView();
        setTreeViewListener();
        initializeGlobalVariables();
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
        Button[] buttons = {btnSearch, btnPlaylist, btnAlbum, btnTrack, btnSettings};

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
        TreeItem<String> root = new TreeItem<>("My Albums");
        mediaTreeView.setRoot(root);
        Platform.runLater(() -> {
            ScrollBar verticalBar = (ScrollBar) mediaTreeView.lookup(".scroll-bar:vertical");
            ScrollBar horizontalBar = (ScrollBar) mediaTreeView.lookup(".scroll-bar:horizontal");
//            verticalBar.setCursor(Cursor.V_RESIZE);
//            horizontalBar.setCursor(Cursor.H_RESIZE);
            root.setExpanded(true);
        });
    }

    /**
     * This method sets a listener for mouse press events on the mediaTreeView.
     * The listener performs several actions when a leaf node (representing a song) is selected:
     * 1. If the selected song is currently playing, it seeks to the start of the song and aborts further actions.
     * 2. If the selected song is not in the current playlist, it loads the playlist that contains the selected song.
     * 3. If a song is currently playing, it stops the playback.
     * 4. Finally, it starts playing the selected song.
     */
    private void setTreeViewListener() {
        // Keep in mind that the order of the branches corresponds to the order of the MediaListRefs
        mediaTreeView.setOnMousePressed(event -> {

            TreeItem<String> selectedItem = mediaTreeView.getSelectionModel().getSelectedItem();

            // If the selected item is not a "song", abort.
            if (selectedItem == null || !selectedItem.isLeaf()) {
                return;
            }

            String currentlyPlayedMrl = Constants.MEDIA_LIST_PLAYER.currentlyPlayedMrl();

            if (!currentlyPlayedMrl.isEmpty()) {
                // Check if the selected item is the same as the song currently playing. If so, seek to 0 and abort.
                if (selectedItem.getValue().equals(StringFormatter.getFileNameFromMrl(currentlyPlayedMrl))) {
                    Constants.MEDIA_LIST_PLAYER.seek(0);
                    return;
                }
            }

            List<String> currentPlaylistMrl = Constants.MEDIA_LIST_PLAYER.getMediaListPlayer().list().media().mrls();
            // Check if the current playlist contains the song playing right now/previously
            if (!playlistContainsSelectedItem(currentPlaylistMrl, selectedItem.getValue())) {
                loadPlaylist(selectedItem, mediaTreeView.getRoot().getChildren());
            }

            // Stop playback if a song is playing
            if (Constants.MEDIA_LIST_PLAYER.isPlaying()) {
                Constants.MEDIA_LIST_PLAYER.stopMedia();
            }

            playSelectedItem(selectedItem.getValue());
        });
    }

    /**
     * This method is used to load a playlist into the media player.
     * It first finds the index of the parent of the selected item in the tree view.
     * Then, it creates a new playlist and loads the album at the found index from a JSON file.
     * It adds all songs from the loaded album to the playlist.
     * After the playlist is ready, it sets the playlist to the media player and releases the resources.
     *
     * @param selectedItem The selected item in the tree view.
     * @param allChildren  All children of the root node in the tree view.
     */
    private void loadPlaylist(TreeItem<String> selectedItem, ObservableList<TreeItem<String>> allChildren) {
        int index = allChildren.indexOf(selectedItem.getParent());

        // Creation of a new playlist
        MediaList mediaList = Constants.MEDIA_LIST_PLAYER.createMediaList();

        ArrayList<Album> albums = json.loadAlbumArrayFromJson(Constants.ALBUM_JSON_LOCATION);
        Album selectedAlbum = albums.get(index);

        selectedAlbum.songs().forEach(songMrl -> mediaList.media().add(songMrl));

        // Playlist is ready
        MediaListRef mediaListRef = mediaList.newMediaListRef();

        // Setting the playlist to the Constants.MEDIA_LIST_PLAYER
        Constants.MEDIA_LIST_PLAYER.setMediaList(mediaListRef);

        // Releasing resources
        mediaList.release();
        mediaListRef.release();
    }

    /**
     * This method is used to play a selected item in the media player.
     * It first matches the selected item to a media reference in the media player.
     * Then, it plays the matched media reference.
     * Finally, it updates the status label in the playback controller to "Playing...".
     *
     * @param selectedItem The selected item to be played.
     */
    private void playSelectedItem(String selectedItem) {
        Constants.MEDIA_LIST_PLAYER.play(Constants.MEDIA_LIST_PLAYER.matchReference(selectedItem));
        Constants.PLAYBACK_CONTROLLER.setLblStatus("Playing...");
    }

    /**
     * This method checks if the current playlist contains the selected item.
     * It uses the Java 8 Stream API to iterate over the current playlist and checks if any of the media resource locators (MRLs) match the selected item.
     * The matching is done by comparing the file name extracted from the MRL with the selected item.
     *
     * @param currentPlaylistMrl A list of MRLs in the current playlist.
     * @param selectedItem       The selected item to be checked if it's in the playlist.
     * @return true if the selected item is in the playlist, false otherwise.
     */
    private boolean playlistContainsSelectedItem(List<String> currentPlaylistMrl, String selectedItem) {
        return currentPlaylistMrl.stream().anyMatch(mrl -> StringFormatter.getFileNameFromMrl(mrl).equals(selectedItem));
    }

    public TreeView<String> getMediaTreeView() {
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


    public void displayAlert() throws IOException {

        var alert = new AlertFX(Alert.AlertType.CONFIRMATION, "Media Discovery", "Reloading the entire library may take some time.",
                "Are you sure you want to proceed?", MusicPlayerFX.createCustomIcon());

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                FXMLLoader loaderLoadingScreen = new FXMLLoader(getClass().getResource("/fxml/loadingScreen.fxml"));
                Parent loadingRoot = null;
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
                            extractJSON(json, mediaTreeView);
                            return null;
                        }
                    };

                    backgroundTask.setOnSucceeded((WorkerStateEvent event) -> {
                        borderPane.setDisable(false);
                        loadingStage.close();
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

        });
    }

    /**
     * This method is used to extract album data from a JSON file and load it into the media tree view.
     * It first writes the album array to the JSON file. The album array is created from the media player's music directories,
     * which are obtained from the status JSON file.
     * Then, it loads the album array from the JSON file into the media tree view.
     * The loading is done on the JavaFX Application thread to ensure that the UI is updated correctly.
     *
     * @param json          The Json object used to handle JSON operations.
     * @param mediaTreeView The TreeView object that represents the media tree view in the UI.
     */
    public void extractJSON(Json json, TreeView<String> mediaTreeView) {

        try {
            json.writeAlbumArrayToJSON(json.createAlbumArray(Constants.MEDIA_LIST_PLAYER,
                    json.obtainMusicDirectories(json.loadStatusJson(Constants.STATUS_JSON_LOCATION).musicFolders())
            ), Constants.ALBUM_JSON_LOCATION);

        } catch (IOException ignored) {
        }

        Platform.runLater(() -> {
            json.loadAlbumArray(Constants.ALBUM_JSON_LOCATION, mediaTreeView,
                    Constants.PLAYBACK_CONTROLLER.getLblStatus());
        });
    }

    public void orderByYear() {
//        if (!songListObservable.isEmpty()) {
//            List<MusicFile> musicFilesToSort;
//
//            if (songListObservable.equals(allSongs)) {
//                musicFilesToSort = new ArrayList<>(songsArray);
//            } else {
//                musicFilesToSort = songListObservable.stream()
//                        .map(this::matchMusicFile)
//                        .collect(Collectors.toList());
//            }
//
//            musicFilesToSort.sort(Comparator.comparing(MusicFile::getYear, String.CASE_INSENSITIVE_ORDER));
//
//            if (ckbDoReverse.isSelected()) {
//                Collections.reverse(musicFilesToSort);
//            }
//
//            songListObservable.clear();
//
//            for (MusicFile musicFile : musicFilesToSort) {
//                songListObservable.add(StringFormatter.formatFileName(musicFile.getName()));
//            }
//            variables.setLblStatus("Ordered by year of release.");
//        }
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
