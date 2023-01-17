package me.jarvis.opengl.objects;

import me.jarvis.opengl.base.Bindable;
import me.jarvis.opengl.base.GLObject;
import org.lwjgl.opengl.GL30;

public class VBO extends GLObject implements Bindable {

    private final int attributeId;

    public VBO(int attributeId) {
        super(GL30.glGenBuffers());
        this.attributeId = attributeId;
    }

    public int getAttributeId() {
        return attributeId;
    }

    public void enableAttribute() {
        GL30.glEnableVertexAttribArray(this.attributeId);
    }

    public void disableAttribute() {
        GL30.glDisableVertexAttribArray(this.attributeId);
    }

    @Override
    public void bind() {
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, this.getHandle());
    }

    @Override
    public void unbind() {
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
    }

    @Override
    public void dispose() {
        GL30.glDeleteBuffers(this.getHandle());
    }
}
