package me.jarvis.util.vector;

public record Vector4i(int x, int y, int z, int w) {
    
    public Vector4i add(Vector4i other) {
        return new Vector4i(this.x + other.x, this.y + other.y, this.z + other.z, this.w + other.w);
    }

    public Vector4i sub(Vector4i other) {
        return new Vector4i(this.x - other.x, this.y - other.y, this.z - other.z, this.w - other.w);
    }
}
