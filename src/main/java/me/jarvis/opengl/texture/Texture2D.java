package me.jarvis.opengl.texture;

import me.jarvis.opengl.base.Bindable;
import me.jarvis.opengl.base.GLObject;
import me.jarvis.util.vector.Vector3b;
import me.jarvis.util.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;

public class Texture2D extends GLObject implements Bindable {

    private static final Logger log = LogManager.getLogger(Texture2D.class);

    private final int width, height;

    private Texture2D(int width, int height) {
        super(GL30.glGenTextures());
        assert width > 0;
        assert height > 0;
        this.width = width;
        this.height = height;
    }

    public static Texture2D create(int width, int height) {
        Texture2D texture = new Texture2D(width, height);

        texture.bind();
        texture.setParameter(GL30.GL_TEXTURE_WRAP_S, GL30.GL_CLAMP_TO_EDGE);
        texture.setParameter(GL30.GL_TEXTURE_WRAP_T, GL30.GL_CLAMP_TO_EDGE);
        texture.setParameter(GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_NEAREST);
        texture.setParameter(GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_NEAREST);
        texture.unbind();

        return texture;
    }

    public static Texture2D createFrom(int width, int height, Function.Binary<Integer, Integer, Vector3b> supplier) {
        Texture2D texture = Texture2D.create(width, height);

        texture.bind();
        ByteBuffer data = TextureUtils.createData(width, height, supplier);
        texture.uploadData(GL30.GL_RGB, width, height, GL30.GL_RGB, data);
        texture.unbind();

        return texture;
    }

    public static Texture2D createEmpty(int width, int height) {
        return createFrom(width, height, (x, y) -> new Vector3b(0, 0, 0));
    }

    public void setParameter(int name, int value) {
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, name, value);
    }

    public void uploadData(int internalFormat, int width, int height, int format, ByteBuffer data) {
        assert width == this.width;
        assert height == this.height;

        this.bind();
        GL30.glTexImage2D(GL30.GL_TEXTURE_2D,
            0,
            internalFormat,
            width, height,
            0,
            format,
            GL30.GL_UNSIGNED_BYTE,
            data);
        this.unbind();
    }

    @Override
    public void bind() {
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, this.getHandle());
    }

    @Override
    public void unbind() {
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
    }

    @Override
    public void dispose() {
        GL30.glDeleteTextures(this.getHandle());
    }
}
