/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ch.tusk.json;

import com.ch.tusk.controllers.LoadingScreenController;
import com.ch.tusk.customnodes.IconImageView;
import com.ch.tusk.mediaListPlayer.MediaListPlayer;
import com.ch.tusk.mediaplayerutil.ImageUtil;
import com.ch.tusk.model.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import uk.co.caprica.vlcj.media.*;
import uk.co.caprica.vlcj.medialist.MediaList;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * @author f_776
 */
public class Json {

    private static final Gson GSON_ALBUM = new GsonBuilder()
            .registerTypeAdapter(Album.class, new AlbumTypeAdapter())
            .create();
    private static final Gson GSON_STATUS = new GsonBuilder()
            .registerTypeAdapter(Status.class, new StatusTypeAdapter())
            .create();

    public Json() {
    }

    public boolean isFirstRuntime(String filePath) {
        return loadStatusJson(filePath).isFirstTime();
    }

    public Status loadStatusJson(String filePath) {

        try (Reader reader = new FileReader(filePath)) {
            // Deserialize the JSON from the file into ArrayList<Album>
            return GSON_STATUS.fromJson(reader, Status.class);
        } catch (Exception ignored) {
            return null;
        }

    }

    public void extractJSON(MediaListPlayer mediaListPlayer, TreeView<MediaTreeItem> mediaTreeView,
                            ListView<MediaTreeItem> mediaListView,
                            LoadingScreenController loaderLoadingScreen) throws IOException {

        boolean loadStatusJSON = isFirstRuntime(Constants.STATUS_JSON_LOCATION);

        if (loadStatusJSON) {
            Platform.runLater(() -> loaderLoadingScreen.setLblProgress("Performing initial setup. This may take a while."));
            writeStatusJSON(Constants.STATUS_JSON_LOCATION, false);
            try {
                writeAlbumArrayToJSON(createAlbumSet(mediaListPlayer,
                        obtainMusicDirectories(
                                Constants.DEFAULT_MUSIC_FOLDER
                        )
                ), Constants.ALBUM_JSON_LOCATION);

            } catch (IOException ignored) {
            }
        }

        Platform.runLater(() -> {
            loaderLoadingScreen.setLblProgress("Loading albums...");
            loadAlbumArray(Constants.ALBUM_JSON_LOCATION, mediaTreeView, mediaListView,
                    Constants.PLAYBACK_CONTROLLER.getLblStatus());
        });


    }

    public void extractJSON(TreeView<MediaTreeItem> mediaTreeView, ListView<MediaTreeItem> mediaListView) {

        try {
            writeAlbumArrayToJSON(createAlbumSet(Constants.MEDIA_LIST_PLAYER,
                    obtainMusicDirectories(loadStatusJson(Constants.STATUS_JSON_LOCATION).musicFolders())
            ), Constants.ALBUM_JSON_LOCATION);

        } catch (IOException ignored) {
        }

        Platform.runLater(() -> {
            loadAlbumArray(Constants.ALBUM_JSON_LOCATION, mediaTreeView, mediaListView,
                    Constants.PLAYBACK_CONTROLLER.getLblStatus());
        });
    }

