package com.ch.tusk.json;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertFalse;

class JsonTest {

    @org.junit.jupiter.api.Test
    void checkObtainedSongs() {
        var json = new Json();
        ArrayList<String> strings = json.obtainMusicDirectories("E:\\Music");

//        assertNotNull(strings);

        assertFalse(strings.isEmpty());
    }

}