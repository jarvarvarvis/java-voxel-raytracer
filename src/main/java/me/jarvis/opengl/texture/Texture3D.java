package me.jarvis.opengl.texture;

import me.jarvis.opengl.base.Bindable;
import me.jarvis.opengl.base.GLObject;
import me.jarvis.util.vector.Vector4b;
import me.jarvis.util.Function;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;

public class Texture3D extends GLObject implements Bindable {

    private final int width, height, depth;

    private Texture3D(int width, int height, int depth) {
        super(GL30.glGenTextures());
        assert width > 0;
        assert height > 0;
        assert depth > 0;
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    public static Texture3D create(int width, int height, int depth) {
        Texture3D texture = new Texture3D(width, height, depth);

        texture.bind();
        texture.setParameter(GL30.GL_TEXTURE_WRAP_S, GL30.GL_CLAMP_TO_EDGE);
        texture.setParameter(GL30.GL_TEXTURE_WRAP_T, GL30.GL_CLAMP_TO_EDGE);
        texture.setParameter(GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_NEAREST);
        texture.setParameter(GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_NEAREST);
        texture.unbind();

        return texture;
    }

    public static Texture3D createFrom(int width, int height, int depth, Function.Ternary<Integer, Integer, Integer, Vector4b> supplier) {
        Texture3D texture = Texture3D.create(width, height, depth);

        texture.bind();
        ByteBuffer data = TextureUtils.createData(width, height, depth, supplier);
        texture.uploadData(GL30.GL_RGBA, width, height, depth, GL30.GL_RGBA, data);
        texture.unbind();

        return texture;
    }

    public void setParameter(int name, int value) {
        GL30.glTexParameteri(GL30.GL_TEXTURE_3D, name, value);
    }

    public void uploadData(int internalFormat, int width, int height, int depth, int format, ByteBuffer data) {
        assert width == this.width;
        assert height == this.height;
        assert depth == this.depth;

        this.bind();
        GL30.glTexImage3D(GL30.GL_TEXTURE_3D,
            0,
            internalFormat,
            width, height, depth,
            0,
            format,
            GL30.GL_UNSIGNED_BYTE,
            data);
        this.unbind();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getDepth() {
        return depth;
    }

    @Override
    public void bind() {
        GL30.glBindTexture(GL30.GL_TEXTURE_3D, this.getHandle());
    }

    @Override
    public void unbind() {
        GL30.glBindTexture(GL30.GL_TEXTURE_3D, 0);
    }

    @Override
    public void dispose() {
        GL30.glDeleteTextures(this.getHandle());
    }
}
