package com.openworld.lighting;

import com.openworld.render.Renderer;
import com.openworld.render.Material;
import com.openworld.render.ShaderProgram;
import com.openworld.world.City;
import com.openworld.vehicle.Car;
import com.openworld.vehicle.VehicleGeometry;

import org.joml.Vector3f;

public final class LightingSystem {

  /*
   * ============================================================
   * ILUMINACIÓN GENERAL
   * ============================================================
   */

  private final Vector3f ambientColor = new Vector3f();

  private final Vector3f directionalColor = new Vector3f();

  /*
   * Representa la dirección en la que viajan
   * los rayos de la luz direccional.
   */
  private final Vector3f directionalDirection = new Vector3f();

  private final Vector3f clearColor = new Vector3f();

  private float ambientStrength;
  private float directionalStrength;

  private boolean night;

  /*
   * ============================================================
   * FAROLAS
   * ============================================================
   */

  /**
   * Las farolas se ubican cerca del borde de las calles
   */
  private static final float LAMP_OFFSET = 5.8f;

  private static final float LAMP_BULB_VISUAL_HEIGHT =
      4.62f;

  private static final float LAMP_LIGHT_HEIGHT =
      LAMP_BULB_VISUAL_HEIGHT;

  private static final float PARK_LAMP_LIGHT_HEIGHT =
      3.13f;

  private static final float PARK_LAMP_OFFSET = 3.75f;

  private static final float NIGHT_LAMP_STRENGTH = 2.6f;

  /*
   * Parámetros de atenuación.
   *
   * La intensidad disminuye progresivamente
   * a medida que aumenta la distancia.
   */
  private static final float LAMP_CONSTANT = 1.0f;

  private static final float LAMP_LINEAR = 0.18f;

  private static final float LAMP_QUADRATIC = 0.04f;

  private final Vector3f lampColor = new Vector3f(
      1.00f,
      0.72f,
      0.32f);

  private static final Material LAMP_BASE_MATERIAL =
      new Material(
          0.08f,
          0.09f,
          0.10f,
          0.0f,
          0.35f,
          28.0f);

  private static final Material LAMP_METAL_MATERIAL =
      new Material(
          0.16f,
          0.18f,
          0.20f,
          0.0f,
          0.55f,
          48.0f);

  private static final Material LAMP_HOUSING_MATERIAL =
      new Material(
          0.05f,
          0.06f,
          0.07f,
          0.0f,
          0.30f,
          24.0f);

  private static final Material LAMP_DAY_GLOW_MATERIAL =
      new Material(
          1.00f,
          0.72f,
          0.32f,
          0.15f,
          0.45f,
          32.0f);

  private static final Material LAMP_NIGHT_GLOW_MATERIAL =
      new Material(
          1.00f,
          0.72f,
          0.32f,
          1.80f,
          0.65f,
          32.0f);

  /*
   * Las farolas se distribuyen en diferentes
   * sectores de la ciudad.
   *
   * Cada elemento contiene:
   *
   * {fila, columna}
   */
  private static final int[][] LAMP_CELLS = {

      { 1, 5 },

      { 5, 1 },
      { 5, 5 },
      { 5, 9 },
  };

  /*
   * Cada par indica hacia qué esquina de la celda
   * se desplaza la farola.
   *
   * Los valores posibles son -1 y +1.
   */
  private static final int[][] LAMP_SIDES = {

      { 1, -1 },

      { 1, 1 },
      { -1, 1 },
      { 1, -1 }
  };

  private final Vector3f[] lampPositions = new Vector3f[LAMP_CELLS.length];

  private static final int[][] PARK_CELLS = {

      { 2, 2 },
      { 2, 8 },
      { 8, 2 },
      { 8, 8 }
  };

  private final Vector3f[] parkLampPositions =
      new Vector3f[PARK_CELLS.length * 4];

  /*
   * ============================================================
   * FAROS DEL VEHÍCULO
   * ============================================================
   */

  private static final int HEADLIGHT_COUNT = 2;

  /*
   * Desplazamiento lateral de cada faro
   * respecto al centro del vehículo.
   */
  private static final float HEADLIGHT_SIDE_OFFSET =
      VehicleGeometry.HEADLIGHT_SIDE_OFFSET;

