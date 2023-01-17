package me.jarvis.event.events;

import me.jarvis.event.Event;

public class ResizeEvent extends Event {

    private final long windowHandle;
    private final int width;
    private final int height;

    public ResizeEvent(long windowHandle, int width, int height) {
        this.windowHandle = windowHandle;
        this.width = width;
        this.height = height;
    }

    public long getWindowHandle() {
        return windowHandle;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
