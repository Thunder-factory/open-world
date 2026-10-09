package com.openworld.render.mesh;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import static org.lwjgl.system.MemoryUtil.*;

public final class Mesh {

  private static final int FLOATS_PER_VERTEX = 6;

  private static final int STRIDE = FLOATS_PER_VERTEX * Float.BYTES;

  private int vaoId;

  private int vboId;

  private int eboId;

  private final int indexCount;

  public Mesh(
      MeshData data) {

    if (data == null) {

      throw new IllegalArgumentException(
          "MeshData no puede ser null");
    }

    float[] vertices = data.getVertices();

    int[] indices = data.getIndices();

    if (vertices.length % FLOATS_PER_VERTEX != 0) {

      throw new IllegalArgumentException(
          "El arreglo de vertices no coincide "
              + "con el formato position + normal");
    }

    indexCount = indices.length;

    vaoId = glGenVertexArrays();

    glBindVertexArray(vaoId);

    vboId = glGenBuffers();

    glBindBuffer(
        GL_ARRAY_BUFFER,
        vboId);

    FloatBuffer vertexBuffer = memAllocFloat(vertices.length);

    try {

      vertexBuffer.put(vertices).flip();

      glBufferData(
          GL_ARRAY_BUFFER,
          vertexBuffer,
          GL_STATIC_DRAW);

    } finally {

      memFree(vertexBuffer);
    }

    eboId = glGenBuffers();

    glBindBuffer(
        GL_ELEMENT_ARRAY_BUFFER,
        eboId);

    IntBuffer indexBuffer = memAllocInt(indices.length);

    try {

      indexBuffer.put(indices).flip();

      glBufferData(
          GL_ELEMENT_ARRAY_BUFFER,
          indexBuffer,
          GL_STATIC_DRAW);

    } finally {

      memFree(indexBuffer);
    }

    glVertexAttribPointer(
        0,
        3,
        GL_FLOAT,
        false,
        STRIDE,
        0L);

    glEnableVertexAttribArray(0);

    glVertexAttribPointer(
        1,
        3,
        GL_FLOAT,
        false,
        STRIDE,
        3L * Float.BYTES);

    glEnableVertexAttribArray(1);

    glBindVertexArray(0);

    glBindBuffer(GL_ARRAY_BUFFER, 0);

    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
  }

  public void bind() {

    glBindVertexArray(vaoId);
  }

  public void draw() {

    bind();

    glDrawElements(
        GL_TRIANGLES,
        indexCount,
        GL_UNSIGNED_INT,
        0L);
  }

  public void cleanup() {

    if (eboId != 0) {

      glDeleteBuffers(eboId);

      eboId = 0;
    }

    if (vboId != 0) {

      glDeleteBuffers(vboId);

      vboId = 0;
    }

    if (vaoId != 0) {

      glDeleteVertexArrays(vaoId);

      vaoId = 0;
    }
  }
}
