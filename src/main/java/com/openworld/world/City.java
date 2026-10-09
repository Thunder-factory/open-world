package com.openworld.world;

import com.openworld.render.Renderer;

public final class City {

  /*
   * ============================================================
   * TIPOS DE CELDA
   * ============================================================
   */

  public static final int ROAD = 0;
  public static final int BUILDING = 1;
  public static final int PARK = 2;

  /*
   * ============================================================
   * CONFIGURACIÓN DEL MAPA
   * ============================================================
   */

  public static final float CELL_SIZE = 10.0f;

  private static final int MIN_BUILDINGS = 12;
  private static final int MIN_PARKS = 4;

  /*
   * ============================================================
   * PASOS PEATONALES
   * ============================================================
   */

  private static final int CROSS_X = 0;

  private static final int CROSS_Z = 1;
  private static final float CROSSWALK_EDGE_OFFSET = 5.80f;

  /*
   * Cada entrada contiene:
   *
   * {fila, columna, orientación}
   */
  private static final int[][] CROSSWALKS = {

      { 1, 3, CROSS_X },
      { 1, 7, CROSS_X },

      { 3, 5, CROSS_Z },

      { 5, 3, CROSS_Z },
      { 5, 7, CROSS_X },

      { 7, 5, CROSS_Z },

      { 9, 3, CROSS_X },
      { 9, 7, CROSS_X }
  };

  /*
   * Alias únicamente para hacer la matriz más legible.
   */
  private static final int R = ROAD;
  private static final int B = BUILDING;
  private static final int P = PARK;

  /*
   * ============================================================
   * MAPA 11 × 11
   * ============================================================
   *
   * Convención:
   *
   * fila → eje Z
   * columna → eje X
   *
   * fila 0 → norte (-Z)
   * fila 10 → sur (+Z)
   *
   * Las calles forman una cuadrícula completamente conectada.
   *
   * Cada manzana queda rodeada por calles.
   */

  private static final int[][] MAP = {

      // 0
      { B, R, B, R, B, R, B, R, B, R, B },

      // 1
      { R, R, R, R, R, R, R, R, R, R, R },

      // 2
      { B, R, P, R, B, R, B, R, P, R, B },

      // 3
      { R, R, R, R, R, R, R, R, R, R, R },

      // 4
      { B, R, B, R, B, R, B, R, B, R, B },

      // 5
      { R, R, R, R, R, R, R, R, R, R, R },

      // 6
      { B, R, B, R, B, R, B, R, B, R, B },

      // 7
      { R, R, R, R, R, R, R, R, R, R, R },

      // 8
      { B, R, P, R, B, R, B, R, P, R, B },

      // 9
      { R, R, R, R, R, R, R, R, R, R, R },

      // 10
      { B, R, B, R, B, R, B, R, B, R, B }
  };

  /*
   * ============================================================
   * DIMENSIONES VISUALES
   * ============================================================
   */

  private static final float GROUND_HEIGHT = 0.50f;

  private static final float ROAD_HEIGHT = 0.05f;

  private static final float SIDEWALK_HEIGHT = 0.20f;

  private static final float PARK_HEIGHT = 0.10f;

  public City() {

    validateMap();

    System.out.printf(
        "Ciudad: %d x %d celdas%n",
        getRows(),
        getColumns());

    System.out.printf(
        "Tamaño mundo: %.1f x %.1f%n",
        getWorldSize(),
        getWorldSize());

    System.out.printf(
        "Edificios: %d | Parques: %d%n",
        countCells(BUILDING),
        countCells(PARK));
  }

  /*
   * ============================================================
   * RENDER
   * ============================================================
   */

  public void render(Renderer renderer, boolean night) {

    drawGround(renderer);

    for (int row = 0; row < getRows(); row++) {

      for (int column = 0; column < getColumns(); column++) {

        int cell = MAP[row][column];

        switch (cell) {

          case ROAD ->
            drawRoad(
                renderer,
                row,
                column);

          case BUILDING ->
            drawBuildingBlock(
                renderer,
                row,
                column,
                night);

          case PARK ->
            drawPark(
                renderer,
                row,
                column);

          default ->
            throw new IllegalStateException(
                "Tipo de celda inválido: "
                    + cell);
        }
      }
    }

    /*
     * Las marcas se dibujan después de las calles
     * para que queden ligeramente por encima.
     */
    drawRoadMarkings(renderer);

    drawCrosswalks(renderer);
  }

