package com.openworld.render;

import com.openworld.lighting.LightingSystem;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import static org.lwjgl.system.MemoryUtil.*;

public final class Renderer {

  /*
   * ============================================================
   * CONFIGURACIÓN DEL VÉRTICE
   * ============================================================
   */

  private static final int FLOATS_PER_VERTEX = 6;

  private static final int STRIDE = FLOATS_PER_VERTEX
      * Float.BYTES;

  private static final int VERTEX_COUNT = 36;

  private static final int TRIANGLE_VERTEX_COUNT = 3;

  /*
   * Triángulo horizontal utilizado como marcador
   * direccional del vehículo en el minimapa.
   *
   * La punta se orienta hacia -Z, que corresponde
   * al frente del vehículo cuando angle = 0.
   */
  private static final float[] TRIANGLE_VERTICES = {

      /*
       * position normal
       */

      0.0f, 0.0f, -0.70f, 0.0f, 1.0f, 0.0f,

      -0.50f, 0.0f, 0.45f, 0.0f, 1.0f, 0.0f,

      0.50f, 0.0f, 0.45f, 0.0f, 1.0f, 0.0f
  };

  /*
   * ============================================================
   * CUBO UNITARIO
   * ============================================================
   *
   * El cubo se encuentra centrado en:
   *
   * (0, 0, 0)
   *
   * Cada vértice contiene:
   *
   * position: x y z
   * normal: x y z
   */

  private static final float[] CUBE_VERTICES = {

      /*
       * ====================================================
       * FRONT +Z
       * ====================================================
       */

      -0.5f, -0.5f, 0.5f, 0, 0, 1,
      0.5f, -0.5f, 0.5f, 0, 0, 1,
      0.5f, 0.5f, 0.5f, 0, 0, 1,

      0.5f, 0.5f, 0.5f, 0, 0, 1,
      -0.5f, 0.5f, 0.5f, 0, 0, 1,
      -0.5f, -0.5f, 0.5f, 0, 0, 1,

      /*
       * ====================================================
       * BACK -Z
       * ====================================================
       */

      0.5f, -0.5f, -0.5f, 0, 0, -1,
      -0.5f, -0.5f, -0.5f, 0, 0, -1,
      -0.5f, 0.5f, -0.5f, 0, 0, -1,

      -0.5f, 0.5f, -0.5f, 0, 0, -1,
      0.5f, 0.5f, -0.5f, 0, 0, -1,
      0.5f, -0.5f, -0.5f, 0, 0, -1,

      /*
       * ====================================================
       * LEFT -X
       * ====================================================
       */

      -0.5f, -0.5f, -0.5f, -1, 0, 0,
      -0.5f, -0.5f, 0.5f, -1, 0, 0,
      -0.5f, 0.5f, 0.5f, -1, 0, 0,

      -0.5f, 0.5f, 0.5f, -1, 0, 0,
      -0.5f, 0.5f, -0.5f, -1, 0, 0,
      -0.5f, -0.5f, -0.5f, -1, 0, 0,

      /*
       * ====================================================
       * RIGHT +X
       * ====================================================
       */

      0.5f, -0.5f, 0.5f, 1, 0, 0,
      0.5f, -0.5f, -0.5f, 1, 0, 0,
      0.5f, 0.5f, -0.5f, 1, 0, 0,

      0.5f, 0.5f, -0.5f, 1, 0, 0,
      0.5f, 0.5f, 0.5f, 1, 0, 0,
      0.5f, -0.5f, 0.5f, 1, 0, 0,

      /*
       * ====================================================
       * TOP +Y
       * ====================================================
       */

      -0.5f, 0.5f, 0.5f, 0, 1, 0,
      0.5f, 0.5f, 0.5f, 0, 1, 0,
      0.5f, 0.5f, -0.5f, 0, 1, 0,

      0.5f, 0.5f, -0.5f, 0, 1, 0,
      -0.5f, 0.5f, -0.5f, 0, 1, 0,
      -0.5f, 0.5f, 0.5f, 0, 1, 0,

      /*
       * ====================================================
       * BOTTOM -Y
       * ====================================================
       */

      -0.5f, -0.5f, -0.5f, 0, -1, 0,
      0.5f, -0.5f, -0.5f, 0, -1, 0,
      0.5f, -0.5f, 0.5f, 0, -1, 0,

      0.5f, -0.5f, 0.5f, 0, -1, 0,
      -0.5f, -0.5f, 0.5f, 0, -1, 0,
      -0.5f, -0.5f, -0.5f, 0, -1, 0
  };

