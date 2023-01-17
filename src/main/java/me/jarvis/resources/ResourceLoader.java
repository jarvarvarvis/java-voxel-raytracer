package me.jarvis.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ResourceLoader implements AutoCloseable {

    InputStream resourceInputStream;

    public ResourceLoader(String path) {
        ClassLoader classLoader = getClass().getClassLoader();
        this.resourceInputStream = classLoader.getResourceAsStream(path);
    }

    public String readToString() throws IOException {
        InputStreamReader streamReader = new InputStreamReader(this.resourceInputStream, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(streamReader);
        StringBuilder builder = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line).append("\n");
        }

        return builder.toString();
    }

    @Override
    public void close() throws IOException {
        this.resourceInputStream.close();
    }
}
