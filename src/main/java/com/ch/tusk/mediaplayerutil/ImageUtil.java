/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ch.tusk.mediaplayerutil;

/**
 * @author f_776
 */

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

/**
 * This class provides utility methods for image processing.
 */
public class ImageUtil {

    /**
     * This method is used to create a thumbnail of an original image.
     * <p>
     * It calculates the scaling factors for width and height based on the target scaling.
     * The minimum scaling factor is used to maintain the aspect ratio of the original image.
     * A new WritableImage with the desired dimensions is created.
     * The PixelReader and PixelWriter for the original and thumbnail images are obtained.
     * The image is scaled by reading and writing pixel data.
     *
     * @param originalImage The original Image object that needs to be scaled.
     * @param targetScaling The target scaling for the thumbnail.
     * @return Image The thumbnail Image object.
     */
    public static Image createThumbnail(Image originalImage, int targetScaling) {

        // Calculate the scaling factors for width and height
        double widthScale = targetScaling / originalImage.getWidth();
        double heightScale = targetScaling / originalImage.getHeight();

        // Use the minimum scaling factor to maintain the aspect ratio
        double scaleFactor = Math.min(widthScale, heightScale);

        // Calculate the new width and height based on the scaling factor
        int thumbnailWidth = (int) (originalImage.getWidth() * scaleFactor);
        int thumbnailHeight = (int) (originalImage.getHeight() * scaleFactor);

        // Create a WritableImage with the desired dimensions
        WritableImage bufferedThumbnail = new WritableImage(thumbnailWidth, thumbnailHeight);

        // Get PixelReader and PixelWriter for the original and thumbnail images
        PixelReader pixelReader = originalImage.getPixelReader();
        PixelWriter pixelWriter = bufferedThumbnail.getPixelWriter();

        // Scale the image by reading and writing pixel data
        for (int y = 0; y < thumbnailHeight; y++) {
            for (int x = 0; x < thumbnailWidth; x++) {
                double sourceX = (x * originalImage.getWidth()) / thumbnailWidth;
                double sourceY = (y * originalImage.getHeight()) / thumbnailHeight;
                pixelWriter.setColor(x, y, pixelReader.getColor((int) sourceX, (int) sourceY));
            }
        }

        return bufferedThumbnail;
    }
}
