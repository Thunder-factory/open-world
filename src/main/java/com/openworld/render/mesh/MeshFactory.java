package com.openworld.render.mesh;

import static com.openworld.vehicle.VehicleGeometry.*;

public final class MeshFactory {

  private static final int FLOATS_PER_VERTEX = 6;

  private MeshFactory() {

  }

  public static MeshData createCube() {

    float[] vertices = {

        -0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f,
        0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f,
        0.5f, 0.5f, 0.5f, 0.0f, 0.0f, 1.0f,
        -0.5f, 0.5f, 0.5f, 0.0f, 0.0f, 1.0f,

        0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f,
        -0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f,
        -0.5f, 0.5f, -0.5f, 0.0f, 0.0f, -1.0f,
        0.5f, 0.5f, -0.5f, 0.0f, 0.0f, -1.0f,

        -0.5f, -0.5f, -0.5f, -1.0f, 0.0f, 0.0f,
        -0.5f, -0.5f, 0.5f, -1.0f, 0.0f, 0.0f,
        -0.5f, 0.5f, 0.5f, -1.0f, 0.0f, 0.0f,
        -0.5f, 0.5f, -0.5f, -1.0f, 0.0f, 0.0f,

        0.5f, -0.5f, 0.5f, 1.0f, 0.0f, 0.0f,
        0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 0.0f,
        0.5f, 0.5f, -0.5f, 1.0f, 0.0f, 0.0f,
        0.5f, 0.5f, 0.5f, 1.0f, 0.0f, 0.0f,

        -0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f,
        0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f,
        0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f,
        -0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f,

        -0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f,
        0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f,
        0.5f, -0.5f, 0.5f, 0.0f, -1.0f, 0.0f,
        -0.5f, -0.5f, 0.5f, 0.0f, -1.0f, 0.0f
    };

    int[] indices = {
        0, 1, 2, 0, 2, 3,
        4, 5, 6, 4, 6, 7,
        8, 9, 10, 8, 10, 11,
        12, 13, 14, 12, 14, 15,
        16, 17, 18, 16, 18, 19,
        20, 21, 22, 20, 22, 23
    };

    return new MeshData(vertices, indices);
  }

  private static int putVertex(
      float[] vertices,
      int offset,
      float x,
      float y,
      float z,
      float normalX,
      float normalY,
      float normalZ) {

    vertices[offset++] = x;
    vertices[offset++] = y;
    vertices[offset++] = z;
    vertices[offset++] = normalX;
    vertices[offset++] = normalY;
    vertices[offset++] = normalZ;

    return offset;
  }

  public static MeshData createWindowFrame(
      float borderRatio) {

    if (borderRatio <= 0.0f
        || borderRatio >= 0.5f) {

      throw new IllegalArgumentException(
          "El borde del marco debe estar entre 0 y 0.5");
    }

    final int barCount = 4;
    final int verticesPerBar = 24;
    final int indicesPerBar = 36;

    float[] vertices = new float[barCount * verticesPerBar * FLOATS_PER_VERTEX];

    int[] indices = new int[barCount * indicesPerBar];

    float sideHeight = 1.0f - borderRatio * 2.0f;
    float horizontalY = 0.5f - borderRatio / 2.0f;
    float verticalX = 0.5f - borderRatio / 2.0f;

    int vertexOffset = 0;
    int indexOffset = 0;
    int baseVertex = 0;

    vertexOffset = appendBoxVertices(
        vertices, vertexOffset,
        0.0f, horizontalY, 0.0f,
        1.0f, borderRatio, 1.0f);
    indexOffset = appendBoxIndices(
        indices, indexOffset, baseVertex);
    baseVertex += verticesPerBar;

    vertexOffset = appendBoxVertices(
        vertices, vertexOffset,
        0.0f, -horizontalY, 0.0f,
        1.0f, borderRatio, 1.0f);
    indexOffset = appendBoxIndices(
        indices, indexOffset, baseVertex);
    baseVertex += verticesPerBar;

    vertexOffset = appendBoxVertices(
        vertices, vertexOffset,
        -verticalX, 0.0f, 0.0f,
        borderRatio, sideHeight, 1.0f);
    indexOffset = appendBoxIndices(
        indices, indexOffset, baseVertex);
    baseVertex += verticesPerBar;

    appendBoxVertices(
        vertices, vertexOffset,
        verticalX, 0.0f, 0.0f,
        borderRatio, sideHeight, 1.0f);
    appendBoxIndices(indices, indexOffset, baseVertex);

    return new MeshData(vertices, indices);
  }

