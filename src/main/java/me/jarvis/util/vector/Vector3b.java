package me.jarvis.util.vector;

import java.util.Objects;

public final class Vector3b {
    private final byte x, y, z;

    public Vector3b(byte x, byte y, byte z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3b(int x, int y, int z) {
        this((byte) x, (byte) y, (byte) z);
    }

    public Vector3b add(Vector3b other) {
        return new Vector3b(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public Vector3b sub(Vector3b other) {
        return new Vector3b(this.x - other.x, this.y - other.y, this.z - other.z);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vector3b vector3b = (Vector3b) o;

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
