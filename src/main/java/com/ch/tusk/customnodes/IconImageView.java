/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ch.tusk.customnodes;

import javafx.scene.image.ImageView;

/**
 * @author f_776
 */
public class IconImageView extends ImageView {

    public IconImageView(double height, double width) {
        super();
        setFitHeight(height - 10);
        setFitWidth(width - 10);
        setSmooth(true);
        setPreserveRatio(true);
    }

}
