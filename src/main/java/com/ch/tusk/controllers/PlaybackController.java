package com.ch.tusk.controllers;

import com.ch.tusk.customnodes.IconImageView;
import com.ch.tusk.json.Constants;
import com.ch.tusk.mediaplayerutil.StringFormatter;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
import java.util.Random;
import java.util.ResourceBundle;


/**
 * @author f_776
 */
public class PlaybackController implements Initializable {

    private final int[] speedList = {25, 50, 75, 100, 125, 150, 175, 200};

    @FXML
    private ChoiceBox<String> speedPicker;
    @FXML
    private Slider volumeBar;
    @FXML
    private Button lblPlay, lblNextSong, lblPreviousSong, lblReset, lblForward, lblRewind, lblShuffle, lblVolume;
    @FXML
    private ImageView coverArt;
    @FXML
    private Label lblSongName, lblCurrentTime, lblDuration, lblSongAlbum, lblSongArtist, lblStatus;
    @FXML
    private ProgressBar pbgSong;
    private int volume;

    public PlaybackController() {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initialize();
    }

    private void initialize() {
        setButtonIcons();
        this.coverArt.setImage(Constants.DEFAULT_ALBUM);
        setUpEventListeners();
    }

    /**
     * This method sets up the event listeners for the volume bar, speed control, and action labels.
     * <p>
     * 1. The volume bar's value property is observed and whenever it changes, the volume of the media player is adjusted accordingly.
     * 2. The speed control is initialized.
     * 3. The action listeners for the labels are set.
     */
    private void setUpEventListeners() {
        // Add a listener to the volume bar's value property.
        // When the value changes, the volume of the media player is set to the new value.
        volumeBar.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) ->
                Constants.MEDIA_LIST_PLAYER.setVolume((int) (volumeBar.getValue())));

        // Initialize the speed control.
        initializeSpeedControl();

        // Set the action listeners for the labels.
        setLabelsActionListeners();
    }


    private void setButtonIcons() {
        String path = "/images/";
        Button[] buttons = new Button[]{lblPlay, lblNextSong, lblPreviousSong, lblReset, lblVolume, lblRewind, lblForward, lblShuffle};

        for (int i = 1; i <= buttons.length; i++) {

            var imageView = new IconImageView(buttons[i - 1].getMinHeight(), buttons[i - 1].getMinWidth());
            imageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(path + i + ".png"))));
            buttons[i - 1].setGraphic(imageView);

        }
    }

    public void setLblPlayIcon(InputStream image) {
        var imageView = (ImageView) this.lblPlay.getGraphic();
        imageView.setImage(new Image(image));
    }

    private void setLabelsActionListeners() {

        lblNextSong.setOnAction(event -> nextSong());
        lblPreviousSong.setOnAction(event -> previousSong());
        lblReset.setOnAction(event -> resetMedia());
        lblForward.setOnAction(event -> forward());
        lblRewind.setOnAction(event -> rewind());
        lblShuffle.setOnAction(event -> shuffle());
        lblVolume.setOnAction(event ->
                Platform.runLater(() -> {
                    if (Constants.MEDIA_LIST_PLAYER.isNotMuted()) {
                        volume = Constants.MEDIA_LIST_PLAYER.getVolume();
                    } else {
                        volumeBar.setValue(volume);
                    }
                    Constants.MEDIA_LIST_PLAYER.mute();
                }));
        lblPlay.setOnAction(
                event -> {
                    if (Constants.MEDIA_LIST_PLAYER.isPlaying()) {
                        Constants.MEDIA_LIST_PLAYER.pauseMedia();
                    } else {
                        playMedia();
                    }

                });


    }

    public void rewind() {
        Constants.MEDIA_LIST_PLAYER.skip(-10000);
        setLblStatus("Rewinded by 10 seconds.");
    }

    public void forward() {
        Constants.MEDIA_LIST_PLAYER.skip(10000);
        setLblStatus("Forwarded by 10 seconds.");
    }

    // On Mouse clicked
    public void changeProgress(MouseEvent e) {
        double progress = getProgressBarProgress(e);

        if (progress != 0) {
            updateProgressBar(progress);
            seekMediaPlayer(progress);
        }
    }

    public void shuffle() {
        Constants.MAIN_SCENE_CONTROLLER.getSelectedTreeViewItem().ifPresent(
                treeItem -> {
                    var album = treeItem.getParent().getChildren();
                    var index = album.indexOf(treeItem);
                    var size = album.size();
                    if (size > 1) {
                        var random = new Random();
                        int randomIndex;
                        do {
                            randomIndex = random.nextInt(size);
                        } while (randomIndex == index);
                        Constants.MAIN_SCENE_CONTROLLER.getMediaTreeView().getSelectionModel().select(album.get(randomIndex));
                    }

                }
        );
    }

    // On mouse released
    public void seekProgress(MouseEvent e) {
        double progress = getProgressBarProgress(e);
        seekMediaPlayer(progress);
    }

    // On mouse dragged
    public void dragProgressBar(MouseEvent e) {
        double progress = getProgressBarProgress(e);
        double songDuration = Constants.MEDIA_LIST_PLAYER.getCurrentSongDuration();
        double millisToSeek = progress * songDuration;
        setLblCurrentTime(StringFormatter.formatDuration(millisToSeek));
        updateProgressBar(progress);
    }

    // Private method to get progress from mouse event
    private double getProgressBarProgress(MouseEvent e) {
        double totalWidth = pbgSong.getWidth();
        double x = e.getX();
        return x / totalWidth;
    }

    // Private method to update the progress bar
    private void updateProgressBar(double progress) {
        pbgSong.setProgress(progress);
    }

    // Private method to seek the media player
    private void seekMediaPlayer(double progress) {
        double songDuration = Constants.MEDIA_LIST_PLAYER.getCurrentSongDuration();
        double millisToSeek = progress * songDuration;
        Constants.MEDIA_LIST_PLAYER.seek(millisToSeek / songDuration);
    }

    private void initializeSpeedControl() {

        for (int j : speedList) {
            speedPicker.getItems().add(j + "%");
        }
        speedPicker.getSelectionModel().select(3);

        speedPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            String value = speedPicker.getValue();
            int speed = Integer.parseInt(value.substring(0, value.indexOf("%")));
            Constants.MEDIA_LIST_PLAYER.setRate((float) (speed * 0.01));
        });

    }


    public void nextSong() {
        Constants.MEDIA_LIST_PLAYER.playNext();
    }

    public void previousSong() {
        Constants.MEDIA_LIST_PLAYER.playPrevious();
    }

    /**
     * Resumes the reproduction of a media after it's been paused or stopped.
     * Assigned to btnPlay.
     */
    public void playMedia() {
        Constants.MEDIA_LIST_PLAYER.playMedia();
        setLblStatus("Playing...");
    }

    /**
     * Resets the current media playback. Assigned to btnReset.
     */
    public void resetMedia() {
        Constants.MEDIA_LIST_PLAYER.resetMedia();
    }

    public void setPbgSong(double pbgSong) {
        this.pbgSong.setProgress(pbgSong);
    }

    public void setLblSongName(String lblSongName) {
        this.lblSongName.setText(lblSongName);
    }

    public void setLblCurrentTime(String lblCurrentTime) {
        this.lblCurrentTime.setText(lblCurrentTime);
    }

    public void setLblDuration(String lblDuration) {
        this.lblDuration.setText(lblDuration);
    }

    public void setLblSongAlbum(String lblSongAlbum) {
        this.lblSongAlbum.setText(lblSongAlbum);
    }

    public void setLblSongArtist(String lblSongArtist) {
        this.lblSongArtist.setText(lblSongArtist);
    }

    public void setCoverArt(Image coverArt) {
        this.coverArt.setImage(coverArt);
    }

    public void setLblVolumeImage(Image image) {
        var imageView = (ImageView) this.lblVolume.getGraphic();
        imageView.setImage(image);
    }

    public Label getLblStatus() {
        return lblStatus;
    }

    public void setLblStatus(String lblStatus) {
        this.lblStatus.setText(lblStatus);
    }


}
