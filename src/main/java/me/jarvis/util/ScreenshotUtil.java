package me.jarvis.util;

import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ScreenshotUtil {

    public static int[] flipPixels(int[] pixels, int width, int height) {
        int[] flippedPixels = new int[width * height];

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                int index = y * width + x;
                int reverseIndex = (height - y - 1) * width + x;
                flippedPixels[reverseIndex] = pixels[index];
            }
        }

        return flippedPixels;
    }

    public static ByteBuffer allocBytes(int size) {
        return ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
    }

    public static void takeScreenshot(int width, int height, String saveFilename) throws IOException {
        // Allocate space for RBG pixels
        ByteBuffer frameByteBuffer = ScreenshotUtil.allocBytes(width * height * 3);
        int[] pixels = new int[width * height];

        // Grab a copy of the current frame contents as RGB (has to be UNSIGNED_BYTE or colors come out too dark)
        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, frameByteBuffer);

        // Copy RGB data from ByteBuffer to integer array
        for (int i = 0; i < pixels.length; i++) {
            int index = i * 3;
            pixels[i] =
                0xFF000000                                                  // A
                    | ((frameByteBuffer.get(index) & 0x000000FF) << 16)     // R
                    | ((frameByteBuffer.get(index + 1) & 0x000000FF) << 8)  // G
                    | ((frameByteBuffer.get(index + 2) & 0x000000FF));      // B
        }

        // Flip the pixels vertically (opengl has 0,0 at lower left, java is upper left)
        pixels = ScreenshotUtil.flipPixels(pixels, width, height);

        // Create a BufferedImage with the RGB pixels then save as PNG
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, width, height, pixels, 0, width);
        ImageIO.write(image, "png", new File(saveFilename));
    }
}
