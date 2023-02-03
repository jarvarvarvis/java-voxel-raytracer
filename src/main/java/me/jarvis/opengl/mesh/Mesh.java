package me.jarvis.opengl.mesh;

import me.jarvis.opengl.base.Disposable;
import me.jarvis.opengl.objects.IBO;
import me.jarvis.opengl.objects.VAO;
import me.jarvis.opengl.shader.ShaderProgram;
import org.lwjgl.opengl.GL33;

public class Mesh implements Disposable {

    protected final IBO ibo;
    protected final VAO vao;
    protected final int vertexCount;

    protected final ShaderProgram shader;

    public Mesh(IBO ibo, VAO vao, int vertexCount, ShaderProgram shader) {
        this.ibo = ibo;
        this.vao = vao;
        this.vertexCount = vertexCount;
        this.shader = shader;
    }

    public ShaderProgram getShader() {
        return shader;
    }

    public void draw() {
        this.shader.bind();
        this.vao.bind();
        this.ibo.bind();
        GL33.glDrawElements(GL33.GL_TRIANGLES, this.vertexCount, GL33.GL_UNSIGNED_INT, 0);
    }

    @Override
    public void dispose() {
        this.ibo.dispose();
        this.vao.dispose();
        this.shader.dispose();
    }
}
