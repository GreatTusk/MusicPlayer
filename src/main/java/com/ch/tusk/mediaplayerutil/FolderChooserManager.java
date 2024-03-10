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

/**
 * This class is used to manage the folder chooser dialog.
 * <p>
 * It contains a DirectoryChooser object that is used to show the folder chooser dialog.
 * The title of the dialog is set to "Open Folder".
 */
public class FolderChooserManager {

    // The DirectoryChooser object used to show the folder chooser dialog.
    private final DirectoryChooser directoryChooser;

    /**
     * This constructor is used to initialize the DirectoryChooser object and set the title of the dialog.
     */
    public FolderChooserManager() {
        directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Open Folder");
    }

    /**
     * This method is used to show the folder chooser dialog and return the absolute path of the selected folder.
     * <p>
     * If no folder is selected, it returns an empty string.
     *
     * @param primaryStage The primary stage on which the folder chooser dialog is shown.
     * @return String The absolute path of the selected folder, or an empty string if no folder is selected.
     */
    public String showFolderChooser(Stage primaryStage) {
        // Show the folder chooser dialog
        File selectedFolder = directoryChooser.showDialog(primaryStage);

        if (selectedFolder != null) {
            // If a folder is selected, return its absolute path
            return selectedFolder.getAbsolutePath();
        } else {
            // If no folder is selected, return an empty string
            return "";
        }
    }

}
