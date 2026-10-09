package com.openworld.render;

import com.openworld.gameplay.DeliveryManager;
import com.openworld.lighting.LightingSystem;
import com.openworld.ui.BitmapFont;
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

  private static final float FRAME_THICKNESS = 3.0f;

  /*
   * Radio visible alrededor del vehículo.
   *
   * La escala permanece fija para que el entorno cercano
   * conserve el mismo tamaño mientras el auto se desplaza.
   */
  private static final float VEHICLE_VIEW_RADIUS = 32.0f;

  private static final float SIZE_SCALE = 0.81f;

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

    int viewportY = MARGIN;

    /*
     * La cámara observa verticalmente hacia abajo.
     *
     * El objetivo sigue al vehículo y el vector UP utiliza
     * su dirección frontal para mantener el auto hacia arriba.
     *
     * El mapa gira debajo del vehículo y conserva el norte
     * como referencia dinámica en sus bordes.
     */

    float forwardX =
        -(float) Math.sin(car.getAngle());

    float forwardZ =
        -(float) Math.cos(car.getAngle());

    viewMatrix
        .identity()
        .lookAt(
            car.getX(),
            CAMERA_HEIGHT,
            car.getZ(),

            car.getX(),
            0.0f,
            car.getZ(),

            forwardX,
            0.0f,
            forwardZ);

    projectionMatrix
        .identity()
        .ortho(
            -VEHICLE_VIEW_RADIUS,
            VEHICLE_VIEW_RADIUS,

            -VEHICLE_VIEW_RADIUS,
            VEHICLE_VIEW_RADIUS,

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

    drawOverlay(
        renderer,
        car,
        viewportX,
        viewportY,
        size,
        framebufferWidth,
        framebufferHeight);
  }

  /*
   * ============================================================
   * MARCO DE INTERFAZ
   * ============================================================
   */

  private void drawOverlay(
      Renderer renderer,
      Car car,
      int viewportX,
      int viewportY,
      int size,
      int framebufferWidth,
      int framebufferHeight) {

    float top = framebufferHeight
        - viewportY
        - size;

    renderer.beginHud(
        framebufferWidth,
        framebufferHeight);

    try {

      drawFrame(
          renderer,
          viewportX,
          top,
          size);

      drawCompass(
          renderer,
          viewportX,
          top,
          size,
          car.getAngle());

    } finally {

      renderer.endHud();
    }
  }

  private void drawFrame(
      Renderer renderer,
      float x,
      float y,
      float size) {

    float thickness = FRAME_THICKNESS;

    float frameRed = 0.20f;
    float frameGreen = 0.72f;
    float frameBlue = 0.90f;

    renderer.hudRect(
        x,
        y,
        size,
        thickness,
        frameRed,
        frameGreen,
        frameBlue);

    renderer.hudRect(
        x,
        y + size - thickness,
        size,
        thickness,
        frameRed,
        frameGreen,
        frameBlue);

    renderer.hudRect(
        x,
        y,
        thickness,
        size,
        frameRed,
        frameGreen,
        frameBlue);

    renderer.hudRect(
        x + size - thickness,
        y,
        thickness,
        size,
        frameRed,
        frameGreen,
        frameBlue);
  }

  private void drawCompass(
      Renderer renderer,
      float x,
      float y,
      float size,
      float rotationY) {

    float centerX = x + size / 2.0f;
    float centerY = y + size / 2.0f;
    float radius = size / 2.0f - 15.0f;

    float northX = (float) Math.sin(rotationY);
    float northY = (float) Math.cos(rotationY);

    drawCompassLabel(
        renderer,
        "N",
        centerX + northX * radius,
        centerY - northY * radius);
  }

  private void drawCompassLabel(
      Renderer renderer,
      String label,
      float centerX,
      float centerY) {

    float pixelSize = 1.8f;
    float textWidth = 6.0f * pixelSize;
    float textHeight = 7.0f * pixelSize;

    BitmapFont.drawText(
        renderer,
        label,
        centerX - textWidth / 2.0f,
        centerY - textHeight / 2.0f,
        pixelSize,
        1.00f,
        0.90f,
        0.38f);
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
            * 0.85f
            * SIZE_SCALE);

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

        1.00f,
        0.08f,
        0.68f,

        1.80f);
  }
}
