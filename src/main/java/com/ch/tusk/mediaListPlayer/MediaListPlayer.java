/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ch.tusk.mediaListPlayer;


import com.ch.tusk.json.Constants;
import com.ch.tusk.mediaplayerutil.ImageUtil;
import com.ch.tusk.mediaplayerutil.StringFormatter;
import javafx.application.Platform;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.util.Duration;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.media.*;
import uk.co.caprica.vlcj.medialist.MediaList;
import uk.co.caprica.vlcj.medialist.MediaListRef;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventListener;
import uk.co.caprica.vlcj.player.base.State;
import uk.co.caprica.vlcj.player.component.AudioPlayerComponent;
import uk.co.caprica.vlcj.player.list.MediaListPlayerEventAdapter;
import uk.co.caprica.vlcj.player.list.PlaybackMode;

import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * @author f_776
 */
public class MediaListPlayer {

    private MediaPlayerFactory factory;
    private uk.co.caprica.vlcj.player.list.MediaListPlayer mediaListPlayer;
    private MediaPlayer mediaPlayer;
    private Image[] imageArray;
    private MediaList mediaList;
    private String previousAlbum = "";


    //private ArrayList<String> pathsArray;

    /**
     * The constructor for the MediaListPlayer class.
     * It initializes the image array, sets up the media player, initializes the media player event listeners,
     * sets the default player settings, and releases the initial resources.
     */
    public MediaListPlayer() {
        initializeImageArray();
        setupMediaPlayer();
        initialize();
        setDefaultPlayerSettings();
        releaseInitialResources();
    }

    /**
     * This method initializes the image array with the images located at the specified paths.
     */
    private void initializeImageArray() {
        imageArray = new Image[]{
                loadImage("/images/5.png"),
                loadImage("/images/15.png"),
                loadImage("/images/12.png"),
                loadImage("/images/11.png"),
                loadImage("/images/10.png")
        };
    }

    /**
     * This method loads an image from the specified path.
     *
     * @param path The path of the image to be loaded.
     * @return The loaded image.
     */
    private Image loadImage(String path) {
        return new Image(Objects.requireNonNull(getClass().getResourceAsStream(path)));
    }

    /**
     * This method sets up the media player.
     * It creates a new media player factory, media list player, media player, and media list.
     * It then sets the media player for the media list player and sets the media list for the media list player.
     */
    private void setupMediaPlayer() {
        factory = new MediaPlayerFactory("--no-video");
        mediaListPlayer = factory.mediaPlayers().newMediaListPlayer();
        mediaPlayer = new AudioPlayerComponent().mediaPlayer();
        mediaList = factory.media().newMediaList();
        mediaListPlayer.mediaPlayer().setMediaPlayer(mediaPlayer);
        MediaListRef mediaListRef = mediaList.newMediaListRef();
        mediaListPlayer.list().setMediaList(mediaListRef);
    }

    /**
     * This method sets the default settings for the media player.
     * It sets the playback mode to loop, sets the volume to 100, and mutes the media player if it is currently muted.
     */
    private void setDefaultPlayerSettings() {
        mediaListPlayer.controls().setMode(PlaybackMode.LOOP);
        mediaListPlayer.mediaPlayer().mediaPlayer().audio().setVolume(100);
        if (mediaPlayer.audio().isMute()) {
            mediaPlayer.audio().mute();
        }
    }

    /**
     * This method releases the initial resources used by the media list and the media player factory.
     */
    private void releaseInitialResources() {
        mediaList.release();
        mediaList.newMediaListRef().release();
        factory.release();
    }

    /**
     * This method creates a new MediaList using the MediaPlayerFactory.
     *
     * @return A new MediaList.
     */
    public MediaList createMediaList() {
        return factory.media().newMediaList();
    }

    /**
     * This method sets the MediaList for the MediaListPlayer.
     * It first creates a new MediaList from the provided MediaListRef,
     * then sets this MediaList as the MediaList for the MediaListPlayer.
     *
     * @param playlist The MediaListRef from which to create the new MediaList.
     */
    public void setMediaList(MediaListRef playlist) {
        mediaList = playlist.newMediaList();
        mediaListPlayer.list().setMediaList(playlist);
    }

