/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ch.tusk.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * @author f_776
 */
public class LoadingScreenController {

    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private HBox hbxTop, hbxBottom;
    @FXML
    private VBox vbxCenter;
    @FXML
    private Label lblProgress, lblTask;

    public ProgressIndicator getProgressIndicator() {
        return progressIndicator;
    }

    public void setProgressIndicator(ProgressIndicator progressIndicator) {
        this.progressIndicator = progressIndicator;
    }

    public HBox getHbxTop() {
        return hbxTop;
    }

    public void setHbxTop(HBox hbxTop) {
        this.hbxTop = hbxTop;
    }

    public HBox getHbxBottom() {
        return hbxBottom;
    }

    public void setHbxBottom(HBox hbxBottom) {
        this.hbxBottom = hbxBottom;
    }

    public VBox getVbxCenter() {
        return vbxCenter;
    }

    public void setVbxCenter(VBox vbxCenter) {
        this.vbxCenter = vbxCenter;
    }

    public Label getLblProgress() {
        return lblProgress;
    }

    public void setLblProgress(String lblProgress) {
        this.lblTask.setText(lblProgress);
    }
}
