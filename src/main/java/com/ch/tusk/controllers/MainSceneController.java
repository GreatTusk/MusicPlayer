package com.ch.tusk.controllers;

import com.ch.tusk.json.Constants;
import com.ch.tusk.json.Json;
import com.ch.tusk.main.MusicPlayerFX;
import com.ch.tusk.mediaListPlayer.MediaListPlayer;
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
import java.util.stream.Collectors;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initialize();
    }

    private void initialize() {
        loadPlaybackController();
        initializeTreeView();
        setTreeViewListener();
        initializeGlobalVariables();
    }

    private void initializeGlobalVariables() {
        fileChooserManager = new FileChooserManager();
        folderChooserManager = new FolderChooserManager();
        json = new Json();
    }

    private void loadPlaybackController() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/playbackBar.fxml"));

            Parent playbackBarRoot = loader.load();
            // Assuming you have a root container like BorderPane in your current FXML
            borderPane.setBottom(playbackBarRoot);

            // Now get the controller
            Constants.PLAYBACK_CONTROLLER = loader.getController();
        } catch (IOException ignored) {
        }
    }

    private void initializeTreeView() {
        TreeItem<String> root = new TreeItem<>("My playlists");
        mediaTreeView.setRoot(root);
        Platform.runLater(() -> {
            ScrollBar verticalBar = (ScrollBar) mediaTreeView.lookup(".scroll-bar:vertical");
            ScrollBar horizontalBar = (ScrollBar) mediaTreeView.lookup(".scroll-bar:horizontal");
//            verticalBar.setCursor(Cursor.V_RESIZE);
//            horizontalBar.setCursor(Cursor.H_RESIZE);
            root.setExpanded(true);
        });
    }

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

    private void loadPlaylist(TreeItem<String> selectedItem, ObservableList<TreeItem<String>> allChildren) {
        int index = allChildren.indexOf(selectedItem.getParent());

        // Creation of a new playlist
        MediaList mediaList = Constants.MEDIA_LIST_PLAYER.createMediaList();

        ArrayList<Album> albums = json.loadAlbumArrayFromJson(Constants.ALBUM_JSON_LOCATION);
        Album selectedAlbum = albums.get(index);

        for (String songMrl : selectedAlbum.songs()) {
            mediaList.media().add(songMrl);
        }

        // Playlist is ready
        MediaListRef mediaListRef = mediaList.newMediaListRef();

        // Setting the playlist to the Constants.MEDIA_LIST_PLAYER
        Constants.MEDIA_LIST_PLAYER.setMediaList(mediaListRef);

        // Releasing resources
        mediaList.release();
        mediaListRef.release();
    }

    private void playSelectedItem(String selectedItem) {
        Constants.MEDIA_LIST_PLAYER.play(Constants.MEDIA_LIST_PLAYER.matchReference(selectedItem));
        Constants.PLAYBACK_CONTROLLER.setLblStatus("Playing...");
    }

    private boolean playlistContainsSelectedItem(List<String> currentPlaylistMrl, String selectedItem) {
        for (String s : currentPlaylistMrl) {
            if (StringFormatter.getFileNameFromMrl(s).equals(selectedItem)) {
                return true;
            }
        }
        return false;
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

    public void openFolder() {
        openFolder(folderChooserManager.showFolderChooser((Stage) borderPane.getScene().getWindow()));
    }

    public void openFolder(String folderPath) {

    }

    private Alert setUpReloadingAlert() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Media Discovery");
        alert.setHeaderText("Reloading the entire library may take some time.");
        alert.setContentText("Are you sure you want to proceed?");
        alert.getDialogPane().setGraphic(MusicPlayerFX.createCustomIcon());
        // Get the DialogPane and set its styles
        alert.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm());
        alert.getDialogPane().getStyleClass().add("styled-alert");
        return alert;
    }

    public void displayAlert() throws IOException {

        Alert alert = setUpReloadingAlert();

        if (alert.showAndWait().get() == ButtonType.OK) {
            FXMLLoader loaderLoadingScreen = new FXMLLoader(getClass().getResource("/fxml/loadingScreen.fxml"));
            Parent loadingRoot = loaderLoadingScreen.load();
            Stage loadingStage = MusicPlayerFX.setUpLoadingStage(new Scene(loadingRoot));

            Platform.runLater(() -> {
                alert.close();
                borderPane.setDisable(true);
                Constants.MEDIA_LIST_PLAYER.clearPlaylist();
                mediaTreeView.getRoot().getChildren().clear();

                Task<Void> backgroundTask = new Task<>() {
                    @Override
                    protected Void call() {
                        extractJSON(json, Constants.MEDIA_LIST_PLAYER, mediaTreeView);
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

    }

    public void extractJSON(Json json, MediaListPlayer mediaListPlayer, TreeView<String> mediaTreeView) {

        try {
            json.writeAlbumArrayToJSON(json.createAlbumArray(Constants.MEDIA_LIST_PLAYER,
                    json.obtainMusicDirectories(
                            Constants.DEFAULT_MUSIC_FOLDER)
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
                .collect(Collectors.toList());
    }

    private void setFilteredSongsToUI(List<String> filteredSongs) {
    }

    public void filterByArtist(ActionEvent e) {
    }

    public void filterByYear(ActionEvent e) {

    }


}
