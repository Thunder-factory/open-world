package com.openworld.world;

import com.openworld.render.Material;
import com.openworld.render.Renderer;

public final class TrafficLightSystem {

  private enum Phase { NS_GREEN, NS_YELLOW, EW_GREEN, EW_YELLOW }

  private enum SignalColor { RED, YELLOW, GREEN }

  private enum Facing { POSITIVE_X, NEGATIVE_X, POSITIVE_Z, NEGATIVE_Z }

  private static final float GREEN_DURATION = 5.0f;
  private static final float YELLOW_DURATION = 1.5f;
  private static final float SIGNAL_OFFSET = City.CELL_SIZE * 0.58f;
  private static final int[][] INTERSECTIONS = {
      { 3, 5 }, { 5, 3 }, { 5, 7 }, { 7, 5 }
  };

  private static final float BASE_WIDTH = 0.55f;
  private static final float BASE_HEIGHT = 0.24f;
  private static final float POLE_WIDTH = 0.18f;
  private static final float POLE_HEIGHT = 3.40f;
  private static final float HEAD_WIDTH = 0.62f;
  private static final float HEAD_HEIGHT = 1.70f;
  private static final float HEAD_DEPTH = HEAD_WIDTH;
  private static final float SIGNAL_FRAME_WIDTH = 0.46f;
  private static final float SIGNAL_FRAME_HEIGHT = 0.34f;
  private static final float SIGNAL_FRAME_DEPTH = 0.20f;
  private static final float SIGNAL_FRAME_SURFACE_GAP = 0.002f;
  private static final float LENS_DIAMETER = 0.24f;
  private static final float LENS_SURFACE_GAP = 0.004f;
  private static final float SIGNAL_VERTICAL_OFFSET = 0.52f;

  private static final Material BASE_MATERIAL =
      new Material(0.10f, 0.10f, 0.11f, 0.0f, 0.18f, 20.0f);
  private static final Material POLE_MATERIAL =
      new Material(0.08f, 0.08f, 0.09f, 0.0f, 0.24f, 28.0f);
  private static final Material HEAD_MATERIAL =
      new Material(0.025f, 0.028f, 0.032f, 0.0f, 0.18f, 20.0f);
  private static final Material SIGNAL_FRAME_MATERIAL =
      new Material(0.010f, 0.012f, 0.015f, 0.0f, 0.24f, 28.0f);
  private static final Material RED_OFF_MATERIAL =
      new Material(0.18f, 0.02f, 0.02f, 0.0f, 0.18f, 20.0f);
  private static final Material RED_ON_MATERIAL =
      new Material(1.00f, 0.05f, 0.02f, 1.60f, 0.45f, 48.0f);
  private static final Material YELLOW_OFF_MATERIAL =
      new Material(0.18f, 0.14f, 0.02f, 0.0f, 0.18f, 20.0f);
  private static final Material YELLOW_ON_MATERIAL =
      new Material(1.00f, 0.72f, 0.05f, 1.45f, 0.45f, 48.0f);
  private static final Material GREEN_OFF_MATERIAL =
      new Material(0.02f, 0.16f, 0.04f, 0.0f, 0.18f, 20.0f);
  private static final Material GREEN_ON_MATERIAL =
      new Material(0.08f, 0.92f, 0.16f, 1.45f, 0.45f, 48.0f);

  private final float[] intersectionX = new float[INTERSECTIONS.length];
  private final float[] intersectionZ = new float[INTERSECTIONS.length];
  private final float sidewalkHeight;
  private Phase phase;
  private float elapsedInPhase;

  public TrafficLightSystem(City city) {
    if (city == null) {
      throw new IllegalArgumentException("La ciudad no puede ser null");
    }
    sidewalkHeight = city.getSidewalkHeight();
    initializeIntersections(city);
    reset();
    System.out.println("Intersecciones con semaforos: "
        + INTERSECTIONS.length);
  }

  private void initializeIntersections(City city) {
    for (int i = 0; i < INTERSECTIONS.length; i++) {
      int row = INTERSECTIONS[i][0];
      int column = INTERSECTIONS[i][1];
      if (!city.isRoadCell(row, column)) {
        throw new IllegalStateException(
            "Interseccion de semaforo fuera de una calle en ["
                + row + ", " + column + "]");
      }
      intersectionX[i] = city.getCellCenterX(column);
      intersectionZ[i] = city.getCellCenterZ(row);
    }
  }

  public void update(float deltaTime) {
    if (deltaTime <= 0.0f) {
      return;
    }
    elapsedInPhase += deltaTime;
    while (elapsedInPhase >= getCurrentPhaseDuration()) {
      elapsedInPhase -= getCurrentPhaseDuration();
      advancePhase();
    }
  }

  private float getCurrentPhaseDuration() {
    return switch (phase) {
      case NS_GREEN, EW_GREEN -> GREEN_DURATION;
      case NS_YELLOW, EW_YELLOW -> YELLOW_DURATION;
    };
  }

  private void advancePhase() {
    phase = switch (phase) {
      case NS_GREEN -> Phase.NS_YELLOW;
      case NS_YELLOW -> Phase.EW_GREEN;
      case EW_GREEN -> Phase.EW_YELLOW;
      case EW_YELLOW -> Phase.NS_GREEN;
    };
  }

  private SignalColor getNorthSouthColor() {
    return switch (phase) {
      case NS_GREEN -> SignalColor.GREEN;
      case NS_YELLOW -> SignalColor.YELLOW;
      case EW_GREEN, EW_YELLOW -> SignalColor.RED;
    };
  }