    public void writeStatusJSON(String jsonPath, boolean value) {
        try (Writer writer = new FileWriter(jsonPath)) {
            Status status = new Status(value, new HashSet<>());
            GSON_STATUS.toJson(status, Status.class, writer);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    public void writeStatusJSON(String jsonPath, Status status) {
        try (Writer writer = new FileWriter(jsonPath)) {
            GSON_STATUS.toJson(status, Status.class, writer);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }


    /**
     * Receives an array of Albums and writes it to a JSON file.
     *
     * @param albumArray array to be written
     * @param jsonPath   path to the json file
     * @throws IOException that could occur during the serializing process
     */
    public void writeAlbumArrayToJSON(Set<Album> albumArray, String jsonPath) throws IOException {

        // Iterating over the sorted entries and adding them to the children
        try (Writer writer = new FileWriter(jsonPath)) {
            GSON_ALBUM.toJson(albumArray, new TypeToken<ArrayList<Album>>() {
            }.getType(), writer);
        }
    }


    /**
     * Using the Gson library, a JSON file is read and deserialized into an
     * ArrayList of albums.
     *
     * @param filePath the path of the JSON file
     * @return an ArrayList of albums
     */
    public ArrayList<Album> loadAlbumArrayFromJson(String filePath) {
        try (Reader reader = new FileReader(filePath)) {

            // Deserialize the JSON from the file into ArrayList<Album>
            return GSON_ALBUM.fromJson(reader, new TypeToken<>() {
            });
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Creates an ArrayList that contains all directories within a directory
     * that contain audio files.
     *
     * @param folderPath the path of the root directory
     * @return an ArrayList of directories. null if the path provided is null or
     * if it is not a directory
     */
    public Set<String> obtainMusicDirectories(String folderPath) {

        // The folderPath must not be null
        if (folderPath != null) {
            return processMusicFolder(folderPath);

        }
        return null;
    }

    public Set<String> obtainMusicDirectories(Set<String> folderPaths) {
        Set<String> allMusicFolders = new HashSet<>();
        folderPaths.forEach(folder -> allMusicFolders.addAll(Objects.requireNonNull(processMusicFolder(folder))));
        return allMusicFolders;
    }

    private Set<String> processMusicFolder(String folder) {
        File rootFolder = new File(folder);
        Set<String> directories = new HashSet<>();

        if (rootFolder.isDirectory()) {
            Queue<File> folderQueue = new ArrayDeque<>(Collections.singleton(rootFolder));

            while (!folderQueue.isEmpty()) {
                File currentFolder = folderQueue.poll();

                if (hasPlayableFile(currentFolder)) {
                    directories.add(currentFolder.getPath());
                }

                File[] subfolders = currentFolder.listFiles(File::isDirectory);
                if (subfolders != null && subfolders.length != 0) {
                    Collections.addAll(folderQueue, subfolders);
                }
            }
        }

        return directories;
    }

    /**
     * This method checks if a given directory contains any playable audio files.
     *
     * @param rootFolder The directory to check for audio files.
     * @return true if the directory contains at least one playable audio file, false otherwise.
     */
    public boolean hasPlayableFile(File rootFolder) {
        // Get a list of all audio files in the directory.
        String[] audioFiles = rootFolder.list(Constants.AUDIO_NAME_FILTER);

        // Return true if the list of audio files is not null and its length is greater than 0.
        return audioFiles != null && audioFiles.length > 0;
    }

    /**
     * Creates a mediaList from all the audio files in the provided array of
     * directories, parses each Media to obtain their album name and cover art,
     * organizes them into a HashMap, which is then sorted by natural order,
     * creates Album objects to store each album's name, array of songs and
     * cover URL, and finally writes this array to a JSON file. This method is
     * meant to be invoked only the first time the program is executed, as it is
     * a lengthy process.
     *
     * @param mediaListPlayer mediaListPlayer that is used like a factory
     * @param directories     array of directories that contain audioFiles
     * @return arrayList of albums
     */
    public Set<Album> createAlbumSet(MediaListPlayer mediaListPlayer, Set<String> directories) throws IOException {

        MediaList mediaList = mediaListPlayer.getMediaListPlayer().list().newMediaList();

        populateMediaList(directories, mediaList);

        HashMap<String, List<Song>> albumMap = new HashMap<>();
        Map<String, String> albumAndUrl = new HashMap<>();

        populateAlbumMaps(mediaList, albumMap, albumAndUrl);

        Set<Album> albumSet = new TreeSet<>();
        albumMap.entrySet().forEach(album -> populateAlbum(album, albumSet, albumAndUrl));

        return albumSet;
    }

    /**
     * Populates an Album array with entries provided inside a for-loop in writeAlbumArray(). Helper method.
     *
     * @param entry       of HashMap<String, List<String>> albumMap
     * @param albumArray  to be filled
     * @param albumAndUrl to obtain the URL of every album's cover art
     */
    private void populateAlbum(Map.Entry<String, List<Song>> entry, Set<Album> albumArray,
                               Map<String, String> albumAndUrl) {

        String albumName = entry.getKey();
        List<Song> songsList = entry.getValue();

        songsList.sort(Comparator.naturalOrder());

        // Create a new Album record using the automatically generated constructor
        Album albumObject = new Album(albumName, songsList, albumAndUrl.get(albumName));

        albumArray.add(albumObject);
    }

    /**
     * Populates a media list with each audio file within each directory in the
     * array of directories.
     *
     * @param directories the array of directores that contain audio files
     * @param mediaList   the mediaList that songs are added to
     */
    private void populateMediaList(Set<String> directories, MediaList mediaList) {
        Set<String> songPaths = new HashSet<>();
        directories.forEach(directory -> {
            var songList = new File(directory).listFiles(Constants.AUDIO_NAME_FILTER);
            if (songList != null) {
                Arrays.stream(songList).forEach(file -> songPaths.add(file.getPath()));
            }
        });

        var media = mediaList.media();
        songPaths.forEach(media::add);

    }

    /**
     * Parses media for each index in the MediaList. A CountDownLatch is used to
     * await for each parsing process to conclude. At the end, the
     * MediaEventListener is removed for cleanup reasons. Each media object
     * created here will be released in the populateAlbumMaps() method
     *
     * @param list  the mediaList containing each media file
     * @param index the index of the media to be parsed and returned
     * @return a parsed Media object
     * @throws Exception for any exception that could arise from the parsing
     */
    private Media getParsedMedia(MediaList list, int index) throws Exception {

        final Media media = list.media().newMedia(index);
        final CountDownLatch latch = new CountDownLatch(1);

        MediaEventListener listener = new MediaEventAdapter() {
            @Override
            public void mediaParsedChanged(Media media, MediaParsedStatus newStatus) {
                if (newStatus == MediaParsedStatus.DONE) {
                    latch.countDown();
                }
            }
        };

        try {
            media.events().addMediaEventListener(listener);
            if (media.parsing().parse()) {
                latch.await();
                return media;
            } else {
                return null;
            }
        } finally {
            media.events().removeMediaEventListener(listener);
        }
    }

    /**
     * Populates Album maps containing key value pairs for an album and its list
     * of songs, and an album and its cover art URL. Each media object is
     * released after its metadata is stored. When the for loop concludes, the
     * MediaList is released as well, as it will no longer be used from this
     * point onwards.
     * <p>
     * Media and MediaList objects should always be released after they no
     * longer serve a use.
     *
     * @param mediaList   media list to obtain audio files from
     * @param albumMap    map to be populated
     * @param albumAndUrl map to be populated
     */
    private void populateAlbumMaps(MediaList mediaList, HashMap<String, List<Song>> albumMap, Map<String, String> albumAndUrl) {

        List<String> mrls = mediaList.media().mrls();

        for (int i = 0; i < mrls.size(); i++) {

            try {
                Media media = getParsedMedia(mediaList, i);
                MetaApi meta = Objects.requireNonNull(media).meta();

                String tempTitle = meta.get(Meta.TITLE);
                String songTitle = tempTitle == null || tempTitle.isBlank() ? "UNKNOWN" : tempTitle;

                String tempArtist = meta.get(Meta.ARTIST);
                String songArtist = tempArtist == null || tempArtist.isBlank() ? "UNKNOWN" : tempArtist;

                String tempGenre = meta.get(Meta.GENRE);
                String songGenre = tempGenre == null || tempGenre.isBlank() ? "UNKNOWN" : tempGenre;

                String tempAlbum = meta.get(Meta.ALBUM);
                String songAlbum = tempAlbum == null || tempAlbum.isBlank() ? "UNKNOWN" : tempAlbum;

                String tempTrackNumber = meta.get(Meta.TRACK_NUMBER);
                String songTrackNumber = tempTrackNumber == null || tempTrackNumber.isBlank() ? "UNKNOWN" : tempTrackNumber;

                String tempTrackTotal = meta.get(Meta.TRACK_TOTAL);
                String songTrackTotal = tempTrackTotal == null || tempTrackTotal.isBlank() ? "UNKNOWN" : tempTrackTotal;

                String tempDate = meta.get(Meta.DATE);
                String songDate = tempDate == null || tempDate.isBlank() ? "UNKNOWN" : tempDate;

                String songPath = mrls.get(i);
                var song = new Song(songTitle, songArtist, songGenre, songAlbum, songTrackNumber, songTrackTotal, songDate, songPath);

                //Add the song to the songAlbum's list in the map
                albumMap.computeIfAbsent(songAlbum, k -> {
                    // This block is executed only if 'songAlbum' is not already present in albumMap
                    List<Song> songs = new ArrayList<>();
                    albumAndUrl.put(songAlbum, meta.get(Meta.ARTWORK_URL));

                    return songs;
                }).add(song); // regardless of whether the key was present in the map or not, the song is added to the associated with the songAlbum.

                media.release();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        mediaList.release();
    }

    /**
     * Returns the Image of an album's cover. If the Image object creation
     * throws an exception, a generic cover will be assigned instead.
     *
     * @param coverPath cover's path
     * @return the cover's Image
     */
    private Image getAlbumCoverImage(String coverPath) {
        Image cover;
        try {
            cover = new Image(coverPath);
        } catch (Exception e) {
            cover = Constants.DEFAULT_ALBUM;
        }
        return cover;
    }

    /**
     * Reads and loads an ArrayList of albums from a JSON. It is then used to
     * populate the mediaTreeView.
     *
     * @param filePath      the path of the JSON file
     * @param mediaTreeView treeView to be populated
     * @param lblStatus     the statusLbl from playbackController
     */
    public void loadAlbumArray(String filePath, TreeView<MediaTreeItem> mediaTreeView, ListView<MediaTreeItem> mediaListView, Label lblStatus) {

        ArrayList<Album> albumArray = loadAlbumArrayFromJson(filePath);

        if (albumArray != null) {


            createThumbnailFolder();
            ObservableList<MediaTreeItem> songs = FXCollections.observableArrayList();

            for (int i = 0; i < albumArray.size(); i++) {
                populateMediaTreeView(albumArray.get(i), mediaTreeView.getRoot().getChildren(), i, songs);
            }

            mediaListView.setItems(songs);
            Platform.runLater(() -> lblStatus.setText("Loaded " + albumArray.size() + " albums!"));

        }

    }

    /**
     * Method that is executed inside for-loops for each album. It creates a
     * TreeItem to represent it, sets a thumbnail for it, and finally populates
     * it with the album's songs.
     *
     * @param album     the album object containing its info
     * @param rootItems the rootItems of the mediaTreeView's root
     */
    private void populateMediaTreeView(Album album, ObservableList<TreeItem<MediaTreeItem>> rootItems, int albumIndex,
                                       ObservableList<MediaTreeItem> songsList) {

        // Add each song from the album to the TreeItem as a child
        ObservableList<TreeItem<MediaTreeItem>> albumTreeItemChildren = FXCollections.observableArrayList();

        album.songs().forEach(
                song -> {
                    songsList.add(new SongTreeItem(song));
                    albumTreeItemChildren.add(new TreeItem<>(new SongTreeItem(song)));
                }
        );

        // No need to continue having the list of songs in memory
        album.songs().clear();

        // Creation of AlbumTreeItem
        var albumTreeItem = new TreeItem<MediaTreeItem>(new AlbumTreeItem(album));
        albumTreeItem.getChildren().setAll(albumTreeItemChildren);
        albumTreeItem.setExpanded(true);
        albumTreeItem.expandedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                albumTreeItem.setExpanded(true);
            }
        });

        // Add the AlbumTreeItem to the root
        rootItems.add(albumTreeItem);

        // Set the album cover as the graphic for the AlbumTreeItem
        try {
            setTreeItemGraphic(albumTreeItem, album, albumIndex);
        } catch (IOException ignored) {
        }
    }

    /**
     * This method is used to set the graphic for a TreeItem representing an album.
     * <p>
     * It first constructs the path to the thumbnail image for the album using the album's index.
     * If the thumbnail image does not exist, it is created using the saveThumbnail method.
     * An IconImageView object is then created and the thumbnail image is set as its image.
     * Finally, the IconImageView object is set as the graphic for the TreeItem.
     *
     * @param albumTreeItem The TreeItem for which the graphic is to be set.
     * @param album         The Album object representing the album.
     * @param albumIndex    The index of the album.
     * @throws IOException If an error occurs when creating the thumbnail image.
     */
    private void setTreeItemGraphic(TreeItem<MediaTreeItem> albumTreeItem, Album album, int albumIndex) throws IOException {

        // Construct the path to the thumbnail image
        String thumbnailPath = Constants.THUMBNAIL_LOCATION + "/" + albumIndex + ".png";
        File thumbnail = new File(thumbnailPath);

        // If the thumbnail image does not exist, create it
        if (!thumbnail.exists()) {
            saveThumbnail(album.imageURL(), albumIndex);
        }

        // Create an IconImageView object and set the thumbnail image as its image
        IconImageView albumCoverMediaView = new IconImageView(60, 60);
        albumCoverMediaView.setImage(new Image(thumbnail.toURI().toURL().toString()));

        // Set the IconImageView object as the graphic for the TreeItem
        albumTreeItem.setGraphic(albumCoverMediaView);
    }

    /**
     * This method is used to save a thumbnail image for an album.
     * <p>
     * It creates a scaled thumbnail of the album cover using the ImageUtil.createThumbnail method.
     * The thumbnail is saved as a PNG file in the directory specified by Constants.THUMBNAIL_LOCATION.
     * The filename of the thumbnail is the index of the album.
     *
     * @param imageURL   The URL of the album cover image.
     * @param albumIndex The index of the album.
     * @throws RuntimeException If an IOException occurs when writing the image file.
     */
    private void saveThumbnail(String imageURL, int albumIndex) {

        // Create a scaled thumbnail using the ImageUtil.createThumbnail method
        File thumbnail = new File(Constants.THUMBNAIL_LOCATION + "/" + albumIndex + ".png");

        // Convert the Image to a BufferedImage
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(ImageUtil.createThumbnail(getAlbumCoverImage(imageURL), 70), null);

        // Write the BufferedImage to the temporary file
        try {
            ImageIO.write(bufferedImage, "png", thumbnail);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * This method is used to create a directory for storing thumbnail images.
     * <p>
     * It first creates a File object representing the directory specified by Constants.THUMBNAIL_LOCATION.
     * If the directory does not exist, it is created using the mkdir method of the File class.
     */
    private void createThumbnailFolder() {

        // Create a File object representing the directory
        File thumbnailFolder = new File(Constants.THUMBNAIL_LOCATION);

        // Check if the directory exists
        if (!thumbnailFolder.exists()) {
            // If the directory does not exist, create it
            thumbnailFolder.mkdir();
        }

    }
}