  public static MeshData createCircle(int segments) {

    if (segments < 3) {
      throw new IllegalArgumentException(
          "El circulo requiere al menos 3 segmentos");
    }

    float[] vertices = new float[(segments + 1) * FLOATS_PER_VERTEX];
    int[] indices = new int[segments * 3];
    int vertexOffset = putVertex(vertices, 0,
        0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f);

    for (int segment = 0; segment < segments; segment++) {
      float angle = (float) (Math.PI * 2.0 * segment / segments);
      vertexOffset = putVertex(vertices, vertexOffset,
          (float) Math.cos(angle) * 0.5f,
          (float) Math.sin(angle) * 0.5f,
          0.0f, 0.0f, 0.0f, 1.0f);
    }

    int indexOffset = 0;
    for (int segment = 0; segment < segments; segment++) {
      indices[indexOffset++] = 0;
      indices[indexOffset++] = 1 + segment;
      indices[indexOffset++] = 1 + (segment + 1) % segments;
    }

    return new MeshData(vertices, indices);
  }

  private static int appendBoxVertices(
      float[] vertices,
      int offset,
      float centerX,
      float centerY,
      float centerZ,
      float sizeX,
      float sizeY,
      float sizeZ) {

    float halfX = sizeX / 2.0f;
    float halfY = sizeY / 2.0f;
    float halfZ = sizeZ / 2.0f;

    float left = centerX - halfX;
    float right = centerX + halfX;
    float bottom = centerY - halfY;
    float top = centerY + halfY;
    float back = centerZ - halfZ;
    float front = centerZ + halfZ;

    offset = putVertex(vertices, offset,
        left, bottom, front, 0.0f, 0.0f, 1.0f);
    offset = putVertex(vertices, offset,
        right, bottom, front, 0.0f, 0.0f, 1.0f);
    offset = putVertex(vertices, offset,
        right, top, front, 0.0f, 0.0f, 1.0f);
    offset = putVertex(vertices, offset,
        left, top, front, 0.0f, 0.0f, 1.0f);

    offset = putVertex(vertices, offset,
        right, bottom, back, 0.0f, 0.0f, -1.0f);
    offset = putVertex(vertices, offset,
        left, bottom, back, 0.0f, 0.0f, -1.0f);
    offset = putVertex(vertices, offset,
        left, top, back, 0.0f, 0.0f, -1.0f);
    offset = putVertex(vertices, offset,
        right, top, back, 0.0f, 0.0f, -1.0f);

    offset = putVertex(vertices, offset,
        left, bottom, back, -1.0f, 0.0f, 0.0f);
    offset = putVertex(vertices, offset,
        left, bottom, front, -1.0f, 0.0f, 0.0f);
    offset = putVertex(vertices, offset,
        left, top, front, -1.0f, 0.0f, 0.0f);
    offset = putVertex(vertices, offset,
        left, top, back, -1.0f, 0.0f, 0.0f);

    offset = putVertex(vertices, offset,
        right, bottom, front, 1.0f, 0.0f, 0.0f);
    offset = putVertex(vertices, offset,
        right, bottom, back, 1.0f, 0.0f, 0.0f);
    offset = putVertex(vertices, offset,
        right, top, back, 1.0f, 0.0f, 0.0f);
    offset = putVertex(vertices, offset,
        right, top, front, 1.0f, 0.0f, 0.0f);

    offset = putVertex(vertices, offset,
        left, top, front, 0.0f, 1.0f, 0.0f);
    offset = putVertex(vertices, offset,
        right, top, front, 0.0f, 1.0f, 0.0f);
    offset = putVertex(vertices, offset,
        right, top, back, 0.0f, 1.0f, 0.0f);
    offset = putVertex(vertices, offset,
        left, top, back, 0.0f, 1.0f, 0.0f);

    offset = putVertex(vertices, offset,
        left, bottom, back, 0.0f, -1.0f, 0.0f);
    offset = putVertex(vertices, offset,
        right, bottom, back, 0.0f, -1.0f, 0.0f);
    offset = putVertex(vertices, offset,
        right, bottom, front, 0.0f, -1.0f, 0.0f);
    return putVertex(vertices, offset,
        left, bottom, front, 0.0f, -1.0f, 0.0f);
  }

