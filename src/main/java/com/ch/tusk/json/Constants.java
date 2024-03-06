/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ch.tusk.json;

import com.ch.tusk.controllers.MainSceneController;
import com.ch.tusk.controllers.PlaybackController;
import com.ch.tusk.mediaListPlayer.MediaListPlayer;
import javafx.scene.image.Image;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Objects;

/**
 *
 * @author f_776
 */
public final class Constants {
    
    public static final String ALBUM_JSON_LOCATION = System.getProperty("user.home") + "/MusicPlayerFX/albums.json";
    public static final String THUMBNAIL_LOCATION = System.getProperty("user.home") + "/MusicPlayerFX/Thumbnails";
    public static final String DEFAULT_MUSIC_FOLDER = System.getProperty("user.home") + File.separatorChar + "Music";
    public static final// Use a FilenameFilter to filter files by extensions
            FilenameFilter AUDIO_NAME_FILTER = (dir, name) -> name.toLowerCase().endsWith(".mp3")
            || name.toLowerCase().endsWith(".flac")
            || name.toLowerCase().endsWith(".ogg")
            || name.toLowerCase().endsWith(".m4a")
            || name.toLowerCase().endsWith(".wav")
            || name.toLowerCase().endsWith(".aac")
            || name.toLowerCase().endsWith(".wma")
            || (name.toLowerCase().endsWith(".aiff")) && !dir.isDirectory();
    
    public static final Image DEFAULT_ALBUM = new Image(Objects.requireNonNull(Constants.class.getResourceAsStream("/images/disc-icon.png")));
    public static final String STATUS_JSON_LOCATION = System.getProperty("user.home") + "/MusicPlayerFX/status.json";
    public static final Image APP_ICON = new Image(Objects.requireNonNull(Constants.class.getResourceAsStream("/logo.png")));

    public static MediaListPlayer MEDIA_LIST_PLAYER;
    public static PlaybackController PLAYBACK_CONTROLLER;
    public static MainSceneController MAIN_SCENE_CONTROLLER;
}
