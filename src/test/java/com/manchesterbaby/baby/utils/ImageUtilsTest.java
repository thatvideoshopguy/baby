package com.manchesterbaby.baby.utils;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Toolkit;

import org.junit.jupiter.api.Test;

import com.manchesterbaby.baby.utils.ImageUtils;

import static org.junit.jupiter.api.Assertions.*;

class ImageUtilsTest {

    @Test
    void testLoadImage_validImage() {
        Image image = ImageUtils.loadImage("/images/test-image.png", 1024 * 1024);
        assertNotNull(image, "Image should not be null for a valid image.");
    }

    @Test
    void testLoadImage_smallMaxSize() {
        Image image = ImageUtils.loadImage("/images/test-image.png", 1);
        assertNull(image, "Image should be null if max size is too small.");
    }

    @Test
    void testEnsureImageIsLoaded_validImage() {
        Image image = ImageUtils.loadImage("/images/test-image.png", 1024 * 1024);
        assertDoesNotThrow(() -> ImageUtils.ensureImageIsLoaded(image),
                "ensureImageIsLoaded should not throw an exception for a valid image.");
    }

    @Test
    void testEnsureImageIsLoaded_invalidImage() {
        Image invalidImage = Toolkit.getDefaultToolkit().createImage(new byte[0]);
        assertThrows(IllegalArgumentException.class,
                () -> ImageUtils.ensureImageIsLoaded(invalidImage),
                "ensureImageIsLoaded should throw IllegalArgumentException for an invalid image.");
    }

    @Test
    void testToBufferedImage_validImage() {
        Image image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        BufferedImage bufferedImage = ImageUtils.toBufferedImage(image);
        assertNotNull(bufferedImage, "BufferedImage should not be null for a valid image.");
    }

    @Test
    void testToBufferedImage_nullImage() {
        assertThrows(IllegalArgumentException.class,
                () -> ImageUtils.toBufferedImage(null),
                "toBufferedImage should throw IllegalArgumentException for null input.");
    }

    @Test
    void testToBufferedImage_invalidDimensions() {
        // Create an image with invalid dimensions using Toolkit
        Image invalidImage = Toolkit.getDefaultToolkit().createImage(new byte[0]);

        // Assert that an exception is thrown
        assertThrows(IllegalArgumentException.class,
                () -> ImageUtils.toBufferedImage(invalidImage),
                "toBufferedImage should throw IllegalArgumentException for an image with invalid dimensions.");
    }
}