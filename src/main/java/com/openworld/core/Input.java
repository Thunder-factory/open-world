package com.openworld.core;

import org.lwjgl.glfw.GLFWKeyCallback;

import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.*;

public final class Input {

    private final long windowHandle;

    /*
     * Estado continuo.
     *
     * true mientras la tecla permanezca presionada.
     */
    private final boolean[] currentKeys =
            new boolean[GLFW_KEY_LAST + 1];

    /*
     * Eventos disponibles durante el frame actual.
     */
    private final boolean[] pressedThisFrame =
            new boolean[GLFW_KEY_LAST + 1];

    private final boolean[] releasedThisFrame =
            new boolean[GLFW_KEY_LAST + 1];

    /*
     * Eventos recibidos por GLFW durante glfwPollEvents().
     *
     * Se transfieren al comienzo del siguiente frame.
     */
    private final boolean[] pendingPressed =
            new boolean[GLFW_KEY_LAST + 1];

    private final boolean[] pendingReleased =
            new boolean[GLFW_KEY_LAST + 1];

    private GLFWKeyCallback keyCallback;

    public Input(long windowHandle) {

        this.windowHandle = windowHandle;

        configureKeyboardCallback();
    }

    private void configureKeyboardCallback() {

        keyCallback = new GLFWKeyCallback() {

            @Override
            public void invoke(
                    long window,
                    int key,
                    int scancode,
                    int action,
                    int mods
            ) {

                /*
                 * GLFW puede enviar GLFW_KEY_UNKNOWN = -1
                 * para teclas que no tienen token conocido.
                 */
                if (!isValidKey(key)) {
                    return;
                }

                switch (action) {

                    case GLFW_PRESS -> {

                        currentKeys[key] = true;
                        pendingPressed[key] = true;
                    }

                    case GLFW_RELEASE -> {

                        currentKeys[key] = false;
                        pendingReleased[key] = true;
                    }

                    case GLFW_REPEAT -> {

                        /*
                         * Sigue estando mantenida.
                         *
                         * NO consideramos REPEAT como
                         * una nueva pulsación.
                         */
                        currentKeys[key] = true;
                    }

                    default -> {
                        // No hacer nada.
                    }
                }
            }
        };

        glfwSetKeyCallback(
                windowHandle,
                keyCallback
        );
    }

    /**
     * Debe ejecutarse una vez al inicio de cada frame.
     *
     * Transfiere los eventos que GLFW recibió
     * durante glfwPollEvents() del frame anterior.
     */
    public void update() {

        System.arraycopy(
                pendingPressed,
                0,
                pressedThisFrame,
                0,
                pendingPressed.length
        );

        System.arraycopy(
                pendingReleased,
                0,
                releasedThisFrame,
                0,
                pendingReleased.length
        );

        Arrays.fill(
                pendingPressed,
                false
        );

        Arrays.fill(
                pendingReleased,
                false
        );
    }

    /**
     * true mientras la tecla permanezca presionada.
     *
     * Ideal para:
     * W, A, S, D, flechas, SPACE...
     */
    public boolean isDown(int key) {

        if (!isValidKey(key)) {
            return false;
        }

        return currentKeys[key];
    }

    /**
     * true solamente durante un frame
     * por cada pulsación física.
     *
     * Ideal para:
     * C, N, F, M, R...
     */
    public boolean wasPressed(int key) {

        if (!isValidKey(key)) {
            return false;
        }

        return pressedThisFrame[key];
    }

    /**
     * true solamente durante un frame
     * cuando la tecla es liberada.
     */
    public boolean wasReleased(int key) {

        if (!isValidKey(key)) {
            return false;
        }

        return releasedThisFrame[key];
    }

    private boolean isValidKey(int key) {

        return key >= 0
                && key <= GLFW_KEY_LAST;
    }

    public void cleanup() {

        if (keyCallback != null) {

            GLFWKeyCallback previousCallback =
                    glfwSetKeyCallback(
                            windowHandle,
                            null
                    );

            if (previousCallback != null) {
                previousCallback.free();
            }

            keyCallback = null;
        }
    }
}