  /*
   * ============================================================
   * SUELO GENERAL
   * ============================================================
   */

  private void drawGround(
      Renderer renderer) {

    float worldSize = getWorldSize();

    renderer.box(
        0.0f,
        -GROUND_HEIGHT / 2.0f,
        0.0f,

        worldSize,
        GROUND_HEIGHT,
        worldSize,

        0.12f,
        0.16f,
        0.12f);
  }

  /*
   * ============================================================
   * CALLE
   * ============================================================
   */

  private void drawRoad(
      Renderer renderer,
      int row,
      int column) {

    float x = getCellCenterX(column);

    float z = getCellCenterZ(row);

    renderer.box(
        x,
        ROAD_HEIGHT / 2.0f,
        z,

        CELL_SIZE,
        ROAD_HEIGHT,
        CELL_SIZE,

        0.16f,
        0.17f,
        0.18f);
  }

  /*
   * ============================================================
   * MANZANA + EDIFICIO
   * ============================================================
   */

  private void drawBuildingBlock(
      Renderer renderer,
      int row,
      int column,
      boolean night) {

    float x = getCellCenterX(column);

    float z = getCellCenterZ(row);

    /*
     * --------------------------------------------------------
     * ACERA / BASE DE LA MANZANA
     * --------------------------------------------------------
     */

    float sidewalkSize = CELL_SIZE;// * 0.92f;

    renderer.box(
        x,
        SIDEWALK_HEIGHT / 2.0f,
        z,

        sidewalkSize,
        SIDEWALK_HEIGHT,
        sidewalkSize,

        0.52f,
        0.52f,
        0.50f);

    /*
     * --------------------------------------------------------
     * EDIFICIO
     * --------------------------------------------------------
     *
     * Se utiliza una fórmula determinista.
     *
     * La ciudad siempre tendrá las mismas alturas
     * y colores al ejecutarse.
     */

    int seed = row * 31
        + column * 17;

    /*
     * Altura:
     *
     * 5.0
     * ...
     * aproximadamente 13.75
     */

    float buildingHeight = 5.0f
        + (seed % 8) * 1.25f;

    /*
     * El tamaño de la planta varía ligeramente.
     */

    float buildingSize = 6.5f
        + (seed % 3) * 0.5f;

    /*
     * Como el edificio debe comenzar encima
     * de la acera:
     *
     * bottom = SIDEWALK_HEIGHT
     */

    float buildingY = SIDEWALK_HEIGHT
        + buildingHeight / 2.0f;

    /*
     * Colores urbanos deterministas.
     */

    float red = 0.35f
        + (seed % 4) * 0.08f;

    float green = 0.32f
        + ((seed / 4) % 4) * 0.07f;

    float blue = 0.30f
        + ((seed / 7) % 4) * 0.06f;

    renderer.box(
        x,
        buildingY,
        z,

        buildingSize,
        buildingHeight,
        buildingSize,

        red,
        green,
        blue);

    /*
     * Las ventanas se dibujan después del volumen principal
     * para quedar ligeramente desplazadas de las fachadas.
     */
    drawBuildingWindows(
        renderer,
        x,
        z,
        buildingSize,
        buildingHeight,
        seed,
        night);
  }

  /*
   * ============================================================
   * VENTANAS
   * ============================================================
   */

