package me.jarvis.opengl.objects;

import me.jarvis.opengl.base.Bindable;
import me.jarvis.opengl.base.GLObject;
import org.lwjgl.opengl.GL30;

public class IBO extends GLObject implements Bindable {

    public IBO() {
        super(GL30.glGenBuffers());
    }

    @Override
    public void bind() {
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, this.getHandle());
    }

    @Override
    public void unbind() {
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    @Override
    public void dispose() {
        GL30.glDeleteBuffers(this.getHandle());
    }
}
