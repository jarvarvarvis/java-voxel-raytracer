package me.jarvis.opengl.objects;

import me.jarvis.opengl.base.Bindable;
import me.jarvis.opengl.base.GLObject;
import org.lwjgl.opengl.GL30;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class VAO extends GLObject implements Bindable {

    private final List<VBO> vboList;

    public VAO() {
        super(GL30.glGenVertexArrays());
        this.vboList = new ArrayList<>();
    }

    public void addVBO(@Nonnull VBO vbo) {
        this.vboList.add(vbo);
    }

    @Override
    public void bind() {
        GL30.glBindVertexArray(this.getHandle());
    }

    @Override
    public void unbind() {
        GL30.glBindVertexArray(0);
    }

    @Override
    public void dispose() {
        for (VBO vbo : this.vboList) {
            vbo.dispose();
        }
        GL30.glDeleteVertexArrays(this.getHandle());
    }
}
