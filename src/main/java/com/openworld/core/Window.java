package com.openworld.core;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    private final int initialWidth;
    private final int initialHeight;
    private final String title;

    private long handle;

    /*
     * Tamaño REAL del framebuffer.
     *
     * Puede ser diferente al tamaño lógico de la ventana,
     * especialmente en pantallas HiDPI.
     */
    private int framebufferWidth;
    private int framebufferHeight;

    private GLFWErrorCallback errorCallback;

    public Window(
            int width,
            int height,
            String title
    ) {
        this.initialWidth = width;
        this.initialHeight = height;
        this.title = title;
    }

    public void create() {

        configureErrorCallback();

        initGlfw();

        configureWindowHints();

        createWindow();

        activateOpenGLContext();

        updateInitialFramebufferSize();

        configureCallbacks();

        /*
         * El viewport siempre trabaja con
         * dimensiones reales del framebuffer.
         */
        glViewport(
                0,
                0,
                framebufferWidth,
                framebufferHeight
        );
    }

    private void configureErrorCallback() {

        errorCallback =
                GLFWErrorCallback.createPrint(System.err);

        errorCallback.set();
    }

    private void initGlfw() {

        if (!glfwInit()) {
            throw new IllegalStateException(
                    "No se pudo inicializar GLFW"
            );
        }
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
         * OpenGL 3.3 Core Profile.
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

    private void createWindow() {

        handle = glfwCreateWindow(
                initialWidth,
                initialHeight,
                title,
                NULL,
                NULL
        );

        if (handle == NULL) {
            throw new IllegalStateException(
                    "No se pudo crear la ventana GLFW"
            );
        }
    }

    private void activateOpenGLContext() {

        glfwMakeContextCurrent(handle);

        /*
         * VSync.
         */
        glfwSwapInterval(1);

        glfwShowWindow(handle);

        /*
         * LWJGL necesita conocer las funciones
         * disponibles del contexto OpenGL actual.
         *
         * Debe ejecutarse después de:
         *
         * glfwMakeContextCurrent(...)
         */
        GL.createCapabilities();
    }

    /**
     * Consulta el tamaño real inicial del framebuffer.
     *
     * No asumimos que sea igual a initialWidth/initialHeight.
     */
    private void updateInitialFramebufferSize() {

        try (MemoryStack stack = MemoryStack.stackPush()) {

            IntBuffer widthBuffer =
                    stack.mallocInt(1);

            IntBuffer heightBuffer =
                    stack.mallocInt(1);

            glfwGetFramebufferSize(
                    handle,
                    widthBuffer,
                    heightBuffer
            );

            framebufferWidth =
                    widthBuffer.get(0);

            framebufferHeight =
                    heightBuffer.get(0);
        }
    }

    private void configureCallbacks() {

        glfwSetFramebufferSizeCallback(
                handle,
                (window, width, height) -> {

                    /*
                     * GLFW puede reportar temporalmente
                     * 0 × 0 cuando la ventana está minimizada.
                     *
                     * Guardamos el valor recibido, pero
                     * se evita utilizar un viewport inválido.
                     */
                    framebufferWidth = width;
                    framebufferHeight = height;

                    System.out.printf(
                        "Framebuffer: %d x %d%n",
                        width,
                        height
                    );

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

    public int getFramebufferWidth() {
        return framebufferWidth;
    }

    public int getFramebufferHeight() {
        return framebufferHeight;
    }

    /**
     * Devuelve una relación de aspecto segura.
     *
     * Más adelante Camera podrá utilizar directamente
     * este método si se desea.
     */
    public float getAspectRatio() {

        if (framebufferHeight <= 0) {
            return 1.0f;
        }

        return (float) framebufferWidth
                / framebufferHeight;
    }

    public void setTitle(String title) {

        glfwSetWindowTitle(
                handle,
                title
        );
    }

    public void destroy() {

        if (handle != NULL) {

            /*
             * Elimina todos los callbacks asociados
             * antes de destruir la ventana.
             */
            GLFWFramebufferSizeCallback framebufferCallback =
                    glfwSetFramebufferSizeCallback(handle, null);

            if (framebufferCallback != null) {
                framebufferCallback.free();
            }

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
