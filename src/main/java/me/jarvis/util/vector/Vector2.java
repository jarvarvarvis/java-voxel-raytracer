package me.jarvis.util.vector;

public record Vector2(float x, float y) {

    public Vector2 add(Vector2 other) {
        return new Vector2(this.x + other.x, this.y + other.y);
    }

    public Vector2 sub(Vector2 other) {
        return new Vector2(this.x - other.x, this.y - other.y);
    }

    public Vector2 scale(float scalar) {
        return new Vector2(this.x * scalar, this.y * scalar);
    }

    public float dot(Vector2 other) {
        return this.x * other.x + this.y * other.y;
    }

    public double squaredLength() {
        return this.x * this.x + this.y * this.y;
    }

    public Vector2 reflect(Vector2 wallNormal) {
        assert wallNormal.squaredLength() == 1;
        return this.sub(wallNormal.scale(2).scale(wallNormal.dot(this)));
    }
}