    /**
     * This method adds a path to the MediaList.
     * The path should point to a folder containing media files.
     *
     * @param folderPath The path to the folder containing the media files.
     */
    public void addPaths(String folderPath) {
        mediaList.media().add(folderPath);
    }

    /**
     * This method sets the playback mode for the MediaListPlayer.
     * The playback mode determines the order in which the media in the MediaList are played.
     *
     * @param state The playback mode to be set.
     */
    public void setPlaybackMode(PlaybackMode state) {
        mediaListPlayer.controls().setMode(state);
    }

    private void addMediaListPlayerEventListener() {
        mediaListPlayer.events().addMediaListPlayerEventListener(new MediaListPlayerEventAdapter() {
            @Override
            public void mediaListPlayerFinished(uk.co.caprica.vlcj.player.list.MediaListPlayer mediaListPlayer) {

            }

            @Override
            /**
             * This method is triggered when the media list player moves to the next item in the media list.
             * It prepares the media for the next item and starts playing it.
             *
             * @param mediaListPlayer The media list player that is moving to the next item.
             * @param item The media reference of the next item in the media list.
             */
            public void nextItem(uk.co.caprica.vlcj.player.list.MediaListPlayer mediaListPlayer, MediaRef item) {
                mediaListPlayer.submit(() -> {
                    prepareMedia(matchReference(item));
                    start();
                });
            }

            @Override
            public void stopped(uk.co.caprica.vlcj.player.list.MediaListPlayer mediaListPlayer) {
            }
        });
    }

