/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ch.tusk.model;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class StatusTypeAdapter extends TypeAdapter<Status> {

    @Override
    public void write(JsonWriter out, Status status) throws IOException {
        out.beginObject();
        out.name("isFirstTime").value(status.isFirstTime());
        out.endObject();
    }

    @Override
    public Status read(JsonReader in) throws IOException {
        boolean isFirstTime = false; // Default value
        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();
            if (name.equals("isFirstTime")) {
                isFirstTime = in.nextBoolean();
            } else {
                in.skipValue();
            }
        }
        in.endObject();

        return new Status(isFirstTime);
    }


}
