package com.openworld.traffic;

import com.openworld.render.Renderer;
import com.openworld.render.Material;
import com.openworld.world.City;
import com.openworld.vehicle.VehicleGeometry;
import com.openworld.visual.VehicleVisual;

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

  private static final float COLLISION_RADIUS =
      VehicleGeometry.TRAFFIC_COLLISION_RADIUS;

  private static final float VISUAL_SCALE =
      VehicleGeometry.TRAFFIC_VISUAL_SCALE;

  private static final VehicleVisual VEHICLE_VISUAL = new VehicleVisual();

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

  private final Material bodyMaterial;

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

    float safeRed = clamp(
        bodyRed,
        0.0f,
        1.0f);

    float safeGreen = clamp(
        bodyGreen,
        0.0f,
        1.0f);

    float safeBlue = clamp(
        bodyBlue,
        0.0f,
        1.0f);

    bodyMaterial = new Material(
        safeRed,
        safeGreen,
        safeBlue,
        0.0f,
        0.58f,
        44.0f);

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
   * Los valores son proporcionados por
   * AutonomousTrafficSystem según la ruta activa.
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
    VEHICLE_VISUAL.render(
        renderer,
        x,
        z,
        angle,
        VISUAL_SCALE,
        bodyMaterial);
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
