package com.openworld.traffic;

import com.openworld.render.Renderer;
import com.openworld.world.City;

public final class TrafficVehicle {

  /*
   * ============================================================
   * MOVIMIENTO
   * ============================================================
   */

  /*
   * Los vehículos autónomos circulan a una velocidad
   * menor que el automóvil controlado.
   */
  private static final float MAX_FORWARD_SPEED = 10.0f;

  /*
   * Aceleración utilizada para aproximarse
   * progresivamente a la velocidad solicitada.
   */
  private static final float ACCELERATION = 5.0f;

  /*
   * Desaceleración utilizada cuando se solicita
   * una velocidad menor que la actual.
   */
  private static final float DECELERATION = 8.0f;

  /*
   * Velocidad angular máxima.
   *
   * Se expresa internamente en radianes por segundo.
   */
  private static final float TURN_SPEED = (float) Math.toRadians(
      95.0);

  /*
   * La colisión utiliza una aproximación circular,
   * igual que el vehículo controlado.
   *
   * El radio es ligeramente menor porque los vehículos
   * de tráfico utilizan una geometría más compacta.
   */
  private static final float COLLISION_RADIUS = 1.20f;

  /*
   * ============================================================
   * ESTADO INICIAL
   * ============================================================
   */

  private final float spawnX;

  private final float spawnZ;

  private final float spawnAngle;

  /*
   * Velocidad normal de circulación.
   *
   * AutonomousTrafficSystem utilizará este valor
   * como referencia durante el seguimiento de rutas.
   */
  private final float cruiseSpeed;

  /*
   * ============================================================
   * COLOR
   * ============================================================
   */

  private final float bodyRed;

  private final float bodyGreen;

  private final float bodyBlue;

  /*
   * ============================================================
   * ESTADO ACTUAL
   * ============================================================
   */

  private float x;

  private float z;

  /*
   * Ángulo en radianes.
   *
   * Convención:
   *
   * angle = 0
   * ↓
   * frente hacia -Z
   *
   * Esta convención coincide con Car.
   */
  private float angle;

  /*
   * Velocidad actual en unidades del mundo por segundo.
   *
   * Los vehículos autónomos de esta implementación
   * circulan únicamente hacia delante.
   */
  private float speed;

  /*
   * ============================================================
   * CONSTRUCTOR
   * ============================================================
   */

  public TrafficVehicle(
      float spawnX,
      float spawnZ,
      float spawnAngle,
      float cruiseSpeed,
      float bodyRed,
      float bodyGreen,
      float bodyBlue) {

    this.spawnX = spawnX;

    this.spawnZ = spawnZ;

    this.spawnAngle = normalizeAngle(
        spawnAngle);

    /*
     * La velocidad de crucero queda limitada
     * al rango permitido por el vehículo.
     */
    this.cruiseSpeed = clamp(
        cruiseSpeed,
        0.0f,
        MAX_FORWARD_SPEED);

    this.bodyRed = clamp(
        bodyRed,
        0.0f,
        1.0f);

    this.bodyGreen = clamp(
        bodyGreen,
        0.0f,
        1.0f);

    this.bodyBlue = clamp(
        bodyBlue,
        0.0f,
        1.0f);

    reset();
  }

  /*
   * ============================================================
   * ACTUALIZACIÓN
   * ============================================================
   */