  private void drawBuildingWindows(
      Renderer renderer,
      float buildingX,
      float buildingZ,
      float buildingSize,
      float buildingHeight,
      int seed,
      boolean night) {

    /*
     * La cantidad de filas depende de la altura
     * y se limita para mantener un costo de dibujo razonable.
     */
    int windowRows = Math.max(
        1,
        Math.min(
            3,
            (int) (buildingHeight
                / 3.0f)));

    float windowWidth = 1.35f;

    float windowHeight = 0.65f;

    float windowThickness = 0.04f;

    /*
     * Las ventanas nocturnas utilizan un tono cálido
     * y una componente emisiva.
     */
    float red = night
        ? 1.00f
        : 0.12f;

    float green = night
        ? 0.78f
        : 0.18f;

    float blue = night
        ? 0.32f
        : 0.24f;

    float emission = night
        ? 1.25f
        : 0.0f;

    float halfBuilding = buildingSize / 2.0f;

    /*
     * Un pequeño desplazamiento evita que las ventanas
     * compartan exactamente el mismo plano que la fachada.
     */
    float faceOffset = halfBuilding
        + 0.025f;

    for (int row = 0; row < windowRows; row++) {

      float normalizedHeight = (row + 1.0f)
          / (windowRows + 1.0f);

      float windowY = SIDEWALK_HEIGHT
          + 0.8f
          + normalizedHeight
              * (buildingHeight - 1.6f);

      /*
       * Fachada norte.
       */
      renderer.emissiveBox(
          buildingX,
          windowY,
          buildingZ - faceOffset,

          windowWidth,
          windowHeight,
          windowThickness,

          red,
          green,
          blue,

          emission);

      /*
       * Fachada sur.
       */
      renderer.emissiveBox(
          buildingX,
          windowY,
          buildingZ + faceOffset,

          windowWidth,
          windowHeight,
          windowThickness,

          red,
          green,
          blue,

          emission);

      /*
       * Fachada oeste.
       */
      renderer.emissiveBox(
          buildingX - faceOffset,
          windowY,
          buildingZ,

          windowThickness,
          windowHeight,
          windowWidth,

          red,
          green,
          blue,

          emission);

      /*
       * Fachada este.
       */
      renderer.emissiveBox(
          buildingX + faceOffset,
          windowY,
          buildingZ,

          windowThickness,
          windowHeight,
          windowWidth,

          red,
          green,
          blue,

          emission);
    }
  }

  /*
   * ============================================================
   * PARQUE
   * ============================================================
   */

  private void drawPark(
      Renderer renderer,
      int row,
      int column) {

    float x = getCellCenterX(column);

    float z = getCellCenterZ(row);

    float parkSize = CELL_SIZE;// * 0.90f;

    renderer.box(
        x,
        PARK_HEIGHT / 2.0f,
        z,

        parkSize,
        PARK_HEIGHT,
        parkSize,

        0.18f,
        0.50f,
        0.22f);

    drawParkDecoration(
        renderer,
        x,
        z);
  }

  /*
   * ============================================================
   * DECORACIÓN DE PARQUES
   * ============================================================
   */

  private void drawParkDecoration(
      Renderer renderer,
      float parkX,
      float parkZ) {

    /*
     * Cuatro árboles distribuidos hacia las esquinas.
     */

    drawTree(
        renderer,
        parkX - 2.7f,
        parkZ - 2.7f);

    drawTree(
        renderer,
        parkX + 2.7f,
        parkZ - 2.7f);

    drawTree(
        renderer,
        parkX - 2.7f,
        parkZ + 2.7f);

    drawTree(
        renderer,
        parkX + 2.7f,
        parkZ + 2.7f);

    /*
     * Dos bancos quedan orientados en direcciones
     * perpendiculares para dar variedad visual.
     */

    drawBenchAlongX(
        renderer,
        parkX,
        parkZ - 1.25f);

    drawBenchAlongZ(
        renderer,
        parkX,
        parkZ + 1.25f);
  }

  private void drawTree(
      Renderer renderer,
      float x,
      float z) {

    /*
     * Tronco.
     */
    renderer.box(
        x,
        1.15f,
        z,

        0.38f,
        2.10f,
        0.38f,

        0.34f,
        0.20f,
        0.08f);

    /*
     * Copa inferior.
     */
    renderer.box(
        x,
        2.55f,
        z,

        2.15f,
        1.60f,
        2.15f,

        0.10f,
        0.38f,
        0.12f);

    /*
     * Copa superior.
     */
    renderer.box(
        x,
        3.45f,
        z,

        1.55f,
        1.20f,
        1.55f,

        0.12f,
        0.46f,
        0.14f);
  }

