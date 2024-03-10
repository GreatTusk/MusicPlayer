/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ch.tusk.model;

import java.util.HashSet;
import java.util.Set;

/**
 * @author f_776
 */
public record Status(boolean isFirstTime, Set<String> musicFolders) {
    public Status() {
        this(false, new HashSet<>());
    }

}

