package me.jarvis.util.math;

import me.jarvis.util.vector.Vector3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TrigonometryHelper {

    public static Logger log = LogManager.getLogger(TrigonometryHelper.class);

    public static float wrapDegrees(float value) {
        if (value < 0) {
            while (value < 0)
                value += 360;
        } else if (value > 360) {
            while (value > 360)
                value -= 360;
        }

        return value;
    }

    public static float clamp(float value, float min, float max) {
        if (value < min)
            return min;
        if (value > max)
            return max;
        return value;
    }

    public static final float DEGREES_TO_RADIANS_FACTOR = (float)Math.PI / 180f;

    public static float degreesToRadians(float degrees) {
        return degrees * DEGREES_TO_RADIANS_FACTOR;
    }

    public static Vector3 degreeEulerAnglesToVector(float yawDegrees, float pitchDegrees) {
        float yaw = (float)Math.toRadians(yawDegrees);
        float pitch = (float)Math.toRadians(pitchDegrees);

        float x = (float)(-Math.sin(yaw) * Math.cos(pitch));
        float y = (float)(Math.sin(pitch));
        float z = (float)(Math.cos(yaw) * Math.cos(pitch));
        return new Vector3(x, y, z);
    }
}