  private SignalColor getEastWestColor() {
    return switch (phase) {
      case EW_GREEN -> SignalColor.GREEN;
      case EW_YELLOW -> SignalColor.YELLOW;
      case NS_GREEN, NS_YELLOW -> SignalColor.RED;
    };
  }

  public void render(Renderer renderer) {
    if (renderer == null) {
      throw new IllegalArgumentException("El renderer no puede ser null");
    }
    SignalColor northSouthColor = getNorthSouthColor();
    SignalColor eastWestColor = getEastWestColor();
    for (int i = 0; i < INTERSECTIONS.length; i++) {
      float centerX = intersectionX[i];
      float centerZ = intersectionZ[i];
      drawTrafficLight(renderer, centerX + SIGNAL_OFFSET,
          centerZ + SIGNAL_OFFSET, northSouthColor, Facing.POSITIVE_Z);
      drawTrafficLight(renderer, centerX - SIGNAL_OFFSET,
          centerZ - SIGNAL_OFFSET, northSouthColor, Facing.NEGATIVE_Z);
      drawTrafficLight(renderer, centerX + SIGNAL_OFFSET,
          centerZ - SIGNAL_OFFSET, eastWestColor, Facing.POSITIVE_X);
      drawTrafficLight(renderer, centerX - SIGNAL_OFFSET,
          centerZ + SIGNAL_OFFSET, eastWestColor, Facing.NEGATIVE_X);
    }
  }

  private void drawTrafficLight(Renderer renderer, float x, float z,
                                SignalColor activeColor, Facing facing) {
    renderer.box(x, sidewalkHeight + BASE_HEIGHT / 2.0f, z,
        BASE_WIDTH, BASE_HEIGHT, BASE_WIDTH, BASE_MATERIAL);
    float poleY = sidewalkHeight + BASE_HEIGHT + POLE_HEIGHT / 2.0f;
    renderer.box(x, poleY, z, POLE_WIDTH, POLE_HEIGHT, POLE_WIDTH,
        POLE_MATERIAL);
    float headY = sidewalkHeight + BASE_HEIGHT + POLE_HEIGHT
        + HEAD_HEIGHT / 2.0f - 0.10f;
    renderer.box(x, headY, z, HEAD_WIDTH, HEAD_HEIGHT, HEAD_DEPTH,
        HEAD_MATERIAL);
    drawSignalModule(renderer, x, headY + SIGNAL_VERTICAL_OFFSET, z,
        SignalColor.RED, activeColor == SignalColor.RED, facing);
    drawSignalModule(renderer, x, headY, z,
        SignalColor.YELLOW, activeColor == SignalColor.YELLOW, facing);
    drawSignalModule(renderer, x, headY - SIGNAL_VERTICAL_OFFSET, z,
        SignalColor.GREEN, activeColor == SignalColor.GREEN, facing);
  }

  private void drawSignalModule(Renderer renderer, float x, float y, float z,
                                SignalColor color, boolean active,
                                Facing facing) {
    float normalX = getFacingNormalX(facing);
    float normalZ = getFacingNormalZ(facing);
    float rotationY = getFacingAngle(facing);
    float faceOffset = getFaceHalfExtent(facing);
    float frameOffset = faceOffset + SIGNAL_FRAME_SURFACE_GAP
        + SIGNAL_FRAME_DEPTH / 2.0f;
    renderer.windowFrame(x + normalX * frameOffset, y,
        z + normalZ * frameOffset, SIGNAL_FRAME_WIDTH,
        SIGNAL_FRAME_HEIGHT, SIGNAL_FRAME_DEPTH, rotationY,
        SIGNAL_FRAME_MATERIAL);
    float lensOffset = faceOffset + LENS_SURFACE_GAP;
    renderer.circle(x + normalX * lensOffset, y,
        z + normalZ * lensOffset, LENS_DIAMETER, rotationY,
        getLensMaterial(color, active));
  }

  private Material getLensMaterial(SignalColor color, boolean active) {
    return switch (color) {
      case RED -> active ? RED_ON_MATERIAL : RED_OFF_MATERIAL;
      case YELLOW -> active ? YELLOW_ON_MATERIAL : YELLOW_OFF_MATERIAL;
      case GREEN -> active ? GREEN_ON_MATERIAL : GREEN_OFF_MATERIAL;
    };
  }

  private float getFaceHalfExtent(Facing facing) {
    return switch (facing) {
      case POSITIVE_Z, NEGATIVE_Z -> HEAD_DEPTH / 2.0f;
      case POSITIVE_X, NEGATIVE_X -> HEAD_WIDTH / 2.0f;
    };
  }

  private float getFacingNormalX(Facing facing) {
    return switch (facing) {
      case POSITIVE_X -> 1.0f;
      case NEGATIVE_X -> -1.0f;
      default -> 0.0f;
    };
  }

  private float getFacingNormalZ(Facing facing) {
    return switch (facing) {
      case POSITIVE_Z -> 1.0f;
      case NEGATIVE_Z -> -1.0f;
      default -> 0.0f;
    };
  }

  private float getFacingAngle(Facing facing) {
    return switch (facing) {
      case POSITIVE_Z -> 0.0f;
      case NEGATIVE_Z -> (float) Math.PI;
      case POSITIVE_X -> (float) (Math.PI / 2.0);
      case NEGATIVE_X -> (float) (-Math.PI / 2.0);
    };
  }

  public void reset() {
    phase = Phase.NS_GREEN;
    elapsedInPhase = 0.0f;
  }

  public String getPhaseName() {
    return phase.name();
  }

  public float getElapsedInPhase() {
    return elapsedInPhase;
  }

  public int getIntersectionCount() {
    return INTERSECTIONS.length;
  }

  public int getSignalCount() {
    return INTERSECTIONS.length * 4;
  }
}