  /**
   * Actualiza el movimiento del vehículo.
   *
   * desiredAngle representa la dirección hacia la que
   * debe orientarse.
   *
   * desiredSpeed representa la velocidad que se desea
   * alcanzar durante el frame actual.
   *
   * La selección de esos valores corresponderá al sistema
   * de rutas que se incorporará posteriormente.
   */
  public void update(
      float deltaTime,
      float desiredAngle,
      float desiredSpeed,
      City city) {

    if (deltaTime <= 0.0f) {
      return;
    }

    desiredAngle = normalizeAngle(
        desiredAngle);

    desiredSpeed = clamp(
        desiredSpeed,
        0.0f,
        MAX_FORWARD_SPEED);

    /*
     * ========================================================
     * ORIENTACIÓN
     * ========================================================
     *
     * Se obtiene la diferencia angular más corta.
     *
     * Esto evita que el vehículo realice casi una vuelta
     * completa cuando el objetivo está muy cerca del límite
     * entre -PI y +PI.
     */

    float angleDifference = normalizeAngle(
        desiredAngle
            - angle);

    float maximumTurn = TURN_SPEED
        * deltaTime;

    angleDifference = clamp(
        angleDifference,
        -maximumTurn,
        maximumTurn);

    angle = normalizeAngle(
        angle
            + angleDifference);

    /*
     * ========================================================
     * VELOCIDAD
     * ========================================================
     */

    if (desiredSpeed > speed) {

      speed = moveToward(
          speed,
          desiredSpeed,
          ACCELERATION
              * deltaTime);

    } else {

      speed = moveToward(
          speed,
          desiredSpeed,
          DECELERATION
              * deltaTime);
    }

    /*
     * Protección adicional del rango.
     */
    speed = clamp(
        speed,
        0.0f,
        MAX_FORWARD_SPEED);

    /*
     * ========================================================
     * VECTOR FRONTAL
     * ========================================================
     *
     * Se utiliza exactamente la misma convención
     * empleada por Car:
     *
     * angle = 0
     * ↓
     * forward = (0, -1)
     */

    float forwardX = -(float) Math.sin(
        angle);

    float forwardZ = -(float) Math.cos(
        angle);

    /*
     * ========================================================
     * DESPLAZAMIENTO
     * ========================================================
     */

    float movementX = forwardX
        * speed
        * deltaTime;

    float movementZ = forwardZ
        * speed
        * deltaTime;

    boolean blockedX = false;

    boolean blockedZ = false;

    /*
     * ========================================================
     * COLISIÓN SOBRE X
     * ========================================================
     */

    if (Math.abs(
        movementX) > 0.00001f) {

      float candidateX = x
          + movementX;

      if (city.canDrive(
          candidateX,
          z,
          COLLISION_RADIUS)) {

        x = candidateX;

      } else {

        blockedX = true;
      }
    }

    /*
     * ========================================================
     * COLISIÓN SOBRE Z
     * ========================================================
     */

    if (Math.abs(
        movementZ) > 0.00001f) {

      float candidateZ = z
          + movementZ;

      if (city.canDrive(
          x,
          candidateZ,
          COLLISION_RADIUS)) {

        z = candidateZ;

      } else {

        blockedZ = true;
      }
    }

    /*
     * Si ambos ejes quedan bloqueados se reduce
     * progresivamente la velocidad.
     *
     * La ruta correcta debería evitar esta situación,
     * pero esta comprobación funciona como protección.
     */
    if (blockedX
        && blockedZ) {

      speed = moveToward(
          speed,
          0.0f,
          DECELERATION
              * deltaTime);
    }
  }

  /*
   * ============================================================
   * RENDER
   * ============================================================
   */

  public void render(
      Renderer renderer) {

    /*
     * ========================================================
     * RUEDAS
     * ========================================================
     */

    drawPart(
        renderer,

        -0.88f,
        0.27f,
        1.00f,

        0.32f,
        0.42f,
        0.62f,

        0.035f,
        0.035f,
        0.035f);

    drawPart(
        renderer,

        0.88f,
        0.27f,
        1.00f,

        0.32f,
        0.42f,
        0.62f,

        0.035f,
        0.035f,
        0.035f);

    drawPart(
        renderer,

        -0.88f,
        0.27f,
        -1.00f,

        0.32f,
        0.42f,
        0.62f,

        0.035f,
        0.035f,
        0.035f);

    drawPart(
        renderer,

        0.88f,
        0.27f,
        -1.00f,

        0.32f,
        0.42f,
        0.62f,

        0.035f,
        0.035f,
        0.035f);

    /*
     * ========================================================
     * CHASIS
     * ========================================================
     */

    drawPart(
        renderer,

        0.0f,
        0.56f,
        0.0f,

        1.85f,
        0.60f,
        3.50f,

        bodyRed,
        bodyGreen,
        bodyBlue);

    /*
     * ========================================================
     * CABINA
     * ========================================================
     */

    float cabinRed = clamp(
        bodyRed * 1.12f,
        0.0f,
        1.0f);

    float cabinGreen = clamp(
        bodyGreen * 1.12f,
        0.0f,
        1.0f);

    float cabinBlue = clamp(
        bodyBlue * 1.12f,
        0.0f,
        1.0f);

    drawPart(
        renderer,

        0.0f,
        1.08f,
        -0.18f,

        1.48f,
        0.58f,
        1.65f,

        cabinRed,
        cabinGreen,
        cabinBlue);

    /*
     * ========================================================
     * PARABRISAS DELANTERO
     * ========================================================
     */

    drawPart(
        renderer,

        0.0f,
        1.13f,
        0.68f,

        1.25f,
        0.35f,
        0.07f,

        0.08f,
        0.16f,
        0.23f);

    /*
     * ========================================================
     * LUCES DELANTERAS VISUALES
     * ========================================================
     *
     * Estas piezas no actúan como fuentes de iluminación.
     */

    drawPart(
        renderer,

        -0.52f,
        0.60f,
        1.77f,

        0.28f,
        0.18f,
        0.07f,

        0.95f,
        0.90f,
        0.50f);

    drawPart(
        renderer,

        0.52f,
        0.60f,
        1.77f,

        0.28f,
        0.18f,
        0.07f,

        0.95f,
        0.90f,
        0.50f);

    /*
     * ========================================================
     * DETALLE FRONTAL
     * ========================================================
     *
     * La fascia y la parrilla diferencian el frente
     * sin modificar los elementos laterales.
     */

    drawPart(
        renderer,

        0.0f,
        0.47f,
        1.82f,

        1.68f,
        0.20f,
        0.12f,

        0.10f,
        0.11f,
        0.13f);

    drawPart(
        renderer,

        0.0f,
        0.61f,
        1.82f,

        0.68f,
        0.16f,
        0.06f,

        0.025f,
        0.035f,
        0.045f);

    drawPart(
        renderer,

        0.0f,
        0.48f,
        1.89f,

        1.35f,
        0.06f,
        0.04f,

        0.24f,
        0.25f,
        0.28f);

    /*
     * ========================================================
     * LUCES TRASERAS
     * ========================================================
     */

    drawPart(
        renderer,

        -0.52f,
        0.57f,
        -1.77f,

        0.25f,
        0.17f,
        0.07f,

        0.72f,
        0.02f,
        0.02f);

    drawPart(
        renderer,

        0.52f,
        0.57f,
        -1.77f,

        0.25f,
        0.17f,
        0.07f,

        0.72f,
        0.02f,
        0.02f);

    /*
     * ========================================================
     * DETALLE TRASERO
     * ========================================================
     *
     * El parachoques y la placa permiten distinguir
     * la parte posterior desde la cámara de seguimiento.
     */

    drawPart(
        renderer,

        0.0f,
        0.46f,
        -1.82f,

        1.68f,
        0.20f,
        0.12f,

        0.10f,
        0.035f,
        0.035f);

    drawPart(
        renderer,

        0.0f,
        0.61f,
        -1.82f,

        0.56f,
        0.19f,
        0.06f,

        0.88f,
        0.86f,
        0.68f);

    drawPart(
        renderer,

        0.0f,
        0.48f,
        -1.89f,

        1.35f,
        0.06f,
        0.04f,

        0.28f,
        0.025f,
        0.025f);
  }