  private static int appendBoxIndices(
      int[] indices,
      int offset,
      int baseVertex) {

    for (int face = 0; face < 6; face++) {

      int first = baseVertex + face * 4;
      indices[offset++] = first;
      indices[offset++] = first + 1;
      indices[offset++] = first + 2;
      indices[offset++] = first;
      indices[offset++] = first + 2;
      indices[offset++] = first + 3;
    }

    return offset;
  }

  public static MeshData createQuad() {

    float[] vertices = {
        -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, 1.0f,
        0.5f, -0.5f, 0.0f, 0.0f, 0.0f, 1.0f,
        0.5f, 0.5f, 0.0f, 0.0f, 0.0f, 1.0f,
        -0.5f, 0.5f, 0.0f, 0.0f, 0.0f, 1.0f
    };

    int[] indices = {
        0, 1, 2,
        0, 2, 3
    };

    return new MeshData(vertices, indices);
  }

  public static MeshData createCylinder(
      int segments) {

    if (segments < 3) {
      throw new IllegalArgumentException(
          "El cilindro requiere al menos 3 segmentos");
    }

    int vertexCount = segments * 4 + 2;
    int indexCount = segments * 12;
    float[] vertices = new float[vertexCount * 6];
    int[] indices = new int[indexCount];

    float radius = 0.5f;
    float bottomY = -0.5f;
    float topY = 0.5f;
    int vertexOffset = 0;

    for (int segment = 0; segment < segments; segment++) {

      float angle = (float) (Math.PI * 2.0 * segment / segments);
      float normalX = (float) Math.cos(angle);
      float normalZ = (float) Math.sin(angle);

      vertexOffset = putVertex(
          vertices, vertexOffset,
          normalX * radius, bottomY, normalZ * radius,
          normalX, 0.0f, normalZ);

      vertexOffset = putVertex(
          vertices, vertexOffset,
          normalX * radius, topY, normalZ * radius,
          normalX, 0.0f, normalZ);
    }

    int topCenterIndex = segments * 2;
    vertexOffset = putVertex(
        vertices, vertexOffset,
        0.0f, topY, 0.0f,
        0.0f, 1.0f, 0.0f);

    int topRingStart = topCenterIndex + 1;

    for (int segment = 0; segment < segments; segment++) {

      float angle = (float) (Math.PI * 2.0 * segment / segments);

      vertexOffset = putVertex(
          vertices, vertexOffset,
          (float) Math.cos(angle) * radius,
          topY,
          (float) Math.sin(angle) * radius,
          0.0f, 1.0f, 0.0f);
    }

    int bottomCenterIndex = topRingStart + segments;
    vertexOffset = putVertex(
        vertices, vertexOffset,
        0.0f, bottomY, 0.0f,
        0.0f, -1.0f, 0.0f);

    int bottomRingStart = bottomCenterIndex + 1;

    for (int segment = 0; segment < segments; segment++) {

      float angle = (float) (Math.PI * 2.0 * segment / segments);

      vertexOffset = putVertex(
          vertices, vertexOffset,
          (float) Math.cos(angle) * radius,
          bottomY,
          (float) Math.sin(angle) * radius,
          0.0f, -1.0f, 0.0f);
    }

    int indexOffset = 0;

    for (int segment = 0; segment < segments; segment++) {

      int next = (segment + 1) % segments;
      int bottomCurrent = segment * 2;
      int topCurrent = bottomCurrent + 1;
      int bottomNext = next * 2;
      int topNext = bottomNext + 1;

      indices[indexOffset++] = bottomCurrent;
      indices[indexOffset++] = topCurrent;
      indices[indexOffset++] = topNext;
      indices[indexOffset++] = bottomCurrent;
      indices[indexOffset++] = topNext;
      indices[indexOffset++] = bottomNext;

      indices[indexOffset++] = topCenterIndex;
      indices[indexOffset++] = topRingStart + next;
      indices[indexOffset++] = topRingStart + segment;

      indices[indexOffset++] = bottomCenterIndex;
      indices[indexOffset++] = bottomRingStart + segment;
      indices[indexOffset++] = bottomRingStart + next;
    }

    return new MeshData(vertices, indices);
  }

