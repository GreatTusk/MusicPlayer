/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ch.tusk.mediaplayerutil;

import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

/**
 * @author f_776
 */
public class FolderChooserManager {

    private final DirectoryChooser directoryChooser;

    public FolderChooserManager() {
        directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Open Folder");
    }

    public String showFolderChooser(Stage primaryStage) {
        // Show the folder chooser dialog
        File selectedFolder = directoryChooser.showDialog(primaryStage);

        if (selectedFolder != null) {
            // Process the selected folder
            return selectedFolder.getAbsolutePath();
        } else {
            return "";
        }
    }

}