  /*
   * ============================================================
   * RECURSOS DE RENDER
   * ============================================================
   */

  private final ShaderProgram shader;

  /*
   * ============================================================
   * DATOS TEMPORALES
   * ============================================================
   */

  private final Matrix4f model = new Matrix4f();

  private final Vector3f color = new Vector3f();

  /*
   * ============================================================
   * OPENGL
   * ============================================================
   */

  private int vaoId;

  private int vboId;

  private int triangleVaoId;

  private int triangleVboId;

  /*
   * ============================================================
   * CONSTRUCTOR
   * ============================================================
   */

  public Renderer() {

    shader = new ShaderProgram(
        "/shaders/vertex.glsl",
        "/shaders/fragment.glsl");

    createCubeMesh();

    createTriangleMesh();

    /*
     * La prueba de profundidad permite determinar
     * correctamente qué superficies se encuentran
     * delante de otras.
     */
    glEnable(
        GL_DEPTH_TEST);
  }

  /*
   * ============================================================
   * CREACIÓN DEL CUBO
   * ============================================================
   */

  private void createCubeMesh() {

    vaoId = glGenVertexArrays();

    glBindVertexArray(
        vaoId);

    vboId = glGenBuffers();

    glBindBuffer(
        GL_ARRAY_BUFFER,
        vboId);

    FloatBuffer buffer = memAllocFloat(
        CUBE_VERTICES.length);

    try {

      buffer
          .put(
              CUBE_VERTICES)
          .flip();

      glBufferData(
          GL_ARRAY_BUFFER,
          buffer,
          GL_STATIC_DRAW);

    } finally {

      memFree(
          buffer);
    }

    /*
     * ========================================================
     * ATRIBUTO 0
     * ========================================================
     *
     * position.xyz
     */

    glVertexAttribPointer(
        0,
        3,
        GL_FLOAT,
        false,
        STRIDE,
        0L);

    glEnableVertexAttribArray(
        0);

    /*
     * ========================================================
     * ATRIBUTO 1
     * ========================================================
     *
     * normal.xyz
     *
     * Las normales comienzan después
     * de los primeros tres floats.
     */

    glVertexAttribPointer(
        1,
        3,
        GL_FLOAT,
        false,
        STRIDE,
        3L * Float.BYTES);

    glEnableVertexAttribArray(
        1);

    glBindBuffer(
        GL_ARRAY_BUFFER,
        0);

    glBindVertexArray(
        0);
  }

  /*
   * ============================================================
   * CREACIÓN DEL TRIÁNGULO
   * ============================================================
   */

  private void createTriangleMesh() {

    triangleVaoId = glGenVertexArrays();

    glBindVertexArray(
        triangleVaoId);

    triangleVboId = glGenBuffers();

    glBindBuffer(
        GL_ARRAY_BUFFER,
        triangleVboId);

    FloatBuffer buffer = memAllocFloat(
        TRIANGLE_VERTICES.length);

    try {

      buffer
          .put(
              TRIANGLE_VERTICES)
          .flip();

      glBufferData(
          GL_ARRAY_BUFFER,
          buffer,
          GL_STATIC_DRAW);

    } finally {

      memFree(
          buffer);
    }

    /*
     * position.xyz
     */
    glVertexAttribPointer(
        0,
        3,
        GL_FLOAT,
        false,
        STRIDE,
        0L);

    glEnableVertexAttribArray(
        0);

    /*
     * normal.xyz
     */
    glVertexAttribPointer(
        1,
        3,
        GL_FLOAT,
        false,
        STRIDE,
        3L * Float.BYTES);

    glEnableVertexAttribArray(
        1);

    glBindBuffer(
        GL_ARRAY_BUFFER,
        0);

    glBindVertexArray(
        0);
  }

