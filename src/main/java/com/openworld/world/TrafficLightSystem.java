package com.openworld.world;

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

  private static final float BULB_SIZE = 0.68f;

  private static final float BULB_HEIGHT = 0.32f;

  private static final float LENS_WIDTH = 0.34f;

  private static final float LENS_HEIGHT = 0.22f;

  private static final float LENS_THICKNESS = 0.06f;

  private static final float LENS_SURFACE_OFFSET = 0.02f;

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

    drawBulb(
        renderer,
        x,
        redY,
        z,
        SignalColor.RED,
        activeColor == SignalColor.RED,
        facing);

    drawBulb(
        renderer,
        x,
        yellowY,
        z,
        SignalColor.YELLOW,
        activeColor == SignalColor.YELLOW,
        facing);

    drawBulb(
        renderer,
        x,
        greenY,
        z,
        SignalColor.GREEN,
        activeColor == SignalColor.GREEN,
        facing);
  }

  private void drawBulb(
      Renderer renderer,
      float x,
      float y,
      float z,
      SignalColor color,
      boolean active,
      Facing facing) {

    float darkRed;
    float darkGreen;
    float darkBlue;

    float lightRed;
    float lightGreen;
    float lightBlue;

    switch (color) {

      case RED -> {

        darkRed = 0.22f;
        darkGreen = 0.03f;
        darkBlue = 0.03f;

        lightRed = 1.00f;
        lightGreen = 0.05f;
        lightBlue = 0.02f;
      }

      case YELLOW -> {

        darkRed = 0.22f;
        darkGreen = 0.18f;
        darkBlue = 0.03f;

        lightRed = 1.00f;
        lightGreen = 0.72f;
        lightBlue = 0.04f;
      }

      case GREEN -> {

        darkRed = 0.03f;
        darkGreen = 0.20f;
        darkBlue = 0.05f;

        lightRed = 0.04f;
        lightGreen = 0.90f;
        lightBlue = 0.12f;
      }

      default ->
        throw new IllegalStateException(
            "Color de semáforo inválido");
    }

    /*
     * La lámpara base permanece oscura y embutida en la carcasa.
     * La parte emisiva se dibuja únicamente sobre una cara frontal.
     */
    switch (facing) {

      case POSITIVE_Z -> {

        float lensZ = z + HEAD_DEPTH / 2.0f + LENS_SURFACE_OFFSET;

        renderer.box(
            x,
            y,
            z + HEAD_DEPTH / 2.0f - 0.01f,

            LENS_WIDTH,
            LENS_HEIGHT,
            LENS_THICKNESS,

            darkRed,
            darkGreen,
            darkBlue);

        if (active) {
          renderer.emissiveBox(
              x,
              y,
              lensZ,

              LENS_WIDTH * 0.92f,
              LENS_HEIGHT * 0.92f,
              LENS_THICKNESS,

              lightRed,
              lightGreen,
              lightBlue,

              1.60f);
        }
      }

      case NEGATIVE_Z -> {

        float lensZ = z - HEAD_DEPTH / 2.0f - LENS_SURFACE_OFFSET;

        renderer.box(
            x,
            y,
            z - HEAD_DEPTH / 2.0f + 0.01f,

            LENS_WIDTH,
            LENS_HEIGHT,
            LENS_THICKNESS,

            darkRed,
            darkGreen,
            darkBlue);

        if (active) {
          renderer.emissiveBox(
              x,
              y,
              lensZ,

              LENS_WIDTH * 0.92f,
              LENS_HEIGHT * 0.92f,
              LENS_THICKNESS,

              lightRed,
              lightGreen,
              lightBlue,

              1.60f);
        }
      }

      case POSITIVE_X -> {

        float lensX = x + HEAD_WIDTH / 2.0f + LENS_SURFACE_OFFSET;

        renderer.box(
            x + HEAD_WIDTH / 2.0f - 0.01f,
            y,
            z,

            LENS_THICKNESS,
            LENS_HEIGHT,
            LENS_WIDTH,

            darkRed,
            darkGreen,
            darkBlue);

        if (active) {
          renderer.emissiveBox(
              lensX,
              y,
              z,

              LENS_THICKNESS,
              LENS_HEIGHT * 0.92f,
              LENS_WIDTH * 0.92f,

              lightRed,
              lightGreen,
              lightBlue,

              1.60f);
        }
      }

      case NEGATIVE_X -> {

        float lensX = x - HEAD_WIDTH / 2.0f - LENS_SURFACE_OFFSET;

        renderer.box(
            x - HEAD_WIDTH / 2.0f + 0.01f,
            y,
            z,

            LENS_THICKNESS,
            LENS_HEIGHT,
            LENS_WIDTH,

            darkRed,
            darkGreen,
            darkBlue);

        if (active) {
          renderer.emissiveBox(
              lensX,
              y,
              z,

              LENS_THICKNESS,
              LENS_HEIGHT * 0.92f,
              LENS_WIDTH * 0.92f,

              lightRed,
              lightGreen,
              lightBlue,

              1.60f);
        }
      }
    }
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