  public static MeshData createSphere(
      int latitudeSegments,
      int longitudeSegments) {

    if (latitudeSegments < 2) {
      throw new IllegalArgumentException(
          "La esfera requiere al menos 2 segmentos de latitud");
    }

    if (longitudeSegments < 3) {
      throw new IllegalArgumentException(
          "La esfera requiere al menos 3 segmentos de longitud");
    }

    int verticesPerRow = longitudeSegments + 1;
    int vertexCount = (latitudeSegments + 1) * verticesPerRow;
    int indexCount = latitudeSegments * longitudeSegments * 6;
    float[] vertices = new float[vertexCount * 6];
    int[] indices = new int[indexCount];
    int vertexOffset = 0;
    float radius = 0.5f;

    for (int latitude = 0; latitude <= latitudeSegments; latitude++) {

      float phi = (float) (Math.PI * latitude / latitudeSegments);
      float sinPhi = (float) Math.sin(phi);
      float cosPhi = (float) Math.cos(phi);

      for (int longitude = 0; longitude <= longitudeSegments; longitude++) {

        float theta = (float) (Math.PI * 2.0 * longitude / longitudeSegments);
        float normalX = sinPhi * (float) Math.cos(theta);
        float normalY = cosPhi;
        float normalZ = sinPhi * (float) Math.sin(theta);

        vertexOffset = putVertex(
            vertices, vertexOffset,
            normalX * radius,
            normalY * radius,
            normalZ * radius,
            normalX,
            normalY,
            normalZ);
      }
    }

    int indexOffset = 0;

    for (int latitude = 0; latitude < latitudeSegments; latitude++) {

      for (int longitude = 0; longitude < longitudeSegments; longitude++) {

        int current = latitude * verticesPerRow + longitude;
        int nextRow = current + verticesPerRow;

        indices[indexOffset++] = current;
        indices[indexOffset++] = current + 1;
        indices[indexOffset++] = nextRow;
        indices[indexOffset++] = nextRow;
        indices[indexOffset++] = current + 1;
        indices[indexOffset++] = nextRow + 1;
      }
    }

    return new MeshData(vertices, indices);
  }

