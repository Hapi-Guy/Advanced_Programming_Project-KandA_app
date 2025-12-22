package com.kna.util;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

/**
 * ImageLoader - Utility for loading and caching images
 */
public class ImageLoader {
    
    private static final Map<String, Image> imageCache = new HashMap<>();
    private static final String DEFAULT_AVATAR = "/images/default-avatar.png";
    private static final String DEFAULT_QUESTION_IMAGE = "/images/default-question.png";

    /**
     * Load image from file path
     * @param imagePath Path to image file
     * @return Image object or null if not found
     */
    public static Image loadImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }

        // Check cache first
        if (imageCache.containsKey(imagePath)) {
            return imageCache.get(imagePath);
        }

        try {
            Image image;
            File file = new File(imagePath);
            
            if (file.exists()) {
                // Load from file system
                image = new Image(new FileInputStream(file));
            } else {
                // Try to load from resources
                image = new Image(ImageLoader.class.getResourceAsStream(imagePath));
            }
            
            // Cache the image
            imageCache.put(imagePath, image);
            return image;
            
        } catch (Exception e) {
            System.err.println("Failed to load image: " + imagePath);
            return null;
        }
    }

    /**
     * Load image with specified dimensions
     * @param imagePath Path to image file
     * @param width Width to resize
     * @param height Height to resize
     * @param preserveRatio Whether to preserve aspect ratio
     * @return Image object
     */
    public static Image loadImage(String imagePath, double width, double height, boolean preserveRatio) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }

        try {
            File file = new File(imagePath);
            
            if (file.exists()) {
                return new Image(new FileInputStream(file), width, height, preserveRatio, true);
            } else {
                return new Image(ImageLoader.class.getResourceAsStream(imagePath), width, height, preserveRatio, true);
            }
            
        } catch (Exception e) {
            System.err.println("Failed to load image: " + imagePath);
            return null;
        }
    }

    /**
     * Load avatar image with default fallback
     * @param imagePath Path to avatar image
     * @param size Size of the avatar (width and height)
     * @return ImageView with avatar
     */
    public static ImageView loadAvatar(String imagePath, double size) {
        Image image = loadImage(imagePath, size, size, true);
        
        if (image == null) {
            image = loadImage(DEFAULT_AVATAR, size, size, true);
        }
        
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(size);
        imageView.setFitHeight(size);
        imageView.setPreserveRatio(true);
        
        // Make circular
        javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(size / 2, size / 2, size / 2);
        imageView.setClip(clip);
        
        return imageView;
    }

    /**
     * Load question image with default fallback
     * @param imagePath Path to question image
     * @param width Maximum width
     * @return ImageView with image
     */
    public static ImageView loadQuestionImage(String imagePath, double width) {
        Image image = loadImage(imagePath, width, 0, true);
        
        if (image == null) {
            return null; // Don't show default for question images
        }
        
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(width);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        
        return imageView;
    }

    /**
     * Clear image cache
     */
    public static void clearCache() {
        imageCache.clear();
    }

    /**
     * Get image from cache
     * @param imagePath Path to image
     * @return Cached image or null
     */
    public static Image getFromCache(String imagePath) {
        return imageCache.get(imagePath);
    }

    /**
     * Save image to upload directory
     * @param sourceFile Source file
     * @param userId User ID
     * @param questionId Question ID
     * @return Path to saved image
     */
    public static String saveQuestionImage(File sourceFile, int userId, int questionId) {
        try {
            // Create uploads directory if not exists
            File uploadsDir = new File("uploads/questions");
            if (!uploadsDir.exists()) {
                uploadsDir.mkdirs();
            }
            
            // Generate filename
            String extension = getFileExtension(sourceFile.getName());
            String filename = "q" + questionId + "_u" + userId + "_" + System.currentTimeMillis() + extension;
            File destFile = new File(uploadsDir, filename);
            
            // Copy file
            java.nio.file.Files.copy(sourceFile.toPath(), destFile.toPath());
            
            return destFile.getAbsolutePath();
            
        } catch (Exception e) {
            System.err.println("Failed to save image: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get file extension
     */
    private static String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            return filename.substring(lastDot);
        }
        return ".jpg";
    }
}
