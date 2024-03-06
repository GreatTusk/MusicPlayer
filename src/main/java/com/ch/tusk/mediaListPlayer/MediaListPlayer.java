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
import javafx.scene.image.ImageView;
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
import java.util.stream.IntStream;

/**
 * @author f_776
 */
public class MediaListPlayer {

    private final MediaPlayerFactory factory;
    private final uk.co.caprica.vlcj.player.list.MediaListPlayer mediaListPlayer;
    private final MediaPlayer mediaPlayer;
    private final Image[] imageArray;
    private final ImageView imageView;
    private MediaList mediaList;
    private String previousAlbum = "";


    //private ArrayList<String> pathsArray;
    public MediaListPlayer() {
        imageArray = new Image[]{new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/5.png"))),
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/12.png"))),
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/11.png"))),
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/10.png")))
        };

        factory = new MediaPlayerFactory("--no-video");
        mediaListPlayer = factory.mediaPlayers().newMediaListPlayer();
        mediaPlayer = new AudioPlayerComponent().mediaPlayer();
        mediaList = factory.media().newMediaList();
        mediaListPlayer.mediaPlayer().setMediaPlayer(mediaPlayer);
        MediaListRef mediaListRef = mediaList.newMediaListRef();
        mediaListPlayer.list().setMediaList(mediaListRef);

        initialize();

        mediaListPlayer.controls().setMode(PlaybackMode.LOOP);
        mediaListPlayer.mediaPlayer().mediaPlayer().audio().setVolume(100);

        if (mediaPlayer.audio().isMute()) {
            mediaPlayer.audio().mute();
        }

        imageView = new ImageView();
        imageView.setFitWidth(30);
        imageView.setFitHeight(30);

        mediaList.release();
        mediaListRef.release();
        factory.release();

    }

    public MediaList createMediaList() {
        // Step 1: create a new MediaList from the factory
        // Step 2: populate it (Constants.MAIN_SCENE_CONTROLLER -> openFolder/openFile)
        return factory.media().newMediaList();
    }

    public void setMediaList(MediaListRef playlist) {

        mediaList = playlist.newMediaList();
        mediaListPlayer.list().setMediaList(playlist);

    }

    public void addPaths(String folderPath) {
        mediaList.media().add(folderPath);
    }

    public void setPlaybackMode(PlaybackMode state) {
        mediaListPlayer.controls().setMode(state);
    }

    private void addMediaListPlayerEventListener() {
        mediaListPlayer.events().addMediaListPlayerEventListener(new MediaListPlayerEventAdapter() {
            @Override
            public void mediaListPlayerFinished(uk.co.caprica.vlcj.player.list.MediaListPlayer mediaListPlayer) {

            }

            @Override
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
            public void finished(MediaPlayer mediaPlayer) {
                try {
                    MultipleSelectionModel<TreeItem<String>> selectionModel = Constants.MAIN_SCENE_CONTROLLER.getMediaTreeView().getSelectionModel();

                    TreeItem<String> selectedItem = selectionModel.getSelectedItem();

                    TreeItem<String> parentItem = selectedItem.getParent();

                    if (parentItem.getChildren().getLast().equals(selectedItem)) {

                        selectionModel.select(parentItem.getChildren().getFirst());

                    } else {
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
//                if (!muted) {
//                    Constants.PLAYBACK_CONTROLLER.setVolume(mediaPlayer.audio().volume());
//                } else {
//                    Constants.PLAYBACK_CONTROLLER.setVolume(0);
//                }

            }

            @Override
            public void volumeChanged(MediaPlayer mediaPlayer, float volume) {

                if (volume != -1) {
                    Platform.runLater(() -> {
                        if (volume == 0) {
                            imageView.setImage(imageArray[0]);
                            Constants.PLAYBACK_CONTROLLER.setLblVolumeImage(imageView);
                        } else if (volume < 0.33) {
                            imageView.setImage(imageArray[1]);
                            Constants.PLAYBACK_CONTROLLER.setLblVolumeImage(imageView);
                        } else if (volume < 0.66) {
                            imageView.setImage(imageArray[2]);
                            Constants.PLAYBACK_CONTROLLER.setLblVolumeImage(imageView);
                        } else {
                            imageView.setImage(imageArray[3]);
                            Constants.PLAYBACK_CONTROLLER.setLblVolumeImage(imageView);
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
            //Constants.PLAYBACK_CONTROLLER.backToDefault();
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

    public void play(String mrl){

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
        return IntStream.range(0, itemsInPlaylist.size()).filter(i -> StringFormatter.getFileNameFromMrl(itemsInPlaylist.get(i)).equals(media)).findFirst().orElse(-1);
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

    public void setVolume(int volume) {
        if (mediaPlayer.status().isPlaying()) {
            mediaPlayer.audio().setVolume(volume);
        }
    }

    public String currentlyPlayedMrl() {
        if (mediaPlayer.media().info() != null) {
            return mediaPlayer.media().info().mrl();
        }
        return "";
    }

}