  /*
   * ============================================================
   * INICIO DE FRAME
   * ============================================================
   */

  public void beginFrame(
      Camera camera,
      float aspectRatio,
      LightingSystem lighting) {

    /*
     * El color de fondo depende del estado
     * actual de iluminación.
     */

    glClearColor(
        lighting.getClearRed(),
        lighting.getClearGreen(),
        lighting.getClearBlue(),
        1.0f);

    glClear(
        GL_COLOR_BUFFER_BIT
            |
            GL_DEPTH_BUFFER_BIT);

    shader.bind();

    /*
     * ========================================================
     * CÁMARA
     * ========================================================
     */

    shader.setUniform(
        "uView",
        camera.getViewMatrix());

    shader.setUniform(
        "uProjection",
        camera.getProjectionMatrix(
            aspectRatio));

    /*
     * ========================================================
     * ILUMINACIÓN
     * ========================================================
     */

    lighting.apply(
        shader);

    glBindVertexArray(
        vaoId);
  }

  /*
   * ============================================================
   * CAJA SIN ROTACIÓN
   * ============================================================
   */

  public void box(
      float x,
      float y,
      float z,
      float sizeX,
      float sizeY,
      float sizeZ,
      float red,
      float green,
      float blue) {

    drawBox(
        x,
        y,
        z,

        sizeX,
        sizeY,
        sizeZ,

        red,
        green,
        blue,

        0.0f,
        0.0f);
  }

  /*
   * ============================================================
   * CAJA ROTADA
   * ============================================================
   */

  /**
   * Dibuja una caja rotada alrededor del eje Y.
   *
   * rotationY se expresa en radianes.
   */
  public void rotatedBox(
      float x,
      float y,
      float z,
      float sizeX,
      float sizeY,
      float sizeZ,
      float red,
      float green,
      float blue,
      float rotationY) {

    drawBox(
        x,
        y,
        z,

        sizeX,
        sizeY,
        sizeZ,

        red,
        green,
        blue,

        rotationY,
        0.0f);
  }

  /*
   * ============================================================
   * CAJA EMISIVA
   * ============================================================
   */

  /**
   * Dibuja una caja con emisión visual.
   *
   * La emisión modifica la apariencia del objeto,
   * pero no constituye una fuente de luz puntual.
   */
  public void emissiveBox(
      float x,
      float y,
      float z,
      float sizeX,
      float sizeY,
      float sizeZ,
      float red,
      float green,
      float blue,
      float emissionStrength) {

    drawBox(
        x,
        y,
        z,

        sizeX,
        sizeY,
        sizeZ,

        red,
        green,
        blue,

        0.0f,
        emissionStrength);
  }

  /*
   * ============================================================
   * TRIÁNGULO PARA MINIMAPA
   * ============================================================
   */

  /**
   * Dibuja un triángulo horizontal sobre el plano X-Z.
   *
   * La punta del triángulo sigue la rotación indicada
   * alrededor del eje Y.
   */
  public void minimapTriangle(
      float x,
      float y,
      float z,
      float size,
      float rotationY,
      float red,
      float green,
      float blue) {

    model
        .identity()

        .translate(
            x,
            y,
            z)

        .rotateY(
            rotationY)

        .scale(
            size,
            1.0f,
            size);

    color.set(
        red,
        green,
        blue);

    shader.setUniform(
        "uModel",
        model);

    shader.setUniform(
        "uColor",
        color);

    /*
     * El marcador no utiliza emisión visual.
     */
    shader.setUniform(
        "uEmissionStrength",
        0.0f);

    glBindVertexArray(
        triangleVaoId);

    glDrawArrays(
        GL_TRIANGLES,
        0,
        TRIANGLE_VERTEX_COUNT);

    /*
     * Se restaura la malla del cubo porque el resto
     * del Renderer utiliza esa geometría.
     */
    glBindVertexArray(
        vaoId);
  }

