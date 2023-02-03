package me.jarvis.opengl.objects;

import me.jarvis.opengl.base.Bindable;
import me.jarvis.opengl.base.GLObject;
import org.lwjgl.opengl.GL33;

public class IBO extends GLObject implements Bindable {

    public IBO() {
        super(GL33.glGenBuffers());
    }

    @Override
    public void bind() {
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, this.getHandle());
    }

    @Override
    public void unbind() {
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    @Override
    public void dispose() {
        GL33.glDeleteBuffers(this.getHandle());
    }
}
