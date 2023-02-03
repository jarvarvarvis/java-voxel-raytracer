package me.jarvis.opengl.objects;

import me.jarvis.opengl.base.Bindable;
import me.jarvis.opengl.base.GLObject;
import org.lwjgl.opengl.GL33;

public class VBO extends GLObject implements Bindable {

    private final int attributeId;

    public VBO(int attributeId) {
        super(GL33.glGenBuffers());
        this.attributeId = attributeId;
    }

    public int getAttributeId() {
        return attributeId;
    }

    public void enableAttribute() {
        GL33.glEnableVertexAttribArray(this.attributeId);
    }

    public void disableAttribute() {
        GL33.glDisableVertexAttribArray(this.attributeId);
    }

    @Override
    public void bind() {
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, this.getHandle());
    }

    @Override
    public void unbind() {
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);
    }

    @Override
    public void dispose() {
        GL33.glDeleteBuffers(this.getHandle());
    }
}
