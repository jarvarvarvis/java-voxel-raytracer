package me.jarvis.event.events;

import me.jarvis.event.Event;

public class CursorPosEvent extends Event {
    
    private final long windowHandle;
    private final double x;
    private final double y;

    public CursorPosEvent(long windowHandle, double x, double y) {
        this.windowHandle = windowHandle;
        this.x = x;
        this.y = y;
    }

    public long getWindowHandle() {
        return windowHandle;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
