package com.openworld.visual;

import com.openworld.render.Material;
import com.openworld.render.Renderer;

public final class DeliveryVisual {

    private static final float GROUND_ROTATION_X =
            (float) Math.toRadians(-90.0);
    private static final float AREA_Y = 0.070f;
    private static final float BOUNDARY_RING_Y = 0.078f;
    private static final float PULSE_RING_Y = 0.082f;
    private static final float CENTER_MARK_Y = 0.086f;
    private static final float AREA_DIAMETER_FACTOR = 1.62f;
    private static final float CENTER_MARK_DIAMETER = 0.28f;
    private static final float RING_PULSE_SPEED = 3.20f;
    private static final float RING_MIN_SCALE = 0.78f;
    private static final float RING_MAX_SCALE = 0.96f;
    private static final float MARKER_BASE_Y = 2.65f;
    private static final float MARKER_BOB_SPEED = 2.60f;
    private static final float MARKER_BOB_HEIGHT = 0.16f;
    private static final float DIAMOND_WIDTH = 0.74f;
    private static final float DIAMOND_HEIGHT = 1.00f;

    private static final Material AREA_MATERIAL =
            new Material(0.12f, 0.015f, 0.080f, 0.08f, 0.10f, 12.0f);
    private static final Material BOUNDARY_MATERIAL =
            new Material(1.00f, 0.08f, 0.68f, 0.95f, 0.28f, 28.0f);
    private static final Material PULSE_MATERIAL =
            new Material(1.00f, 0.22f, 0.78f, 0.70f, 0.24f, 24.0f);
    private static final Material CENTER_MATERIAL =
            new Material(1.00f, 0.50f, 0.88f, 0.80f, 0.30f, 30.0f);
    private static final Material DIAMOND_MATERIAL =
            new Material(1.00f, 0.08f, 0.68f, 1.10f, 0.38f, 40.0f);
    private static final Material DIAMOND_CORE_MATERIAL =
            new Material(1.00f, 0.62f, 0.90f, 0.75f, 0.35f, 36.0f);

    public void render(Renderer renderer, float x, float z,
                       float arrivalRadius, float elapsedTime) {
        if (renderer == null) {
            throw new IllegalArgumentException("El renderer no puede ser null");
        }
        if (arrivalRadius <= 0.0f) {
            return;
        }

        float arrivalDiameter = arrivalRadius * 2.0f;
        renderer.circle(x, AREA_Y, z,
                arrivalDiameter * AREA_DIAMETER_FACTOR / 2.0f,
                GROUND_ROTATION_X, 0.0f, 0.0f, AREA_MATERIAL);
        renderer.ring(x, BOUNDARY_RING_Y, z, arrivalDiameter,
                GROUND_ROTATION_X, 0.0f, 0.0f, BOUNDARY_MATERIAL);
        renderer.circle(x, CENTER_MARK_Y, z, CENTER_MARK_DIAMETER,
                GROUND_ROTATION_X, 0.0f, 0.0f, CENTER_MATERIAL);

        float wave = 0.5f + 0.5f
                * (float) Math.sin(elapsedTime * RING_PULSE_SPEED);
        float pulseScale = RING_MIN_SCALE
                + (RING_MAX_SCALE - RING_MIN_SCALE) * wave;
        renderer.ring(x, PULSE_RING_Y, z, arrivalDiameter * pulseScale,
                GROUND_ROTATION_X, 0.0f, 0.0f, PULSE_MATERIAL);

        float verticalOffset = (float) Math.sin(elapsedTime
                * MARKER_BOB_SPEED) * MARKER_BOB_HEIGHT;
        float markerY = MARKER_BASE_Y + verticalOffset;
        float halfHeight = DIAMOND_HEIGHT / 2.0f;
        renderer.cone(x, markerY + DIAMOND_HEIGHT / 4.0f, z,
                DIAMOND_WIDTH, halfHeight, DIAMOND_WIDTH,
                0.0f, 0.0f, 0.0f, DIAMOND_MATERIAL);
        renderer.cone(x, markerY - DIAMOND_HEIGHT / 4.0f, z,
                DIAMOND_WIDTH, halfHeight, DIAMOND_WIDTH,
                (float) Math.PI, 0.0f, 0.0f, DIAMOND_MATERIAL);
        renderer.sphere(x, markerY, z, 0.22f, 0.22f, 0.22f,
                0.0f, 0.0f, 0.0f, DIAMOND_CORE_MATERIAL);
    }
}
