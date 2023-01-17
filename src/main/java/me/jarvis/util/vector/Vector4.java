package me.jarvis.util.vector;

public record Vector4(float x, float y, float z, float w) {

    public Vector4 add(Vector4 other) {
        return new Vector4(this.x + other.x, this.y + other.y, this.z + other.z, this.w + other.w);
    }

    public Vector4 sub(Vector4 other) {
        return new Vector4(this.x - other.x, this.y - other.y, this.z - other.z, this.w - other.w);
    }
}
