package com.openworld.visual;

import com.openworld.render.Material;
import com.openworld.render.Renderer;

public final class BuildingWindowVisual {

  private static final float FRAME_DEPTH = 0.08f;
  private static final float FRAME_SURFACE_GAP = 0.002f;
  private static final float GLASS_SURFACE_GAP = 0.004f;

  private static final float GLASS_WIDTH_FACTOR = 0.80f;
  private static final float GLASS_HEIGHT_FACTOR = 0.80f;

    private static final Material FRAME_MATERIAL =
            new Material(0.12f, 0.13f, 0.15f, 0.0f, 0.18f, 24.0f);
    private static final Material DAY_GLASS_MATERIAL =
            new Material(0.08f, 0.15f, 0.22f, 0.0f, 0.82f, 96.0f);
    private static final Material NIGHT_GLASS_MATERIAL =
            new Material(1.00f, 0.78f, 0.32f, 1.25f, 0.72f, 72.0f);

  public void render(
      Renderer renderer,
      float surfaceX,
      float y,
      float surfaceZ,
      float rotationY,
      float width,
      float height,
      boolean night) {

    float normalX = (float) Math.sin(rotationY);
    float normalZ = (float) Math.cos(rotationY);
    float frameOffset = FRAME_SURFACE_GAP
        + FRAME_DEPTH / 2.0f;

    renderer.windowFrame(
        surfaceX + normalX * frameOffset,
        y,
        surfaceZ + normalZ * frameOffset,
        width,
        height,
        FRAME_DEPTH,
        rotationY,
                FRAME_MATERIAL);

        Material glassMaterial = night
                ? NIGHT_GLASS_MATERIAL
                : DAY_GLASS_MATERIAL;

    renderer.quad(
        surfaceX + normalX * GLASS_SURFACE_GAP,
        y,
        surfaceZ + normalZ * GLASS_SURFACE_GAP,
        width * GLASS_WIDTH_FACTOR,
        height * GLASS_HEIGHT_FACTOR,
        0.0f,
        rotationY,
        0.0f,
                glassMaterial);
  }
}
