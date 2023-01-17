package me.jarvis.raytracing;

import me.jarvis.util.vector.Vector3;
import me.jarvis.util.math.TrigonometryHelper;

public class Camera {

    private Vector3 position;
    private float yaw, pitch;
    private final float sensitivity;

    public Camera(Vector3 position, float yaw, float pitch, float sensitivity) {
        this.position = position;
        this.yaw = yaw;
        this.pitch = pitch;
        this.sensitivity = sensitivity;
    }

    public void moveForwardBackward(float speed) {
        Vector3 direction = TrigonometryHelper.degreeEulerAnglesToVector(this.yaw, this.pitch);
        this.position = this.position.add(direction.scale(speed));
    }

    public void moveLeftRight(float speed) {
        // Pitch fix: add 0.5 degrees to the pitch value to avoid flipped movement that occurs at pitch = -90
        // for some reason.
        // This is a dirty fix but it works.
        Vector3 front = TrigonometryHelper.degreeEulerAnglesToVector(this.yaw, this.pitch + 0.5f);
        Vector3 right = front.cross(Vector3.UP).normalized();
        this.position = this.position.add(right.scale(speed));
    }

    public void moveUp(float speed) {
        this.position = this.position.add(Vector3.UP.scale(speed));
    }

    public void updateYaw(float added) {
        this.yaw = TrigonometryHelper.wrapDegrees(this.yaw + added * this.sensitivity);
    }

    public void updatePitch(float added) {
        this.pitch = TrigonometryHelper.clamp(this.pitch + added * this.sensitivity, -90, 90);
    }

    public Vector3 getPosition() {
        return this.position;
    }

    public float getYawAsRadians() {
        return TrigonometryHelper.degreesToRadians(this.yaw);
    }

    public float getPitchAsRadians() {
        return TrigonometryHelper.degreesToRadians(this.pitch);
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }
}
