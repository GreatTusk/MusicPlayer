/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ch.tusk.model;

import java.util.List;

/**
 * @author f_776
 */
public record Album(String albumName, List<String> songs, String imageURL) {
}
