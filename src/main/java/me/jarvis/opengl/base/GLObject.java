package me.jarvis.opengl.base;

public abstract class GLObject implements Disposable {

    private final int handle;

    public GLObject(int handle) {
        this.handle = handle;
    }

    public int getHandle() {
        return this.handle;
    }
}
