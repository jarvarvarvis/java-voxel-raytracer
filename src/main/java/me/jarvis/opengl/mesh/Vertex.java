package me.jarvis.opengl.mesh;

public class Vertex {

    private final float[] data;

    public Vertex(float... data) {
        this.data = data;
    }

    public int getComponents() {
        return this.data.length;
    }

    public float getValue(int index) {
        return this.data[index];
    }
}
