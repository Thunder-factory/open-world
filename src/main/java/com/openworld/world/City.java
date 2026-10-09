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
   * VENTANAS DE EDIFICIOS
   * ============================================================
   */

  private static final int BUILDING_WINDOW_COLUMNS = 2;

  /*
   * Dimensiones visuales de cada ventana.
   */
  private static final float BUILDING_WINDOW_WIDTH = 1.15f;

  private static final float BUILDING_WINDOW_HEIGHT = 0.65f;

  private static final float BUILDING_WINDOW_THICKNESS = 0.04f;

  /*
   * Separación vertical aproximada entre filas.
   *
   * Este valor determina automáticamente cuántas filas
   * caben según la altura del edificio.
   */
  private static final float BUILDING_WINDOW_ROW_SPACING = 2.15f;

  /*
   * Márgenes verticales utilizados para evitar ventanas
   * demasiado próximas al suelo o al techo.
   */
  private static final float BUILDING_WINDOW_BOTTOM_MARGIN = 1.10f;

  private static final float BUILDING_WINDOW_TOP_MARGIN = 0.85f;

  /*
   * Separación horizontal de las columnas respecto
   * al centro de cada fachada.
   */
  private static final float BUILDING_WINDOW_COLUMN_FACTOR = 0.22f;

  /*
   * Pequeño desplazamiento exterior para evitar
   * superposición con la pared.
   */
  private static final float BUILDING_WINDOW_FACE_OFFSET = 0.025f;

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

  private static final float ROAD_MARKING_LENGTH = 2.0f;

  private static final float ROAD_MARKING_WIDTH = 0.12f;

  private static final float ROAD_MARKING_OFFSET = 3.0f;

  private static final float SIDEWALK_HEIGHT = 0.20f;

  private static final float PARK_HEIGHT = 0.10f;

  /*
   * ============================================================
   * PARADAS DE AUTOBÚS
   * ============================================================
   */

  /*
   * La parada se coloca cerca del borde sur del parque,
   * junto a la calle adyacente.
   */
  private static final float BUS_STOP_EDGE_OFFSET = 4.15f;

  /*
   * Dimensiones principales del refugio.
   */
  private static final float BUS_STOP_WIDTH = 2.40f;

  private static final float BUS_STOP_DEPTH = 0.65f;

  private static final float BUS_STOP_HEIGHT = 2.35f;

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
      boolean night) {

    /*
     * ============================================================
     * ESPACIO VERTICAL DISPONIBLE
     * ============================================================
     */

    float usableWindowHeight = buildingHeight
        - BUILDING_WINDOW_BOTTOM_MARGIN
        - BUILDING_WINDOW_TOP_MARGIN;

    /*
     * La cantidad de filas se obtiene automáticamente
     * a partir de la altura disponible.
     */
    int windowRows = Math.max(
        1,
        (int) Math.floor(
            usableWindowHeight
                / BUILDING_WINDOW_ROW_SPACING));

    /*
     * ============================================================
     * COLUMNAS
     * ============================================================
     */

    float windowColumnOffset = buildingSize
        * BUILDING_WINDOW_COLUMN_FACTOR;

    /*
     * ============================================================
     * COLOR
     * ============================================================
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

    /*
     * ============================================================
     * POSICIÓN DE FACHADAS
     * ============================================================
     */

    float halfBuilding = buildingSize
        / 2.0f;

    float faceOffset = halfBuilding
        + BUILDING_WINDOW_FACE_OFFSET;

    /*
     * ============================================================
     * FILAS
     * ============================================================
     */

    for (int row = 0; row < windowRows; row++) {

      /*
       * Las filas se distribuyen uniformemente
       * dentro del espacio vertical disponible.
       */
      float normalizedHeight = (row + 1.0f)
          / (windowRows + 1.0f);

      float windowY = SIDEWALK_HEIGHT
          + BUILDING_WINDOW_BOTTOM_MARGIN
          + normalizedHeight
              * usableWindowHeight;

      /*
       * ========================================================
       * COLUMNAS
       * ========================================================
       */

      for (int column = 0; column < BUILDING_WINDOW_COLUMNS; column++) {

        /*
         * Se obtiene una posición normalizada
         * entre -1 y +1.
         */
        float normalizedColumn = BUILDING_WINDOW_COLUMNS == 1
            ? 0.0f
            : ((float) column
                / (BUILDING_WINDOW_COLUMNS
                    - 1))
                * 2.0f
                - 1.0f;

        float lateralOffset = normalizedColumn
            * windowColumnOffset;

        /*
         * ====================================================
         * NORTE
         * ====================================================
         */

        renderer.emissiveBox(
            buildingX
                + lateralOffset,

            windowY,

            buildingZ
                - faceOffset,

            BUILDING_WINDOW_WIDTH,
            BUILDING_WINDOW_HEIGHT,
            BUILDING_WINDOW_THICKNESS,

            red,
            green,
            blue,

            emission);

        /*
         * ====================================================
         * SUR
         * ====================================================
         */

        renderer.emissiveBox(
            buildingX
                + lateralOffset,

            windowY,

            buildingZ
                + faceOffset,

            BUILDING_WINDOW_WIDTH,
            BUILDING_WINDOW_HEIGHT,
            BUILDING_WINDOW_THICKNESS,

            red,
            green,
            blue,

            emission);

        /*
         * ====================================================
         * OESTE
         * ====================================================
         */

        renderer.emissiveBox(
            buildingX
                - faceOffset,

            windowY,

            buildingZ
                + lateralOffset,

            BUILDING_WINDOW_THICKNESS,
            BUILDING_WINDOW_HEIGHT,
            BUILDING_WINDOW_WIDTH,

            red,
            green,
            blue,

            emission);

        /*
         * ====================================================
         * ESTE
         * ====================================================
         */

        renderer.emissiveBox(
            buildingX
                + faceOffset,

            windowY,

            buildingZ
                + lateralOffset,

            BUILDING_WINDOW_THICKNESS,
            BUILDING_WINDOW_HEIGHT,
            BUILDING_WINDOW_WIDTH,

            red,
            green,
            blue,

            emission);
      }
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

    /*
     * La parada queda junto al borde sur del parque.
     *
     * La celda contigua corresponde a una calle
     * en la distribución actual de los parques.
     */
    drawBusStop(
        renderer,
        parkX,
        parkZ + BUS_STOP_EDGE_OFFSET);
  }

  private void drawTree(
      Renderer renderer,
      float x,
      float z) {

    /*
     * ========================================================
     * RAÍCES Y TRONCO
     * ========================================================
     */

    renderer.box(
        x,
        0.18f,
        z,

        0.72f,
        0.18f,
        0.72f,

        0.22f,
        0.12f,
        0.05f);

    renderer.box(
        x,
        1.10f,
        z,

        0.34f,
        1.85f,
        0.34f,

        0.34f,
        0.20f,
        0.08f);

    renderer.rotatedBox(
        x,
        1.82f,
        z,

        1.25f,
        0.20f,
        0.34f,

        0.25f,
        0.13f,
        0.05f,

        0.0f);

    renderer.rotatedBox(
        x,
        1.82f,
        z,

        0.34f,
        0.20f,
        1.25f,

        0.25f,
        0.13f,
        0.05f,

        0.0f);

    /*
     * ========================================================
     * COPA EN CAPAS
     * ========================================================
     */

    renderer.rotatedBox(
        x,
        2.30f,
        z,

        2.25f,
        1.15f,
        2.25f,

        0.07f,
        0.30f,
        0.09f,

        (float) Math.toRadians(45.0));

    renderer.rotatedBox(
        x,
        3.08f,
        z,

        1.85f,
        1.20f,
        1.85f,

        0.10f,
        0.42f,
        0.12f,

        0.0f);

    renderer.rotatedBox(
        x,
        3.78f,
        z,

        1.28f,
        0.95f,
        1.28f,

        0.15f,
        0.52f,
        0.16f,

        (float) Math.toRadians(45.0));

    renderer.box(
        x,
        4.32f,
        z,

        0.70f,
        0.48f,
        0.70f,

        0.20f,
        0.60f,
        0.19f);
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
   * PARADA DE AUTOBÚS
   * ============================================================
   */

  private void drawBusStop(
      Renderer renderer,
      float x,
      float z) {

    /*
     * ========================================================
     * PLATAFORMA
     * ========================================================
     *
     * Una base ligeramente elevada permite distinguir
     * visualmente el área del refugio.
     */

    renderer.box(
        x,
        PARK_HEIGHT + 0.04f,
        z,

        BUS_STOP_WIDTH + 0.55f,
        0.08f,
        BUS_STOP_DEPTH + 0.55f,

        0.48f,
        0.49f,
        0.48f);

    /*
     * ========================================================
     * SOPORTES
     * ========================================================
     */

    float supportY = PARK_HEIGHT
        + BUS_STOP_HEIGHT / 2.0f;

    float supportOffset = BUS_STOP_WIDTH / 2.0f
        - 0.18f;

    renderer.box(
        x - supportOffset,
        supportY,
        z - 0.18f,

        0.12f,
        BUS_STOP_HEIGHT,
        0.12f,

        0.18f,
        0.20f,
        0.22f);

    renderer.box(
        x + supportOffset,
        supportY,
        z - 0.18f,

        0.12f,
        BUS_STOP_HEIGHT,
        0.12f,

        0.18f,
        0.20f,
        0.22f);

    /*
     * ========================================================
     * TECHO
     * ========================================================
     */

    float roofY = PARK_HEIGHT
        + BUS_STOP_HEIGHT
        + 0.08f;

    renderer.box(
        x,
        roofY,
        z,

        BUS_STOP_WIDTH + 0.30f,
        0.16f,
        BUS_STOP_DEPTH + 0.35f,

        0.16f,
        0.28f,
        0.40f);

    /*
     * ========================================================
     * PANEL TRASERO
     * ========================================================
     *
     * Se utiliza un tono azulado para simular
     * un panel de vidrio oscuro.
     */

    renderer.box(
        x,
        PARK_HEIGHT + 1.18f,
        z - BUS_STOP_DEPTH / 2.0f,

        BUS_STOP_WIDTH - 0.30f,
        1.75f,
        0.07f,

        0.12f,
        0.24f,
        0.32f);

    /*
     * ========================================================
     * FRANJA DEL PANEL
     * ========================================================
     *
     * Un elemento claro mejora la identificación
     * visual del mobiliario.
     */

    renderer.box(
        x,
        PARK_HEIGHT + 1.72f,
        z - BUS_STOP_DEPTH / 2.0f - 0.045f,

        BUS_STOP_WIDTH - 0.45f,
        0.14f,
        0.04f,

        0.18f,
        0.62f,
        0.88f);

    /*
     * ========================================================
     * BANCO INTERIOR
     * ========================================================
     */

    renderer.box(
        x,
        PARK_HEIGHT + 0.52f,
        z + 0.02f,

        1.75f,
        0.16f,
        0.40f,

        0.46f,
        0.25f,
        0.10f);

    /*
     * Patas del banco.
     */

    renderer.box(
        x - 0.58f,
        PARK_HEIGHT + 0.30f,
        z + 0.02f,

        0.10f,
        0.38f,
        0.10f,

        0.17f,
        0.17f,
        0.17f);

    renderer.box(
        x + 0.58f,
        PARK_HEIGHT + 0.30f,
        z + 0.02f,

        0.10f,
        0.38f,
        0.10f,

        0.17f,
        0.17f,
        0.17f);

    /*
     * ========================================================
     * POSTE DE PARADA
     * ========================================================
     *
     * El poste se desplaza lateralmente para no quedar
     * superpuesto con el refugio.
     */

    float signX = x + BUS_STOP_WIDTH / 2.0f + 0.55f;

    float signZ = z + 0.05f;

    renderer.box(
        signX,
        PARK_HEIGHT + 1.05f,
        signZ,

        0.10f,
        1.90f,
        0.10f,

        0.20f,
        0.21f,
        0.22f);

    /*
     * ========================================================
     * PLACA DE SEÑALIZACIÓN
     * ========================================================
     */

    renderer.box(
        signX,
        PARK_HEIGHT + 2.00f,
        signZ,

        0.58f,
        0.62f,
        0.12f,

        0.08f,
        0.38f,
        0.72f);

    /*
     * Un pequeño marcador amarillo permite distinguir
     * la placa a mayor distancia.
     */

    renderer.box(
        signX,
        PARK_HEIGHT + 2.00f,
        signZ + 0.07f,

        0.28f,
        0.18f,
        0.04f,

        0.95f,
        0.78f,
        0.12f);
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

          for (int segment = -1;
              segment <= 1;
              segment++) {

            renderer.box(
                x
                    + segment
                        * ROAD_MARKING_OFFSET,
                0.065f,
                z,

                ROAD_MARKING_LENGTH,
                0.02f,
                ROAD_MARKING_WIDTH,

                0.90f,
                0.82f,
                0.20f);
          }
        }

        /*
         * Calle vertical:
         *
         * línea paralela al eje Z.
         */
        if (vertical) {

          for (int segment = -1;
              segment <= 1;
              segment++) {

            renderer.box(
                x,
                0.065f,
                z
                    + segment
                        * ROAD_MARKING_OFFSET,

                ROAD_MARKING_WIDTH,
                0.02f,
                ROAD_MARKING_LENGTH,

                0.90f,
                0.82f,
                0.20f);
          }
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
     * Las celdas utilizan su tamaño completo para formar
     * una representación continua de la ciudad.
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
