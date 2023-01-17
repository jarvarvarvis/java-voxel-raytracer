package me.jarvis.util.vector;

public record Vector3(float x, float y, float z) {

    public static Vector3 UP = new Vector3(0, 1, 0);

    public Vector3 add(Vector3 other) {
        return new Vector3(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public Vector3 scale(float scalar) {
        return new Vector3(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    public float dot(Vector3 other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    public float squaredDistance() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public float distance() {
        return (float)Math.sqrt(this.squaredDistance());
    }

    public Vector3 normalized() {
        return this.scale(1f / this.distance());
    }

    public Vector3 cross(Vector3 other) {
        return new Vector3(
            this.y * other.z - this.z * other.y,
            this.z * other.x - this.x * other.z,
            this.x * other.y - this.y * other.x
        );
    }
}
