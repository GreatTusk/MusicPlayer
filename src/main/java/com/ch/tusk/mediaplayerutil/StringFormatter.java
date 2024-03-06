/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ch.tusk.mediaplayerutil;

import javafx.util.Duration;

import java.net.URI;
import java.nio.file.Paths;

/**
 * Abstract class containing the formatting logic for media durations.
 *
 * @author f_776
 */
public abstract class StringFormatter {

    /**
     * Formats a Duration object into a string representation of hours, minutes,
     * and seconds.
     *
     * @param duration The Duration object to be formatted.
     * @return A formatted string in the "HH:mm:ss" format.
     */
    public static String formatDuration(Duration duration) {
        int hours = (int) duration.toHours();
        int minutes = (int) (duration.toMinutes() % 60);
        int seconds = (int) (duration.toSeconds() % 60);

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static String formatFileName(String fileName) {
        return fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf(".")) : fileName;
    }

    public static String getFileNameFromMrl(String mrl) {
        return getNameWithoutExtension(Paths.get(URI.create(mrl)).getFileName().toString());
    }

    /**
     * Helper method that returns a file's name without its extension,
     * considering that this will be its full name minus whatever is after
     * the last period on its name.
     *
     * @param fileName string to be transformed
     * @return a filename without any extensions
     */
    private static String getNameWithoutExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        } else {
            return fileName; // File has no extension
        }
    }
}
