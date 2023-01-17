package me.jarvis.opengl.shader;

import me.jarvis.opengl.base.Bindable;
import me.jarvis.opengl.base.GLObject;
import me.jarvis.util.vector.Vector2;
import me.jarvis.util.vector.Vector2i;
import me.jarvis.util.vector.Vector3;
import me.jarvis.util.vector.Vector3i;
import org.lwjgl.opengl.GL30;

import javax.annotation.Nonnull;

public class ShaderProgram extends GLObject implements Bindable {

    public ShaderProgram() {
        super(GL30.glCreateProgram());
    }

    public void attachShader(@Nonnull Shader shader) {
        GL30.glAttachShader(this.getHandle(), shader.getHandle());
    }

    public static ShaderProgram fromShaders(Shader... shaders) {
        ShaderProgram program = new ShaderProgram();

        for (Shader shader : shaders) {
            program.attachShader(shader);
        }

        return program;
    }

    public void link() {
        GL30.glLinkProgram(this.getHandle());
        this.checkStatus();
    }

    public void checkStatus() {
        int status = GL30.glGetProgrami(this.getHandle(), GL30.GL_LINK_STATUS);
        if (status != GL30.GL_TRUE)
            throw new RuntimeException(GL30.glGetProgramInfoLog(this.getHandle()));
    }


    @Override
    public void bind() {
        GL30.glUseProgram(this.getHandle());
    }

    @Override
    public void unbind() {
        GL30.glUseProgram(0);
    }


    public int getAttributeLocation(CharSequence name) {
        return GL30.glGetAttribLocation(this.getHandle(), name);
    }

    public int getUniformLocation(CharSequence name) {
        return GL30.glGetUniformLocation(this.getHandle(), name);
    }


    public void setUniform(String name, int value) {
        GL30.glUniform1i(this.getUniformLocation(name), value);
    }

    public void setUniform(String name, Vector2 value) {
        GL30.glUniform2f(this.getUniformLocation(name), value.x(), value.y());
    }

    public void setUniform(String name, Vector2i value) {
        GL30.glUniform2i(this.getUniformLocation(name), value.x(), value.y());
    }

    public void setUniform(String name, int x, int y) {
        GL30.glUniform2i(this.getUniformLocation(name), x, y);
    }

    public void setUniform(String name, Vector3 value) {
        GL30.glUniform3f(this.getUniformLocation(name), value.x(), value.y(), value.z());
    }

    public void setUniform(String name, float x, float y, float z) {
        GL30.glUniform3f(this.getUniformLocation(name), x, y, z);
    }

    public void setUniform(String name, Vector3i value) {
        GL30.glUniform3i(this.getUniformLocation(name), value.x(), value.y(), value.z());
    }


    @Override
    public void dispose() {
        this.unbind();
        GL30.glDeleteProgram(this.getHandle());
    }
}
