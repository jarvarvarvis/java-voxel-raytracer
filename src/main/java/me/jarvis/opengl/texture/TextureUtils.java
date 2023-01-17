package me.jarvis.opengl.texture;

import me.jarvis.util.vector.Vector3b;
import me.jarvis.util.vector.Vector4b;
import me.jarvis.util.Function;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

public class TextureUtils {

    public static ByteBuffer createData(int width, int height, Function.Binary<Integer, Integer, Vector3b> supplier) {
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 3);

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                Vector3b color = supplier.apply(x, y);
                buffer.put(color.x());
                buffer.put(color.y());
                buffer.put(color.z());
            }
        }

        return buffer.flip();
    }

    public static ByteBuffer createData(int width, int height, int depth, Function.Ternary<Integer, Integer, Integer, Vector4b> supplier) {
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * depth * 4);

        for (int z = 0; z < depth; ++z) {
            for (int y = 0; y < height; ++y) {
                for (int x = 0; x < width; ++x) {
                    Vector4b color = supplier.apply(x, y, z);
                    buffer.put(color.x());
                    buffer.put(color.y());
                    buffer.put(color.z());
                    buffer.put(color.w());
                }
            }
        }

        return buffer.flip();
    }
}
