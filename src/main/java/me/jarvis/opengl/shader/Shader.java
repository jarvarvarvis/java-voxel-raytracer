package me.jarvis.opengl.shader;

import me.jarvis.opengl.base.GLObject;
import org.lwjgl.opengl.GL33;

import javax.annotation.Nonnull;

public class Shader extends GLObject {

    private final int shaderType;

    public Shader(int shaderType) {
        super(GL33.glCreateShader(shaderType));
        this.shaderType = shaderType;
    }

    public int getShaderType() {
        return shaderType;
    }

    public void setSourceCode(@Nonnull CharSequence source) {
        GL33.glShaderSource(this.getHandle(), source);
    }

    public void compile() {
        GL33.glCompileShader(this.getHandle());
        this.checkStatus();
    }

    private void checkStatus() {
        int status = GL33.glGetShaderi(this.getHandle(), GL33.GL_COMPILE_STATUS);
        if (status != GL33.GL_TRUE)
            throw new RuntimeException(GL33.glGetShaderInfoLog(this.getHandle()));
    }

    @Override
    public void dispose() {
        GL33.glDeleteShader(this.getHandle());
    }
}
