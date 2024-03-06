/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ch.tusk.json;

import com.ch.tusk.controllers.LoadingScreenController;
import com.ch.tusk.customnodes.AlbumCoverImageView;
import com.ch.tusk.mediaListPlayer.MediaListPlayer;
import com.ch.tusk.mediametadata.NumericFilenameComparator;
import com.ch.tusk.mediaplayerutil.ImageUtil;
import com.ch.tusk.mediaplayerutil.StringFormatter;
import com.ch.tusk.model.Album;
import com.ch.tusk.model.AlbumTypeAdapter;
import com.ch.tusk.model.Status;
import com.ch.tusk.model.StatusTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import uk.co.caprica.vlcj.media.*;
import uk.co.caprica.vlcj.medialist.MediaList;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public boolean loadStatusJSON(String filePath) {

        try (Reader reader = new FileReader(filePath)) {

            // Deserialize the JSON from the file into ArrayList<Album>
            Status status = GSON_STATUS.fromJson(reader, Status.class);
            return
//                    status != null &&
                    status.isFirstTime();
        } catch (Exception ignored) {
        }
        return true;
    }

    public void extractJSON(MediaListPlayer mediaListPlayer, TreeView<String> mediaTreeView, LoadingScreenController loaderLoadingScreen) throws IOException {

        boolean loadStatusJSON = loadStatusJSON(Constants.STATUS_JSON_LOCATION);

        if (loadStatusJSON) {
            Platform.runLater(() -> loaderLoadingScreen.setLblProgress("Performing initial setup. This may take a while."));

            try {
                writeAlbumArrayToJSON(createAlbumArray(mediaListPlayer,
                        obtainMusicDirectories(
                                Constants.DEFAULT_MUSIC_FOLDER)
                ), Constants.ALBUM_JSON_LOCATION);

                writeStatusJSON(Constants.STATUS_JSON_LOCATION, false);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        Platform.runLater(() -> loaderLoadingScreen.setLblProgress("Loading albums..."));
        loadAlbumArray(Constants.ALBUM_JSON_LOCATION, mediaTreeView,
                Constants.PLAYBACK_CONTROLLER.getLblStatus());

    }

    public void writeStatusJSON(String jsonPath, boolean value) {
        try (Writer writer = new FileWriter(jsonPath)) {
            Status status = new Status(value);
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
    public void writeAlbumArrayToJSON(ArrayList<Album> albumArray, String jsonPath) throws IOException {

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
    public ArrayList<String> obtainMusicDirectories(String folderPath) {

        // The folderPath must not be null
        if (folderPath != null) {

            File rootFolder = new File(folderPath);

            if (rootFolder.isDirectory()) {
                ArrayList<File> allFolders = new ArrayList<>();
                Queue<File> folderQueue = new ArrayDeque<>();
                folderQueue.add(rootFolder);

                // Process folders using a while loop
                while (!folderQueue.isEmpty()) {
                    File currentFolder = folderQueue.poll();

                    // Check if the folder contains playable files
                    String[] audioFiles = currentFolder.list(Constants.AUDIO_NAME_FILTER);
                    if (audioFiles != null && audioFiles.length > 0) {
                        // if so add them to the array
                        allFolders.add(currentFolder);
                    }

                    // Get all subfolders and add them to the queue
                    File[] subfolders = currentFolder.listFiles(File::isDirectory);
                    if (subfolders != null) {
                        folderQueue.addAll(Arrays.asList(subfolders));
                    }
                }

                ArrayList<String> directories = new ArrayList<>();

                for (File folder : allFolders) {
                    directories.add(folder.getPath());
                }

                return directories;

            }

        }
        return null;
    }

    public boolean hasPlayableFile(File rootFolder) {
        Queue<File> folderQueue = new ArrayDeque<>();
        folderQueue.add(rootFolder);

        // Process folders using a while loop
        while (!folderQueue.isEmpty()) {
            File currentFolder = folderQueue.poll();

            // Check if the folder contains playable files
            String[] audioFiles = currentFolder.list(Constants.AUDIO_NAME_FILTER);
            if (audioFiles != null && audioFiles.length > 0) {
                // If a playable file is found, return true
                return true;
            }

            // Get all subfolders and add them to the queue
            File[] subfolders = currentFolder.listFiles(File::isDirectory);
            if (subfolders != null) {
                folderQueue.addAll(Arrays.asList(subfolders));
            }
        }

        // No playable file found in any folder
        return false;
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
    public ArrayList<Album> createAlbumArray(MediaListPlayer mediaListPlayer, ArrayList<String> directories) throws IOException {

        MediaList mediaList = mediaListPlayer.getMediaListPlayer().list().newMediaList();

        populateMediaList(directories, mediaList);

        //HashSet<String> albums = new HashSet<>();
        HashMap<String, List<String>> albumMap = new HashMap<>();
        Map<String, String> albumAndUrl = new HashMap<>();

        populateAlbumMaps(mediaList, albumMap, albumAndUrl);

        // Sorting the albumMap by keys in natural order
        List<Map.Entry<String, List<String>>> sortedAlbums = new ArrayList<>(albumMap.entrySet());
        sortedAlbums.sort(Map.Entry.comparingByKey());

        // Array to be written
        ArrayList<Album> albumArray = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : sortedAlbums) {
            populateAlbum(entry, albumArray, albumAndUrl);
        }

        return albumArray;
    }

    /**
     * Populates an Album array with entries provided inside a for-loop in writeAlbumArray(). Helper method.
     *
     * @param entry       of HashMap<String, List<String>> albumMap
     * @param albumArray  to be filled
     * @param albumAndUrl to obtain the URL of every album's cover art
     */
    private void populateAlbum(Map.Entry<String, List<String>> entry, ArrayList<Album> albumArray,
                               Map<String, String> albumAndUrl) {

        String album = entry.getKey();
        List<String> songs = entry.getValue();

        if (songs.size() >= 100) {
            songs.sort(new NumericFilenameComparator());
        }

        // Create a new Album record using the automatically generated constructor
        Album albumObject = new Album(album, songs, albumAndUrl.get(album));

        albumArray.add(albumObject);
    }

    /**
     * Populates a media list with each audio file within each directory in the
     * array of directories.
     *
     * @param directories the array of directores that contain audio files
     * @param mediaList   the mediaList that songs are added to
     */
    private void populateMediaList(ArrayList<String> directories, MediaList mediaList) {
        for (String directory : directories) {
            Path get = Paths.get(directory);
            String[] list = get.toFile().list(Constants.AUDIO_NAME_FILTER);
            if (list != null) {
                for (String file : list) {
                    mediaList.media().add(directory + File.separatorChar + file);
                }
            }
        }
    }

    /**
     * Parses media for each index in the MediaList. A CountDownLatch is used to
     * await for each parsing process to conclude. At the end, the
     * MediaEventListener is removed for cleanup reasons. Each media object
     * created here will be released in the populateAlbumMaps() method
     *
     * @param list   the mediaList containing each media file
     * @param indexl the index of the media to be parsed and returned
     * @return a parsed Media object
     * @throws Exception for any exception that could arise from the parsing
     */
    private Media getParsedMedia(MediaList list, int indexl) throws Exception {

        final Media media = list.media().newMedia(indexl);
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
    private void populateAlbumMaps(MediaList mediaList, HashMap<String, List<String>> albumMap, Map<String, String> albumAndUrl) {

        List<String> mrls = mediaList.media().mrls();
        for (int i = 0; i < mrls.size(); i++) {

            try {
                Media media = getParsedMedia(mediaList, i);
                MetaApi meta = Objects.requireNonNull(media).meta();
                String album = meta.get(Meta.ALBUM);
                String song = mrls.get(i);

                //Add the song to the album's list in the map
                albumMap.computeIfAbsent(album, k -> {
                    // This block is executed only if 'album' is not already present in albumMap
                    List<String> songs = new ArrayList<>();
                    albumAndUrl.put(album, meta.get(Meta.ARTWORK_URL));

                    return songs;
                }).add(song); // regardless of whether the key was present in the map or not, the song is added to the List<String> associated with the album.

                media.release();
            } catch (Exception ignored) {
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
    public void loadAlbumArray(String filePath, TreeView<String> mediaTreeView, Label lblStatus) {

        ArrayList<Album> albumArray = loadAlbumArrayFromJson(filePath);
//        // Send album map to MainSceneController
//
//        HashMap<String, String> albumCoverMap = new HashMap<>();
//
//        for (Album album : albumArray) {
//            albumCoverMap.put(album.getAlbumName(), album.getImageURL());
//        }
//
//        MainSceneController.albumCoverMap = albumCoverMap;

        if (albumArray != null) {

            createThumbnailFolder();

            for (int i = 0; i < albumArray.size(); i++) {
                populateMediaTreeView(albumArray.get(i), mediaTreeView.getRoot().getChildren(), i);
            }

            Platform.runLater(() -> lblStatus.setText("Loaded " + albumArray.size() + " albums!"));

        }

    }

    /**
     * Method that is executed inside for-loops for each album. It creates a
     * TreeItem to represent it, sets a thumbnail for it, and finally populates
     * it with the album's songs.
     *
     * @param album    the album object containing its info
     * @param children the children of the mediaTreeView's root
     */
    private void populateMediaTreeView(Album album, ObservableList<TreeItem<String>> children, int i) {

        TreeItem<String> treeItem = new TreeItem<>(album.albumName());

        children.add(treeItem);

        try {
            setTreeItemGraphic(treeItem, album, i);
        } catch (Exception ignored) {

        }

        // Iterate through the songs in the current album
        for (String song : album.songs()) {
            treeItem.getChildren().add(new TreeItem<>(StringFormatter.getFileNameFromMrl(song)));
        }

    }

    private void setTreeItemGraphic(TreeItem<String> treeItem, Album album, int i) throws IOException {
        AlbumCoverImageView albumCoverMediaView = new AlbumCoverImageView();
        String thumbnailPath = Constants.THUMBNAIL_LOCATION + "/" + i + ".png";
        File thumbnail = new File(thumbnailPath);
        if (!thumbnail.exists()) {
            saveThumbnail(album.imageURL(), i);
        }

        albumCoverMediaView.setImage(new Image(thumbnail.toURI().toURL().toString()));

        treeItem.setGraphic(albumCoverMediaView);
    }

    private void saveThumbnail(String imageURL, int i) {

        // Create a scaled thumbnail using the ImageUtil.createThumbnail method
        File thumbnail = new File(Constants.THUMBNAIL_LOCATION + "/" + i + ".png");

        // Convert the Image to a BufferedImage
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(ImageUtil.createThumbnail(getAlbumCoverImage(imageURL), 70), null);

        // Write the BufferedImage to the temporary file
        try {
            ImageIO.write(bufferedImage, "png", thumbnail);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void createThumbnailFolder() {

        File thumbnailFolder = new File(Constants.THUMBNAIL_LOCATION);
        if (!thumbnailFolder.exists()) {
            thumbnailFolder.mkdir();
        }

    }
}
