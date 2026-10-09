package com.openworld.visual;

import com.openworld.render.Material;
import com.openworld.render.Renderer;

public final class VehicleVisual {

    private static final float WHEEL_CENTER_HEIGHT = 0.34f;
    private static final float WHEEL_AXIS_ROTATION =
            (float) Math.toRadians(-90.0);

    private static final float FRONT_GLASS_LOCAL_Y = 1.18f;
    private static final float FRONT_GLASS_FORWARD = 1.04f;
    private static final float FRONT_GLASS_WIDTH = 1.50f;
    private static final float FRONT_GLASS_HEIGHT = 0.64f;
    private static final float FRONT_GLASS_PITCH =
            (float) Math.toRadians(-52.4);
    private static final float FRONT_GLASS_YAW = (float) Math.PI;

    private static final float REAR_GLASS_LOCAL_Y = 1.15f;
    private static final float REAR_GLASS_FORWARD = -1.05f;
    private static final float REAR_GLASS_WIDTH = 1.46f;
    private static final float REAR_GLASS_HEIGHT = 0.66f;
    private static final float REAR_GLASS_PITCH =
            (float) Math.toRadians(-47.9);
    private static final float REAR_GLASS_YAW = 0.0f;

    private static final Material TIRE_MATERIAL =
            new Material(0.04f, 0.04f, 0.04f, 0.0f, 0.08f, 8.0f);
    private static final Material HUB_MATERIAL =
            new Material(0.34f, 0.36f, 0.38f, 0.0f, 0.75f, 64.0f);
    private static final Material GLASS_MATERIAL =
            new Material(0.07f, 0.14f, 0.21f, 0.0f, 0.90f, 96.0f);
    private static final Material FRONT_LIGHT_MATERIAL =
            new Material(1.00f, 0.94f, 0.58f, 0.35f, 0.65f, 64.0f);
    private static final Material REAR_LIGHT_MATERIAL =
            new Material(0.82f, 0.03f, 0.02f, 0.25f, 0.55f, 48.0f);
    private static final Material GRILLE_MATERIAL =
            new Material(0.045f, 0.050f, 0.060f, 0.0f, 0.35f, 32.0f);
    private static final Material PLATE_MATERIAL =
            new Material(0.82f, 0.82f, 0.70f, 0.0f, 0.20f, 24.0f);

