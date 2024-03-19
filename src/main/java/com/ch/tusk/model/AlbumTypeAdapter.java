/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ch.tusk.model;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AlbumTypeAdapter extends TypeAdapter<Album> {

    /**
     * This method is used to write the JSON representation of an Album object.
     *
     * @param out   The JsonWriter object used to write the JSON output.
     * @param album The Album object that needs to be converted to JSON.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void write(JsonWriter out, Album album) throws IOException {
        out.beginObject();  // Start the JSON object.
        out.name("albumName").value(album.albumName());  // Write the album name.
        out.name("songs").beginArray();  // Start the songs array.
        SongTypeAdapter songAdapter = new SongTypeAdapter();
        for (Song song : album.songs()) {  // Iterate over each song on the album.
            songAdapter.write(out, song);  // Write the song to the JSON output.
        }
        out.endArray();  // End the songs array.
        out.name("imageURL").value(album.imageURL());  // Write the album image URL.
        out.endObject();  // End the JSON object.
    }

    /**
     * This method is used to read the JSON representation of an Album object.
     *
     * @param in The JsonReader object used to read the JSON input.
     * @return Album The Album object that is created from the JSON input.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public Album read(JsonReader in) throws IOException {
        String albumName = "", coverUrl = "";
        List<Song> songsArray = null;

        in.beginObject();
        while (in.hasNext()) {
            String field = in.nextName();
            switch (field) {
                case "albumName" -> albumName = in.nextString();
                case "songs" -> songsArray = readSongsArray(in);
                case "imageURL" -> coverUrl = in.nextString();
                default -> // Handle other fields if needed
                        in.skipValue();
            }
        }
        in.endObject();

        return new Album(albumName,
                songsArray,
                coverUrl);
    }

    /**
     * This method is used to read an array of songs from the JSON input.
     *
     * @param in The JsonReader object used to read the JSON input.
     * @return List<String> The list of songs that is created from the JSON input.
     * @throws IOException If an I/O error occurs.
     */
    private List<Song> readSongsArray(JsonReader in) throws IOException {
        List<Song> songs = new ArrayList<>();
        in.beginArray();
        SongTypeAdapter songAdapter = new SongTypeAdapter();
        while (in.hasNext()) {
            songs.add(songAdapter.read(in));
        }
        in.endArray();
        return songs;
    }
}
