package me.jarvis.opengl.shader;

import org.lwjgl.opengl.GL30;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ShaderFactory {

    public static Shader createShader(int type, CharSequence sourceCode) {
        Shader shader = new Shader(type);
        shader.setSourceCode(sourceCode);
        shader.compile();

        return shader;
    }

    public static Shader createVertexShader(CharSequence sourceCode) {
        return createShader(GL30.GL_VERTEX_SHADER, sourceCode);
    }

    public static Shader createFragmentShader(CharSequence sourceCode) {
        return createShader(GL30.GL_FRAGMENT_SHADER, sourceCode);
    }
}
