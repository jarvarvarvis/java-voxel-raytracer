package me.jarvis;

import com.google.common.eventbus.Subscribe;
import me.jarvis.event.EventManager;
import me.jarvis.event.events.CursorPosEvent;
import me.jarvis.event.events.KeyEvent;
import me.jarvis.event.events.ResizeEvent;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL33;

public class PixelEngineWindow {

    private int width, height;
    private final long handle;

    private static void createCallbacks(long windowHandle) {
        GLFWFramebufferSizeCallback.create((window, width, height) ->
                EventManager.post(new ResizeEvent(window, width, height)))
            .set(windowHandle);
        GLFWCursorPosCallback.create((window, x, y) ->
                EventManager.post(new CursorPosEvent(window, x, y)))
            .set(windowHandle);
        GLFWKeyCallback.create((window, key, scancode, action, mods) ->
                EventManager.post(new KeyEvent(window, key, scancode, action, mods)))
            .set(windowHandle);
    }

    public PixelEngineWindow(int width, int height, String name) {
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);

        this.width = width;
        this.height = height;
        this.handle = GLFW.glfwCreateWindow(width, height, name, 0, 0);

        if (this.handle == 0)
            throw new RuntimeException("Unable to create GLFW window");

        EventManager.register(this);
        createCallbacks(this.handle);

        GLFW.glfwMakeContextCurrent(this.handle);
        GL.createCapabilities();

        GLFW.glfwSwapInterval(1);
        GLFW.glfwShowWindow(this.handle);
    }

    @Subscribe
    private void handleResize(ResizeEvent event) {
        int width = event.getWidth();
        int height = event.getHeight();

        GL33.glViewport(0, 0, width, height);
        this.width = width;
        this.height = height;
    }

    public boolean shouldClose() {
        return GLFW.glfwWindowShouldClose(this.handle);
    }

    public void swapBuffers() {
        GLFW.glfwSwapBuffers(this.handle);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public long getHandle() {
        return handle;
    }
}
