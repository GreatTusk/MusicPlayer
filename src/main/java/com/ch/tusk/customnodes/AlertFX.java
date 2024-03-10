package com.ch.tusk.customnodes;

import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;

import java.util.Objects;

public class AlertFX extends Alert {

    /**
     * Constructs an AlertFX object.
     *
     * @param alertType   The type of the alert. This parameter determines the default
     *                    button types of the alert.
     * @param title       The title of the alert dialog.
     * @param headerText  The header text of the alert dialog.
     * @param contextText The context text of the alert dialog.
     * @param icon        The icon to display in the alert dialog.
     */
    public AlertFX(Alert.AlertType alertType, String title, String headerText, String contextText, ImageView icon) {
        super(alertType);  // Call the superclass constructor with the alert type

        setTitle(title);  // Set the title of the alert
        setHeaderText(headerText);  // Set the header text of the alert
        setContentText(contextText);  // Set the context text of the alert
        getDialogPane().setGraphic(icon);  // Set the icon of the alert

        // Get the DialogPane and set its styles
        // Add the stylesheet to the DialogPane
        getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm());
        // Add the style class to the DialogPane
        getDialogPane().getStyleClass().add("styled-alert");
    }

}
