package com.openworld.vehicle;

public final class VehicleGeometry {

    public static final float PLAYER_VISUAL_SCALE = 0.90f;
    public static final float TRAFFIC_VISUAL_SCALE = 0.88f;

    public static final float PLAYER_COLLISION_RADIUS = 1.92f;
    public static final float TRAFFIC_COLLISION_RADIUS = 1.88f;

    public static final float FRONT_FACE_Z = -1.95f;
    public static final float REAR_FACE_Z = 1.48f;
    public static final float SURFACE_EPSILON = 0.010f;

    public static final float GLASS_TRIM_OFFSET = 0.014f;
    public static final float GLASS_OFFSET = 0.024f;

    public static final float WHEEL_LOCAL_X = 0.99f;
    public static final float FRONT_WHEEL_FORWARD = 1.18f;
    public static final float REAR_WHEEL_FORWARD = -0.92f;
    public static final float WHEEL_CENTER_Y = 0.33f;
    public static final float WHEEL_DIAMETER = 0.70f;
    public static final float WHEEL_WIDTH = 0.34f;

    public static final float HUB_LOCAL_X = 1.19f;
    public static final float HUB_DIAMETER = 0.31f;
    public static final float HUB_WIDTH = 0.07f;

    public static final float FRONT_LIGHT_LOCAL_X = 0.53f;
    public static final float FRONT_LIGHT_Y = 0.53f;
    public static final float FRONT_LIGHT_SURFACE_Z =
            FRONT_FACE_Z - SURFACE_EPSILON;
    public static final float HEADLIGHT_FORWARD_OFFSET =
            -FRONT_LIGHT_SURFACE_Z * PLAYER_VISUAL_SCALE;
    public static final float HEADLIGHT_SIDE_OFFSET =
            FRONT_LIGHT_LOCAL_X * PLAYER_VISUAL_SCALE;
    public static final float HEADLIGHT_HEIGHT =
            FRONT_LIGHT_Y * PLAYER_VISUAL_SCALE;

    public static final float MIRROR_LOCAL_X = 0.91f;
    public static final float MIRROR_Y = 1.06f;
    public static final float MIRROR_FORWARD = 0.18f;
    public static final float MIRROR_HOUSING_WIDTH = 0.20f;
    public static final float MIRROR_HOUSING_HEIGHT = 0.10f;
    public static final float MIRROR_HOUSING_DEPTH = 0.14f;
    public static final float MIRROR_GLASS_X_OFFSET = 0.105f;
    public static final float MIRROR_GLASS_WIDTH = 0.12f;
    public static final float MIRROR_GLASS_HEIGHT = 0.07f;

    private VehicleGeometry() {
    }
}
