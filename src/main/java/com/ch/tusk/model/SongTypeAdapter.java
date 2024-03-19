package com.ch.tusk.model;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class SongTypeAdapter extends TypeAdapter<Song> {

    @Override
    public void write(JsonWriter out, Song song) throws IOException {
        out.beginObject();
        out.name("title").value(song.title());
        out.name("artist").value(song.artist());
        out.name("genre").value(song.genre());
        out.name("album").value(song.album());
        out.name("trackNumber").value(song.trackNumber());
        out.name("trackTotal").value(song.trackTotal());
        out.name("date").value(song.date());
        out.name("path").value(song.path());
        out.endObject();
    }

    @Override
    public Song read(JsonReader in) throws IOException {
        String title = "", artist = "", genre = "", album = "", trackNumber = "", trackTotal = "", date = "", path = "";

        in.beginObject();
        while (in.hasNext()) {
            String field = in.nextName();
            switch (field) {
                case "title" -> title = in.nextString();
                case "artist" -> artist = in.nextString();
                case "genre" -> genre = in.nextString();
                case "album" -> album = in.nextString();
                case "trackNumber" -> trackNumber = in.nextString();
                case "trackTotal" -> trackTotal = in.nextString();
                case "date" -> date = in.nextString();
                case "path" -> path = in.nextString();
                default -> in.skipValue();
            }
        }
        in.endObject();

        return new Song(title, artist, genre, album, trackNumber, trackTotal, date, path);
    }
}