  private void drawBenchAlongX(
      Renderer renderer,
      float x,
      float z) {

    /*
     * Asiento.
     */
    renderer.box(
        x,
        0.58f,
        z,

        2.20f,
        0.18f,
        0.65f,

        0.46f,
        0.25f,
        0.10f);

    /*
     * Respaldo.
     */
    renderer.box(
        x,
        1.00f,
        z + 0.27f,

        2.20f,
        0.72f,
        0.15f,

        0.42f,
        0.22f,
        0.08f);

    /*
     * Soportes.
     */
    renderer.box(
        x - 0.75f,
        0.32f,
        z,

        0.14f,
        0.45f,
        0.14f,

        0.18f,
        0.18f,
        0.18f);

    renderer.box(
        x + 0.75f,
        0.32f,
        z,

        0.14f,
        0.45f,
        0.14f,

        0.18f,
        0.18f,
        0.18f);
  }

  private void drawBenchAlongZ(
      Renderer renderer,
      float x,
      float z) {

    /*
     * Asiento.
     */
    renderer.box(
        x,
        0.58f,
        z,

        0.65f,
        0.18f,
        2.20f,

        0.46f,
        0.25f,
        0.10f);

    /*
     * Respaldo.
     */
    renderer.box(
        x + 0.27f,
        1.00f,
        z,

        0.15f,
        0.72f,
        2.20f,

        0.42f,
        0.22f,
        0.08f);

    /*
     * Soportes.
     */
    renderer.box(
        x,
        0.32f,
        z - 0.75f,

        0.14f,
        0.45f,
        0.14f,

        0.18f,
        0.18f,
        0.18f);

    renderer.box(
        x,
        0.32f,
        z + 0.75f,

        0.14f,
        0.45f,
        0.14f,

        0.18f,
        0.18f,
        0.18f);
  }

  /*
   * ============================================================
   * PASOS PEATONALES
   * ============================================================
   */

  private void drawCrosswalks(
      Renderer renderer) {

    for (int[] crosswalk : CROSSWALKS) {

      int row = crosswalk[0];

      int column = crosswalk[1];

      int orientation = crosswalk[2];

      if (!isRoadCell(
          row,
          column)) {

        throw new IllegalStateException(
            "Paso peatonal fuera de una calle en ["
                + row
                + ", "
                + column
                + "]");
      }

      float x = getCellCenterX(
          column);

      float z = getCellCenterZ(
          row);

      if (orientation == CROSS_X) {

        /*
         * El paso se desplaza hacia uno de los
         * bordes de la intersección.
         */
        z += CROSSWALK_EDGE_OFFSET;

        drawCrosswalkAlongX(
            renderer,
            x,
            z);

      } else {

        /*
         * El paso se desplaza hacia uno de los
         * bordes de la intersección.
         */
        x += CROSSWALK_EDGE_OFFSET;

        drawCrosswalkAlongZ(
            renderer,
            x,
            z);
      }
    }
  }

  private void drawCrosswalkAlongX(
      Renderer renderer,
      float x,
      float z) {

    int stripeCount = 20;

    float stripeWidth = 0.20f;

    float stripeLength = 1.55f;

    float stripeGap = 0.25f;

    float totalWidth = stripeCount * stripeWidth
        + (stripeCount - 1)
            * stripeGap;

    float startX = x
        - totalWidth / 2.0f
        + stripeWidth / 2.0f;

    for (int i = 0; i < stripeCount; i++) {

      float stripeX = startX
          + i
              * (stripeWidth + stripeGap);

      renderer.box(
          stripeX,
          0.072f,
          z,

          stripeWidth,
          0.025f,
          stripeLength,

          0.90f,
          0.90f,
          0.86f);
    }
  }

  private void drawCrosswalkAlongZ(
      Renderer renderer,
      float x,
      float z) {

    int stripeCount = 20;

    float stripeWidth = 0.20f;

    float stripeLength = 1.55f;

    float stripeGap = 0.25f;

    float totalWidth = stripeCount * stripeWidth
        + (stripeCount - 1)
            * stripeGap;

    float startZ = z
        - totalWidth / 2.0f
        + stripeWidth / 2.0f;

    for (int i = 0; i < stripeCount; i++) {

      float stripeZ = startZ
          + i
              * (stripeWidth + stripeGap);

      renderer.box(
          x,
          0.072f,
          stripeZ,

          stripeLength,
          0.025f,
          stripeWidth,

          0.90f,
          0.90f,
          0.86f);
    }
  }

