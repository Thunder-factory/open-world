package com.openworld.render;

import com.openworld.gameplay.DeliveryManager;
import com.openworld.lighting.LightingSystem;
import com.openworld.ui.BitmapFont;
import com.openworld.vehicle.Car;
import com.openworld.world.City;

import org.joml.Matrix4f;

public final class Minimap {

  public enum Mode {

    NORTH_UP,
    HEADING_UP
  }

  /*
   * ============================================================
   * CONFIGURACIÓN
   * ============================================================
   */

  private static final int MARGIN = 18;

  private static final int MIN_SIZE = 140;

  private static final int MAX_SIZE = 300;

  private static final float MINIMAP_SIZE_RATIO = 0.23f;

  private static final float FRAME_THICKNESS = 3.0f;

  /*
   * Radio visible alrededor del vehículo.
   *
   * La escala permanece fija para que el entorno cercano
   * conserve el mismo tamaño mientras el auto se desplaza.
   */
  private static final float VEHICLE_VIEW_RADIUS_CELLS = 3.20f;

  private static final float VEHICLE_VIEW_RADIUS =
      City.CELL_SIZE * VEHICLE_VIEW_RADIUS_CELLS;

  private static final float CAMERA_HEIGHT = 180.0f;

  private static final float NEAR_PLANE = 0.10f;

  private static final float FAR_PLANE_PADDING = 50.0f;

  private static final int FRAME_SEGMENT_COUNT = 72;

  private static final float DESTINATION_VISIBLE_RADIUS_FACTOR = 0.86f;

  private static final float DESTINATION_INDICATOR_RADIUS_FACTOR = 0.92f;

  private static final float MIN_DIRECTION_DISTANCE = 0.0001f;

  private static final float COMPASS_EDGE_PADDING = 15.0f;

  private static final float COMPASS_PIXEL_SIZE = 1.80f;

  private static final float PLAYER_MARKER_HEIGHT = 0.30f;

  private static final float PLAYER_MARKER_SIZE = 2.80f;

  private static final float PLAYER_MARKER_RED = 1.00f;

  private static final float PLAYER_MARKER_GREEN = 0.20f;

  private static final float PLAYER_MARKER_BLUE = 0.08f;

  private static final float DESTINATION_MARKER_HEIGHT = 0.30f;

  private static final float DESTINATION_MARKER_SIZE = 2.20f;

  private static final float DESTINATION_MARKER_RED = 1.00f;

  private static final float DESTINATION_MARKER_GREEN = 0.08f;

  private static final float DESTINATION_MARKER_BLUE = 0.68f;

  private static final float DESTINATION_INDICATOR_HEIGHT = 0.60f;

  private static final float DESTINATION_INDICATOR_SIZE = 2.80f;

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

  private Mode mode = Mode.HEADING_UP;

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

  public Mode getMode() {

    return mode;
  }

  public void setMode(Mode mode) {

    if (mode == null) {
      throw new IllegalArgumentException(
          "El modo del minimapa no puede ser null");
    }

    this.mode = mode;
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

    float upX = 0.0f;
    float upZ = -1.0f;

    if (mode == Mode.HEADING_UP) {

      upX = -(float) Math.sin(car.getAngle());
      upZ = -(float) Math.cos(car.getAngle());
    }

    viewMatrix
        .identity()
        .lookAt(
            car.getX(),
            CAMERA_HEIGHT,
            car.getZ(),

            car.getX(),
            0.0f,
            car.getZ(),

            upX,
            0.0f,
            upZ);

    projectionMatrix
        .identity()
        .ortho(
            -VEHICLE_VIEW_RADIUS,
            VEHICLE_VIEW_RADIUS,

            -VEHICLE_VIEW_RADIUS,
            VEHICLE_VIEW_RADIUS,

            NEAR_PLANE,
            CAMERA_HEIGHT + FAR_PLANE_PADDING);

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
          deliveryManager,
          car);

      drawDestinationIndicator(
          renderer,
          deliveryManager,
          car);

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

    float centerX = x + size / 2.0f;
    float centerY = y + size / 2.0f;
    float radius = size / 2.0f - FRAME_THICKNESS / 2.0f;
    int segmentCount = FRAME_SEGMENT_COUNT;
    float segmentLength = (float) (
        2.0 * Math.PI * radius / segmentCount
            + FRAME_THICKNESS);

