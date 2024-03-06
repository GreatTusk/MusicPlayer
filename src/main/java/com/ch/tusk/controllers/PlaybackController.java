package com.ch.tusk.controllers;

import com.ch.tusk.json.Constants;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 * @author f_776
 */
public class PlaybackController implements Initializable {

    private final int[] speedList = {25, 50, 75, 100, 125, 150, 175, 200};
    //    @FXML
//    private HBox hbxStatus;
//    @FXML
//    private VBox vBoxPlayBackBar;
//    @FXML
//    private Button btnSearchSong;
    @FXML
    private ChoiceBox<String> speedPicker;
    @FXML
    private Slider volumeBar;
    @FXML
    private ImageView coverArt;
    @FXML
    private Label lblSongName, lblCurrentTime, lblDuration, lblSongAlbum, lblSongArtist, lblVolume, lblStatus,
            lblPlay, lblNextSong, lblPreviousSong, lblReset, lblForward, lblRewind, lblShuffle;
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

    private void setUpEventListeners() {
        volumeBar.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) ->
                Constants.MEDIA_LIST_PLAYER.setVolume((int) (volumeBar.getValue())));
        initializeSpeedControl();
        setLabelsActionListeners();
    }


    private void setButtonIcons() {
        String path = "/images/";
        Label[] labels = new Label[]{lblPlay, lblNextSong, lblPreviousSong, lblReset, lblVolume, lblRewind, lblForward, lblShuffle};

        for (int i = 1; i <= labels.length; i++) {
            ImageView imageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(path + i + ".png"))));
            imageView.setFitWidth(labels[i - 1].getMinWidth());
            imageView.setFitHeight(labels[i - 1].getMinHeight());
            // Set preserveRatio to true
            imageView.setPreserveRatio(true);

            // Set smooth to true for image smoothing
            imageView.setSmooth(true);
            labels[i - 1].setGraphic(imageView);

        }
    }

    public void setLblPlayIcon(InputStream image) {
        ImageView imageView = new ImageView(new Image(image));
        imageView.setFitWidth(this.lblPlay.getMinWidth());
        imageView.setFitHeight(this.lblPlay.getMinHeight());
        // Set preserveRatio to true
        imageView.setPreserveRatio(true);

        // Set smooth to true for image smoothing
        imageView.setSmooth(true);
        this.lblPlay.setGraphic(imageView);
    }

    private void setLabelsActionListeners() {
        lblPlay.setOnMousePressed(event -> {
            if (Constants.MEDIA_LIST_PLAYER.isPlaying()) {
                Constants.MEDIA_LIST_PLAYER.pauseMedia();
            } else {
                playMedia();
            }

        });
        lblNextSong.setOnMousePressed(event -> nextSong());
        lblPreviousSong.setOnMousePressed(event -> previousSong());
        lblReset.setOnMousePressed(event -> resetMedia());
        lblVolume.setOnMousePressed(event -> {
            if (!Constants.MEDIA_LIST_PLAYER.isMuted()) {
                volume = Constants.MEDIA_LIST_PLAYER.getVolume();
                volumeBar.setValue(0);
            } else {
                volumeBar.setValue(volume);
            }
            Constants.MEDIA_LIST_PLAYER.mute();

        });
        lblForward.setOnMousePressed(event -> forward());
        lblRewind.setOnMousePressed(event -> rewind());

        lblShuffle.setOnMousePressed(event -> Constants.MEDIA_LIST_PLAYER.shuffle());

    }

    public void rewind() {
        Constants.MEDIA_LIST_PLAYER.skip(-10000);
        setLblStatus("Rewinded by 10 seconds.");
    }

    public void forward() {
        Constants.MEDIA_LIST_PLAYER.skip(10000);
        setLblStatus("Ordered by albums.");
    }

    // On Mouse clicked
    public void changeProgress(MouseEvent e) {
        double progress = getProgressBarProgress(e);

        if (progress != 0) {
            updateProgressBar(progress);
            seekMediaPlayer(progress);
        }
    }

    // On mouse released
    public void seekProgress(MouseEvent e) {
        double progress = getProgressBarProgress(e);
        seekMediaPlayer(progress);
    }

    // On mouse dragged
    public void dragProgressBar(MouseEvent e) {
        double progress = getProgressBarProgress(e);
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

    public void setLblVolumeImage(ImageView lblVolume) {
        this.lblVolume.setGraphic(lblVolume);
    }

    public Label getLblStatus() {
        return lblStatus;
    }

    public void setLblStatus(String lblStatus) {
        this.lblStatus.setText(lblStatus);
    }


}
