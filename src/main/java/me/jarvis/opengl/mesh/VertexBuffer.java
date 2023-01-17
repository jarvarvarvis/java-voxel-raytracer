package me.jarvis.opengl.mesh;

import java.util.Arrays;
import java.util.List;

public class VertexBuffer {

    private final List<Vertex> vertices;

    private int components;

    public VertexBuffer(Vertex... vertexArray) {
        this.vertices = Arrays.stream(vertexArray).toList();

        this.components = -1;
        for (Vertex vertex : this.vertices) {
            int vertexComponents = vertex.getComponents();
            if (this.components == -1) {
                this.components = vertexComponents;
            }

            assert vertexComponents == this.components;
        }
        assert this.components != -1;
    }

    public int getComponents() {
        return components;
    }

    public float[] buildDataBuffer() {
        float[] finalData = new float[this.vertices.size() * this.components];

        List<Vertex> vertexList = this.vertices;
        for (int i = 0; i < vertexList.size(); i++) {
            Vertex vertex = vertexList.get(i);

            for (int j = 0; j < this.components; ++j) {
                finalData[i * this.components + j] = vertex.getValue(j);
            }
        }

        return finalData;
    }
}
