package com.openworld.world;

import com.openworld.render.Material;
import com.openworld.render.Renderer;

public final class TrafficLightSystem {

  /*
   * ============================================================
   * FASES DEL CICLO
   * ============================================================
   */

  private enum Phase {

    NS_GREEN,
    NS_YELLOW,

    EW_GREEN,
    EW_YELLOW
  }

  private enum SignalColor {

    RED,
    YELLOW,
    GREEN
  }

  private enum Facing {

    POSITIVE_X,
    NEGATIVE_X,

    POSITIVE_Z,
    NEGATIVE_Z
  }

  /*
   * ============================================================
   * DURACIONES
   * ============================================================
   */

  private static final float GREEN_DURATION = 5.0f;

  private static final float YELLOW_DURATION = 1.5f;

  /*
   * ============================================================
   * POSICIÓN
   * ============================================================
   *
   * El desplazamiento coloca los postes cerca de
   * las esquinas y sobre la zona de acera.
   */

  private static final float SIGNAL_OFFSET = City.CELL_SIZE * 0.58f;

  /*
   * Cada entrada contiene:
   *
   * {fila, columna}
   *
   * Todas corresponden a intersecciones de la red vial.
   */

  private static final int[][] INTERSECTIONS = {

      { 3, 5 },

      { 5, 3 },
      { 5, 7 },

      { 7, 5 }
  };

  /*
   * ============================================================
   * GEOMETRÍA
   * ============================================================
   */

  private static final float BASE_WIDTH = 0.55f;

  private static final float BASE_HEIGHT = 0.24f;

  private static final float POLE_WIDTH = 0.18f;

  private static final float POLE_HEIGHT = 3.40f;

  private static final float HEAD_WIDTH = 0.60f;

  private static final float HEAD_HEIGHT = 1.70f;

  private static final float HEAD_DEPTH = 0.60f;

  private static final float LENS_WIDTH = 0.26f;

  private static final float LENS_HEIGHT = 0.16f;

  private static final float LENS_SURFACE_OFFSET = 0.010f;

  private static final float SIGNAL_FRAME_WIDTH = 0.44f;

  private static final float SIGNAL_FRAME_HEIGHT = 0.32f;

  private static final float SIGNAL_FRAME_DEPTH = 0.22f;

  private static final float SIGNAL_FRAME_OFFSET = 0.010f;

  private static final float VISOR_DEPTH = 0.18f;

  private static final float VISOR_TOP_THICKNESS = 0.04f;

  private static final float VISOR_SIDE_THICKNESS = 0.04f;

  private static final float VISOR_EXTRA_WIDTH = 0.04f;

  private static final float SIGNAL_FRAME_RED = 0.015f;

  private static final float SIGNAL_FRAME_GREEN = 0.018f;

  private static final float SIGNAL_FRAME_BLUE = 0.022f;

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

  /*
   * ============================================================
   * ESTADO
   * ============================================================
   */

  private final float[] intersectionX = new float[INTERSECTIONS.length];

  private final float[] intersectionZ = new float[INTERSECTIONS.length];

  private final float sidewalkHeight;

  private Phase phase;

  private float elapsedInPhase;

  public TrafficLightSystem(
      City city) {

    sidewalkHeight = city.getSidewalkHeight();

    initializeIntersections(
        city);

    reset();

    System.out.println(
        "Intersecciones con semáforos: "
            + INTERSECTIONS.length);
  }

  /*
   * ============================================================
   * INICIALIZACIÓN
   * ============================================================
   */

  private void initializeIntersections(
      City city) {

    for (int i = 0; i < INTERSECTIONS.length; i++) {

      int row = INTERSECTIONS[i][0];

      int column = INTERSECTIONS[i][1];

      if (!city.isRoadCell(
          row,
          column)) {

        throw new IllegalStateException(
            "Intersección de semáforo fuera de una calle en ["
                + row
                + ", "
                + column
                + "]");
      }

      intersectionX[i] = city.getCellCenterX(
          column);

      intersectionZ[i] = city.getCellCenterZ(
          row);
    }
  }