    public void render(Renderer renderer, float x, float z, float angle,
                       float scale, Material bodyMaterial) {
        if (bodyMaterial == null) {
            throw new IllegalArgumentException(
                    "El material de carroceria no puede ser null");
        }
        if (scale <= 0.0f) {
            return;
        }

        renderer.vehicleBody(x, 0.0f, z, scale, angle, bodyMaterial);

        drawWheel(renderer, x, z, angle, scale, -1.05f, 1.25f);
        drawWheel(renderer, x, z, angle, scale, 1.05f, 1.25f);
        drawWheel(renderer, x, z, angle, scale, -1.05f, -1.25f);
        drawWheel(renderer, x, z, angle, scale, 1.05f, -1.25f);

        drawQuad(renderer, x, z, angle, scale,
                0.0f, FRONT_GLASS_LOCAL_Y, FRONT_GLASS_FORWARD,
                FRONT_GLASS_WIDTH, FRONT_GLASS_HEIGHT,
                FRONT_GLASS_PITCH, FRONT_GLASS_YAW,
                GLASS_MATERIAL);
        drawQuad(renderer, x, z, angle, scale,
                0.0f, REAR_GLASS_LOCAL_Y, REAR_GLASS_FORWARD,
                REAR_GLASS_WIDTH, REAR_GLASS_HEIGHT,
                REAR_GLASS_PITCH, REAR_GLASS_YAW,
                GLASS_MATERIAL);

        drawQuad(renderer, x, z, angle, scale,
                -0.825f, 1.16f, 0.35f, 0.82f, 0.38f,
                0.0f, (float) -Math.PI / 2.0f, GLASS_MATERIAL);
        drawQuad(renderer, x, z, angle, scale,
                -0.825f, 1.16f, -0.38f, 0.74f, 0.38f,
                0.0f, (float) -Math.PI / 2.0f, GLASS_MATERIAL);
        drawQuad(renderer, x, z, angle, scale,
                0.825f, 1.16f, 0.35f, 0.82f, 0.38f,
                0.0f, (float) Math.PI / 2.0f, GLASS_MATERIAL);
        drawQuad(renderer, x, z, angle, scale,
                0.825f, 1.16f, -0.38f, 0.74f, 0.38f,
                0.0f, (float) Math.PI / 2.0f, GLASS_MATERIAL);

        drawQuad(renderer, x, z, angle, scale,
                -0.65f, 0.70f, 2.16f, 0.34f, 0.20f,
                0.0f, (float) Math.PI, FRONT_LIGHT_MATERIAL);
        drawQuad(renderer, x, z, angle, scale,
                0.65f, 0.70f, 2.16f, 0.34f, 0.20f,
                0.0f, (float) Math.PI, FRONT_LIGHT_MATERIAL);
        drawQuad(renderer, x, z, angle, scale,
                0.0f, 0.52f, 2.17f, 0.78f, 0.20f,
                0.0f, (float) Math.PI, GRILLE_MATERIAL);

        drawQuad(renderer, x, z, angle, scale,
                -0.62f, 0.64f, -2.16f, 0.30f, 0.18f,
                0.0f, 0.0f, REAR_LIGHT_MATERIAL);
        drawQuad(renderer, x, z, angle, scale,
                0.62f, 0.64f, -2.16f, 0.30f, 0.18f,
                0.0f, 0.0f, REAR_LIGHT_MATERIAL);
        drawQuad(renderer, x, z, angle, scale,
                0.0f, 0.55f, -2.17f, 0.58f, 0.20f,
                0.0f, 0.0f, PLATE_MATERIAL);
    }

    private void drawWheel(Renderer renderer, float vehicleX, float vehicleZ,
                           float angle, float scale,
                           float localX, float localForward) {
        drawCylinder(renderer, vehicleX, vehicleZ, angle, scale,
                localX, WHEEL_CENTER_HEIGHT, localForward,
                0.72f, 0.38f, TIRE_MATERIAL);
        drawCylinder(renderer, vehicleX, vehicleZ, angle, scale,
                Math.copySign(1.27f, localX), WHEEL_CENTER_HEIGHT,
                localForward, 0.34f, 0.08f, HUB_MATERIAL);
    }

    private void drawCylinder(Renderer renderer, float vehicleX, float vehicleZ,
                              float angle, float scale,
                              float localX, float localY, float localForward,
                              float diameter, float width,
                              Material material) {
        float worldX = calculateWorldX(vehicleX, angle,
                localX * scale, localForward * scale);
        float worldZ = calculateWorldZ(vehicleZ, angle,
                localX * scale, localForward * scale);
        renderer.cylinder(worldX, localY * scale, worldZ,
                diameter * scale, width * scale, diameter * scale,
                0.0f, angle, WHEEL_AXIS_ROTATION, material);
    }

    private void drawQuad(Renderer renderer, float vehicleX, float vehicleZ,
                          float angle, float scale,
                          float localX, float localY, float localForward,
                          float width, float height,
                          float rotationX, float localRotationY,
                          Material material) {
        float worldX = calculateWorldX(vehicleX, angle,
                localX * scale, localForward * scale);
        float worldZ = calculateWorldZ(vehicleZ, angle,
                localX * scale, localForward * scale);
        renderer.quad(worldX, localY * scale, worldZ,
                width * scale, height * scale,
                rotationX, angle + localRotationY, 0.0f, material);
    }

    private float calculateWorldX(float vehicleX, float angle,
                                  float localX, float localForward) {
        float sin = (float) Math.sin(angle);
        float cos = (float) Math.cos(angle);
        return vehicleX + cos * localX - sin * localForward;
    }

    private float calculateWorldZ(float vehicleZ, float angle,
                                  float localX, float localForward) {
        float sin = (float) Math.sin(angle);
        float cos = (float) Math.cos(angle);
        return vehicleZ - sin * localX - cos * localForward;
    }
}
