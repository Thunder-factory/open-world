package com.openworld.visual;

import com.openworld.render.Material;
import com.openworld.render.Renderer;

import static com.openworld.vehicle.VehicleGeometry.*;

public final class VehicleVisual {

    private static final float WHEEL_AXIS_ROTATION =
            (float) Math.toRadians(-90.0);

    private static final Material TIRE_MATERIAL =
            new Material(0.025f, 0.025f, 0.025f, 0.0f, 0.05f, 8.0f);
    private static final Material HUB_MATERIAL =
            new Material(0.34f, 0.36f, 0.38f, 0.0f, 0.72f, 60.0f);
    private static final Material GLASS_TRIM_MATERIAL =
            new Material(0.010f, 0.010f, 0.012f, 0.0f, 0.10f, 16.0f);
    private static final Material GLASS_MATERIAL =
            new Material(0.008f, 0.012f, 0.016f, 0.0f, 0.45f, 64.0f);
    private static final Material FRONT_LIGHT_MATERIAL =
            new Material(1.00f, 0.94f, 0.58f, 0.32f, 0.65f, 64.0f);
    private static final Material REAR_LIGHT_MATERIAL =
            new Material(0.84f, 0.025f, 0.015f, 0.24f, 0.55f, 48.0f);
    private static final Material GRILLE_MATERIAL =
            new Material(0.020f, 0.025f, 0.030f, 0.0f, 0.25f, 28.0f);
    private static final Material PLATE_MATERIAL =
            new Material(0.78f, 0.78f, 0.68f, 0.0f, 0.20f, 24.0f);
    private static final Material MIRROR_GLASS_MATERIAL =
            new Material(0.08f, 0.13f, 0.17f, 0.0f, 0.82f, 72.0f);

    public void render(Renderer renderer, float x, float z, float angle,
                       float scale, Material bodyMaterial) {
        if (renderer == null) {
            throw new IllegalArgumentException("El renderer no puede ser null");
        }
        if (bodyMaterial == null) {
            throw new IllegalArgumentException(
                    "El material de carroceria no puede ser null");
        }
        if (scale <= 0.0f) {
            return;
        }

        renderer.vehicleBody(x, 0.0f, z, scale, angle, bodyMaterial);
        renderer.vehicleGlassTrim(x, 0.0f, z, scale, angle,
                GLASS_TRIM_MATERIAL);
        renderer.vehicleGlass(x, 0.0f, z, scale, angle, GLASS_MATERIAL);
        renderer.vehicleFrontLights(x, 0.0f, z, scale, angle,
                FRONT_LIGHT_MATERIAL);
        renderer.vehicleGrille(x, 0.0f, z, scale, angle, GRILLE_MATERIAL);
        renderer.vehicleRearLights(x, 0.0f, z, scale, angle,
                REAR_LIGHT_MATERIAL);
        renderer.vehicleRearPlate(x, 0.0f, z, scale, angle,
                PLATE_MATERIAL);

        drawWheel(renderer, x, z, angle, scale,
                -WHEEL_LOCAL_X, FRONT_WHEEL_FORWARD);
        drawWheel(renderer, x, z, angle, scale,
                WHEEL_LOCAL_X, FRONT_WHEEL_FORWARD);
        drawWheel(renderer, x, z, angle, scale,
                -WHEEL_LOCAL_X, REAR_WHEEL_FORWARD);
        drawWheel(renderer, x, z, angle, scale,
                WHEEL_LOCAL_X, REAR_WHEEL_FORWARD);

        drawMirror(renderer, x, z, angle, scale, -1.0f, bodyMaterial);
        drawMirror(renderer, x, z, angle, scale, 1.0f, bodyMaterial);
    }

    private void drawWheel(Renderer renderer, float vehicleX, float vehicleZ,
                           float angle, float scale,
                           float localX, float localForward) {
        drawCylinderPart(renderer, vehicleX, vehicleZ, angle, scale,
                localX, WHEEL_CENTER_Y, localForward,
                WHEEL_DIAMETER, WHEEL_WIDTH, TIRE_MATERIAL);
        drawCylinderPart(renderer, vehicleX, vehicleZ, angle, scale,
                Math.copySign(HUB_LOCAL_X, localX), WHEEL_CENTER_Y,
                localForward, HUB_DIAMETER, HUB_WIDTH, HUB_MATERIAL);
    }

    private void drawMirror(Renderer renderer, float vehicleX, float vehicleZ,
                            float angle, float scale, float side,
                            Material bodyMaterial) {
        float localX = MIRROR_LOCAL_X * side;
        drawSpherePart(renderer, vehicleX, vehicleZ, angle, scale,
                localX, MIRROR_Y, MIRROR_FORWARD,
                MIRROR_HOUSING_WIDTH, MIRROR_HOUSING_HEIGHT,
                MIRROR_HOUSING_DEPTH, bodyMaterial);

        float glassLocalX = (MIRROR_LOCAL_X + MIRROR_GLASS_X_OFFSET) * side;
        float rotationY = side < 0.0f
                ? (float) (-Math.PI / 2.0)
                : (float) (Math.PI / 2.0);
        drawQuadPart(renderer, vehicleX, vehicleZ, angle, scale,
                glassLocalX, MIRROR_Y, MIRROR_FORWARD,
                MIRROR_GLASS_WIDTH, MIRROR_GLASS_HEIGHT,
                0.0f, rotationY,
                MIRROR_GLASS_MATERIAL);
    }

    private void drawCylinderPart(Renderer renderer, float vehicleX,
                                  float vehicleZ, float angle, float scale,
                                  float localX, float localY,
                                  float localForward, float diameter,
                                  float width, Material material) {
        float worldX = calculateWorldX(vehicleX, angle,
                localX * scale, localForward * scale);
        float worldZ = calculateWorldZ(vehicleZ, angle,
                localX * scale, localForward * scale);
        renderer.cylinder(worldX, localY * scale, worldZ,
                diameter * scale, width * scale, diameter * scale,
                0.0f, angle, WHEEL_AXIS_ROTATION, material);
    }

    private void drawSpherePart(Renderer renderer, float vehicleX,
                                float vehicleZ, float angle, float scale,
                                float localX, float localY,
                                float localForward, float sizeX, float sizeY,
                                float sizeZ, Material material) {
        float worldX = calculateWorldX(vehicleX, angle,
                localX * scale, localForward * scale);
        float worldZ = calculateWorldZ(vehicleZ, angle,
                localX * scale, localForward * scale);
        renderer.sphere(worldX, localY * scale, worldZ,
                sizeX * scale, sizeY * scale, sizeZ * scale,
                0.0f, angle, 0.0f, material);
    }

    private void drawQuadPart(Renderer renderer, float vehicleX,
                              float vehicleZ, float angle, float scale,
                              float localX, float localY, float localForward,
                              float width, float height, float rotationX,
                              float localRotationY, Material material) {
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
