package me.jarvis.opengl.shader;

import me.jarvis.opengl.base.GLObject;
import org.lwjgl.opengl.GL30;

import javax.annotation.Nonnull;

public class Shader extends GLObject {

    private final int shaderType;

    public Shader(int shaderType) {
        super(GL30.glCreateShader(shaderType));
        this.shaderType = shaderType;
    }

    public int getShaderType() {
        return shaderType;
    }

    public void setSourceCode(@Nonnull CharSequence source) {
        GL30.glShaderSource(this.getHandle(), source);
    }

    public void compile() {
        GL30.glCompileShader(this.getHandle());
        this.checkStatus();
    }

    private void checkStatus() {
        int status = GL30.glGetShaderi(this.getHandle(), GL30.GL_COMPILE_STATUS);
        if (status != GL30.GL_TRUE)
            throw new RuntimeException(GL30.glGetShaderInfoLog(this.getHandle()));
    }

    @Override
    public void dispose() {
        GL30.glDeleteShader(this.getHandle());
    }
}