  public static MeshData createCone(
      int segments) {

    if (segments < 3) {
      throw new IllegalArgumentException(
          "El cono requiere al menos 3 segmentos");
    }

    int vertexCount = segments * 3 + segments + 1;
    int indexCount = segments * 6;
    float[] vertices = new float[vertexCount * 6];
    int[] indices = new int[indexCount];
    float radius = 0.5f;
    float bottomY = -0.5f;
    float topY = 0.5f;
    int vertexOffset = 0;
    int indexOffset = 0;

    for (int segment = 0; segment < segments; segment++) {

      int next = (segment + 1) % segments;
      float angle = (float) (Math.PI * 2.0 * segment / segments);
      float nextAngle = (float) (Math.PI * 2.0 * next / segments);

      float x0 = (float) Math.cos(angle) * radius;
      float z0 = (float) Math.sin(angle) * radius;
      float x1 = (float) Math.cos(nextAngle) * radius;
      float z1 = (float) Math.sin(nextAngle) * radius;

      float edgeAX = -x0;
      float edgeAY = topY - bottomY;
      float edgeAZ = -z0;
      float edgeBX = x1 - x0;
      float edgeBZ = z1 - z0;

      float normalX = edgeAY * edgeBZ;
      float normalY = edgeAZ * edgeBX
          - edgeAX * edgeBZ;
      float normalZ = -edgeAY * edgeBX;
      float normalLength = (float) Math.sqrt(
          normalX * normalX
              + normalY * normalY
              + normalZ * normalZ);

      normalX /= normalLength;
      normalY /= normalLength;
      normalZ /= normalLength;

      int firstVertex = vertexOffset / 6;

      vertexOffset = putVertex(
          vertices, vertexOffset,
          x0, bottomY, z0,
          normalX, normalY, normalZ);
      vertexOffset = putVertex(
          vertices, vertexOffset,
          0.0f, topY, 0.0f,
          normalX, normalY, normalZ);
      vertexOffset = putVertex(
          vertices, vertexOffset,
          x1, bottomY, z1,
          normalX, normalY, normalZ);

      indices[indexOffset++] = firstVertex;
      indices[indexOffset++] = firstVertex + 1;
      indices[indexOffset++] = firstVertex + 2;
    }

    int bottomCenterIndex = vertexOffset / 6;
    vertexOffset = putVertex(
        vertices, vertexOffset,
        0.0f, bottomY, 0.0f,
        0.0f, -1.0f, 0.0f);

    int bottomRingStart = bottomCenterIndex + 1;

    for (int segment = 0; segment < segments; segment++) {

      float angle = (float) (Math.PI * 2.0 * segment / segments);

      vertexOffset = putVertex(
          vertices, vertexOffset,
          (float) Math.cos(angle) * radius,
          bottomY,
          (float) Math.sin(angle) * radius,
          0.0f, -1.0f, 0.0f);
    }

    for (int segment = 0; segment < segments; segment++) {

      int next = (segment + 1) % segments;
      indices[indexOffset++] = bottomCenterIndex;
      indices[indexOffset++] = bottomRingStart + segment;
      indices[indexOffset++] = bottomRingStart + next;
    }

    return new MeshData(vertices, indices);
  }

  public static MeshData createVehicleBody() {

    float[][] sections = {
        { FRONT_FACE_Z, 0.82f, 0.34f, 0.60f },
        { -1.70f, 0.98f, 0.27f, 0.70f },
        { -1.10f, 1.00f, 0.27f, 0.80f },
        { -0.66f, 0.93f, 0.29f, 0.85f },
        { -0.22f, 0.82f, 0.29f, 1.28f },
        { 0.42f, 0.82f, 0.29f, 1.30f },
        { 0.88f, 0.92f, 0.30f, 0.86f },
        { 1.20f, 0.94f, 0.32f, 0.69f },
        { REAR_FACE_Z, 0.84f, 0.36f, 0.60f }
    };

    int quadCount = (sections.length - 1) * 4 + 2;
    float[] vertices = new float[quadCount * 4 * FLOATS_PER_VERTEX];
    int[] indices = new int[quadCount * 6];
    int quad = 0;

    for (int section = 0; section < sections.length - 1; section++) {
      float[] current = sections[section];
      float[] next = sections[section + 1];

      appendFlatQuad(vertices, indices, quad++,
          -current[1], current[3], current[0],
          current[1], current[3], current[0],
          next[1], next[3], next[0],
          -next[1], next[3], next[0],
          0.0f, 1.0f, 0.0f);

      appendFlatQuad(vertices, indices, quad++,
          -current[1], current[2], current[0],
          -next[1], next[2], next[0],
          next[1], next[2], next[0],
          current[1], current[2], current[0],
          0.0f, -1.0f, 0.0f);

      appendFlatQuad(vertices, indices, quad++,
          -current[1], current[2], current[0],
          -current[1], current[3], current[0],
          -next[1], next[3], next[0],
          -next[1], next[2], next[0],
          -1.0f, 0.0f, 0.0f);

      appendFlatQuad(vertices, indices, quad++,
          current[1], current[2], current[0],
          next[1], next[2], next[0],
          next[1], next[3], next[0],
          current[1], current[3], current[0],
          1.0f, 0.0f, 0.0f);
    }

    float[] front = sections[0];
    appendFlatQuad(vertices, indices, quad++,
        -front[1], front[2], front[0],
        front[1], front[2], front[0],
        front[1], front[3], front[0],
        -front[1], front[3], front[0],
        0.0f, 0.0f, -1.0f);

    float[] rear = sections[sections.length - 1];
    appendFlatQuad(vertices, indices, quad,
        rear[1], rear[2], rear[0],
        -rear[1], rear[2], rear[0],
        -rear[1], rear[3], rear[0],
        rear[1], rear[3], rear[0],
        0.0f, 0.0f, 1.0f);

    return new MeshData(vertices, indices);
  }

