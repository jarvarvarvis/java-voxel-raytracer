package me.jarvis.opengl.objects;

import me.jarvis.opengl.base.Bindable;
import me.jarvis.opengl.base.GLObject;
import org.lwjgl.opengl.GL33;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class VAO extends GLObject implements Bindable {

    private final List<VBO> vboList;

    public VAO() {
        super(GL33.glGenVertexArrays());
        this.vboList = new ArrayList<>();
    }

    public void addVBO(@Nonnull VBO vbo) {
        this.vboList.add(vbo);
    }

    @Override
    public void bind() {
        GL33.glBindVertexArray(this.getHandle());
    }

    @Override
    public void unbind() {
        GL33.glBindVertexArray(0);
    }

    @Override
    public void dispose() {
        for (VBO vbo : this.vboList) {
            vbo.dispose();
        }
        GL33.glDeleteVertexArrays(this.getHandle());
    }
}