  /*
   * ============================================================
   * TRANSFORMACIÓN LOCAL → MUNDO
   * ============================================================
   */

  private void drawPart(
      Renderer renderer,

      float localX,
      float worldY,
      float localForward,

      float sizeX,
      float sizeY,
      float sizeZ,

      float red,
      float green,
      float blue) {

    float sin = (float) Math.sin(
        angle);

    float cos = (float) Math.cos(
        angle);

    /*
     * Vector derecho:
     *
     * ( cos(angle), -sin(angle) )
     *
     * Vector frontal:
     *
     * ( -sin(angle), -cos(angle) )
     */

    float worldX = x
        + cos
            * localX
        - sin
            * localForward;

    float worldZ = z
        - sin
            * localX
        - cos
            * localForward;

    renderer.rotatedBox(
        worldX,
        worldY,
        worldZ,

        sizeX,
        sizeY,
        sizeZ,

        red,
        green,
        blue,

        angle);
  }

  /*
   * ============================================================
   * RESET
   * ============================================================
   */

  public void reset() {

    x = spawnX;

    z = spawnZ;

    angle = spawnAngle;

    speed = 0.0f;
  }

  /*
   * ============================================================
   * HELPERS
   * ============================================================
   */

  /**
   * Aproxima un valor hacia un objetivo sin sobrepasarlo.
   */
  private float moveToward(
      float value,
      float target,
      float maximumDelta) {

    if (value < target) {

      return Math.min(
          value
              + maximumDelta,
          target);
    }

    if (value > target) {

      return Math.max(
          value
              - maximumDelta,
          target);
    }

    return target;
  }

  /**
   * Mantiene el ángulo dentro del rango:
   *
   * [-PI, +PI]
   */
  private float normalizeAngle(
      float value) {

    float fullTurn = (float) (Math.PI
        * 2.0);

    while (value > Math.PI) {

      value -= fullTurn;
    }

    while (value < -Math.PI) {

      value += fullTurn;
    }

    return value;
  }

  private float clamp(
      float value,
      float minimum,
      float maximum) {

    return Math.max(
        minimum,
        Math.min(
            maximum,
            value));
  }

  /*
   * ============================================================
   * CONSULTAS
   * ============================================================
   */

  public float getX() {

    return x;
  }

  public float getZ() {

    return z;
  }

  public float getAngle() {

    return angle;
  }

  public float getSpeed() {

    return speed;
  }

  public float getCruiseSpeed() {

    return cruiseSpeed;
  }

  public float getCollisionRadius() {

    return COLLISION_RADIUS;
  }
}
