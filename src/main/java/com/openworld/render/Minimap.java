package com.openworld.render;

import com.openworld.gameplay.DeliveryManager;
import com.openworld.lighting.LightingSystem;
import com.openworld.vehicle.Car;
import com.openworld.world.City;

import org.joml.Matrix4f;

public final class Minimap {

  /*
   * ============================================================
   * CONFIGURACIÓN
   * ============================================================
   */

  private static final int MARGIN = 18;

  private static final int MIN_SIZE = 140;

  private static final int MAX_SIZE = 300;

  /*
   * Se agrega un pequeño margen alrededor de la ciudad
   * para evitar que los extremos queden pegados al borde.
   */
  private static final float WORLD_MARGIN = 4.0f;

  private static final float CAMERA_HEIGHT = 180.0f;

  /*
   * ============================================================
   * MATRICES
   * ============================================================
   */

  private final Matrix4f viewMatrix = new Matrix4f();

  private final Matrix4f projectionMatrix = new Matrix4f();

  /*
   * ============================================================
   * ESTADO
   * ============================================================
   */

  private boolean visible = true;

  /*
   * ============================================================
   * CONTROL
   * ============================================================
   */

  public void toggle() {

    visible = !visible;
  }

  public void reset() {

    visible = true;
  }

  public boolean isVisible() {

    return visible;
  }

  public String getStateName() {

    return visible
        ? "ON"
        : "OFF";
  }

  /*
   * ============================================================
   * RENDER
   * ============================================================
   */

  public void render(
      Renderer renderer,
      City city,
      Car car,
      DeliveryManager deliveryManager,
      LightingSystem lighting,
      int framebufferWidth,
      int framebufferHeight) {

    if (!visible) {
      return;
    }

    if (framebufferWidth <= 0
        || framebufferHeight <= 0) {

      return;
    }

    int size = calculateSize(
        framebufferWidth,
        framebufferHeight);

    if (size <= 0) {
      return;
    }

    /*
     * OpenGL utiliza la esquina inferior izquierda
     * como origen del viewport.
     *
     * El cálculo coloca el minimapa visualmente
     * en la esquina superior derecha.
     */

    int viewportX = framebufferWidth
        - size
        - MARGIN;

    int viewportY = framebufferHeight
        - size
        - MARGIN;

    float extent = city.getHalfSize()
        + WORLD_MARGIN;

    /*
     * La cámara observa verticalmente hacia abajo.
     *
     * El vector UP se alinea con -Z para mantener
     * el norte en la parte superior del minimapa.
     */

    viewMatrix
        .identity()
        .lookAt(
            0.0f,
            CAMERA_HEIGHT,
            0.0f,

            0.0f,
            0.0f,
            0.0f,

            0.0f,
            0.0f,
            -1.0f);

    projectionMatrix
        .identity()
        .ortho(
            -extent,
            extent,

            -extent,
            extent,

            0.1f,
            CAMERA_HEIGHT + 50.0f);

    renderer.beginMinimap(
        viewportX,
        viewportY,
        size,
        size,

        viewMatrix,
        projectionMatrix,

        lighting);

    try {

      city.renderMinimap(
          renderer);

      drawDestination(
          renderer,
          deliveryManager);

      drawCar(
          renderer,
          car);

    } finally {

      /*
       * El viewport principal se restaura incluso si
       * ocurre una excepción durante el segundo render.
       */

      renderer.endMinimap(
          framebufferWidth,
          framebufferHeight);
    }
  }

  /*
   * ============================================================
   * TAMAÑO DEL VIEWPORT
   * ============================================================
   */

  private int calculateSize(
      int framebufferWidth,
      int framebufferHeight) {

    int minimumDimension = Math.min(
        framebufferWidth,
        framebufferHeight);

    int desiredSize = Math.round(
        (minimumDimension / 3.0f)
            * 0.85f);

    desiredSize = Math.max(
        MIN_SIZE,
        desiredSize);

    desiredSize = Math.min(
        MAX_SIZE,
        desiredSize);

    int maximumAvailable = minimumDimension
        - MARGIN * 2;

    if (maximumAvailable <= 0) {
      return 0;
    }

    return Math.min(
        desiredSize,
        maximumAvailable);
  }

  /*
   * ============================================================
   * VEHÍCULO
   * ============================================================
   */

  private void drawCar(
      Renderer renderer,
      Car car) {

    /*
     * El vehículo se representa mediante un triángulo.
     *
     * La punta indica directamente la orientación
     * actual del automóvil.
     */
    renderer.minimapTriangle(
        car.getX(),
        0.30f,
        car.getZ(),
        // Size del tringulo del car
        2.8f,

        car.getAngle(),

        1.00f,
        0.20f,
        0.08f);
  }

  /*
   * ============================================================
   * DESTINO ACTIVO
   * ============================================================
   */

  private void drawDestination(
      Renderer renderer,
      DeliveryManager deliveryManager) {

    if (!deliveryManager.hasActiveDestination()) {
      return;
    }

    renderer.emissiveBox(
        deliveryManager.getCurrentDestinationX(),
        0.28f,
        deliveryManager.getCurrentDestinationZ(),

        2.20f,
        0.30f,
        2.20f,

        0.15f,
        0.75f,
        1.00f,

        1.80f);
  }
}