  /*
   * ============================================================
   * MARCAS VIALES
   * ============================================================
   */

  private void drawRoadMarkings(
      Renderer renderer) {

    for (int row = 0; row < getRows(); row++) {

      for (int column = 0; column < getColumns(); column++) {

        if (!isRoadCell(
            row,
            column)) {

          continue;
        }

        boolean horizontal = hasRoad(
            row,
            column - 1)
            ||
            hasRoad(
                row,
                column + 1);

        boolean vertical = hasRoad(
            row - 1,
            column)
            ||
            hasRoad(
                row + 1,
                column);

        /*
         * Intersección.
         *
         * No se dibuja línea central aquí.
         */
        if (horizontal && vertical) {
          continue;
        }

        float x = getCellCenterX(column);

        float z = getCellCenterZ(row);

        /*
         * Calle horizontal:
         *
         * línea paralela al eje X.
         */
        if (horizontal) {

          renderer.box(
              x,
              0.065f,
              z,

              4.0f,
              0.02f,
              0.12f,

              0.90f,
              0.82f,
              0.20f);
        }

        /*
         * Calle vertical:
         *
         * línea paralela al eje Z.
         */
        if (vertical) {

          renderer.box(
              x,
              0.065f,
              z,

              0.12f,
              0.02f,
              4.0f,

              0.90f,
              0.82f,
              0.20f);
        }
      }
    }
  }

  /*
   * ============================================================
   * COORDENADAS
   * ============================================================
   */

  public float getCellCenterX(
      int column) {

    return getCellCenter(
        column);
  }

  public float getCellCenterZ(
      int row) {

    return getCellCenter(
        row);
  }

  private float getCellCenter(
      int index) {

    return -getHalfSize()
        + CELL_SIZE
            * (index + 0.5f);
  }

  /*
   * ============================================================
   * CONSULTAS DEL MAPA
   * ============================================================
   */

  public int getCell(
      int row,
      int column) {

    if (!isValidCell(
        row,
        column)) {

      throw new IndexOutOfBoundsException(
          "Celda fuera del mapa: "
              + row
              + ", "
              + column);
    }

    return MAP[row][column];
  }

  public boolean isRoadCell(
      int row,
      int column) {

    return isValidCell(
        row,
        column)
        &&
        MAP[row][column] == ROAD;
  }

  public boolean isBuildingCell(
      int row,
      int column) {

    return isValidCell(
        row,
        column)
        &&
        MAP[row][column] == BUILDING;
  }

  public boolean isParkCell(
      int row,
      int column) {

    return isValidCell(
        row,
        column)
        &&
        MAP[row][column] == PARK;
  }

  private boolean hasRoad(
      int row,
      int column) {

    return isRoadCell(
        row,
        column);
  }

  public boolean isValidCell(
      int row,
      int column) {

    return row >= 0
        && row < getRows()
        && column >= 0
        && column < getColumns();
  }

  /*
   * ============================================================
   * DIMENSIONES
   * ============================================================
   */

  public int getRows() {
    return MAP.length;
  }

  public int getColumns() {
    return MAP[0].length;
  }

  public float getWorldSize() {

    return getRows()
        * CELL_SIZE;
  }

  public float getHalfSize() {

    return getWorldSize()
        / 2.0f;
  }

  public float getSidewalkHeight() {

    return SIDEWALK_HEIGHT;
  }

  /*
   * ============================================================
   * COLISIONES
   * ============================================================
   */

  /**
   * Indica si un vehículo circular puede ocupar una
   * determinada posición del mundo.
   *
   * El automóvil se aproxima mediante un círculo
   * sobre el plano X-Z.
   */
  public boolean canDrive(
      float worldX,
      float worldZ,
      float radius) {

    if (radius < 0.0f) {
      return false;
    }

    float minimum = -getHalfSize() + radius;

    float maximum = getHalfSize() - radius;

    if (worldX < minimum
        || worldX > maximum
        || worldZ < minimum
        || worldZ > maximum) {

      return false;
    }

    for (int row = 0; row < getRows(); row++) {

      for (int column = 0; column < getColumns(); column++) {

        if (MAP[row][column] == ROAD) {
          continue;
        }

        float centerX = getCellCenterX(column);

        float centerZ = getCellCenterZ(row);

        if (circleIntersectsCell(
            worldX,
            worldZ,
            radius,
            centerX,
            centerZ)) {

          return false;
        }
      }
    }

    return true;
  }