  /*
   * ============================================================
   * UPDATE
   * ============================================================
   */

  public void update(
      float deltaTime) {

    elapsedInPhase += deltaTime;

    /*
     * El bucle permite conservar correctamente el tiempo
     * restante incluso si un frame supera el cambio de fase.
     */

    while (elapsedInPhase >= getCurrentPhaseDuration()) {

      elapsedInPhase -= getCurrentPhaseDuration();

      advancePhase();
    }
  }

  private float getCurrentPhaseDuration() {

    return switch (phase) {

      case NS_GREEN,
          EW_GREEN ->
        GREEN_DURATION;

      case NS_YELLOW,
          EW_YELLOW ->
        YELLOW_DURATION;
    };
  }

  private void advancePhase() {

    phase = switch (phase) {

      case NS_GREEN ->
        Phase.NS_YELLOW;

      case NS_YELLOW ->
        Phase.EW_GREEN;

      case EW_GREEN ->
        Phase.EW_YELLOW;

      case EW_YELLOW ->
        Phase.NS_GREEN;
    };
  }

  /*
   * ============================================================
   * ESTADO DE CADA DIRECCIÓN
   * ============================================================
   */

  private SignalColor getNorthSouthColor() {

    return switch (phase) {

      case NS_GREEN ->
        SignalColor.GREEN;

      case NS_YELLOW ->
        SignalColor.YELLOW;

      case EW_GREEN,
          EW_YELLOW ->
        SignalColor.RED;
    };
  }

  private SignalColor getEastWestColor() {

    return switch (phase) {

      case EW_GREEN ->
        SignalColor.GREEN;

      case EW_YELLOW ->
        SignalColor.YELLOW;

      case NS_GREEN,
          NS_YELLOW ->
        SignalColor.RED;
    };
  }

  /*
   * ============================================================
   * RENDER
   * ============================================================
   */

  public void render(
      Renderer renderer) {

    SignalColor northSouthColor = getNorthSouthColor();

    SignalColor eastWestColor = getEastWestColor();

    for (int i = 0; i < INTERSECTIONS.length; i++) {

      float centerX = intersectionX[i];

      float centerZ = intersectionZ[i];

      /*
       * El semáforo norte-sur se ubica en una esquina
       * y su cara visible apunta hacia la calle.
       */
      drawTrafficLight(
          renderer,

          centerX + SIGNAL_OFFSET,
          centerZ + SIGNAL_OFFSET,

          northSouthColor,
          Facing.NEGATIVE_Z);

      /*
       * El semáforo este-oeste se ubica en la esquina opuesta
       * y su cara visible apunta hacia la calle transversal.
       */
      drawTrafficLight(
          renderer,

          centerX - SIGNAL_OFFSET,
          centerZ - SIGNAL_OFFSET,

          eastWestColor,
          Facing.POSITIVE_X);
    }
  }

  /*
   * ============================================================
   * GEOMETRÍA DEL SEMÁFORO
   * ============================================================
   */

  private void drawTrafficLight(
      Renderer renderer,
      float x,
      float z,
      SignalColor activeColor,
      Facing facing) {

    /*
     * Base.
     */
    renderer.box(
        x,

        sidewalkHeight
            + BASE_HEIGHT / 2.0f,

        z,

        BASE_WIDTH,
        BASE_HEIGHT,
        BASE_WIDTH,

        0.12f,
        0.12f,
        0.12f);

    /*
     * Poste.
     */
    float poleY = sidewalkHeight
        + BASE_HEIGHT
        + POLE_HEIGHT / 2.0f;

    renderer.box(
        x,
        poleY,
        z,

        POLE_WIDTH,
        POLE_HEIGHT,
        POLE_WIDTH,

        0.10f,
        0.10f,
        0.10f);

    /*
     * Carcasa.
     */
    float headY = sidewalkHeight
        + BASE_HEIGHT
        + POLE_HEIGHT
        + HEAD_HEIGHT / 2.0f
        - 0.10f;

    renderer.box(
        x,
        headY,
        z,

        HEAD_WIDTH,
        HEAD_HEIGHT,
        HEAD_DEPTH,

        0.04f,
        0.04f,
        0.04f);

    float redY = headY + 0.52f;

    float yellowY = headY;

    float greenY = headY - 0.52f;

    drawSignalModule(
        renderer,
        x,
        redY,
        z,
        SignalColor.RED,
        activeColor == SignalColor.RED,
        facing);

    drawSignalModule(
        renderer,
        x,
        yellowY,
        z,
        SignalColor.YELLOW,
        activeColor == SignalColor.YELLOW,
        facing);

    drawSignalModule(
        renderer,
        x,
        greenY,
        z,
        SignalColor.GREEN,
        activeColor == SignalColor.GREEN,
        facing);
  }

