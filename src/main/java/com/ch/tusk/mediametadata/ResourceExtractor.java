package com.ch.tusk.mediametadata;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ResourceExtractor {

    public ResourceExtractor() {
    }

    public boolean doesFolderExist(String subfolderName) {

        // Create a subfolder within user.home to store the extracted files
        String destinationDirectory = System.getProperty("user.home") + File.separatorChar + subfolderName;
        // Check if the destination directory already exists
        File destinationDir = new File(destinationDirectory);
        return destinationDir.exists() && destinationDir.isDirectory();
    }

    public boolean doesFileExist(String subfolderName, String fileName) {
        // Create a subfolder within user.home to store the files
        String destinationDirectory = System.getProperty("user.home") + File.separatorChar + subfolderName;

        // Create a File object for the specific file within the subfolder
        File file = new File(destinationDirectory, fileName);

        // Check if the file exists and is a regular file (not a directory)
        return file.exists() && file.isFile();
    }

    public void extractResourceToUserDir(String subfolderName, String resource, String extractedZip) throws IOException {
        // Create a subfolder within user.home to store the extracted files

        String destinationDirectory = System.getProperty("user.home") + File.separatorChar + subfolderName;

        // Extract file from jar
        File zipFromJar;
        try (InputStream in = getClass().getResourceAsStream(resource)) {
            zipFromJar = new File(System.getProperty("user.home") + File.separatorChar + extractedZip);
            if (zipFromJar.exists()) {
                zipFromJar.delete();
            }
            Files.copy(Objects.requireNonNull(in), zipFromJar.toPath());
        }

        try (ZipFile zipFile = new ZipFile(zipFromJar)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            final int BUFFER = 2048;

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File destFile = new File(destinationDirectory + File.separatorChar + entry.getName());
                destFile.getParentFile().mkdirs();

                if (!entry.isDirectory()) {
                    try (BufferedInputStream is = new BufferedInputStream(zipFile.getInputStream(entry))) {
                        int currentByte;
                        byte[] data = new byte[BUFFER];

                        // Write the current file to disk
                        FileOutputStream fos = new FileOutputStream(destFile);
                        try (BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER)) {
                            // Read and write until the last byte is encountered
                            while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                                dest.write(data, 0, currentByte);
                            }
                            dest.flush();
                        }
                    }
                }

            }
        } finally {
            zipFromJar.delete();
        }
    }

    public void extractFileToUserDir(String subfolderName, String resource, String extractedFileName) throws IOException {
        // Create a subfolder within user.home to store the extracted files
        String destinationDirectory = System.getProperty("user.home") + File.separatorChar + subfolderName;

        // Extract file from jar
        try (InputStream in = getClass().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IOException("Resource not found: " + resource);
            }

            // Create the destination File for the extracted file
            File extractedFile = new File(destinationDirectory + File.separatorChar + extractedFileName);

            // Copy the InputStream to the destination File
            Files.copy(in, extractedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

//    private ProgressBar progressBar;
//    
//    public void start() {
//        Platform.runLater(() -> {
//            Stage primaryStage = new Stage();
//            progressBar = new ProgressBar(0);
//            progressBar.setStyle("""
//                                 -fx-accent: #3498db; /* Blue accent color */
//                                     -fx-background-color: #ecf0f1; /* Light gray background color */
//                                     -fx-background-insets: 0;
//                                     -fx-padding: 4;
//                                     -fx-border-color: #bdc3c7; /* Border color */
//                                     -fx-border-width: 1;
//                                     -fx-border-radius: 5; /* Border radius */""");
//            
//            
//            VBox root = new VBox(progressBar);
//            progressBar.setMaxWidth(Integer.MAX_VALUE);
//            progressBar.setMaxHeight(Integer.MAX_VALUE);
//            Scene scene = new Scene(root, 300, 100);
//            primaryStage.setTitle("Extraction Progress");
//            primaryStage.setScene(scene);
//            primaryStage.show();
//
//            // Run the extraction process in a separate thread to avoid blocking the JavaFX application thread
//            new Thread(() -> {
//                try {
//                    extractResourceToUserDir();
//                    Platform.runLater(() -> progressBar.setProgress(1.0)); // Set progress to 100%
//                    Platform.runLater(primaryStage::close);
//                } catch (IOException e) {
//                }
//            }).start();
//        });
//    }
}
