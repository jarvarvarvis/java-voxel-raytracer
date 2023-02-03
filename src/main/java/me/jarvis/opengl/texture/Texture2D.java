package me.jarvis.opengl.texture;

import me.jarvis.opengl.base.Bindable;
import me.jarvis.opengl.base.GLObject;
import me.jarvis.util.vector.Vector3b;
import me.jarvis.util.Function;
import org.lwjgl.opengl.GL33;

import java.nio.ByteBuffer;

public class Texture2D extends GLObject implements Bindable {

    private final int width, height;

    private Texture2D(int width, int height) {
        super(GL33.glGenTextures());
        assert width > 0;
        assert height > 0;
        this.width = width;
        this.height = height;
    }

    public static Texture2D create(int width, int height) {
        Texture2D texture = new Texture2D(width, height);

        texture.bind();
        texture.setParameter(GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
        texture.setParameter(GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);
        texture.setParameter(GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_NEAREST);
        texture.setParameter(GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_NEAREST);
        texture.unbind();

        return texture;
    }

    public static Texture2D createFrom(int width, int height, Function.Binary<Integer, Integer, Vector3b> supplier) {
        Texture2D texture = Texture2D.create(width, height);

        texture.bind();
        ByteBuffer data = TextureUtils.createData(width, height, supplier);
        texture.uploadData(GL33.GL_RGB, width, height, GL33.GL_RGB, data);
        texture.unbind();

        return texture;
    }

    public static Texture2D createEmpty(int width, int height) {
        return createFrom(width, height, (x, y) -> new Vector3b(0, 0, 0));
    }

    public void setParameter(int name, int value) {
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, name, value);
    }

    public void uploadData(int internalFormat, int width, int height, int format, ByteBuffer data) {
        assert width == this.width;
        assert height == this.height;

        this.bind();
        GL33.glTexImage2D(GL33.GL_TEXTURE_2D,
            0,
            internalFormat,
            width, height,
            0,
            format,
            GL33.GL_UNSIGNED_BYTE,
            data);
        this.unbind();
    }

    @Override
    public void bind() {
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, this.getHandle());
    }

    @Override
    public void unbind() {
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0);
    }

    @Override
    public void dispose() {
        GL33.glDeleteTextures(this.getHandle());
    }
}