  private void drawSignalModule(
      Renderer renderer,
      float x,
      float y,
      float z,
      SignalColor color,
      boolean active,
      Facing facing) {

    drawSignalFrame(renderer, x, y, z, facing);
    drawSignalVisor(renderer, x, y, z, facing);
    drawBulb(renderer, x, y, z, color, active, facing);
  }

  private void drawSignalFrame(
      Renderer renderer,
      float x,
      float y,
      float z,
      Facing facing) {

    float normalX = getFacingNormalX(facing);
    float normalZ = getFacingNormalZ(facing);
    float rotationY = getFacingAngle(facing);
    float faceOffset = getFaceHalfExtent(facing);

    float frameX = x + normalX * (faceOffset
        + SIGNAL_FRAME_OFFSET
        + SIGNAL_FRAME_DEPTH / 2.0f);

    float frameZ = z + normalZ * (faceOffset
        + SIGNAL_FRAME_OFFSET
        + SIGNAL_FRAME_DEPTH / 2.0f);

    renderer.windowFrame(
        frameX,
        y,
        frameZ,
        SIGNAL_FRAME_WIDTH,
        SIGNAL_FRAME_HEIGHT,
        SIGNAL_FRAME_DEPTH,
        rotationY,
        SIGNAL_FRAME_RED,
        SIGNAL_FRAME_GREEN,
        SIGNAL_FRAME_BLUE);
  }

  private void drawSignalVisor(
      Renderer renderer,
      float x,
      float y,
      float z,
      Facing facing) {

    float faceOffset = getFaceHalfExtent(facing);

    switch (facing) {

      case POSITIVE_Z -> {
        float visorCenterZ = z + faceOffset
            + SIGNAL_FRAME_OFFSET
            + VISOR_DEPTH / 2.0f;

        drawHorizontalVisor(renderer, x, y, visorCenterZ);
      }

      case NEGATIVE_Z -> {
        float visorCenterZ = z - faceOffset
            - SIGNAL_FRAME_OFFSET
            - VISOR_DEPTH / 2.0f;

        drawHorizontalVisor(renderer, x, y, visorCenterZ);
      }

      case POSITIVE_X -> {
        float visorCenterX = x + faceOffset
            + SIGNAL_FRAME_OFFSET
            + VISOR_DEPTH / 2.0f;

        drawVerticalVisor(renderer, visorCenterX, y, z);
      }

      case NEGATIVE_X -> {
        float visorCenterX = x - faceOffset
            - SIGNAL_FRAME_OFFSET
            - VISOR_DEPTH / 2.0f;

        drawVerticalVisor(renderer, visorCenterX, y, z);
      }
    }
  }