  public static MeshData createVehicleGlass() {

    final int panelCount = 6;
    float[] vertices = new float[panelCount * 4 * FLOATS_PER_VERTEX];
    int[] indices = new int[panelCount * 6];
    int panel = 0;

    appendOffsetFlatQuad(vertices, indices, panel++,
        -0.82f, 0.92f, -0.59f, 0.82f, 0.92f, -0.59f,
        0.71f, 1.21f, -0.29f, -0.71f, 1.21f, -0.29f,
        0.0f, 1.0f, -1.0f, GLASS_OFFSET);

    appendOffsetFlatQuad(vertices, indices, panel++,
        -0.71f, 1.23f, 0.49f, 0.71f, 1.23f, 0.49f,
        0.81f, 0.92f, 0.82f, -0.81f, 0.92f, 0.82f,
        0.0f, 1.0f, 1.0f, GLASS_OFFSET);

    appendOffsetFlatQuad(vertices, indices, panel++,
        -0.885f, 0.94f, -0.52f, -0.825f, 1.21f, -0.18f,
        -0.825f, 1.22f, 0.04f, -0.825f, 0.94f, 0.04f,
        -1.0f, 0.0f, 0.0f, GLASS_OFFSET);

    appendOffsetFlatQuad(vertices, indices, panel++,
        -0.825f, 0.94f, 0.19f, -0.825f, 1.22f, 0.19f,
        -0.835f, 1.15f, 0.46f, -0.865f, 0.94f, 0.75f,
        -1.0f, 0.0f, 0.0f, GLASS_OFFSET);

    appendOffsetFlatQuad(vertices, indices, panel++,
        0.885f, 0.94f, -0.52f, 0.825f, 0.94f, 0.04f,
        0.825f, 1.22f, 0.04f, 0.825f, 1.21f, -0.18f,
        1.0f, 0.0f, 0.0f, GLASS_OFFSET);

    appendOffsetFlatQuad(vertices, indices, panel,
        0.825f, 0.94f, 0.19f, 0.865f, 0.94f, 0.75f,
        0.835f, 1.15f, 0.46f, 0.825f, 1.22f, 0.19f,
        1.0f, 0.0f, 0.0f, GLASS_OFFSET);

    return new MeshData(vertices, indices);
  }

