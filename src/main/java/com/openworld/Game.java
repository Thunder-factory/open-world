package com.openworld;

import com.openworld.core.Window;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Game {

    private Window window;

    public void run() {
        try {
            init();
            loop();
        } finally {
            cleanup();
        }
    }

    private void init() {
        window = new Window(
                1280,
                720,
                "Open World"
        );

        window.create();

        glEnable(GL_DEPTH_TEST);

        System.out.println("Open World iniciado correctamente");
    }

    private void loop() {

        while (!window.shouldClose()) {

            processInput();

            update();

            render();

            window.update();
        }
    }

    private void processInput() {

        if (window.isKeyPressed(GLFW_KEY_ESCAPE)) {
            window.requestClose();
        }
    }

    private void update() {
        // Más adelante:
        // auto.update(deltaTime);
        // semaforos.update(deltaTime);
        // entregas.update(...);
    }

    private void render() {

        glClearColor(
                0.12f,
                0.18f,
                0.24f,
                1.0f
        );

        glClear(
                GL_COLOR_BUFFER_BIT |
                GL_DEPTH_BUFFER_BIT
        );
    }

    private void cleanup() {

        if (window != null) {
            window.destroy();
        }

        System.out.println("Open World finalizado");
    }
}