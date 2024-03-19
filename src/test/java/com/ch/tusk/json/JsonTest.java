package com.ch.tusk.json;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;

class JsonTest {

    @org.junit.jupiter.api.Test
    void checkObtainedSongs() {
        var json = new Json();
        Set<String> strings = json.obtainMusicDirectories("E:\\Music");

//        assertNotNull(strings);

        assertFalse(strings.isEmpty());
    }

}