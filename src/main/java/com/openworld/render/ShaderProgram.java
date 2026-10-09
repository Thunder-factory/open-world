package com.openworld.render;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;

public final class ShaderProgram {

    private final int programId;

    private final Map<String, Integer> uniformLocations =
            new HashMap<>();

    public ShaderProgram(
            String vertexShaderResource,
            String fragmentShaderResource
    ) {

        String vertexSource =
                loadResource(vertexShaderResource);

        String fragmentSource =
                loadResource(fragmentShaderResource);

        int vertexShaderId =
                compileShader(
                        GL_VERTEX_SHADER,
                        vertexSource,
                        vertexShaderResource
                );

        int fragmentShaderId =
                compileShader(
                        GL_FRAGMENT_SHADER,
                        fragmentSource,
                        fragmentShaderResource
                );

        programId = glCreateProgram();

        if (programId == 0) {

            glDeleteShader(vertexShaderId);
            glDeleteShader(fragmentShaderId);

            throw new IllegalStateException(
                    "No se pudo crear el programa OpenGL"
            );
        }

        glAttachShader(
                programId,
                vertexShaderId
        );

        glAttachShader(
                programId,
                fragmentShaderId
        );

        glLinkProgram(programId);

        if (glGetProgrami(
                programId,
                GL_LINK_STATUS
        ) == GL_FALSE) {

            String log =
                    glGetProgramInfoLog(programId);

            glDetachShader(
                    programId,
                    vertexShaderId
            );

            glDetachShader(
                    programId,
                    fragmentShaderId
            );

            glDeleteShader(vertexShaderId);
            glDeleteShader(fragmentShaderId);

            glDeleteProgram(programId);

            throw new IllegalStateException(
                    "Error enlazando shaders:\n"
                            + log
            );
        }

        /*
         * Una vez enlazado el programa,
         * los shaders individuales ya no
         * son necesarios.
         */
        glDetachShader(
                programId,
                vertexShaderId
        );

        glDetachShader(
                programId,
                fragmentShaderId
        );

        glDeleteShader(vertexShaderId);
        glDeleteShader(fragmentShaderId);
    }

    private int compileShader(
            int type,
            String source,
            String resourceName
    ) {

        int shaderId =
                glCreateShader(type);

        if (shaderId == 0) {
            throw new IllegalStateException(
                    "No se pudo crear shader: "
                            + resourceName
            );
        }

        glShaderSource(
                shaderId,
                source
        );

        glCompileShader(shaderId);

        if (glGetShaderi(
                shaderId,
                GL_COMPILE_STATUS
        ) == GL_FALSE) {

            String log =
                    glGetShaderInfoLog(shaderId);

            glDeleteShader(shaderId);

            throw new IllegalStateException(
                    "Error compilando "
                            + resourceName
                            + ":\n"
                            + log
            );
        }

        return shaderId;
    }

    private String loadResource(
            String resourcePath
    ) {

        try (InputStream inputStream =
                     ShaderProgram.class
                             .getResourceAsStream(
                                     resourcePath
                             )) {

            if (inputStream == null) {

                throw new IllegalStateException(
                        "No se encontró recurso: "
                                + resourcePath
                );
            }

            return new String(
                    inputStream.readAllBytes(),
                    StandardCharsets.UTF_8
            );

        } catch (IOException e) {

            throw new IllegalStateException(
                    "No se pudo leer recurso: "
                            + resourcePath,
                    e
            );
        }
    }

    public void bind() {
        glUseProgram(programId);
    }

    public static void unbind() {
        glUseProgram(0);
    }

    private int getUniformLocation(
            String name
    ) {

        Integer cached =
                uniformLocations.get(name);

        if (cached != null) {
            return cached;
        }

        int location =
                glGetUniformLocation(
                        programId,
                        name
                );

        if (location < 0) {
            throw new IllegalArgumentException(
                    "Uniform no encontrado: "
                            + name
            );
        }

        uniformLocations.put(
                name,
                location
        );

        return location;
    }

    public void setUniform(
            String name,
            Matrix4f matrix
    ) {

        int location =
                getUniformLocation(name);

        try (MemoryStack stack =
                     MemoryStack.stackPush()) {

            FloatBuffer buffer =
                    stack.mallocFloat(16);

            matrix.get(buffer);

            glUniformMatrix4fv(
                    location,
                    false,
                    buffer
            );
        }
    }

    public void setUniform(
            String name,
            Vector3f vector
    ) {

        glUniform3f(
                getUniformLocation(name),
                vector.x,
                vector.y,
                vector.z
        );
    }

    public void setUniform(String name, float value) {
        
        glUniform1f(getUniformLocation(name), value);
    }

    public void setUniform(String name, int value) {
        
        glUniform1i(getUniformLocation(name), value);
    }

    public void cleanup() {
        glDeleteProgram(programId);
    }
}