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

    public static final int RGB_MASK = 0xff;
    public static final int FULL_ALPHA = 0xff000000;

    public static int rgbToArgb(byte r, byte g, byte b) {
        return
              FULL_ALPHA              // A
            | ((r & RGB_MASK) << 16)  // R
            | ((g & RGB_MASK) << 8)   // G
            | (b & RGB_MASK);         // B
    }

    public static void saveImage(int width, int height, int[] pixelData, String filename) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, width, height, pixelData, 0, width);
        ImageIO.write(image, "png", new File(filename));
    }

    public static void takeScreenshot(int width, int height, String filename) throws IOException {
        ByteBuffer frameByteBuffer = ScreenshotUtil.allocBytes(width * height * 3);
        int[] pixels = new int[width * height];

        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, frameByteBuffer);

        for (int i = 0; i < pixels.length; i++) {
            int index = i * 3;
            byte r = frameByteBuffer.get(index);
            byte g = frameByteBuffer.get(index + 1);
            byte b = frameByteBuffer.get(index + 2);
            pixels[i] = ScreenshotUtil.rgbToArgb(r, g, b);
        }

        // Flip the pixels vertically (OpenGL has 0,0 at lower left, Java is upper left)
        pixels = ScreenshotUtil.flipPixels(pixels, width, height);
        ScreenshotUtil.saveImage(width, height, pixels, filename);
    }
}
