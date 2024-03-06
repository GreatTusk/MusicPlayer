/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ch.tusk.mediaplayerutil;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

/**
 * The FileChooserManager class is in charge of setting up a FileChooser object
 * and managing the paths of the files that are chosen by the user.
 *
 * @author f_776
 */
public class FileChooserManager {

    private final FileChooser fileChooser;

    /*
    The fileChooser is instantiated with a title.
     */
    public FileChooserManager() {
        fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        // Set the extension filter for audio files
        FileChooser.ExtensionFilter music = new FileChooser.ExtensionFilter("Audio Files", "*.mp3",
                "*.flac", "*.ogg", "*.m4a");

        fileChooser.getExtensionFilters().add(music);
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
    }

    /**
     * The openFileChooser method opens up a window to allow the user to select
     * a file. Then, if the File object is not null, the String representation
     * of the file's URI path is returned.
     *
     * @param primaryStage the Stage
     * @return String selectedFilePath
     */
    public File openFileChooser(Stage primaryStage) {
        return fileChooser.showOpenDialog(primaryStage);
    }


}