  /**
   * Comprueba la colisión entre un círculo y una celda rectangular.
   */
  private boolean circleIntersectsCell(
      float circleX,
      float circleZ,
      float radius,
      float cellCenterX,
      float cellCenterZ) {

    float halfCell = CELL_SIZE / 2.0f;

    float minimumX = cellCenterX - halfCell;

    float maximumX = cellCenterX + halfCell;

    float minimumZ = cellCenterZ - halfCell;

    float maximumZ = cellCenterZ + halfCell;

    float closestX = clamp(
        circleX,
        minimumX,
        maximumX);

    float closestZ = clamp(
        circleZ,
        minimumZ,
        maximumZ);

    float deltaX = circleX - closestX;

    float deltaZ = circleZ - closestZ;

    float distanceSquared = deltaX * deltaX
        + deltaZ * deltaZ;

    float radiusSquared = radius * radius;

    return distanceSquared <= radiusSquared;
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
   * REPRESENTACIÓN PARA MINIMAPA
   * ============================================================
   */

  public void renderMinimap(
      Renderer renderer) {

    for (int row = 0; row < getRows(); row++) {

      for (int column = 0; column < getColumns(); column++) {

        float x = getCellCenterX(
            column);

        float z = getCellCenterZ(
            row);

        int cell = MAP[row][column];

        switch (cell) {

          case ROAD ->
            drawMinimapCell(
                renderer,
                x,
                z,

                0.10f,
                0.11f,
                0.13f);

          case BUILDING ->
            drawMinimapCell(
                renderer,
                x,
                z,

                0.52f,
                0.45f,
                0.36f);

          case PARK ->
            drawMinimapCell(
                renderer,
                x,
                z,

                0.10f,
                0.52f,
                0.16f);

          default ->
            throw new IllegalStateException(
                "Tipo de celda inválido: "
                    + cell);
        }
      }
    }
  }

  private void drawMinimapCell(
      Renderer renderer,
      float x,
      float z,
      float red,
      float green,
      float blue) {

    /*
     * Una pequeña separación permite distinguir
     * visualmente las celdas desde la vista superior.
     */

    float size = CELL_SIZE;// * 0.96f;

    renderer.emissiveBox(
        x,
        0.08f,
        z,

        size,
        0.08f,
        size,

        red,
        green,
        blue,

        0.65f);
  }

  /*
   * ============================================================
   * VALIDACIÓN
   * ============================================================
   */

  private void validateMap() {

    if (MAP.length < 11) {

      throw new IllegalStateException(
          "El mapa debe tener mínimo 11 filas");
    }

    int expectedColumns = MAP[0].length;

    if (expectedColumns < 11) {

      throw new IllegalStateException(
          "El mapa debe tener mínimo 11 columnas");
    }

    for (int row = 0; row < MAP.length; row++) {

      if (MAP[row].length != expectedColumns) {

        throw new IllegalStateException(
            "La fila "
                + row
                + " tiene un tamaño inválido");
      }

      for (int column = 0; column < MAP[row].length; column++) {

        int cell = MAP[row][column];

        if (cell != ROAD
            && cell != BUILDING
            && cell != PARK) {

          throw new IllegalStateException(
              "Celda inválida en ["
                  + row
                  + "]["
                  + column
                  + "]");
        }
      }
    }

    int buildings = countCells(BUILDING);

    int parks = countCells(PARK);

    if (buildings < MIN_BUILDINGS) {

      throw new IllegalStateException(
          "Se necesitan al menos "
              + MIN_BUILDINGS
              + " manzanas con edificios");
    }

    if (parks < MIN_PARKS) {

      throw new IllegalStateException(
          "Se necesitan al menos "
              + MIN_PARKS
              + " parques");
    }
  }

  private int countCells(
      int type) {

    int count = 0;

    for (int[] row : MAP) {

      for (int cell : row) {

        if (cell == type) {
          count++;
        }
      }
    }

    return count;
  }

}
