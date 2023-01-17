package me.jarvis.raytracing;

import me.jarvis.opengl.base.Disposable;
import me.jarvis.opengl.mesh.Mesh;
import me.jarvis.opengl.mesh.MeshBuilder;
import me.jarvis.opengl.mesh.Vertex;
import me.jarvis.opengl.mesh.VertexBuffer;
import me.jarvis.opengl.shader.Shader;
import me.jarvis.opengl.shader.ShaderFactory;
import me.jarvis.opengl.shader.ShaderProgram;
import me.jarvis.resources.ResourceLoader;

import java.io.IOException;

public class DrawQuad implements Disposable {

    private final Mesh mesh;

    private static ShaderProgram loadShader() throws IOException {
        String vertexCode, fragmentCode;
        try (ResourceLoader loader = new ResourceLoader("shaders/vert.glsl")) {
            vertexCode = loader.readToString();
        }
        try (ResourceLoader loader = new ResourceLoader("shaders/frag.glsl")) {
            fragmentCode = loader.readToString();
        }

        Shader vertexShader = ShaderFactory.createVertexShader(vertexCode);
        Shader fragmentShader = ShaderFactory.createFragmentShader(fragmentCode);

        ShaderProgram program = ShaderProgram.fromShaders(vertexShader, fragmentShader);
        program.link();

        vertexShader.dispose();
        fragmentShader.dispose();

        return program;
    }

    private static Mesh createMesh(ShaderProgram shader) {
        return new MeshBuilder()
            .addBuffer(new VertexBuffer(
                new Vertex(-1.0f,  1.0f, 0.0f),
                new Vertex(-1.0f, -1.0f, 0.0f),
                new Vertex( 1.0f, -1.0f, 0.0f),
                new Vertex( 1.0f,  1.0f, 0.0f)
            ))
            .addBuffer(new VertexBuffer(
                new Vertex(0.0f, 1.0f),
                new Vertex(0.0f, 0.0f),
                new Vertex(1.0f, 0.0f),
                new Vertex(1.0f, 1.0f)
            ))
            .setIndices(0, 1, 2, 0, 2, 3)
            .setShader(shader)
            .build();
    }

    public DrawQuad() throws IOException {
        ShaderProgram shader = loadShader();
        this.mesh = createMesh(shader);
    }

    public void draw() {
        this.mesh.draw();
    }

    public Mesh getMesh() {
        return mesh;
    }

    @Override
    public void dispose() {
        this.mesh.dispose();
    }
}
