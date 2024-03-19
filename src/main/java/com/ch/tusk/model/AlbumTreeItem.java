package com.ch.tusk.model;

public record AlbumTreeItem(Album album) implements MediaTreeItem {
    @Override
    public String toString() {
        return album.albumName();
    }

}