  public static MeshData createVehicleGlassTrim() {

    final int panelCount = 6;
    float[] vertices = new float[panelCount * 4 * FLOATS_PER_VERTEX];
    int[] indices = new int[panelCount * 6];
    int panel = 0;

    appendOffsetFlatQuad(vertices, indices, panel++,
        -0.90f, 0.875f, -0.64f, 0.90f, 0.875f, -0.64f,
        0.78f, 1.255f, -0.24f, -0.78f, 1.255f, -0.24f,
        0.0f, 1.0f, -1.0f, GLASS_TRIM_OFFSET);

    appendOffsetFlatQuad(vertices, indices, panel++,
        -0.78f, 1.275f, 0.44f, 0.78f, 1.275f, 0.44f,
        0.88f, 0.885f, 0.86f, -0.88f, 0.885f, 0.86f,
        0.0f, 1.0f, 1.0f, GLASS_TRIM_OFFSET);

    appendOffsetFlatQuad(vertices, indices, panel++,
        -0.895f, 0.91f, -0.57f, -0.825f, 1.25f, -0.20f,
        -0.825f, 1.26f, 0.08f, -0.825f, 0.91f, 0.08f,
        -1.0f, 0.0f, 0.0f, GLASS_TRIM_OFFSET);

    appendOffsetFlatQuad(vertices, indices, panel++,
        -0.825f, 0.91f, 0.15f, -0.825f, 1.26f, 0.15f,
        -0.835f, 1.20f, 0.48f, -0.885f, 0.91f, 0.82f,
        -1.0f, 0.0f, 0.0f, GLASS_TRIM_OFFSET);

    appendOffsetFlatQuad(vertices, indices, panel++,
        0.895f, 0.91f, -0.57f, 0.825f, 0.91f, 0.08f,
        0.825f, 1.26f, 0.08f, 0.825f, 1.25f, -0.20f,
        1.0f, 0.0f, 0.0f, GLASS_TRIM_OFFSET);

    appendOffsetFlatQuad(vertices, indices, panel,
        0.825f, 0.91f, 0.15f, 0.885f, 0.91f, 0.82f,
        0.835f, 1.20f, 0.48f, 0.825f, 1.26f, 0.15f,
        1.0f, 0.0f, 0.0f, GLASS_TRIM_OFFSET);

    return new MeshData(vertices, indices);
  }

  public static MeshData createVehicleFrontLights() {

    float[] vertices = new float[2 * 4 * FLOATS_PER_VERTEX];
    int[] indices = new int[2 * 6];
    float z = FRONT_LIGHT_SURFACE_Z;

    appendFlatQuad(vertices, indices, 0,
        -0.70f, 0.47f, z, -0.34f, 0.47f, z,
        -0.38f, 0.61f, z, -0.67f, 0.61f, z,
        0.0f, 0.0f, -1.0f);

    appendFlatQuad(vertices, indices, 1,
        0.34f, 0.47f, z, 0.70f, 0.47f, z,
        0.67f, 0.61f, z, 0.38f, 0.61f, z,
        0.0f, 0.0f, -1.0f);

    return new MeshData(vertices, indices);
  }

  public static MeshData createVehicleGrille() {

    float[] vertices = new float[4 * FLOATS_PER_VERTEX];
    int[] indices = new int[6];
    float z = FRONT_FACE_Z - SURFACE_EPSILON - 0.001f;

    appendFlatQuad(vertices, indices, 0,
        -0.46f, 0.355f, z, 0.46f, 0.355f, z,
        0.39f, 0.455f, z, -0.39f, 0.455f, z,
        0.0f, 0.0f, -1.0f);

    return new MeshData(vertices, indices);
  }

  public static MeshData createVehicleRearLights() {

    float[] vertices = new float[2 * 4 * FLOATS_PER_VERTEX];
    int[] indices = new int[2 * 6];
    float z = REAR_FACE_Z + SURFACE_EPSILON;

    appendFlatQuad(vertices, indices, 0,
        -0.69f, 0.48f, z, -0.36f, 0.48f, z,
        -0.34f, 0.60f, z, -0.66f, 0.60f, z,
        0.0f, 0.0f, 1.0f);

    appendFlatQuad(vertices, indices, 1,
        0.36f, 0.48f, z, 0.69f, 0.48f, z,
        0.66f, 0.60f, z, 0.34f, 0.60f, z,
        0.0f, 0.0f, 1.0f);

    return new MeshData(vertices, indices);
  }

  public static MeshData createVehicleRearPlate() {

    float[] vertices = new float[4 * FLOATS_PER_VERTEX];
    int[] indices = new int[6];
    float z = REAR_FACE_Z + SURFACE_EPSILON + 0.001f;

    appendFlatQuad(vertices, indices, 0,
        -0.28f, 0.375f, z, 0.28f, 0.375f, z,
        0.28f, 0.455f, z, -0.28f, 0.455f, z,
        0.0f, 0.0f, 1.0f);

    return new MeshData(vertices, indices);
  }