    private void addMediaPlayerEventListener() {
        mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventListener() {
            @Override
            public void mediaChanged(MediaPlayer mediaPlayer, MediaRef media) {

            }

            @Override
            public void opening(MediaPlayer mediaPlayer) {

            }

            @Override
            public void buffering(MediaPlayer mediaPlayer, float newCache) {

            }

            @Override
            public void playing(MediaPlayer mediaPlayer) {
                Platform.runLater(() -> Constants.PLAYBACK_CONTROLLER.setLblPlayIcon(getClass().getResourceAsStream("/images/14.png")));
            }

            @Override
            public void paused(MediaPlayer mediaPlayer) {
                Platform.runLater(() -> Constants.PLAYBACK_CONTROLLER.setLblPlayIcon(getClass().getResourceAsStream("/images/1.png")));
            }

            @Override
            public void stopped(MediaPlayer mediaPlayer) {

            }

            @Override
            public void forward(MediaPlayer mediaPlayer) {

            }

            @Override
            public void backward(MediaPlayer mediaPlayer) {

            }

            @Override
            /*
             * This method is triggered when the media player has finished playing the current media.
             * It selects the next item in the media list to be played.
             * If the current item is the last item in the list, it wraps around and selects the first item.
             *
             * @param mediaPlayer The media player that has finished playing the current media.
             */
            public void finished(MediaPlayer mediaPlayer) {
                try {
                    // Get the selection model of the media tree view
                    MultipleSelectionModel<TreeItem<String>> selectionModel = Constants.MAIN_SCENE_CONTROLLER.getMediaTreeView().getSelectionModel();
                    // Get the currently selected item
                    TreeItem<String> selectedItem = selectionModel.getSelectedItem();
                    // Get the parent of the selected item
                    TreeItem<String> parentItem = selectedItem.getParent();
                    // Get the first child of the parent item
                    TreeItem<String> firstChild = parentItem.getChildren().getFirst();
                    // Get the last child of the parent item
                    TreeItem<String> lastChild = parentItem.getChildren().getLast();

                    // If the selected item is the last child, select the first child
                    if (lastChild.equals(selectedItem)) {
                        selectionModel.select(firstChild);
                    } else {
                        // Otherwise, select the next sibling of the selected item
                        selectionModel.selectNext();
                    }
                } catch (Exception ignored) {
                }
            }

            @Override
            public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
                long length = mediaPlayer.status().length();
                String formattedCurrentTime = StringFormatter.formatDuration(Duration.millis(newTime));
                Platform.runLater(() -> {

                    Constants.PLAYBACK_CONTROLLER.setLblCurrentTime(formattedCurrentTime);
                    Constants.PLAYBACK_CONTROLLER.setPbgSong((double) newTime / length);
                });
            }

            @Override
            public void seekableChanged(MediaPlayer mediaPlayer, int newSeekable) {

            }

            @Override
            public void pausableChanged(MediaPlayer mediaPlayer, int newPausable) {

            }

            @Override
            public void snapshotTaken(MediaPlayer mediaPlayer, String filename) {

            }

            @Override
            public void lengthChanged(MediaPlayer mediaPlayer, long newLength) {

            }

            @Override
            public void videoOutput(MediaPlayer mediaPlayer, int newCount) {

            }

            @Override
            public void corked(MediaPlayer mediaPlayer, boolean corked) {

            }

            @Override
            public void muted(MediaPlayer mediaPlayer, boolean muted) {
                Platform.runLater(() -> Constants.PLAYBACK_CONTROLLER.setLblVolumeImage(imageArray[0]));
            }

            @Override
            public void volumeChanged(MediaPlayer mediaPlayer, float volume) {

                if (volume != -1 && !isMuted()) {
                    Platform.runLater(() -> {
                        if (volume == 0) {
                            Constants.PLAYBACK_CONTROLLER.setLblVolumeImage(imageArray[1]);
                        } else if (volume < 0.33) {
                            Constants.PLAYBACK_CONTROLLER.setLblVolumeImage(imageArray[2]);
                        } else if (volume < 0.66) {
                            Constants.PLAYBACK_CONTROLLER.setLblVolumeImage(imageArray[3]);
                        } else if (volume <= 1){
                            Constants.PLAYBACK_CONTROLLER.setLblVolumeImage(imageArray[4]);
                        }
                    });
                }
            }

            @Override
            public void audioDeviceChanged(MediaPlayer mediaPlayer, String audioDevice) {

            }

            @Override
            public void chapterChanged(MediaPlayer mediaPlayer, int newChapter) {

            }

            @Override
            public void error(MediaPlayer mediaPlayer) {

            }

            @Override
            public void mediaPlayerReady(MediaPlayer mediaPlayer) {
                long length = mediaPlayer.status().length();
                String formattedTotalDuration = StringFormatter.formatDuration(Duration.millis(length));
                MetaApi meta = mediaPlayer.media().meta();

                Platform.runLater(() -> {
                    String album = meta.get(Meta.ALBUM);
                    Constants.PLAYBACK_CONTROLLER.setLblDuration(formattedTotalDuration);
                    Constants.PLAYBACK_CONTROLLER.setLblSongName(meta.get(Meta.TITLE));
                    if (!album.equals(previousAlbum)) {
                        Constants.PLAYBACK_CONTROLLER.setLblSongAlbum(album);
                    }
                    Constants.PLAYBACK_CONTROLLER.setLblSongArtist(meta.get(Meta.ARTIST));

                    TreeItem<String> selectedItem = Constants.MAIN_SCENE_CONTROLLER.getMediaTreeView().getSelectionModel().getSelectedItem();

                    if (selectedItem.isLeaf() && !album.equals(previousAlbum)) {

                        Constants.PLAYBACK_CONTROLLER.setCoverArt(meta.get(Meta.ARTWORK_URL) != null
                                ? new Image(meta.get(Meta.ARTWORK_URL)) : ImageUtil.createThumbnail(
                                Constants.DEFAULT_ALBUM, 75));
                    }

                    previousAlbum = album;

                });

            }

            @Override
            public void positionChanged(MediaPlayer mp, float f) {

            }

            @Override
            public void titleChanged(MediaPlayer mp, int i) {

            }

            @Override
            public void scrambledChanged(MediaPlayer mp, int i) {

            }

            @Override
            public void elementaryStreamAdded(MediaPlayer mp, TrackType tt, int i) {

            }

            @Override
            public void elementaryStreamDeleted(MediaPlayer mp, TrackType tt, int i) {

            }

            @Override
            public void elementaryStreamSelected(MediaPlayer mp, TrackType tt, int i) {

            }

        });
    }

    private void addMediaEventListener() {

        mediaPlayer.events().addMediaEventListener(new MediaEventListener() {
            @Override
            public void mediaMetaChanged(Media media, Meta metaType) {

            }

            @Override
            public void mediaSubItemAdded(Media media, MediaRef newChild) {

            }

            @Override
            public void mediaDurationChanged(Media media, long newDuration) {

            }

            @Override
            public void mediaParsedChanged(Media media, MediaParsedStatus newStatus) {

            }

            @Override
            public void mediaSubItemTreeAdded(Media media, MediaRef item) {

            }

            @Override
            public void mediaThumbnailGenerated(Media media, Picture picture) {
            }

            @Override
            public void mediaFreed(Media media, MediaRef mr) {
            }

            @Override
            public void mediaStateChanged(Media media, State state) {

            }
        });
    }

    private void initialize() {
        addMediaListPlayerEventListener();
        addMediaEventListener();
        addMediaPlayerEventListener();
    }

    public void start() {
        mediaListPlayer.controls().play();
    }


    public uk.co.caprica.vlcj.player.list.MediaListPlayer getMediaListPlayer() {
        return mediaListPlayer;
    }

    /**
     * This method allows playing some file after it's been paused.
     */
    public void playMedia() {
        if (!mediaListPlayer.status().isPlaying()) {
            mediaListPlayer.controls().play();
        }
    }

    public boolean isPlaying() {
        return mediaListPlayer.status().isPlaying();
    }

    /**
     * This method stops playing whatever media is contained in the mediaPlayer
     * if it is not null.
     */
    public void stopMedia() {
        if (mediaPlayer.status().isPlaying()) {
            mediaListPlayer.controls().stop();
        }
    }

    /**
     * The resetMedia method resets the Media in the MediaPlayer by setting its
     * playback time to 0. It also resets the progress bar that keeps track of
     * its progress.
     */
    public void resetMedia() {
        if (mediaPlayer.status().isPlaying()) {
            mediaPlayer.controls().setTime(0);
        }
    }

    /**
     * These methods pause the media playback if the mediaPlayer has been
     * instantiated and the media is currently playing.
     */
    public void pauseMedia() {
        if (mediaPlayer.status().isPlaying() && mediaPlayer.status().canPause()) {
            mediaListPlayer.controls().pause();
        }

    }

    public void seek(double position) {
        if (mediaPlayer.status().isPlaying() && mediaPlayer.status().isSeekable()) {
            mediaPlayer.controls().setPosition((float) position);
        }
    }

    public void setRate(float rate) {
        if (mediaPlayer.status().isPlaying()) {
            mediaPlayer.controls().setRate(rate);
        }
    }

    public void releaseResources() {

        mediaPlayer.release();
        mediaListPlayer.release();


    }

    public void skip(long delta) {
        mediaPlayer.controls().skipTime(delta);
    }

    public void playNext() {
        MultipleSelectionModel<TreeItem<String>> selectionModel = Constants.MAIN_SCENE_CONTROLLER.getMediaTreeView().getSelectionModel();
        TreeItem<String> selectedItem = selectionModel.getSelectedItem();

        if (selectedItem.nextSibling() != null) {
            // If there is a next sibling, select it
            selectionModel.selectNext();
        } else {
            // If there is no next sibling, select the first child of the parent
            TreeItem<String> parentItem = selectedItem.getParent();
            if (parentItem != null && !parentItem.getChildren().isEmpty()) {
                selectionModel.select(parentItem.getChildren().getFirst());
            }
        }

        // Perform the media playback action
        mediaListPlayer.controls().playNext();
    }

    public void playPrevious() {
        MultipleSelectionModel<TreeItem<String>> selectionModel = Constants.MAIN_SCENE_CONTROLLER.getMediaTreeView().getSelectionModel();
        TreeItem<String> selectedItem = selectionModel.getSelectedItem();
        if (selectionModel.getSelectedItem().previousSibling() != null) {
            selectionModel.selectPrevious();

        } else {
            // If there is no previous sibling, select the last child of the parent
            TreeItem<String> parentItem = selectedItem.getParent();
            if (parentItem != null && !parentItem.getChildren().isEmpty()) {
                selectionModel.select(parentItem.getChildren().getLast());
            }
        }
        mediaListPlayer.controls().playPrevious();
    }

    public List<String> getItemsInPlaylist() {
        return mediaList.media().mrls();
    }

    public int getPlaylistSize() {
        return mediaList.media().count();
    }

    public void clearPlaylist() {
        stopMedia();
        mediaList.media().clear();
    }

    public double getCurrentSongDuration() {
        return mediaPlayer.status().length();
    }

    public void prepareMedia(int index) {
        MediaRef newMediaRef = mediaList.media().newMediaRef(index);
        mediaPlayer.media().prepare(newMediaRef);
        mediaPlayer.media().parsing().parse();
        newMediaRef.release();
    }

    public void play(int index) {

        prepareMedia(index);
        mediaListPlayer.controls().play(index);

    }

    public void play(String mrl) {

        mediaPlayer.media().startPaused(mrl);
        mediaPlayer.controls().play();
    }

    public void mute() {
        mediaPlayer.audio().mute();

    }

    public boolean isMuted() {
        return mediaPlayer.audio().isMute();
    }

    private int matchReference(MediaRef media) {
        List<String> itemsInPlaylist = getItemsInPlaylist();
        Media newMedia = media.newMedia();
        String currentlyPlayingMrl = newMedia.info().mrl();
        for (int i = 0; i < itemsInPlaylist.size(); i++) {
            if (itemsInPlaylist.get(i).equals(currentlyPlayingMrl)) {
                newMedia.release();
                return i;
            }
        }
        newMedia.release();
        return -1;
    }

    public int matchReference(String media) {
        List<String> itemsInPlaylist = getItemsInPlaylist();
        for (int i = 0; i < itemsInPlaylist.size(); i++) {
            if (StringFormatter.getFileNameFromMrl(itemsInPlaylist.get(i)).equals(media)) {
                return i;
            }
        }
        return -1;
    }

    private int matchReference() {
        List<String> itemsInPlaylist = getItemsInPlaylist();
        String mrl = mediaPlayer.media().info().mrl();
        for (int i = 0; i < itemsInPlaylist.size(); i++) {
            if (itemsInPlaylist.get(i).equals(mrl)) {
                return i;
            }
        }
        return -1;
    }

    public void shuffle() {
        Random random = new Random();
        play(random.nextInt(getPlaylistSize()));
        MultipleSelectionModel<TreeItem<String>> selectionModel = Constants.MAIN_SCENE_CONTROLLER.getMediaTreeView().getSelectionModel();
        selectionModel.select(selectionModel.getSelectedItem().getParent().getChildren().get(matchReference()));
    }

    public int getVolume() {
        return mediaPlayer.audio().volume();
    }

    /**
     * This method is used to set the volume of the media player.
     * It first checks if the media player is currently playing any media.
     * If media is playing, it sets the volume to the specified value.
     *
     * @param volume The volume level to be set. The value should be between 0 and 100.
     */
    public void setVolume(int volume) {
        if (mediaPlayer.status().isPlaying()) {
            mediaPlayer.audio().setVolume(volume);
        }
    }

    /**
     * This method is used to get the Media Resource Locator (MRL) of the currently playing media.
     * It first checks if the media player has any media loaded.
     * If media is loaded, it returns the MRL of the media.
     * If no media is loaded, it returns an empty string.
     *
     * @return The MRL of the currently playing media, or an empty string if no media is loaded.
     */
    public String currentlyPlayedMrl() {
        return mediaPlayer.media().info() != null ? mediaPlayer.media().info().mrl() : "";
    }
}
