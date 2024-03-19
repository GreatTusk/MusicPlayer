/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ch.tusk.model;

import java.util.List;

/*
 * @author f_776
 */

/**
 * A record representing an Album.
 *
 * @param albumName The name of the album.
 * @param songs     A list of songs on the album.
 * @param imageURL  The URL of the album's image.
 */
public record Album(String albumName, List<Song> songs, String imageURL) implements Comparable<Album> {
    @Override
    public int compareTo(Album other) {
        return this.albumName.compareTo(other.albumName);
    }
}