    for (int segment = 0;
        segment < segmentCount;
        segment++) {

      float angle = (float) (
          2.0 * Math.PI
              * segment
              / segmentCount);

      float segmentCenterX = centerX
          + (float) Math.cos(angle) * radius;

      float segmentCenterY = centerY
          + (float) Math.sin(angle) * radius;

      renderer.hudRectRotated(
          segmentCenterX - segmentLength / 2.0f,
          segmentCenterY - FRAME_THICKNESS / 2.0f,
          segmentLength,
          FRAME_THICKNESS,
          angle + (float) (Math.PI / 2.0),
          0.20f,
          0.72f,
          0.90f);
    }
  }

  private void drawCompass(
      Renderer renderer,
      float x,
      float y,
      float size,
      float rotationY) {

    float centerX = x + size / 2.0f;
    float centerY = y + size / 2.0f;
    float radius = size / 2.0f
        - COMPASS_EDGE_PADDING;

    float northX = mode == Mode.HEADING_UP
        ? (float) Math.sin(rotationY)
        : 0.0f;

    float northY = mode == Mode.HEADING_UP
        ? (float) Math.cos(rotationY)
        : 1.0f;

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

    float pixelSize = COMPASS_PIXEL_SIZE;
    float textWidth = BitmapFont.getTextWidth(
        label,
        pixelSize);
    float textHeight = BitmapFont.getTextHeight(
        pixelSize);

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
            minimumDimension
                * MINIMAP_SIZE_RATIO);

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
        PLAYER_MARKER_HEIGHT,
        car.getZ(),

        PLAYER_MARKER_SIZE,

        car.getAngle(),

        PLAYER_MARKER_RED,
        PLAYER_MARKER_GREEN,
        PLAYER_MARKER_BLUE);
  }

  /*
   * ============================================================
   * DESTINO ACTIVO
   * ============================================================
   */

  private void drawDestination(
      Renderer renderer,
      DeliveryManager deliveryManager,
      Car car) {

    if (!isDestinationVisible(
        deliveryManager,
        car)) {
      return;
    }

    renderer.minimapMarkerBox(
        deliveryManager.getCurrentDestinationX(),
        DESTINATION_MARKER_HEIGHT,
        deliveryManager.getCurrentDestinationZ(),

        DESTINATION_MARKER_SIZE,
        DESTINATION_MARKER_HEIGHT,
        DESTINATION_MARKER_SIZE,

        DESTINATION_MARKER_RED,
        DESTINATION_MARKER_GREEN,
        DESTINATION_MARKER_BLUE);
  }

  private boolean isDestinationVisible(
      DeliveryManager deliveryManager,
      Car car) {

    if (!deliveryManager.hasActiveDestination()) {
      return false;
    }

    float deltaX = deliveryManager.getCurrentDestinationX()
        - car.getX();

    float deltaZ = deliveryManager.getCurrentDestinationZ()
        - car.getZ();

    float distance = (float) Math.sqrt(
        deltaX * deltaX
            + deltaZ * deltaZ);

    return distance
        <= VEHICLE_VIEW_RADIUS
            * DESTINATION_VISIBLE_RADIUS_FACTOR;
  }

  private void drawDestinationIndicator(
      Renderer renderer,
      DeliveryManager deliveryManager,
      Car car) {

    if (!deliveryManager.hasActiveDestination()) {
      return;
    }

    float deltaX = deliveryManager.getCurrentDestinationX()
        - car.getX();

    float deltaZ = deliveryManager.getCurrentDestinationZ()
        - car.getZ();

    float distance = (float) Math.sqrt(
        deltaX * deltaX
            + deltaZ * deltaZ);

    if (distance
        <= VEHICLE_VIEW_RADIUS
            * DESTINATION_VISIBLE_RADIUS_FACTOR) {
      return;
    }

    if (distance <= MIN_DIRECTION_DISTANCE) {
      return;
    }

    float indicatorDistance =
        VEHICLE_VIEW_RADIUS
            * DESTINATION_INDICATOR_RADIUS_FACTOR;

    float directionX = deltaX / distance;
    float directionZ = deltaZ / distance;

    float indicatorX = car.getX()
        + directionX * indicatorDistance;

    float indicatorZ = car.getZ()
        + directionZ * indicatorDistance;

    float directionAngle = (float) Math.atan2(
        -directionX,
        -directionZ);

    renderer.minimapTriangle(
        indicatorX,
        DESTINATION_INDICATOR_HEIGHT,
        indicatorZ,
        DESTINATION_INDICATOR_SIZE,
        directionAngle,
        DESTINATION_MARKER_RED,
        DESTINATION_MARKER_GREEN,
        DESTINATION_MARKER_BLUE);
  }
}
