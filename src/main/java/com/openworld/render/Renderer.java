package com.openworld.render;

import com.openworld.lighting.LightingSystem;
import com.openworld.render.mesh.Mesh;
import com.openworld.render.mesh.MeshLibrary;

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

  private static final int TRIANGLE_VERTEX_COUNT = 3;

  private static final float DEFAULT_SPECULAR_STRENGTH = 0.0f;

  private static final float DEFAULT_SHININESS = 16.0f;

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
   * RECURSOS DE RENDER
   * ============================================================
   */

  private final ShaderProgram shader;

  private final MeshLibrary meshLibrary;

  /*
   * ============================================================
   * DATOS TEMPORALES
   * ============================================================
   */

  private final Matrix4f model = new Matrix4f();

  private final Vector3f color = new Vector3f();

  private final Vector3f viewPosition = new Vector3f();

  private final Vector3f minimapCircle = new Vector3f(-1.0f, -1.0f, -1.0f);

  /*
   * Matrices utilizadas exclusivamente
   * por la capa de interfaz 2D.
   */
  private final Matrix4f hudView = new Matrix4f();

  private final Matrix4f hudProjection = new Matrix4f();

  /*
   * ============================================================
   * OPENGL
   * ============================================================
   */

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

    meshLibrary = new MeshLibrary();

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
     * La escena principal utiliza iluminación normal.
     */
    shader.setUniform(
        "uUnlit",
        0);

    minimapCircle.set(
        -1.0f,
        -1.0f,
        -1.0f);

    shader.setUniform(
        "uMinimapCircle",
        minimapCircle);

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

    camera.copyPosition(viewPosition);

    shader.setUniform(
        "uViewPosition",
        viewPosition);

    /*
     * ========================================================
     * ILUMINACIÓN
     * ========================================================
     */

    lighting.apply(
        shader);

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

  public void box(
      float x,
      float y,
      float z,
      float sizeX,
      float sizeY,
      float sizeZ,
      Material material) {

    drawMesh(
        meshLibrary.getCube(),
        x, y, z,
        0.0f, 0.0f, 0.0f,
        sizeX, sizeY, sizeZ,
        material);
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

  public void quad(
      float x,
      float y,
      float z,
      float width,
      float height,
      float rotationX,
      float rotationY,
      float rotationZ,
      float red,
      float green,
      float blue,
      float emissionStrength) {

    drawMesh(
        meshLibrary.getQuad(),
        x, y, z,
        rotationX, rotationY, rotationZ,
        width, height, 1.0f,
        red, green, blue,
        emissionStrength);
  }

  public void cylinder(
      float x,
      float y,
      float z,
      float sizeX,
      float sizeY,
      float sizeZ,
      float rotationX,
      float rotationY,
      float rotationZ,
      float red,
      float green,
      float blue,
      float emissionStrength) {

    drawMesh(
        meshLibrary.getCylinder(),
        x, y, z,
        rotationX, rotationY, rotationZ,
        sizeX, sizeY, sizeZ,
        red, green, blue,
        emissionStrength);
  }

  public void sphere(
      float x,
      float y,
      float z,
      float sizeX,
      float sizeY,
      float sizeZ,
      float rotationX,
      float rotationY,
      float rotationZ,
      float red,
      float green,
      float blue,
      float emissionStrength) {

    drawMesh(
        meshLibrary.getSphere(),
        x, y, z,
        rotationX, rotationY, rotationZ,
        sizeX, sizeY, sizeZ,
        red, green, blue,
        emissionStrength);
  }

  public void cone(
      float x,
      float y,
      float z,
      float sizeX,
      float sizeY,
      float sizeZ,
      float rotationX,
      float rotationY,
      float rotationZ,
      float red,
      float green,
      float blue,
      float emissionStrength) {

    drawMesh(
        meshLibrary.getCone(),
        x, y, z,
        rotationX, rotationY, rotationZ,
        sizeX, sizeY, sizeZ,
        red, green, blue,
        emissionStrength);
  }

  public void windowFrame(
      float x,
      float y,
      float z,
      float width,
      float height,
      float depth,
      float rotationY,
      float red,
      float green,
      float blue) {

    drawMesh(
        meshLibrary.getWindowFrame(),
        x, y, z,
        0.0f, rotationY, 0.0f,
        width, height, depth,
        red, green, blue,
        0.0f);
  }

  public void vehicleBody(
      float x,
      float y,
      float z,
      float scale,
      float rotationY,
      float red,
      float green,
      float blue) {

    if (scale <= 0.0f) {
      return;
    }

    drawMesh(
        meshLibrary.getVehicleBody(),
        x, y, z,
        0.0f, rotationY, 0.0f,
        scale, scale, scale,
        red, green, blue,
        0.0f);
  }

  public void quad(
      float x, float y, float z,
      float width, float height,
      float rotationX, float rotationY, float rotationZ,
      Material material) {

    drawMesh(meshLibrary.getQuad(), x, y, z,
        rotationX, rotationY, rotationZ,
        width, height, 1.0f, material);
  }

  public void cylinder(
      float x, float y, float z,
      float sizeX, float sizeY, float sizeZ,
      float rotationX, float rotationY, float rotationZ,
      Material material) {

    drawMesh(meshLibrary.getCylinder(), x, y, z,
        rotationX, rotationY, rotationZ,
        sizeX, sizeY, sizeZ, material);
  }

  public void sphere(
      float x, float y, float z,
      float sizeX, float sizeY, float sizeZ,
      float rotationX, float rotationY, float rotationZ,
      Material material) {

    drawMesh(meshLibrary.getSphere(), x, y, z,
        rotationX, rotationY, rotationZ,
        sizeX, sizeY, sizeZ, material);
  }

  public void cone(
      float x, float y, float z,
      float sizeX, float sizeY, float sizeZ,
      float rotationX, float rotationY, float rotationZ,
      Material material) {

    drawMesh(meshLibrary.getCone(), x, y, z,
        rotationX, rotationY, rotationZ,
        sizeX, sizeY, sizeZ, material);
  }

  public void windowFrame(
      float x, float y, float z,
      float width, float height, float depth,
      float rotationY,
      Material material) {

    drawMesh(meshLibrary.getWindowFrame(), x, y, z,
        0.0f, rotationY, 0.0f,
        width, height, depth, material);
  }

  public void vehicleBody(
      float x, float y, float z,
      float scale, float rotationY,
      Material material) {

    if (scale <= 0.0f) {
      return;
    }

    drawMesh(meshLibrary.getVehicleBody(), x, y, z,
        0.0f, rotationY, 0.0f,
        scale, scale, scale, material);
  }

  public void vehicleGlass(
      float x,
      float y,
      float z,
      float scale,
      float rotationY,
      Material material) {

    if (scale <= 0.0f) {
      return;
    }

    drawMesh(meshLibrary.getVehicleGlass(), x, y, z,
        0.0f, rotationY, 0.0f,
        scale, scale, scale, material);
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

    shader.setUniform(
        "uUnlit",
        1);

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

    shader.setUniform(
        "uUnlit",
        0);

    /*
     * Se restaura la malla del cubo porque el resto
     * del Renderer utiliza esa geometría.
     */
    meshLibrary.getCube().bind();
  }

  public void minimapMarkerBox(
      float x,
      float y,
      float z,
      float sizeX,
      float sizeY,
      float sizeZ,
      float red,
      float green,
      float blue) {

    shader.setUniform(
        "uUnlit",
        1);

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

    shader.setUniform(
        "uUnlit",
        0);
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

    drawMesh(
        meshLibrary.getCube(),
        x,
        y,
        z,
        0.0f,
        rotationY,
        0.0f,
        sizeX,
        sizeY,
        sizeZ,
        red,
        green,
        blue,
        emissionStrength);
  }

  private void drawMesh(
      Mesh mesh,
      float x,
      float y,
      float z,
      float rotationX,
      float rotationY,
      float rotationZ,
      float scaleX,
      float scaleY,
      float scaleZ,
      float red,
      float green,
      float blue,
      float emissionStrength) {

    drawMesh(mesh, x, y, z,
        rotationX, rotationY, rotationZ,
        scaleX, scaleY, scaleZ,
        red, green, blue, emissionStrength,
        DEFAULT_SPECULAR_STRENGTH, DEFAULT_SHININESS);
  }

  private void drawMesh(
      Mesh mesh,
      float x, float y, float z,
      float rotationX, float rotationY, float rotationZ,
      float scaleX, float scaleY, float scaleZ,
      Material material) {

    if (material == null) {
      throw new IllegalArgumentException(
          "El material no puede ser null");
    }

    drawMesh(mesh, x, y, z,
        rotationX, rotationY, rotationZ,
        scaleX, scaleY, scaleZ,
        material.getRed(), material.getGreen(), material.getBlue(),
        material.getEmissionStrength(), material.getSpecularStrength(),
        material.getShininess());
  }

  private void drawMesh(
      Mesh mesh,
      float x, float y, float z,
      float rotationX, float rotationY, float rotationZ,
      float scaleX, float scaleY, float scaleZ,
      float red, float green, float blue,
      float emissionStrength,
      float specularStrength,
      float shininess) {

    if (mesh == null) {

      throw new IllegalArgumentException(
          "La malla no puede ser null");
    }

    model
        .identity()
        .translate(x, y, z)
        .rotateY(rotationY)
        .rotateX(rotationX)
        .rotateZ(rotationZ)
        .scale(scaleX, scaleY, scaleZ);

    color.set(red, green, blue);

    shader.setUniform("uModel", model);
    shader.setUniform("uColor", color);
    shader.setUniform("uEmissionStrength", emissionStrength);
    shader.setUniform("uSpecularStrength", specularStrength);
    shader.setUniform("uShininess", shininess);

    mesh.draw();
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
      LightingSystem lighting,
      float cameraX,
      float cameraY,
      float cameraZ) {

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

    glClear(
        GL_DEPTH_BUFFER_BIT);

    shader.bind();

    /*
     * El minimapa mantiene la iluminación utilizada
     * actualmente por su representación.
     */
    shader.setUniform(
        "uUnlit",
        0);

    minimapCircle.set(
        x + width / 2.0f,
        y + height / 2.0f,
        Math.min(width, height) / 2.0f);

    shader.setUniform(
        "uMinimapCircle",
        minimapCircle);

    shader.setUniform(
        "uView",
        view);

    shader.setUniform(
        "uProjection",
        projection);

    viewPosition.set(cameraX, cameraY, cameraZ);

    shader.setUniform(
        "uViewPosition",
        viewPosition);

    lighting.apply(
        shader);

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

    minimapCircle.set(
        -1.0f,
        -1.0f,
        -1.0f);

    shader.setUniform(
        "uMinimapCircle",
        minimapCircle);

    glDisable(
        GL_SCISSOR_TEST);
  }

  /*
   * ============================================================
   * HUD
   * ============================================================
   */

  /**
   * Prepara una capa 2D expresada en píxeles.
   *
   * El origen se coloca visualmente en la esquina
   * superior izquierda.
   */
  public void beginHud(
      int framebufferWidth,
      int framebufferHeight) {

    if (framebufferWidth <= 0
        || framebufferHeight <= 0) {

      return;
    }

    /*
     * Se utiliza nuevamente todo el framebuffer.
     */
    glViewport(
        0,
        0,
        framebufferWidth,
        framebufferHeight);

    /*
     * La interfaz no necesita prueba de profundidad.
     *
     * De esta forma permanece visible sobre la escena 3D.
     */
    glDisable(
        GL_DEPTH_TEST);

    /*
     * La vista del HUD no utiliza cámara.
     */
    hudView
        .identity();

    /*
     * Coordenadas de pantalla:
     *
     * (0, 0)
     * esquina superior izquierda
     *
     * X aumenta hacia la derecha.
     * Y aumenta hacia abajo.
     */
    hudProjection
        .identity()
        .ortho(
            0.0f,
            framebufferWidth,

            framebufferHeight,
            0.0f,

            -1.0f,
            1.0f);

    shader.bind();

    minimapCircle.set(
        -1.0f,
        -1.0f,
        -1.0f);

    shader.setUniform(
        "uMinimapCircle",
        minimapCircle);

    shader.setUniform(
        "uView",
        hudView);

    shader.setUniform(
        "uProjection",
        hudProjection);

    /*
     * Los elementos del HUD mantienen su color
     * independientemente de la iluminación 3D.
     */
    shader.setUniform(
        "uUnlit",
        1);

    /*
     * La malla cúbica también se reutiliza
     * para formar rectángulos planos.
     */
  }

  /**
   * Dibuja un rectángulo 2D utilizando coordenadas
   * de pantalla.
   *
   * x e y representan la esquina superior izquierda.
   */
  public void hudRect(
      float x,
      float y,

      float width,
      float height,

      float red,
      float green,
      float blue) {

    if (width <= 0.0f
        || height <= 0.0f) {

      return;
    }

    /*
     * La malla base está centrada en el origen.
     *
     * Por eso se convierte la esquina superior izquierda
     * recibida al centro del rectángulo.
     */
    float centerX = x
        + width / 2.0f;

    float centerY = y
        + height / 2.0f;

    drawMesh(
        meshLibrary.getCube(),
        centerX,
        centerY,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        width,
        height,
        1.0f,
        red,
        green,
        blue,
        0.0f);
  }

  public void hudRectRotated(
      float x,
      float y,
      float width,
      float height,
      float rotation,
      float red,
      float green,
      float blue) {

    if (width <= 0.0f
        || height <= 0.0f) {

      return;
    }

    float centerX = x
        + width / 2.0f;

    float centerY = y
        + height / 2.0f;

    drawMesh(
        meshLibrary.getCube(),
        centerX,
        centerY,
        0.0f,
        0.0f,
        0.0f,
        rotation,
        width,
        height,
        1.0f,
        red,
        green,
        blue,
        0.0f);
  }

  /**
   * Restaura los estados temporales modificados
   * durante el renderizado de la interfaz.
   */
  public void endHud() {

    /*
     * Se restaura el modo iluminado para mantener
     * un estado consistente.
     */
    shader.setUniform(
        "uUnlit",
        0);

    /*
     * El siguiente frame vuelve a utilizar
     * prueba de profundidad para la escena 3D.
     */
    glEnable(
        GL_DEPTH_TEST);
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

    drawMesh(
        meshLibrary.getCube(),
        0.0f,
        0.0f,
        0.0f,
        (float) Math.toRadians(-25.0),
        (float) Math.toRadians(35.0),
        0.0f,
        0.9f,
        0.9f,
        0.9f,
        0.15f,
        0.65f,
        0.95f,
        0.0f);
  }

  /*
   * ============================================================
   * DRAW CALL
   * ============================================================
   */

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

    meshLibrary.cleanup();

    shader.cleanup();
  }
}
