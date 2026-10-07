package com.openworld.core;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    private final int width;
    private final int height;
    private final String title;

    private long handle;

    private GLFWErrorCallback errorCallback;

    public Window(
            int width,
            int height,
            String title
    ) {
        this.width = width;
        this.height = height;
        this.title = title;
    }

    public void create() {

        errorCallback =
                GLFWErrorCallback.createPrint(System.err);

        errorCallback.set();

        if (!glfwInit()) {
            throw new IllegalStateException(
                    "No se pudo inicializar GLFW"
            );
        }

        configureWindowHints();

        handle = glfwCreateWindow(
                width,
                height,
                title,
                NULL,
                NULL
        );

        if (handle == NULL) {
            throw new IllegalStateException(
                    "No se pudo crear la ventana GLFW"
            );
        }

        glfwMakeContextCurrent(handle);

        // Sincronización vertical.
        glfwSwapInterval(1);

        glfwShowWindow(handle);

        /*
         * Debe ejecutarse después de crear
         * y activar el contexto OpenGL.
         */
        GL.createCapabilities();

        configureCallbacks();

        /*
         * Viewport inicial.
         */
        glViewport(
                0,
                0,
                width,
                height
        );
    }

    private void configureWindowHints() {

        glfwDefaultWindowHints();

        glfwWindowHint(
                GLFW_VISIBLE,
                GLFW_FALSE
        );

        glfwWindowHint(
                GLFW_RESIZABLE,
                GLFW_TRUE
        );

        /*
         * OpenGL 3.3 Core.
         *
         * Es suficiente para el proyecto
         * y coincide con la filosofía
         * del proyecto del docente.
         */
        glfwWindowHint(
                GLFW_CONTEXT_VERSION_MAJOR,
                3
        );

        glfwWindowHint(
                GLFW_CONTEXT_VERSION_MINOR,
                3
        );

        glfwWindowHint(
                GLFW_OPENGL_PROFILE,
                GLFW_OPENGL_CORE_PROFILE
        );
    }

    private void configureCallbacks() {

        glfwSetFramebufferSizeCallback(
                handle,
                (window, width, height) -> {

                    if (width > 0 && height > 0) {
                        glViewport(
                                0,
                                0,
                                width,
                                height
                        );
                    }
                }
        );
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(handle);
    }

    public void requestClose() {
        glfwSetWindowShouldClose(
                handle,
                true
        );
    }

    public boolean isKeyPressed(int key) {

        return glfwGetKey(
                handle,
                key
        ) == GLFW_PRESS;
    }

    public void update() {

        glfwSwapBuffers(handle);

        glfwPollEvents();
    }

    public long getHandle() {
        return handle;
    }

    public void destroy() {

        if (handle != NULL) {

            glfwDestroyWindow(handle);

            handle = NULL;
        }

        glfwTerminate();

        if (errorCallback != null) {
            errorCallback.free();
            errorCallback = null;
        }
    }
}
