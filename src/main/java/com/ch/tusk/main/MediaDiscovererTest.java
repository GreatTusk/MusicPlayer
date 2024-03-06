/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ch.tusk.main;

import com.ch.tusk.json.Json;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.media.Media;
import uk.co.caprica.vlcj.media.MediaRef;
import uk.co.caprica.vlcj.media.discoverer.MediaDiscoverer;
import uk.co.caprica.vlcj.media.discoverer.MediaDiscovererCategory;
import uk.co.caprica.vlcj.media.discoverer.MediaDiscovererDescription;
import uk.co.caprica.vlcj.medialist.MediaList;
import uk.co.caprica.vlcj.medialist.MediaListEventAdapter;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author f_776
 */
public class MediaDiscovererTest {

    private static final ObservableSet<String> observableSet = FXCollections.observableSet();
    private static final SimpleSetProperty<String> setProperty = new SimpleSetProperty<>(observableSet);
    private static int previousSize;

    public static void main(String[] args) throws Exception {

        Json json = new Json();
        setProperty.addListener(
                (observable, oldValue, newValue) -> {
                    System.out.println("Items added: " + newValue.size());
                    for (String string : newValue) {
                        System.out.println(string);
                    }

                });

        MediaPlayerFactory factory = new MediaPlayerFactory();
        List<MediaDiscovererDescription> discoverers = factory.mediaDiscoverers().discoverers(MediaDiscovererCategory.LOCAL_DIRS);

        if (discoverers.isEmpty()) {
            return;
        }

        // as I'm only interested in audio directories
        discoverers.removeIf(discoverer -> discoverer.name().equals("picture_dir") || discoverer.name().equals("video_dir"));

        for (MediaDiscovererDescription discoverer : discoverers) {
            System.out.println(discoverer.toString());
        }

        MediaList list = null;
        MediaDiscoverer discoverer = null;
        MediaListEventAdapter mediaListEventAdapter = null;

        for (MediaDiscovererDescription description : discoverers) {
            final String name = description.name();
            discoverer = factory.mediaDiscoverers().discoverer(name);

            list = discoverer.newMediaList();

            mediaListEventAdapter = new MediaListEventAdapter() {

                @Override
                public void mediaListItemAdded(MediaList mediaList, MediaRef item, int index) {
                    Media newMedia = item.newMedia();
                    try {

                        String mrl = newMedia.info().mrl();

                        URI uri = new URI(mrl);
                        String path = uri.getPath();
                        File file = new File(path);

                        if (file.isDirectory() && json.hasPlayableFile(file)) {
//                            System.out.println(mrl);
                            observableSet.add(mrl);

                        }

                    } catch (URISyntaxException ignored) {

                    } finally {
                        newMedia.release();
                    }
                }

            };

            list.events().addMediaListEventListener(mediaListEventAdapter);

            discoverer.start();

        }
//        Thread.sleep(1000);
//        if (list != null && discoverer != null) {
//            list.release();
//            list.events().removeMediaListEventListener(mediaListEventAdapter);
//            discoverer.release();
//        }

//        System.out.println(observableSet.size());
//        for (String musicDirectory : observableSet) {
//            System.out.println(musicDirectory);
//        }
        Thread.currentThread()
                .join();

    }

    private void startMonitoringSet() {
        try (ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1)) {
            scheduler.scheduleAtFixedRate(this::checkSetSize, 0, 1, TimeUnit.SECONDS);
        }

    }

    public void checkSetSize() {
        int currentSize = observableSet.size();

        if (currentSize == previousSize) {
            System.out.println("No new items are being added to the set.");
            // Do something when no new items are being added
        }

        previousSize = currentSize;
    }

}