  /*
   * ============================================================
   * DIBUJO INTERNO DE CAJAS
   * ============================================================
   */

  private void drawBox(
      float x,
      float y,
      float z,
      float sizeX,
      float sizeY,
      float sizeZ,
      float red,
      float green,
      float blue,
      float rotationY,
      float emissionStrength) {

    model
        .identity()

        .translate(
            x,
            y,
            z)

        .rotateY(
            rotationY)

        .scale(
            sizeX,
            sizeY,
            sizeZ);

    color.set(
        red,
        green,
        blue);

    drawCurrentModel(
        emissionStrength);
  }

  /*
   * ============================================================
   * MINIMAPA
   * ============================================================
   */

  public void beginMinimap(
      int x,
      int y,
      int width,
      int height,
      Matrix4f view,
      Matrix4f projection,
      LightingSystem lighting) {

    /*
     * Scissor limita el clear exclusivamente
     * al rectángulo del minimapa.
     */

    glEnable(
        GL_SCISSOR_TEST);

    glScissor(
        x,
        y,
        width,
        height);

    glViewport(
        x,
        y,
        width,
        height);

    /*
     * Solo se limpia el área perteneciente
     * al minimapa.
     */

    glClearColor(
        0.025f,
        0.030f,
        0.040f,
        1.0f);

    glClear(
        GL_COLOR_BUFFER_BIT
            |
            GL_DEPTH_BUFFER_BIT);

    shader.bind();

    shader.setUniform(
        "uView",
        view);

    shader.setUniform(
        "uProjection",
        projection);

    lighting.apply(
        shader);

    glBindVertexArray(
        vaoId);
  }

  public void endMinimap(
      int framebufferWidth,
      int framebufferHeight) {

    /*
     * El viewport principal se restaura para evitar
     * que el minimapa modifique frames posteriores.
     */

    glViewport(
        0,
        0,
        framebufferWidth,
        framebufferHeight);

    glDisable(
        GL_SCISSOR_TEST);
  }

  /*
   * ============================================================
   * CUBO DE PRUEBA
   * ============================================================
   */

  /**
   * Dibuja un cubo utilizado para comprobaciones visuales.
   */
  public void drawTestCube() {

    model
        .identity()

        .rotateX(
            (float) Math.toRadians(
                -25.0))

        .rotateY(
            (float) Math.toRadians(
                35.0))

        .scale(
            0.9f);

    color.set(
        0.15f,
        0.65f,
        0.95f);

    drawCurrentModel(
        0.0f);
  }

  /*
   * ============================================================
   * DRAW CALL
   * ============================================================
   */

  private void drawCurrentModel(
      float emissionStrength) {

    shader.setUniform(
        "uModel",
        model);

    shader.setUniform(
        "uColor",
        color);

    /*
     * Los objetos convencionales reciben 0.
     *
     * Las bombillas pueden utilizar valores
     * mayores para permanecer visualmente brillantes.
     */

    shader.setUniform(
        "uEmissionStrength",
        emissionStrength);

    glDrawArrays(
        GL_TRIANGLES,
        0,
        VERTEX_COUNT);
  }

  /*
   * ============================================================
   * FIN DE FRAME
   * ============================================================
   */

  public void endFrame() {

    glBindVertexArray(
        0);

    ShaderProgram.unbind();
  }

  /*
   * ============================================================
   * CLEANUP
   * ============================================================
   */

  public void cleanup() {

    if (triangleVboId != 0) {

      glDeleteBuffers(
          triangleVboId);

      triangleVboId = 0;
    }

    if (triangleVaoId != 0) {

      glDeleteVertexArrays(
          triangleVaoId);

      triangleVaoId = 0;
    }

    if (vboId != 0) {

      glDeleteBuffers(
          vboId);

      vboId = 0;
    }

    if (vaoId != 0) {

      glDeleteVertexArrays(
          vaoId);

      vaoId = 0;
    }

    shader.cleanup();
  }
}
