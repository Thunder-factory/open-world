package com.openworld.ui;

import com.openworld.lighting.LightingSystem;
import com.openworld.render.Renderer;
import com.openworld.vehicle.Car;

import java.util.Locale;

public final class Hud {

  /*
   * ============================================================
   * DISEÑO BASE
   * ============================================================
   */

  private static final float BASE_WIDTH = 1280.0f;

  private static final float BASE_HEIGHT = 720.0f;

  private static final float MIN_SCALE = 0.65f;

  private static final float MAX_SCALE = 1.0f;

  /*
   * ============================================================
   * PANEL PRINCIPAL
   * ============================================================
   */

  private static final float BASE_MARGIN = 18.0f;

  private static final float BASE_PANEL_WIDTH = 280.0f;

  private static final float BASE_PANEL_HEIGHT = 112.0f;

  private static final float BASE_BORDER_SIZE = 2.0f;

  /*
   * ============================================================
   * TEXTO PRINCIPAL
   * ============================================================
   */

  private static final float BASE_PIXEL_SIZE = 3.0f;

  private static final float BASE_TEXT_X = 14.0f;

  private static final float BASE_FIRST_LINE_Y = 14.0f;

  private static final float BASE_LINE_DISTANCE = 32.0f;

  /*
   * ============================================================
   * PANEL DE AYUDA
   * ============================================================
   */

  private static final float BASE_HELP_WIDTH = 430.0f;

  private static final float BASE_HELP_HEIGHT = 390.0f;

  private static final float BASE_HELP_PIXEL_SIZE = 2.5f;

  private static final float BASE_HELP_PADDING = 24.0f;

  private static final float BASE_HELP_LINE_DISTANCE = 25.0f;

  /*
   * ============================================================
   * ESTADO
   * ============================================================
   */

  private boolean helpVisible = false;

  /*
   * ============================================================
   * CONTROL
   * ============================================================
   */

  public void toggleHelp() {

    helpVisible = !helpVisible;
  }

  public boolean isHelpVisible() {

    return helpVisible;
  }

  public String getHelpStateName() {

    return helpVisible
        ? "ON"
        : "OFF";
  }

  public void reset() {

    helpVisible = false;
  }

  /*
   * ============================================================
   * RENDER
   * ============================================================
   */

  public void render(
      Renderer renderer,
      Car car,
      LightingSystem lighting,
      int framebufferWidth,
      int framebufferHeight) {

    if (framebufferWidth <= 0
        || framebufferHeight <= 0) {

      return;
    }

    float scale = calculateScale(
        framebufferWidth,
        framebufferHeight);

    renderer.beginHud(
        framebufferWidth,
        framebufferHeight);

    try {

      /*
       * El panel de estado permanece siempre visible.
       */
      drawMainPanel(
          renderer,
          car,
          lighting,
          scale);

      /*
       * El panel de ayuda se dibuja solamente
       * cuando su estado se encuentra activo.
       */
      if (helpVisible) {

        drawHelpPanel(
            renderer,
            framebufferWidth,
            framebufferHeight,
            scale);
      }

    } finally {

      renderer.endHud();
    }
  }

  /*
   * ============================================================
   * ESCALA
   * ============================================================
   */

  private float calculateScale(
      int framebufferWidth,
      int framebufferHeight) {

    float horizontalScale = framebufferWidth
        / BASE_WIDTH;

    float verticalScale = framebufferHeight
        / BASE_HEIGHT;

    float scale = Math.min(
        horizontalScale,
        verticalScale);

    return Math.max(
        MIN_SCALE,
        Math.min(
            MAX_SCALE,
            scale));
  }

  /*
   * ============================================================
   * PANEL PRINCIPAL
   * ============================================================
   */

  private void drawMainPanel(
      Renderer renderer,
      Car car,
      LightingSystem lighting,
      float scale) {

    float margin = BASE_MARGIN
        * scale;

    float panelWidth = BASE_PANEL_WIDTH
        * scale;

    float panelHeight = BASE_PANEL_HEIGHT
        * scale;

    float borderSize = BASE_BORDER_SIZE
        * scale;

    /*
     * Borde exterior.
     */
    renderer.hudRect(
        margin,
        margin,

        panelWidth,
        panelHeight,

        0.68f,
        0.72f,
        0.78f);

    /*
     * Fondo interior.
     */
    renderer.hudRect(
        margin
            + borderSize,

        margin
            + borderSize,

        panelWidth
            - borderSize
                * 2.0f,

        panelHeight
            - borderSize
                * 2.0f,

        0.035f,
        0.045f,
        0.060f);

    /*
     * ========================================================
     * TEXTOS
     * ========================================================
     */

    String speedText = String.format(
        Locale.ROOT,
        "VEL: %.1f",
        car.getSpeed());

    String headlightsText = "FAROS: "
        + lighting.getHeadlightStateName();

    String modeText = "MODO: "
        + lighting.getModeName();

    float textX = margin
        + BASE_TEXT_X
            * scale;

    float firstLineY = margin
        + BASE_FIRST_LINE_Y
            * scale;

    float lineDistance = BASE_LINE_DISTANCE
        * scale;

    float pixelSize = BASE_PIXEL_SIZE
        * scale;

    /*
     * Velocidad.
     */
    BitmapFont.drawText(
        renderer,
        speedText,

        textX,
        firstLineY,

        pixelSize,

        0.90f,
        0.94f,
        1.00f);

    /*
     * Faros.
     */
    float headlightRed;
    float headlightGreen;
    float headlightBlue;

    if (lighting.areHeadlightsOn()) {

      headlightRed = 1.00f;

      headlightGreen = 0.86f;

      headlightBlue = 0.38f;

    } else {

      headlightRed = 0.62f;

      headlightGreen = 0.66f;

      headlightBlue = 0.72f;
    }

    BitmapFont.drawText(
        renderer,
        headlightsText,

        textX,
        firstLineY
            + lineDistance,

        pixelSize,

        headlightRed,
        headlightGreen,
        headlightBlue);

    /*
     * Día / noche.
     */
    float modeRed;
    float modeGreen;
    float modeBlue;

    if (lighting.isNight()) {

      modeRed = 0.48f;

      modeGreen = 0.68f;

      modeBlue = 1.00f;

    } else {

      modeRed = 1.00f;

      modeGreen = 0.78f;

      modeBlue = 0.32f;
    }

    BitmapFont.drawText(
        renderer,
        modeText,

        textX,
        firstLineY
            + lineDistance
                * 2.0f,

        pixelSize,

        modeRed,
        modeGreen,
        modeBlue);
  }

