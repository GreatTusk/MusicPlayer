package com.ch.tusk.model;

public record Song(String title, String artist, String genre,
                   String album, String trackNumber, String trackTotal, String date,
                   String path) implements Comparable<Song> {
    @Override
    public int compareTo(Song other) {
        try {
            // Try to parse trackNumber to an integer and compare
            int thisTrackNumber = Integer.parseInt(this.trackNumber);
            int otherTrackNumber = Integer.parseInt(other.trackNumber);
            return Integer.compare(thisTrackNumber, otherTrackNumber);
        } catch (NumberFormatException e) {
            // If parsing fails, compare by title
            return this.title.compareTo(other.title);
        }
    }
}