  /*
   * Posición longitudinal de los faros.
   *
   * Un valor positivo representa la parte frontal
   * del vehículo.
   */
  private static final float HEADLIGHT_FORWARD_OFFSET =
      VehicleGeometry.HEADLIGHT_FORWARD_OFFSET;

  private static final float HEADLIGHT_HEIGHT =
      VehicleGeometry.HEADLIGHT_HEIGHT;

  /*
   * El haz se inclina ligeramente hacia el suelo.
   */
  private static final float HEADLIGHT_DOWNWARD_TILT = 0.12f;

  private static final float HEADLIGHT_STRENGTH = 5.0f;

  private static final float HEADLIGHT_MAX_DISTANCE = 24.0f;

  /*
   * Atenuación por distancia.
   */
  private static final float HEADLIGHT_CONSTANT = 1.0f;

  private static final float HEADLIGHT_LINEAR = 0.09f;

  private static final float HEADLIGHT_QUADRATIC = 0.025f;

  /*
   * Apertura del cono.
   *
   * El shader trabaja con el coseno de los ángulos.
   */
  private static final float HEADLIGHT_INNER_CUTOFF = (float) Math.cos(
      Math.toRadians(12.0));

  private static final float HEADLIGHT_OUTER_CUTOFF = (float) Math.cos(
      Math.toRadians(22.0));

  private final Vector3f headlightColor = new Vector3f(
      1.00f,
      0.92f,
      0.68f);

  private final Vector3f[] headlightPositions = {

      new Vector3f(),
      new Vector3f()
  };

  private final Vector3f headlightDirection = new Vector3f(
      0.0f,
      -HEADLIGHT_DOWNWARD_TILT,
      -1.0f)
      .normalize();

  private boolean headlightsOn = false;

  public LightingSystem(
      City city) {

    initializeStreetLamps(
        city);

    initializeParkLamps(
        city);

    night = false;

    applyDayPreset();
  }

  /*
   * ============================================================
   * INICIALIZACIÓN DE FAROLAS
   * ============================================================
   */

  private void initializeStreetLamps(
      City city) {

    for (int i = 0; i < LAMP_CELLS.length; i++) {

      int row = LAMP_CELLS[i][0];

      int column = LAMP_CELLS[i][1];

      if (!city.isRoadCell(
          row,
          column)) {

        throw new IllegalStateException(
            "La farola "
                + i
                + " no se encuentra sobre una calle");
      }

      float x = city.getCellCenterX(
          column)
          + LAMP_SIDES[i][0]
              * LAMP_OFFSET;

      float z = city.getCellCenterZ(
          row)
          + LAMP_SIDES[i][1]
              * LAMP_OFFSET;

      lampPositions[i] = new Vector3f(
          x,
          LAMP_LIGHT_HEIGHT,
          z);
    }

    System.out.println(
        "Farolas: "
            + lampPositions.length);
  }

  private void initializeParkLamps(
      City city) {

    int lampIndex = 0;

    for (int[] parkCell : PARK_CELLS) {

      float parkX = city.getCellCenterX(
          parkCell[1]);

      float parkZ = city.getCellCenterZ(
          parkCell[0]);

      float[] offsets = {
          -PARK_LAMP_OFFSET,
          PARK_LAMP_OFFSET
      };

      for (float offsetX : offsets) {

        for (float offsetZ : offsets) {

          parkLampPositions[lampIndex++] = new Vector3f(
              parkX + offsetX,
              PARK_LAMP_LIGHT_HEIGHT,
              parkZ + offsetZ);
        }
      }
    }
  }

  /*
   * ============================================================
   * ACTUALIZACIÓN DE FAROS
   * ============================================================
   */

  public void update(
      Car car) {

    float angle = car.getAngle();

    float sin = (float) Math.sin(
        angle);

    float cos = (float) Math.cos(
        angle);

    /*
     * Vector frontal del vehículo.
     */
    float forwardX = -sin;

    float forwardZ = -cos;

    /*
     * Vector derecho del vehículo.
     */
    float rightX = cos;

    float rightZ = -sin;

    updateHeadlightPosition(
        headlightPositions[0],

        car,

        -HEADLIGHT_SIDE_OFFSET,

        forwardX,
        forwardZ,

        rightX,
        rightZ);

    updateHeadlightPosition(
        headlightPositions[1],

        car,

        HEADLIGHT_SIDE_OFFSET,

        forwardX,
        forwardZ,

        rightX,
        rightZ);

    /*
     * Ambos faros comparten la orientación frontal.
     *
     * La componente Y negativa inclina ligeramente
     * el haz hacia el suelo.
     */
    headlightDirection
        .set(
            forwardX,
            -HEADLIGHT_DOWNWARD_TILT,
            forwardZ)
        .normalize();
  }

