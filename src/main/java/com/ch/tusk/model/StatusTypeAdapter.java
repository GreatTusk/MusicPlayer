/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ch.tusk.model;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class StatusTypeAdapter extends TypeAdapter<Status> {

    /**
     * This method is used to write the JSON representation of a Status object.
     *
     * @param out    The JsonWriter object used to write the JSON output.
     * @param status The Status object that needs to be converted to JSON.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void write(JsonWriter out, Status status) throws IOException {
        out.beginObject();  // Start the JSON object.
        out.name("isFirstTime").value(status.isFirstTime());  // Write the isFirstTime value.
        out.name("musicFolders").beginArray();  // Start the musicFolders array.
        for (String folder : status.musicFolders()) {  // Iterate over each folder in the musicFolders.
            out.value(folder);  // Write the folder to the JSON output.
        }
        out.endArray();  // End the musicFolders array.
        out.endObject();  // End the JSON object.
    }

    /**
     * This method is used to read the JSON representation of a Status object.
     *
     * @param in The JsonReader object used to read the JSON input.
     * @return Status The Status object that is created from the JSON input.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public Status read(JsonReader in) throws IOException {
        boolean isFirstTime = false; // Default value
        Set<String> musicFolders = null;

        in.beginObject();  // Start the JSON object.
        while (in.hasNext()) {  // While there are more elements in the JSON object.
            String fieldName = in.nextName();  // Get the name of the next field.
            switch (fieldName) {  // Switch on the field name.
                case "isFirstTime" ->
                        isFirstTime = in.nextBoolean();  // If the field name is "isFirstTime", read the boolean value.
                case "musicFolders" ->
                        musicFolders = readMusicFolders(in);  // If the field name is "musicFolders", read the musicFolders array.
            }
        }
        in.endObject();  // End the JSON object.

        return new Status(isFirstTime, musicFolders);  // Return a new Status object created from the JSON input.
    }

    /**
     * This method is used to read an array of music folders from the JSON input.
     *
     * @param in The JsonReader object used to read the JSON input.
     * @return Set<String> The set of music folders that is created from the JSON input.
     * @throws IOException If an I/O error occurs.
     */
    private Set<String> readMusicFolders(JsonReader in) throws IOException {
        Set<String> songs = new HashSet<>();  // Create a new HashSet to store the music folders.
        in.beginArray();  // Start the musicFolders array.
        while (in.hasNext()) {  // While there are more elements in the musicFolders array.
            songs.add(in.nextString());  // Add the next string in the musicFolders array to the HashSet.
        }
        in.endArray();  // End the musicFolders array.
        return songs;  // Return the HashSet of music folders.
    }


}
