package me.jarvis.opengl.mesh;

import me.jarvis.opengl.objects.IBO;
import me.jarvis.opengl.objects.VAO;
import me.jarvis.opengl.objects.VBO;
import me.jarvis.opengl.shader.ShaderProgram;
import me.jarvis.opengl.texture.Texture2D;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class MeshBuilder {

    private Texture2D texture;

    private ShaderProgram shader;

    private final List<VertexBuffer> vertexBuffers;

    private int[] indices;

    public MeshBuilder() {
        this.vertexBuffers = new ArrayList<>();
        this.texture = null;
    }

    public MeshBuilder addBuffer(VertexBuffer data) {
        this.vertexBuffers.add(data);
        return this;
    }

    public MeshBuilder setIndices(int... indices) {
        this.indices = indices;
        return this;
    }

    public MeshBuilder setShader(ShaderProgram shader) {
        this.shader = shader;
        return this;
    }

    public MeshBuilder setTexture(Texture2D texture) {
        this.texture = texture;
        return this;
    }

    private IBO finishIBO() {
        IBO ibo = new IBO();
        ibo.bind();

        IntBuffer nativeIndexBuffer = BufferUtils.createIntBuffer(this.indices.length);
        nativeIndexBuffer.put(this.indices).flip();

        GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, nativeIndexBuffer, GL30.GL_STATIC_DRAW);
        ibo.unbind();
        return ibo;
    }

    private List<VBO> finishVBOs() {
        int currentAttributeId = 0;

        List<VBO> vboList = new ArrayList<>();
        for (VertexBuffer buffer : this.vertexBuffers) {
            VBO vbo = new VBO(currentAttributeId);
            vbo.bind();

            float[] data = buffer.buildDataBuffer();
            FloatBuffer nativeData = BufferUtils.createFloatBuffer(data.length);
            nativeData.put(data).flip();

            GL30.glBufferData(GL30.GL_ARRAY_BUFFER, nativeData, GL30.GL_STATIC_DRAW);
            GL30.glVertexAttribPointer(currentAttributeId,
                buffer.getComponents(),
                GL30.GL_FLOAT,
                false,
                0,
                0);
            GL30.glEnableVertexAttribArray(currentAttributeId);

            vbo.unbind();

            currentAttributeId++;
            vboList.add(vbo);
        }

        return vboList;
    }

    private VAO finishVAO() {
        VAO vao = new VAO();
        vao.bind();
        List<VBO> vboList = this.finishVBOs();
        for (VBO vbo : vboList) {
            vao.addVBO(vbo);
        }
        vao.unbind();
        return vao;
    }

    public Mesh build() {
        IBO ibo = this.finishIBO();
        VAO vao = this.finishVAO();

        if (this.texture == null) {
            return new Mesh(ibo, vao, this.indices.length, this.shader);
        }
        return new TexturedMesh(ibo, vao, this.indices.length, this.shader, this.texture);
    }
}