  /*
   * ============================================================
   * PANEL DE AYUDA
   * ============================================================
   */

  private void drawHelpPanel(
      Renderer renderer,
      int framebufferWidth,
      int framebufferHeight,
      float scale) {

    float panelWidth = BASE_HELP_WIDTH
        * scale;

    float panelHeight = BASE_HELP_HEIGHT
        * scale;

    /*
     * El panel se centra dentro del framebuffer.
     */
    float panelX = (framebufferWidth
        - panelWidth)
        / 2.0f;

    float panelY = (framebufferHeight
        - panelHeight)
        / 2.0f;

    float borderSize = BASE_BORDER_SIZE
        * scale;

    /*
     * ========================================================
     * BORDE
     * ========================================================
     */

    renderer.hudRect(
        panelX,
        panelY,

        panelWidth,
        panelHeight,

        0.72f,
        0.76f,
        0.84f);

    /*
     * ========================================================
     * FONDO
     * ========================================================
     */

    renderer.hudRect(
        panelX
            + borderSize,

        panelY
            + borderSize,

        panelWidth
            - borderSize
                * 2.0f,

        panelHeight
            - borderSize
                * 2.0f,

        0.025f,
        0.032f,
        0.045f);

    /*
     * ========================================================
     * TÍTULO
     * ========================================================
     */

    float titlePixelSize = 3.2f
        * scale;

    BitmapFont.drawText(
        renderer,
        "CONTROLES",

        panelX
            + BASE_HELP_PADDING
                * scale,

        panelY
            + 20.0f
                * scale,

        titlePixelSize,

        0.35f,
        0.82f,
        1.00f);

    /*
     * Línea separadora.
     */
    renderer.hudRect(
        panelX
            + BASE_HELP_PADDING
                * scale,

        panelY
            + 55.0f
                * scale,

        panelWidth
            - BASE_HELP_PADDING
                * 2.0f
                * scale,

        1.5f
            * scale,

        0.20f,
        0.28f,
        0.38f);

    /*
     * ========================================================
     * CONTROLES
     * ========================================================
     */

    float textX = panelX
        + BASE_HELP_PADDING
            * scale;

    float textY = panelY
        + 75.0f
            * scale;

    float pixelSize = BASE_HELP_PIXEL_SIZE
        * scale;

    float lineDistance = BASE_HELP_LINE_DISTANCE
        * scale;

    drawHelpLine(
        renderer,
        "W / UP: ACELERAR",
        textX,
        textY,
        pixelSize);

    drawHelpLine(
        renderer,
        "S / DOWN: REVERSA",
        textX,
        textY
            + lineDistance,
        pixelSize);

    drawHelpLine(
        renderer,
        "A / LEFT: IZQUIERDA",
        textX,
        textY
            + lineDistance
                * 2.0f,
        pixelSize);

    drawHelpLine(
        renderer,
        "D / RIGHT: DERECHA",
        textX,
        textY
            + lineDistance
                * 3.0f,
        pixelSize);

    drawHelpLine(
        renderer,
        "SPACE: FRENO",
        textX,
        textY
            + lineDistance
                * 4.0f,
        pixelSize);

    drawHelpLine(
        renderer,
        "C: CAMARA",
        textX,
        textY
            + lineDistance
                * 5.0f,
        pixelSize);

    drawHelpLine(
        renderer,
        "N: DIA / NOCHE",
        textX,
        textY
            + lineDistance
                * 6.0f,
        pixelSize);

    drawHelpLine(
        renderer,
        "F: FAROS",
        textX,
        textY
            + lineDistance
                * 7.0f,
        pixelSize);

    drawHelpLine(
        renderer,
        "M: MINIMAPA",
        textX,
        textY
            + lineDistance
                * 8.0f,
        pixelSize);

    drawHelpLine(
        renderer,
        "R: REINICIO",
        textX,
        textY
            + lineDistance
                * 9.0f,
        pixelSize);

    drawHelpLine(
        renderer,
        "H: CERRAR AYUDA",
        textX,
        textY
            + lineDistance
                * 10.0f,
        pixelSize);

    drawHelpLine(
        renderer,
        "ESC: SALIR",
        textX,
        textY
            + lineDistance
                * 11.0f,
        pixelSize);
  }

  /*
   * ============================================================
   * LÍNEA DE AYUDA
   * ============================================================
   */

  private void drawHelpLine(
      Renderer renderer,
      String text,

      float x,
      float y,

      float pixelSize) {

    BitmapFont.drawText(
        renderer,
        text,

        x,
        y,

        pixelSize,

        0.86f,
        0.89f,
        0.94f);
  }
}