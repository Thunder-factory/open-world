package com.openworld.render;

import com.openworld.vehicle.Car;
import com.openworld.world.City;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class Camera {

  public enum Mode {
    FOLLOW,
    AERIAL
  }

  /*
   * ============================================================
   * PROYECCIÓN
   * ============================================================
   */

  private static final float DEFAULT_FOV = 60.0f;

  private static final float DEFAULT_NEAR_PLANE = 0.1f;

  private static final float DEFAULT_FAR_PLANE = 300.0f;

  /*
   * ============================================================
   * CÁMARA FOLLOW
   * ============================================================
   */

  private static final float FOLLOW_DISTANCE = 9.0f;

  private static final float FOLLOW_HEIGHT = 5.0f;

  private static final float FOLLOW_LOOK_AHEAD = 4.0f;

  private static final float FOLLOW_TARGET_HEIGHT = 0.8f;

  private static final float FRONT_DISTANCE = 8.0f;

  private static final float FRONT_HEIGHT = 3.2f;

  private static final float FRONT_TARGET_HEIGHT = 0.9f;

  /*
   * ============================================================
   * ESTADO
   * ============================================================
   */

  private final Vector3f position = new Vector3f();

  private final Vector3f target = new Vector3f();

  private final Vector3f up = new Vector3f(
      0.0f,
      1.0f,
      0.0f);

  private final Matrix4f viewMatrix = new Matrix4f();

  private final Matrix4f projectionMatrix = new Matrix4f();

  private float fieldOfView;
  private float nearPlane;
  private float farPlane;

  private Mode mode = Mode.FOLLOW;

  private boolean frontView;

  public Camera() {

    this(
        DEFAULT_FOV,
        DEFAULT_NEAR_PLANE,
        DEFAULT_FAR_PLANE);
  }

  public Camera(
      float fieldOfView,
      float nearPlane,
      float farPlane) {

    this.fieldOfView = fieldOfView;

    this.nearPlane = nearPlane;

    this.farPlane = farPlane;
  }

  /*
   * ============================================================
   * ACTUALIZACIÓN
   * ============================================================
   */

  public void update(
      Car car,
      City city) {

    if (frontView) {

      updateFront(car);

      return;
    }

    switch (mode) {

      case FOLLOW ->
        updateFollow(car);

      case AERIAL ->
        updateAerial(city);
    }
  }

  private void updateFront(
      Car car) {

    float angle = car.getAngle();

    float forwardX = -(float) Math.sin(angle);

    float forwardZ = -(float) Math.cos(angle);

    float cameraX = car.getX()
        + forwardX
            * FRONT_DISTANCE;

    float cameraZ = car.getZ()
        + forwardZ
            * FRONT_DISTANCE;

    lookAt(
        cameraX,
        FRONT_HEIGHT,
        cameraZ,

        car.getX(),
        FRONT_TARGET_HEIGHT,
        car.getZ());
  }

  /*
   * La cámara se coloca detrás del vehículo utilizando
   * el mismo vector frontal empleado por la conducción.
   */
  private void updateFollow(
      Car car) {

    float angle = car.getAngle();

    float forwardX = -(float) Math.sin(angle);

    float forwardZ = -(float) Math.cos(angle);

    /*
     * La posición se desplaza en dirección contraria
     * al vector frontal.
     */

    float cameraX = car.getX()
        - forwardX
            * FOLLOW_DISTANCE;

    float cameraZ = car.getZ()
        - forwardZ
            * FOLLOW_DISTANCE;

    /*
     * El objetivo se coloca ligeramente por delante
     * del vehículo para mejorar la visibilidad.
     */

    float targetX = car.getX()
        + forwardX
            * FOLLOW_LOOK_AHEAD;

    float targetZ = car.getZ()
        + forwardZ
            * FOLLOW_LOOK_AHEAD;

    lookAt(
        cameraX,
        FOLLOW_HEIGHT,
        cameraZ,

        targetX,
        FOLLOW_TARGET_HEIGHT,
        targetZ);
  }

  /*
   * La cámara aérea deriva sus dimensiones del tamaño
   * real de la ciudad.
   */
  private void updateAerial(
      City city) {

    float halfSize = city.getHalfSize();

    float horizontalOffset = halfSize * 1.25f;

    float height = halfSize * 1.35f;

    lookAt(
        horizontalOffset,
        height,
        horizontalOffset,

        0.0f,
        0.0f,
        0.0f);
  }

  /*
   * ============================================================
   * MODOS
   * ============================================================
   */

  public void toggleMode() {

    mode = switch (mode) {

      case FOLLOW ->
        Mode.AERIAL;

      case AERIAL ->
        Mode.FOLLOW;
    };
  }

  public Mode getMode() {
    return mode;
  }

  public boolean isFrontView() {

    return frontView;
  }

  public void setFrontView(
      boolean frontView) {

    this.frontView = frontView;
  }

  public void setMode(
      Mode mode) {

    this.mode = mode;
  }

  /*
   * ============================================================
   * MATRICES
   * ============================================================
   */

  public Matrix4f getViewMatrix() {

    return viewMatrix
        .identity()
        .lookAt(
            position,
            target,
            up);
  }

  public Matrix4f getProjectionMatrix(
      float aspectRatio) {

    float safeAspectRatio = aspectRatio > 0.0f
        ? aspectRatio
        : 1.0f;

    return projectionMatrix
        .identity()
        .perspective(
            (float) Math.toRadians(
                fieldOfView),
            safeAspectRatio,
            nearPlane,
            farPlane);
  }

  /*
   * ============================================================
   * POSICIÓN Y OBJETIVO
   * ============================================================
   */

  public void setPosition(
      float x,
      float y,
      float z) {

    position.set(
        x,
        y,
        z);
  }

  public void setTarget(
      float x,
      float y,
      float z) {

    target.set(
        x,
        y,
        z);
  }

  public void lookAt(
      float positionX,
      float positionY,
      float positionZ,
      float targetX,
      float targetY,
      float targetZ) {

    setPosition(
        positionX,
        positionY,
        positionZ);

    setTarget(
        targetX,
        targetY,
        targetZ);
  }

  public void copyPosition(
      Vector3f destination) {

    if (destination == null) {
      throw new IllegalArgumentException(
          "El destino de la posicion no puede ser null");
    }

    destination.set(position);
  }

  /*
   * ============================================================
   * CONFIGURACIÓN
   * ============================================================
   */

  public float getFieldOfView() {
    return fieldOfView;
  }

  public void setFieldOfView(
      float fieldOfView) {

    this.fieldOfView = fieldOfView;
  }

  public float getNearPlane() {
    return nearPlane;
  }

  public void setNearPlane(
      float nearPlane) {

    this.nearPlane = nearPlane;
  }

  public float getFarPlane() {
    return farPlane;
  }

  public void setFarPlane(
      float farPlane) {

    this.farPlane = farPlane;
  }
}