  private void updateHeadlightPosition(
      Vector3f destination,
      Car car,
      float localX,
      float forwardX,
      float forwardZ,
      float rightX,
      float rightZ) {

    float worldX = car.getX()
        + rightX
            * localX
        + forwardX
            * HEADLIGHT_FORWARD_OFFSET;

    float worldZ = car.getZ()
        + rightZ
            * localX
        + forwardZ
            * HEADLIGHT_FORWARD_OFFSET;

    destination.set(
        worldX,
        HEADLIGHT_HEIGHT,
        worldZ);
  }

  /*
   * ============================================================
   * CONTROL DE FAROS
   * ============================================================
   */

  public void toggleHeadlights() {

    headlightsOn = !headlightsOn;
  }

  public boolean areHeadlightsOn() {

    return headlightsOn;
  }

  public String getHeadlightStateName() {

    return headlightsOn
        ? "ON"
        : "OFF";
  }

  /*
   * ============================================================
   * CAMBIO DÍA / NOCHE
   * ============================================================
   */

  public void toggleDayNight() {

    night = !night;

    if (night) {

      applyNightPreset();

    } else {

      applyDayPreset();
    }
  }

  /*
   * ============================================================
   * CONFIGURACIÓN DE DÍA
   * ============================================================
   */

  private void applyDayPreset() {

    ambientColor.set(
        1.00f,
        0.95f,
        0.85f);

    ambientStrength = 0.35f;

    directionalColor.set(
        1.00f,
        0.95f,
        0.82f);

    directionalStrength = 0.75f;

    directionalDirection
        .set(
            0.35f,
            -1.00f,
            0.25f)
        .normalize();

    clearColor.set(
        0.45f,
        0.70f,
        0.92f);
  }

  /*
   * ============================================================
   * CONFIGURACIÓN DE NOCHE
   * ============================================================
   */

  private void applyNightPreset() {

    ambientColor.set(
        0.18f,
        0.24f,
        0.40f);

    ambientStrength = 0.45f;

    directionalColor.set(
        0.48f,
        0.58f,
        0.85f);

    directionalStrength = 0.28f;

    directionalDirection
        .set(
            -0.25f,
            -1.00f,
            0.35f)
        .normalize();

    clearColor.set(
        0.025f,
        0.040f,
        0.090f);
  }

  /*
   * ============================================================
   * UNIFORMS
   * ============================================================
   */

  public void apply(
      ShaderProgram shader) {

    /*
     * Iluminación ambiental.
     */

    shader.setUniform(
        "uAmbientColor",
        ambientColor);

    shader.setUniform(
        "uAmbientStrength",
        ambientStrength);

    /*
     * Iluminación direccional.
     */

    shader.setUniform(
        "uDirectionalColor",
        directionalColor);

    shader.setUniform(
        "uDirectionalStrength",
        directionalStrength);

    shader.setUniform(
        "uDirectionalDirection",
        directionalDirection);

    /*
     * Iluminación puntual de las farolas.
     */

    int totalLampCount = lampPositions.length
        + parkLampPositions.length;

    shader.setUniform(
        "uLampCount",
        totalLampCount);

    for (int i = 0; i < lampPositions.length; i++) {

      shader.setUniform(
          "uLampPositions[" + i + "]",
          lampPositions[i]);
    }

    for (int i = 0; i < parkLampPositions.length; i++) {

      shader.setUniform(
          "uLampPositions["
              + (lampPositions.length + i)
              + "]",
          parkLampPositions[i]);
    }

    shader.setUniform(
        "uLampColor",
        lampColor);

    /*
     * La iluminación puntual se activa
     * únicamente durante la noche.
     */

    shader.setUniform(
        "uLampStrength",
        night
            ? NIGHT_LAMP_STRENGTH
            : 0.0f);

    shader.setUniform(
        "uLampConstant",
        LAMP_CONSTANT);

    shader.setUniform(
        "uLampLinear",
        LAMP_LINEAR);

    shader.setUniform(
        "uLampQuadratic",
        LAMP_QUADRATIC);

    /*
     * ============================================================
     * FAROS DEL VEHÍCULO
     * ============================================================
     */

    shader.setUniform(
        "uHeadlightsEnabled",
        headlightsOn
            ? 1
            : 0);

    for (int i = 0; i < HEADLIGHT_COUNT; i++) {

      shader.setUniform(
          "uHeadlightPositions[" + i + "]",
          headlightPositions[i]);
    }

    shader.setUniform(
        "uHeadlightDirection",
        headlightDirection);

    shader.setUniform(
        "uHeadlightColor",
        headlightColor);

    shader.setUniform(
        "uHeadlightStrength",
        HEADLIGHT_STRENGTH);

    shader.setUniform(
        "uHeadlightMaxDistance",
        HEADLIGHT_MAX_DISTANCE);

    shader.setUniform(
        "uHeadlightConstant",
        HEADLIGHT_CONSTANT);

    shader.setUniform(
        "uHeadlightLinear",
        HEADLIGHT_LINEAR);

    shader.setUniform(
        "uHeadlightQuadratic",
        HEADLIGHT_QUADRATIC);

    shader.setUniform(
        "uHeadlightInnerCutoff",
        HEADLIGHT_INNER_CUTOFF);

    shader.setUniform(
        "uHeadlightOuterCutoff",
        HEADLIGHT_OUTER_CUTOFF);
  }

