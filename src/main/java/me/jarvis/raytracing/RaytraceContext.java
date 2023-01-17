package me.jarvis.raytracing;

import me.jarvis.opengl.base.Disposable;
import me.jarvis.opengl.shader.ShaderProgram;

import java.io.IOException;

public class RaytraceContext implements Disposable {

    private final World world;
    private final Camera camera;
    private final DrawQuad drawQuad;

    public RaytraceContext(World world, Camera camera) throws IOException {
        this.world = world;
        this.camera = camera;
        this.drawQuad = new DrawQuad();
    }

    public void draw(int windowWidth, int windowHeight) {
        ShaderProgram drawShader = this.drawQuad.getMesh().getShader();
        drawShader.setUniform("iResolution", windowWidth, windowHeight);
        drawShader.setUniform("worldSize", this.world.getWorldSize());
        drawShader.setUniform("cameraOrigin", this.camera.getPosition());
        drawShader.setUniform("cameraRotation", this.camera.getPitchAsRadians(), this.camera.getYawAsRadians(), 0);
        this.world.bindWorldTexture();
        this.drawQuad.draw();
    }

    @Override
    public void dispose() {
        this.drawQuad.dispose();
    }
}
