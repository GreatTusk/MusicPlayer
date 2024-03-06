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

    @Override
    public void write(JsonWriter out, Album album) throws IOException {
        out.beginObject();
        out.name("albumName").value(album.albumName());
        out.name("songs").beginArray();
        for (String song : album.songs()) {
            out.value(song);
        }
        out.endArray();
        out.name("imageURL").value(album.imageURL());
        out.endObject();
    }

    @Override
    public Album read(JsonReader in) throws IOException {


        String albumName = "", coverUrl = "";
        List<String> songsArray = null;


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

    private List<String> readSongsArray(JsonReader in) throws IOException {
        List<String> songs = new ArrayList<>();
        in.beginArray();
        while (in.hasNext()) {
            songs.add(in.nextString());
        }
        in.endArray();
        return songs;
    }
}
