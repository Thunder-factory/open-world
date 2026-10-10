package com.openworld.visual;

import com.openworld.render.Material;
import com.openworld.render.Renderer;

public final class TreeVisual {

  private static final float ROOT_HEIGHT = 0.18f;
  private static final float ROOT_DIAMETER = 0.72f;

  private static final float TRUNK_HEIGHT = 2.05f;
  private static final float TRUNK_DIAMETER = 0.40f;
  private static final float TRUNK_CENTER_Y = 1.15f;

  private static final float LOWER_CROWN_Y = 2.55f;
  private static final float LOWER_CROWN_WIDTH = 2.50f;
  private static final float LOWER_CROWN_HEIGHT = 1.60f;

  private static final float MIDDLE_CROWN_Y = 3.30f;
  private static final float MIDDLE_CROWN_WIDTH = 2.05f;
  private static final float MIDDLE_CROWN_HEIGHT = 1.55f;

  private static final float TOP_CROWN_Y = 4.02f;
  private static final float TOP_CROWN_WIDTH = 1.35f;
  private static final float TOP_CROWN_HEIGHT = 1.10f;

    private static final Material ROOT_MATERIAL =
            new Material(0.22f, 0.12f, 0.05f, 0.0f, 0.05f, 8.0f);
    private static final Material TRUNK_MATERIAL =
            new Material(0.34f, 0.20f, 0.08f, 0.0f, 0.06f, 8.0f);
    private static final Material LOWER_LEAF_MATERIAL =
            new Material(0.07f, 0.30f, 0.09f, 0.0f, 0.08f, 12.0f);
    private static final Material MIDDLE_LEAF_MATERIAL =
            new Material(0.10f, 0.42f, 0.12f, 0.0f, 0.08f, 12.0f);
    private static final Material TOP_LEAF_MATERIAL =
            new Material(0.18f, 0.55f, 0.18f, 0.0f, 0.10f, 12.0f);

  public void render(
      Renderer renderer,
      float x,
      float z) {

    renderer.cylinder(
        x,
        ROOT_HEIGHT / 2.0f,
        z,
        ROOT_DIAMETER,
        ROOT_HEIGHT,
        ROOT_DIAMETER,
        0.0f, 0.0f, 0.0f,
                ROOT_MATERIAL);

    renderer.cylinder(
        x,
        TRUNK_CENTER_Y,
        z,
        TRUNK_DIAMETER,
        TRUNK_HEIGHT,
        TRUNK_DIAMETER,
        0.0f, 0.0f, 0.0f,
                TRUNK_MATERIAL);

    renderer.sphere(
        x,
        LOWER_CROWN_Y,
        z,
        LOWER_CROWN_WIDTH,
        LOWER_CROWN_HEIGHT,
        LOWER_CROWN_WIDTH,
        0.0f, 0.0f, 0.0f,
                LOWER_LEAF_MATERIAL);

    renderer.sphere(
        x,
        MIDDLE_CROWN_Y,
        z,
        MIDDLE_CROWN_WIDTH,
        MIDDLE_CROWN_HEIGHT,
        MIDDLE_CROWN_WIDTH,
        0.0f, 0.0f, 0.0f,
                MIDDLE_LEAF_MATERIAL);

    renderer.sphere(
        x,
        TOP_CROWN_Y,
        z,
        TOP_CROWN_WIDTH,
        TOP_CROWN_HEIGHT,
        TOP_CROWN_WIDTH,
        0.0f, 0.0f, 0.0f,
                TOP_LEAF_MATERIAL);
  }
}
