package me.jarvis.util.vector;

import java.util.Objects;

public class Vector4b {
    private final byte x, y, z, w;

    public Vector4b(byte x, byte y, byte z, byte w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Vector4b(int x, int y, int z, int w) {
        this((byte) x, (byte) y, (byte) z, (byte) w);
    }

    public Vector4b add(Vector4b other) {
        return new Vector4b(this.x + other.x, this.y + other.y, this.z + other.z, this.w + other.w);
    }

    public Vector4b sub(Vector4b other) {
        return new Vector4b(this.x - other.x, this.y - other.y, this.z - other.z, this.w + other.w);
    }

    public byte x() {
        return x;
    }

    public byte y() {
        return y;
    }

    public byte z() {
        return z;
    }
    public byte w() {
        return w;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vector4b vector3b = (Vector4b) o;

        if (x != vector3b.x) return false;
        if (y != vector3b.y) return false;
        return z == vector3b.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "Vector3b(" +
            "x=" + x + ", " +
            "y=" + y + ", " +
            "z=" + z + ")";
    }
}
