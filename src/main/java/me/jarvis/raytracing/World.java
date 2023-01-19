package me.jarvis.raytracing;

import me.jarvis.util.vector.Vector3i;
import me.jarvis.util.vector.Vector4b;
import me.jarvis.opengl.texture.Texture3D;

public class World {

    private final Texture3D worldTexture;

    public World(int width, int height, int depth) {
        this.worldTexture = Texture3D.createFrom(width, height, depth, (x, y, z) -> {
            if (Math.random() > 0.9) {
                return new Vector4b(x * 8, y * 8, z * 8, 255);
            }
            return new Vector4b(0, 0, 0, 0);
        });
    }

    public Vector3i getWorldSize() {
        int width = this.worldTexture.getWidth();
        int height = this.worldTexture.getHeight();
        int depth = this.worldTexture.getDepth();
        return new Vector3i(width, height, depth);
    }

    public void bindWorldTexture() {
        this.worldTexture.bind();
    }
}
