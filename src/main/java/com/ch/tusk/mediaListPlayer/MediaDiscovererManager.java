/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ch.tusk.mediaListPlayer;

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
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author f_776
 */
public class MediaDiscovererManager {

    private final ObservableSet<String> observableSet;
    private final SimpleSetProperty<String> setProperty;
    private final MediaListEventAdapter mediaListEventAdapter;
    private MediaList list;
    private int previousSize = 1;
    private MediaDiscoverer discoverer;
    private ScheduledExecutorService scheduler;

    public MediaDiscovererManager() {
        observableSet = FXCollections.observableSet();
        setProperty = new SimpleSetProperty<>(observableSet);

        Json json = new Json();

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
                        observableSet.add(mrl);
                    }

                } catch (URISyntaxException ex) {

                } finally {
                    newMedia.release();
                }
            }

        };

        setProperty.addListener(
                (observable, oldValue, newValue) -> {

                    System.out.println(newValue.size());
                    System.out.println(newValue);
                });
        discoverMedia();
        startMonitoringSet();

    }

    private void discoverMedia() {

        for (MediaDiscovererDescription description : obtainDiscoverers()) {
            final String name = description.name();
            discoverer = createDiscoverer(name);

            list = discoverer.newMediaList();
            list.events().addMediaListEventListener(mediaListEventAdapter);

            discoverer.start();

        }
    }

    private void startMonitoringSet() {
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::checkSetSize, 1, 1, TimeUnit.SECONDS);

    }

    public void checkSetSize() {
        int currentSize = observableSet.size();

        if (currentSize == previousSize) {
            System.out.println("No new items are being added to the set.");
            releaseList();
            scheduler.close();
        }

        previousSize = currentSize;
    }

    private void releaseList() {
        list.events().removeMediaListEventListener(mediaListEventAdapter);
        list.release();
        discoverer.stop();
        discoverer.release();
    }

    private HashSet<MediaDiscovererDescription> obtainDiscoverers() {
        MediaPlayerFactory factory = new MediaPlayerFactory();

        List<MediaDiscovererDescription> discoverers = factory.mediaDiscoverers().discoverers(MediaDiscovererCategory.LOCAL_DIRS);

        if (discoverers.isEmpty()) {
            factory.release();
            return null;
        }

        // as I'm only interested in audio directories
        discoverers.removeIf(discovererDesc -> discovererDesc.name().equals("picture_dir") || discovererDesc.name().equals("video_dir"));
        factory.release();
        return new HashSet<>(discoverers);
    }

    private MediaDiscoverer createDiscoverer(String name) {
        MediaPlayerFactory factory = new MediaPlayerFactory();
        MediaDiscoverer newDiscoverer = factory.mediaDiscoverers().discoverer(name);
        factory.release();
        return newDiscoverer;
    }


//    public static void main(String[] args) throws InterruptedException {
//        MediaDiscovererManager mediaDiscovererManager = new MediaDiscovererManager();
//        Thread.currentThread()
//                .join();
//    }

    public ObservableSet<String> getObservableSet() {
        if (scheduler.isShutdown()) {
            return observableSet;
        }
        return null;
    }

}
