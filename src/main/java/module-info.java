module com.ch.tusk {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.media;

    requires uk.co.caprica.vlcj;
    requires uk.co.caprica.vlcj.natives;
    requires uk.co.caprica.vlcj.javafx;
    requires com.google.gson;
    requires com.sun.jna.platform;
    requires javafx.swing;

    opens com.ch.tusk.controllers to javafx.fxml;
    exports com.ch.tusk.model;
    exports com.ch.tusk.main;

}