  private void drawHorizontalVisor(
      Renderer renderer,
      float x,
      float y,
      float visorCenterZ) {

    renderer.box(
        x,
        y + SIGNAL_FRAME_HEIGHT / 2.0f
            + VISOR_TOP_THICKNESS / 2.0f,
        visorCenterZ,
        SIGNAL_FRAME_WIDTH + VISOR_EXTRA_WIDTH,
        VISOR_TOP_THICKNESS,
        VISOR_DEPTH,
        SIGNAL_FRAME_RED,
        SIGNAL_FRAME_GREEN,
        SIGNAL_FRAME_BLUE);

    renderer.box(
        x - SIGNAL_FRAME_WIDTH / 2.0f
            - VISOR_SIDE_THICKNESS / 2.0f,
        y,
        visorCenterZ,
        VISOR_SIDE_THICKNESS,
        SIGNAL_FRAME_HEIGHT,
        VISOR_DEPTH,
        SIGNAL_FRAME_RED,
        SIGNAL_FRAME_GREEN,
        SIGNAL_FRAME_BLUE);

    renderer.box(
        x + SIGNAL_FRAME_WIDTH / 2.0f
            + VISOR_SIDE_THICKNESS / 2.0f,
        y,
        visorCenterZ,
        VISOR_SIDE_THICKNESS,
        SIGNAL_FRAME_HEIGHT,
        VISOR_DEPTH,
        SIGNAL_FRAME_RED,
        SIGNAL_FRAME_GREEN,
        SIGNAL_FRAME_BLUE);
  }

  private void drawVerticalVisor(
      Renderer renderer,
      float visorCenterX,
      float y,
      float z) {

    renderer.box(
        visorCenterX,
        y + SIGNAL_FRAME_HEIGHT / 2.0f
            + VISOR_TOP_THICKNESS / 2.0f,
        z,
        VISOR_DEPTH,
        VISOR_TOP_THICKNESS,
        SIGNAL_FRAME_WIDTH + VISOR_EXTRA_WIDTH,
        SIGNAL_FRAME_RED,
        SIGNAL_FRAME_GREEN,
        SIGNAL_FRAME_BLUE);

    renderer.box(
        visorCenterX,
        y,
        z - SIGNAL_FRAME_WIDTH / 2.0f
            - VISOR_SIDE_THICKNESS / 2.0f,
        VISOR_DEPTH,
        SIGNAL_FRAME_HEIGHT,
        VISOR_SIDE_THICKNESS,
        SIGNAL_FRAME_RED,
        SIGNAL_FRAME_GREEN,
        SIGNAL_FRAME_BLUE);

    renderer.box(
        visorCenterX,
        y,
        z + SIGNAL_FRAME_WIDTH / 2.0f
            + VISOR_SIDE_THICKNESS / 2.0f,
        VISOR_DEPTH,
        SIGNAL_FRAME_HEIGHT,
        VISOR_SIDE_THICKNESS,
        SIGNAL_FRAME_RED,
        SIGNAL_FRAME_GREEN,
        SIGNAL_FRAME_BLUE);
  }

  private void drawBulb(
      Renderer renderer,
      float x,
      float y,
      float z,
      SignalColor color,
      boolean active,
      Facing facing) {

    Material material = getLensMaterial(color, active);
    float normalX = getFacingNormalX(facing);
    float normalZ = getFacingNormalZ(facing);
    float rotationY = getFacingAngle(facing);
    float faceOffset = getFaceHalfExtent(facing);

    float lensX = x + normalX
        * (faceOffset + LENS_SURFACE_OFFSET);
    float lensZ = z + normalZ
        * (faceOffset + LENS_SURFACE_OFFSET);

    renderer.quad(
        lensX,
        y,
        lensZ,
        LENS_WIDTH,
        LENS_HEIGHT,
        0.0f,
        rotationY,
        0.0f,
        material);
  }

  private Material getLensMaterial(
      SignalColor color,
      boolean active) {

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
      case POSITIVE_X -> (float) Math.PI / 2.0f;
      case NEGATIVE_X -> (float) -Math.PI / 2.0f;
    };
  }

  /*
   * ============================================================
   * RESET
   * ============================================================
   */

  public void reset() {

    phase = Phase.NS_GREEN;

    elapsedInPhase = 0.0f;
  }

  /*
   * ============================================================
   * CONSULTAS
   * ============================================================
   */

  public String getPhaseName() {

    return phase.name();
  }

  public float getElapsedInPhase() {

    return elapsedInPhase;
  }
}
