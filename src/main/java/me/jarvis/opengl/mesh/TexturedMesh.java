package me.jarvis.opengl.mesh;

import me.jarvis.opengl.objects.IBO;
import me.jarvis.opengl.objects.VAO;
import me.jarvis.opengl.shader.ShaderProgram;
import me.jarvis.opengl.texture.Texture2D;
import org.lwjgl.opengl.GL30;

public class TexturedMesh extends Mesh {

    private final Texture2D texture;

    public TexturedMesh(IBO ibo, VAO vao, int vertexCount, ShaderProgram shader, Texture2D texture) {
        super(ibo, vao, vertexCount, shader);
        this.texture = texture;
    }

    public void draw() {
        super.shader.bind();
        this.texture.bind();
        super.vao.bind();
        super.ibo.bind();
        GL30.glDrawElements(GL30.GL_TRIANGLES, super.vertexCount, GL30.GL_UNSIGNED_INT, 0);
    }

    @Override
    public void dispose() {
        super.dispose();
        this.texture.dispose();
    }
}
