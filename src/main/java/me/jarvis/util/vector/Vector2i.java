package me.jarvis.util.vector;

public record Vector2i(int x, int y) {

    public Vector2i add(Vector2i other) {
        return new Vector2i(this.x + other.x, this.y + other.y);
    }

    public Vector2i sub(Vector2i other) {
        return new Vector2i(this.x - other.x, this.y - other.y);
    }

    public Vector2i scale(int scalar) {
        return new Vector2i(this.x * scalar, this.y * scalar);
    }

    public int dot(Vector2i other) {
        return this.x * other.x + this.y * other.y;
    }

    public int squaredLength() {
        return this.x * this.x + this.y * this.y;
    }
}
