package com.ch.tusk.model;

import com.ch.tusk.mediaplayerutil.StringFormatter;

public record SongTreeItem(Song song) implements MediaTreeItem{
    @Override
    public String toString() {
        return StringFormatter.getFileNameFromMrl(song.path());
    }

}