  /*
   * ============================================================
   * RENDER DE FAROLAS
   * ============================================================
   */

  public void render(
      Renderer renderer) {

    for (Vector3f position : lampPositions) {

      drawStreetLamp(
          renderer,
          position);
    }
  }

  private void drawStreetLamp(
      Renderer renderer,
      Vector3f lightPosition) {

    float x = lightPosition.x;

    float z = lightPosition.z;

    renderer.cylinder(
        x,
        0.20f,
        z,

        0.72f,
        0.28f,
        0.72f,

        0.0f,
        0.0f,
        0.0f,

        LAMP_BASE_MATERIAL);

    renderer.cylinder(
        x,
        2.58f,
        z,

        0.20f,
        4.76f,
        0.20f,

        0.0f,
        0.0f,
        0.0f,

        LAMP_METAL_MATERIAL);

    renderer.cylinder(
        x,
        4.94f,
        z,

        0.32f,
        0.16f,
        0.32f,

        0.0f,
        0.0f,
        0.0f,

        LAMP_METAL_MATERIAL);

    renderer.cylinder(
        x + 0.28f,
        5.08f,
        z,

        0.12f,
        0.64f,
        0.12f,

        0.0f,
        0.0f,
        (float) Math.toRadians(90.0),

        LAMP_METAL_MATERIAL);

    renderer.box(
        x + 0.55f,
        5.02f,
        z,

        0.58f,
        0.22f,
        0.44f,

        LAMP_HOUSING_MATERIAL);

    renderer.cone(
        x + 0.55f,
        4.82f,
        z,

        0.58f,
        0.30f,
        0.44f,

        0.0f,
        0.0f,
        0.0f,

        LAMP_HOUSING_MATERIAL);

    renderer.sphere(
        x + 0.55f,
        LAMP_BULB_VISUAL_HEIGHT,
        z,

        0.28f,
        0.20f,
        0.28f,

        0.0f,
        0.0f,
        0.0f,

        night
            ? LAMP_NIGHT_GLOW_MATERIAL
            : LAMP_DAY_GLOW_MATERIAL);
  }

  /*
   * ============================================================
   * RESET
   * ============================================================
   */

  public void reset(
      Car car) {

    /*
     * Se restaura la iluminación general
     * a la configuración diurna inicial.
     */
    night = false;

    applyDayPreset();

    /*
     * Los faros delanteros comienzan apagados.
     */
    headlightsOn = false;

    /*
     * Las posiciones y la dirección de los faros
     * se sincronizan con la posición restaurada
     * del vehículo.
     */
    update(
        car);
  }

  /*
   * ============================================================
   * CONSULTAS
   * ============================================================
   */

  public boolean isNight() {
    return night;
  }

  public String getModeName() {

    return night
        ? "NIGHT"
        : "DAY";
  }

  public int getLampCount() {

    return lampPositions.length
        + parkLampPositions.length;
  }

  public float getClearRed() {
    return clearColor.x;
  }

  public float getClearGreen() {
    return clearColor.y;
  }

  public float getClearBlue() {
    return clearColor.z;
  }
}
