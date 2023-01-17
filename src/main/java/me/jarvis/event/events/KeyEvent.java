package me.jarvis.event.events;

import me.jarvis.event.Event;

public class KeyEvent extends Event {

    private final long windowHandle;
    private final int key;
    private final int scancode;
    private final int action;
    private final int mods;

    public KeyEvent(long windowHandle, int key, int scancode, int action, int mods) {
        this.windowHandle = windowHandle;
        this.key = key;
        this.scancode = scancode;
        this.action = action;
        this.mods = mods;
    }

    public long getWindowHandle() {
        return windowHandle;
    }

    public int getKey() {
        return key;
    }

    public int getScancode() {
        return scancode;
    }

    public int getAction() {
        return action;
    }

    public int getMods() {
        return mods;
    }
}
