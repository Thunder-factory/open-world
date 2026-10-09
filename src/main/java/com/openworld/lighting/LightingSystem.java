package com.openworld.lighting;

import com.openworld.render.Renderer;
import com.openworld.render.ShaderProgram;
import com.openworld.world.City;
import com.openworld.vehicle.Car;

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

  private static final float LAMP_LIGHT_HEIGHT = 5.25f;

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

  /*
   * Las farolas se distribuyen en diferentes
   * sectores de la ciudad.
   *
   * Cada elemento contiene:
   *
   * {fila, columna}
   */
  private static final int[][] LAMP_CELLS = {

      { 1, 1 },
      { 1, 5 },
      { 1, 9 },

      { 3, 3 },
      { 3, 7 },

      { 5, 1 },
      { 5, 5 },
      { 5, 9 },

      { 7, 3 },
      { 7, 7 },

      { 9, 1 },
      { 9, 9 }
  };

  /*
   * Cada par indica hacia qué esquina de la celda
   * se desplaza la farola.
   *
   * Los valores posibles son -1 y +1.
   */
  private static final int[][] LAMP_SIDES = {

      { -1, -1 },
      { 1, -1 },
      { -1, 1 },

      { 1, 1 },
      { -1, -1 },

      { 1, 1 },
      { -1, 1 },
      { 1, -1 },

      { -1, 1 },
      { 1, -1 },

      { -1, -1 },
      { 1, 1 }
  };

  private final Vector3f[] lampPositions = new Vector3f[LAMP_CELLS.length];

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
  private static final float HEADLIGHT_SIDE_OFFSET = 0.65f;

  /*
   * Posición longitudinal de los faros.
   *
   * Un valor positivo representa la parte frontal
   * del vehículo.
   */
  private static final float HEADLIGHT_FORWARD_OFFSET = 2.25f;

  private static final float HEADLIGHT_HEIGHT = 0.72f;

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

    shader.setUniform(
        "uLampCount",
        lampPositions.length);

    for (int i = 0; i < lampPositions.length; i++) {

      shader.setUniform(
          "uLampPositions[" + i + "]",
          lampPositions[i]);
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

    /*
     * Base.
     */

    renderer.box(
        x,
        0.32f,
        z,

        0.55f,
        0.24f,
        0.55f,

        0.18f,
        0.19f,
        0.20f);

    /*
     * Poste.
     */

    renderer.box(
        x,
        2.55f,
        z,

        0.18f,
        5.0f,
        0.18f,

        0.22f,
        0.23f,
        0.24f);

    /*
     * Soporte superior.
     */

    renderer.box(
        x,
        5.03f,
        z,

        0.60f,
        0.12f,
        0.60f,

        0.20f,
        0.21f,
        0.22f);

    /*
     * La bombilla utiliza emisión visual independiente
     * de la iluminación puntual aplicada al entorno.
     */

    renderer.emissiveBox(
        x,
        LAMP_LIGHT_HEIGHT,
        z,

        0.42f,
        0.30f,
        0.42f,

        lampColor.x,
        lampColor.y,
        lampColor.z,

        night
            ? 1.8f
            : 0.15f);
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

    return lampPositions.length;
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