  private static void appendOffsetFlatQuad(
      float[] vertices,
      int[] indices,
      int quadIndex,
      float x0, float y0, float z0,
      float x1, float y1, float z1,
      float x2, float y2, float z2,
      float x3, float y3, float z3,
      float expectedNormalX,
      float expectedNormalY,
      float expectedNormalZ,
      float offset) {

    float length = (float) Math.sqrt(
        expectedNormalX * expectedNormalX
            + expectedNormalY * expectedNormalY
            + expectedNormalZ * expectedNormalZ);
    if (length <= 0.000001f) {
      throw new IllegalArgumentException(
          "La normal de desplazamiento no puede ser nula");
    }

    float offsetX = expectedNormalX / length * offset;
    float offsetY = expectedNormalY / length * offset;
    float offsetZ = expectedNormalZ / length * offset;

    appendFlatQuad(vertices, indices, quadIndex,
        x0 + offsetX, y0 + offsetY, z0 + offsetZ,
        x1 + offsetX, y1 + offsetY, z1 + offsetZ,
        x2 + offsetX, y2 + offsetY, z2 + offsetZ,
        x3 + offsetX, y3 + offsetY, z3 + offsetZ,
        expectedNormalX, expectedNormalY, expectedNormalZ);
  }

  private static void appendFlatQuad(
      float[] vertices,
      int[] indices,
      int quadIndex,
      float x0, float y0, float z0,
      float x1, float y1, float z1,
      float x2, float y2, float z2,
      float x3, float y3, float z3,
      float expectedNormalX,
      float expectedNormalY,
      float expectedNormalZ) {

    float edgeAX = x1 - x0;
    float edgeAY = y1 - y0;
    float edgeAZ = z1 - z0;
    float edgeBX = x2 - x0;
    float edgeBY = y2 - y0;
    float edgeBZ = z2 - z0;

    float normalX = edgeAY * edgeBZ - edgeAZ * edgeBY;
    float normalY = edgeAZ * edgeBX - edgeAX * edgeBZ;
    float normalZ = edgeAX * edgeBY - edgeAY * edgeBX;
    float normalLength = (float) Math.sqrt(
        normalX * normalX + normalY * normalY + normalZ * normalZ);

    if (normalLength <= 0.000001f) {
      throw new IllegalStateException(
          "La malla contiene una cara degenerada");
    }

    normalX /= normalLength;
    normalY /= normalLength;
    normalZ /= normalLength;

    boolean reverse = normalX * expectedNormalX
        + normalY * expectedNormalY
        + normalZ * expectedNormalZ < 0.0f;

    if (reverse) {
      normalX = -normalX;
      normalY = -normalY;
      normalZ = -normalZ;
    }

    int vertexOffset = quadIndex * 4 * FLOATS_PER_VERTEX;
    if (!reverse) {
      vertexOffset = putVertex(vertices, vertexOffset,
          x0, y0, z0, normalX, normalY, normalZ);
      vertexOffset = putVertex(vertices, vertexOffset,
          x1, y1, z1, normalX, normalY, normalZ);
      vertexOffset = putVertex(vertices, vertexOffset,
          x2, y2, z2, normalX, normalY, normalZ);
      putVertex(vertices, vertexOffset,
          x3, y3, z3, normalX, normalY, normalZ);
    } else {
      vertexOffset = putVertex(vertices, vertexOffset,
          x0, y0, z0, normalX, normalY, normalZ);
      vertexOffset = putVertex(vertices, vertexOffset,
          x3, y3, z3, normalX, normalY, normalZ);
      vertexOffset = putVertex(vertices, vertexOffset,
          x2, y2, z2, normalX, normalY, normalZ);
      putVertex(vertices, vertexOffset,
          x1, y1, z1, normalX, normalY, normalZ);
    }

    int baseVertex = quadIndex * 4;
    int indexOffset = quadIndex * 6;
    indices[indexOffset] = baseVertex;
    indices[indexOffset + 1] = baseVertex + 1;
    indices[indexOffset + 2] = baseVertex + 2;
    indices[indexOffset + 3] = baseVertex;
    indices[indexOffset + 4] = baseVertex + 2;
    indices[indexOffset + 5] = baseVertex + 3;
  }
}
