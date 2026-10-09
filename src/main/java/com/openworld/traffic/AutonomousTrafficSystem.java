
package com.openworld.traffic;

import com.openworld.render.Renderer;
import com.openworld.world.City;

public final class AutonomousTrafficSystem {

  /*
   * ============================================================
   * CONFIGURACIÓN GENERAL
   * ============================================================
   */

  /*
   * Distancia máxima respecto al waypoint
   * para considerarlo alcanzado.
   */
  private static final float WAYPOINT_RADIUS = 0.50f;

  /*
   * Distancia desde la cual comienza a reducirse
   * la velocidad antes de un cambio de trayectoria.
   */
  private static final float SLOWDOWN_DISTANCE = 4.50f;

  /*
   * Velocidad lineal utilizada durante los giros.
   */
  private static final float CORNER_SPEED = 2.60f;

  /*
   * Diferencia angular mínima para considerar
   * que se está realizando un giro.
   */
  private static final float TURN_ANGLE_THRESHOLD = (float) Math.toRadians(
      6.0);

  /*
   * ============================================================
   * CARRIL
   * ============================================================
   */

  /*
   * Desplazamiento lateral respecto al centro
   * de la calle.
   *
   * El desplazamiento se aplica hacia la derecha
   * según la dirección de circulación.
   */
  private static final float LANE_OFFSET = 2.60f;

  /*
   * Distancia desde el centro de una intersección
   * donde comienza o termina la transición del giro.
   */
  private static final float CORNER_TRANSITION_DISTANCE = 3.00f;

  /*
   * ============================================================
   * RUTAS
   * ============================================================
   *
   * Cada waypoint contiene:
   *
   * { fila, columna }
   *
   * Las tres rutas utilizan sentido horario.
   */

  private static final int[][] ROUTE_1 = {

      { 1, 1 },
      { 1, 9 },
      { 9, 9 },
      { 9, 1 }
  };

  private static final int[][] ROUTE_2 = {

      { 3, 3 },
      { 3, 7 },
      { 7, 7 },
      { 7, 3 }
  };

  private static final int[][] ROUTE_3 = {

      { 1, 3 },
      { 1, 7 },
      { 5, 7 },
      { 5, 3 }
  };

  /*
   * Mismo circuito exterior que ROUTE_1,
   * pero recorrido en sentido contrario.
   *
   * Debido a la inversión del recorrido,
   * las cuatro curvas son hacia la izquierda.
   *
   * El cálculo automático del carril mantiene
   * el desplazamiento hacia la derecha respecto
   * a la dirección de circulación.
   */
  private static final int[][] ROUTE_4 = {

      { 9, 1 },
      { 9, 9 },
      { 1, 9 },
      { 1, 1 }
  };

  /*
   * Ruta irregular que combina calles exteriores
   * e interiores.
   *
   * Contiene giros a izquierda y derecha y atraviesa
   * diferentes sectores de la ciudad.
   */
  private static final int[][] ROUTE_5 = {

      { 1, 5 },
      { 1, 9 },

      { 3, 9 },
      { 3, 7 },

      { 5, 7 },
      { 5, 9 },

      { 9, 9 },
      { 9, 5 },

      { 7, 5 },
      { 7, 1 },

      { 3, 1 },
      { 3, 3 },

      { 5, 3 },
      { 5, 5 }
  };

  /*
   * Todas las rutas se agrupan para permitir
   * un tratamiento uniforme.
   */
  private static final int[][][] ROUTES = {

      ROUTE_1,
      ROUTE_2,
      ROUTE_3,
      ROUTE_4,
      ROUTE_5
  };

  /*
   * ============================================================
   * VELOCIDADES DE CRUCERO
   * ============================================================
   */

  private static final float[] CRUISE_SPEEDS = {

      7.0f,
      6.2f,
      7.8f,
      7.0f,
      6.6f
  };

  /*
   * ============================================================
   * COLORES
   * ============================================================
   *
   * Cada entrada contiene:
   *
   * { red, green, blue }
   */

  private static final float[][] VEHICLE_COLORS = {

      /*
       * Azul.
       */
      {
          0.12f,
          0.36f,
          0.82f
      },

      /*
       * Verde.
       */
      {
          0.16f,
          0.66f,
          0.28f
      },

      /*
       * Violeta.
       */
      {
          0.58f,
          0.20f,
          0.76f
      },

      /*
       * Rojo.
       */
      {
          0.82f,
          0.16f,
          0.12f
      },

      /*
       * Naranja.
       */
      {
          0.92f,
          0.42f,
          0.08f
      }
  };

  /*
   * ============================================================
   * VEHÍCULOS
   * ============================================================
   */

  private final TrafficVehicle[] vehicles = new TrafficVehicle[ROUTES.length];

  /*
   * ============================================================
   * RUTAS EN COORDENADAS DEL MUNDO
   * ============================================================
   *
   * Cada vehículo tiene su propio arreglo de puntos.
   *
   * routeX[vehiculo][waypoint]
   * routeZ[vehiculo][waypoint]
   */

  private final float[][] routeX = new float[ROUTES.length][];

  private final float[][] routeZ = new float[ROUTES.length][];

  /*
   * Cada vehículo mantiene independientemente
   * el waypoint que está siguiendo.
   */
  private final int[] targetWaypointIndex = new int[ROUTES.length];

  /*
   * ============================================================
   * CONSTRUCTOR
   * ============================================================
   */

  public AutonomousTrafficSystem(
      City city) {

    validateConfiguration();

    for (int i = 0; i < ROUTES.length; i++) {

      initializeVehicle(
          city,
          i);
    }

    reset();

    System.out.printf(
        "Trafico autonomo -> "
            + "%d vehiculos | "
            + "%d rutas%n",

        vehicles.length,
        ROUTES.length);
  }

  /*
   * ============================================================
   * VALIDACIÓN DE CONFIGURACIÓN
   * ============================================================
   */

  private void validateConfiguration() {

    if (ROUTES.length != CRUISE_SPEEDS.length) {

      throw new IllegalStateException(
          "La cantidad de rutas y velocidades "
              + "de trafico no coincide");
    }

    if (ROUTES.length != VEHICLE_COLORS.length) {

      throw new IllegalStateException(
          "La cantidad de rutas y colores "
              + "de trafico no coincide");
    }

    if (ROUTES.length < 3) {

      throw new IllegalStateException(
          "Se requieren al menos tres "
              + "vehiculos autonomos");
    }
  }

  /*
   * ============================================================
   * INICIALIZACIÓN DE VEHÍCULO
   * ============================================================
   */

  private void initializeVehicle(
      City city,
      int vehicleIndex) {

    int[][] route = ROUTES[vehicleIndex];

    initializeRoute(
        city,
        vehicleIndex,
        route);

    float spawnX = routeX[vehicleIndex][0];

    float spawnZ = routeZ[vehicleIndex][0];

    float initialAngle = calculateAngleToTarget(

        spawnX,
        spawnZ,

        routeX[vehicleIndex][1],

        routeZ[vehicleIndex][1]);

    float[] color = VEHICLE_COLORS[vehicleIndex];

    vehicles[vehicleIndex] = new TrafficVehicle(

        spawnX,
        spawnZ,

        initialAngle,

        CRUISE_SPEEDS[vehicleIndex],

        color[0],
        color[1],
        color[2]);

    TrafficVehicle vehicle = vehicles[vehicleIndex];

    if (!city.canDrive(
        vehicle.getX(),
        vehicle.getZ(),
        vehicle.getCollisionRadius())) {

      throw new IllegalStateException(
          "El vehiculo autonomo "
              + (vehicleIndex + 1)
              + " inicia fuera de una zona transitable");
    }

    System.out.printf(
        "Vehiculo autonomo %d -> "
            + "velocidad %.1f | "
            + "%d puntos de navegacion%n",

        vehicleIndex + 1,

        vehicle.getCruiseSpeed(),

        routeX[vehicleIndex].length);
  }

  /*
   * ============================================================
   * INICIALIZACIÓN DE RUTA
   * ============================================================
   */

  private void initializeRoute(
      City city,
      int routeIndex,
      int[][] route) {

    if (route.length < 2) {

      throw new IllegalStateException(
          "La ruta "
              + routeIndex
              + " requiere al menos dos waypoints");
    }

    float[] cornerX = new float[route.length];

    float[] cornerZ = new float[route.length];

    /*
     * Primero se convierten las esquinas originales
     * a coordenadas del mundo.
     */

    for (int i = 0; i < route.length; i++) {

      int row = route[i][0];

      int column = route[i][1];

      if (!city.isRoadCell(
          row,
          column)) {

        throw new IllegalStateException(
            "Waypoint de trafico fuera de una calle en ["
                + row
                + ", "
                + column
                + "]");
      }

      cornerX[i] = city.getCellCenterX(
          column);

      cornerZ[i] = city.getCellCenterZ(
          row);
    }

    /*
     * Se comprueban todos los tramos,
     * incluido el último hacia el primero.
     */

    for (int i = 0; i < route.length; i++) {

      int nextIndex = (i + 1)
          % route.length;

      validateSegment(
          city,

          route[i][0],
          route[i][1],

          route[nextIndex][0],
          route[nextIndex][1]);
    }

    /*
     * Dos puntos físicos por cada esquina:
     *
     * 1. salida de la esquina;
     * 2. aproximación a la siguiente.
     */

    routeX[routeIndex] = new float[route.length * 2];

    routeZ[routeIndex] = new float[route.length * 2];

    generateLaneRoute(
        routeIndex,
        cornerX,
        cornerZ);
  }

  /*
   * ============================================================
   * GENERACIÓN DEL CARRIL
   * ============================================================
   */

  private void generateLaneRoute(
      int routeIndex,
      float[] cornerX,
      float[] cornerZ) {

    int outputIndex = 0;

    for (int i = 0; i < cornerX.length; i++) {

      int nextIndex = (i + 1)
          % cornerX.length;

      float currentX = cornerX[i];

      float currentZ = cornerZ[i];

      float nextX = cornerX[nextIndex];

      float nextZ = cornerZ[nextIndex];

      /*
       * Debido a que todos los segmentos fueron
       * validados como horizontales o verticales,
       * las componentes serán -1, 0 o +1.
       */

      float directionX = Math.signum(
          nextX
              - currentX);

      float directionZ = Math.signum(
          nextZ
              - currentZ);

      /*
       * Vector derecho respecto a la dirección:
       *
       * right = (-directionZ, directionX)
       *
       * este -> sur
       * sur -> oeste
       * oeste -> norte
       * norte -> este
       */

      float rightX = -directionZ;

      float rightZ = directionX;

      /*
       * ====================================================
       * SALIDA DE LA ESQUINA
       * ====================================================
       */

      routeX[routeIndex][outputIndex] = currentX
          + directionX
              * CORNER_TRANSITION_DISTANCE
          + rightX
              * LANE_OFFSET;

      routeZ[routeIndex][outputIndex] = currentZ
          + directionZ
              * CORNER_TRANSITION_DISTANCE
          + rightZ
              * LANE_OFFSET;

      outputIndex++;

      /*
       * ====================================================
       * APROXIMACIÓN A LA SIGUIENTE ESQUINA
       * ====================================================
       */

      routeX[routeIndex][outputIndex] = nextX
          - directionX
              * CORNER_TRANSITION_DISTANCE
          + rightX
              * LANE_OFFSET;

      routeZ[routeIndex][outputIndex] = nextZ
          - directionZ
              * CORNER_TRANSITION_DISTANCE
          + rightZ
              * LANE_OFFSET;

      outputIndex++;
    }
  }

  /*
   * ============================================================
   * VALIDACIÓN DE SEGMENTOS
   * ============================================================
   */

  private void validateSegment(
      City city,

      int startRow,
      int startColumn,

      int endRow,
      int endColumn) {

    if (startRow == endRow
        && startColumn == endColumn) {

      throw new IllegalStateException(
          "La ruta contiene waypoints "
              + "consecutivos iguales");
    }

    boolean horizontal = startRow == endRow;

    boolean vertical = startColumn == endColumn;

    if (!horizontal
        && !vertical) {

      throw new IllegalStateException(
          "La ruta contiene un tramo diagonal entre ["
              + startRow
              + ", "
              + startColumn
              + "] y ["
              + endRow
              + ", "
              + endColumn
              + "]");
    }

    if (horizontal) {

      validateHorizontalSegment(
          city,
          startRow,
          startColumn,
          endColumn);

    } else {

      validateVerticalSegment(
          city,
          startColumn,
          startRow,
          endRow);
    }
  }

  private void validateHorizontalSegment(
      City city,
      int row,
      int startColumn,
      int endColumn) {

    int step = Integer.compare(
        endColumn,
        startColumn);

    int column = startColumn;

    while (true) {

      requireRoadCell(
          city,
          row,
          column);

      if (column == endColumn) {
        break;
      }

      column += step;
    }
  }

  private void validateVerticalSegment(
      City city,
      int column,
      int startRow,
      int endRow) {

    int step = Integer.compare(
        endRow,
        startRow);

    int row = startRow;

    while (true) {

      requireRoadCell(
          city,
          row,
          column);

      if (row == endRow) {
        break;
      }

      row += step;
    }
  }

  private void requireRoadCell(
      City city,
      int row,
      int column) {

    if (!city.isRoadCell(
        row,
        column)) {

      throw new IllegalStateException(
          "Una ruta de trafico atraviesa "
              + "una celda no transitable en ["
              + row
              + ", "
              + column
              + "]");
    }
  }

  /*
   * ============================================================
   * ACTUALIZACIÓN GENERAL
   * ============================================================
   */

  public void update(
      float deltaTime,
      City city) {

    for (int i = 0; i < vehicles.length; i++) {

      updateVehicle(
          i,
          deltaTime,
          city);
    }
  }

  /*
   * ============================================================
   * ACTUALIZACIÓN INDIVIDUAL
   * ============================================================
   */

  private void updateVehicle(
      int vehicleIndex,
      float deltaTime,
      City city) {

    TrafficVehicle vehicle = vehicles[vehicleIndex];

    int waypointIndex = targetWaypointIndex[vehicleIndex];

    float targetX = routeX[vehicleIndex][waypointIndex];

    float targetZ = routeZ[vehicleIndex][waypointIndex];

    float deltaX = targetX
        - vehicle.getX();

    float deltaZ = targetZ
        - vehicle.getZ();

    float distanceSquared = deltaX
        * deltaX
        + deltaZ
            * deltaZ;

    float waypointRadiusSquared = WAYPOINT_RADIUS
        * WAYPOINT_RADIUS;

    /*
     * Se cambia de waypoint al entrar
     * en el radio de aceptación.
     */
    if (distanceSquared <= waypointRadiusSquared) {

      advanceWaypoint(
          vehicleIndex);

      waypointIndex = targetWaypointIndex[vehicleIndex];

      targetX = routeX[vehicleIndex][waypointIndex];

      targetZ = routeZ[vehicleIndex][waypointIndex];

      deltaX = targetX
          - vehicle.getX();

      deltaZ = targetZ
          - vehicle.getZ();

      distanceSquared = deltaX
          * deltaX
          + deltaZ
              * deltaZ;
    }

    /*
     * Ángulo correspondiente al objetivo actual.
     */
    float desiredAngle = calculateAngleToTarget(

        vehicle.getX(),
        vehicle.getZ(),

        targetX,
        targetZ);

    float angleDifference = Math.abs(
        normalizeAngle(
            desiredAngle
                - vehicle.getAngle()));

    /*
     * ========================================================
     * VELOCIDAD
     * ========================================================
     */

    float desiredSpeed = vehicle.getCruiseSpeed();

    float slowdownDistanceSquared = SLOWDOWN_DISTANCE
        * SLOWDOWN_DISTANCE;

    boolean nearWaypoint = distanceSquared <= slowdownDistanceSquared;

    boolean turning = angleDifference > TURN_ANGLE_THRESHOLD;

    if (nearWaypoint
        || turning) {

      desiredSpeed = CORNER_SPEED;
    }

    vehicle.update(
        deltaTime,
        desiredAngle,
        desiredSpeed,
        city);
  }

  /*
   * ============================================================
   * CAMBIO DE WAYPOINT
   * ============================================================
   */

  private void advanceWaypoint(
      int vehicleIndex) {

    targetWaypointIndex[vehicleIndex] = (targetWaypointIndex[vehicleIndex]
        + 1)
        % routeX[vehicleIndex].length;
  }

  /*
   * ============================================================
   * ORIENTACIÓN
   * ============================================================
   */

  private float calculateAngleToTarget(
      float fromX,
      float fromZ,
      float targetX,
      float targetZ) {

    float deltaX = targetX
        - fromX;

    float deltaZ = targetZ
        - fromZ;

    /*
     * Conserva la convención:
     *
     * angle = 0
     * ↓
     * frente hacia -Z.
     */
    return (float) Math.atan2(
        -deltaX,
        -deltaZ);
  }

  /*
   * ============================================================
   * RENDER
   * ============================================================
   */

  public void render(
      Renderer renderer) {

    for (TrafficVehicle vehicle : vehicles) {

      vehicle.render(
          renderer);
    }
  }

  /*
   * ============================================================
   * RESET
   * ============================================================
   */

  public void reset() {

    for (int i = 0; i < vehicles.length; i++) {

      vehicles[i].reset();

      /*
       * El punto 0 corresponde al spawn.
       *
       * El primer objetivo es el punto 1.
       */
      targetWaypointIndex[i] = 1;
    }
  }

  /*
   * ============================================================
   * HELPERS
   * ============================================================
   */

  private float normalizeAngle(
      float value) {

    float fullTurn = (float) (Math.PI
        * 2.0);

    while (value > Math.PI) {

      value -= fullTurn;
    }

    while (value < -Math.PI) {

      value += fullTurn;
    }

    return value;
  }

  private void validateVehicleIndex(
      int vehicleIndex) {

    if (vehicleIndex < 0
        || vehicleIndex >= vehicles.length) {

      throw new IndexOutOfBoundsException(
          "Indice de vehiculo autonomo invalido: "
              + vehicleIndex);
    }
  }

  /*
   * ============================================================
   * CONSULTAS GENERALES
   * ============================================================
   */

  public int getVehicleCount() {

    return vehicles.length;
  }

  public float getVehicleCollisionRadius(
      int vehicleIndex) {

    validateVehicleIndex(
        vehicleIndex);

    return vehicles[vehicleIndex].getCollisionRadius();
  }

  /*
   * ============================================================
   * CONSULTAS POR VEHÍCULO
   * ============================================================
   */

  public float getVehicleX(
      int vehicleIndex) {

    validateVehicleIndex(
        vehicleIndex);

    return vehicles[vehicleIndex].getX();
  }

  public float getVehicleZ(
      int vehicleIndex) {

    validateVehicleIndex(
        vehicleIndex);

    return vehicles[vehicleIndex].getZ();
  }

  public float getVehicleAngle(
      int vehicleIndex) {

    validateVehicleIndex(
        vehicleIndex);

    return vehicles[vehicleIndex].getAngle();
  }

  public float getVehicleSpeed(
      int vehicleIndex) {

    validateVehicleIndex(
        vehicleIndex);

    return vehicles[vehicleIndex].getSpeed();
  }

  public int getCurrentWaypointNumber(
      int vehicleIndex) {

    validateVehicleIndex(
        vehicleIndex);

    return targetWaypointIndex[vehicleIndex]
        + 1;
  }

  public int getRoutePointCount(
      int vehicleIndex) {

    validateVehicleIndex(
        vehicleIndex);

    return routeX[vehicleIndex].length;
  }

  /*
   * ============================================================
   * CONSULTAS COMPATIBLES CON 25B
   * ============================================================
   *
   * Se mantienen para que cualquier diagnóstico
   * existente del primer vehículo siga compilando.
   */

  public float getVehicleX() {

    return getVehicleX(
        0);
  }

  public float getVehicleZ() {

    return getVehicleZ(
        0);
  }

  public float getVehicleAngle() {

    return getVehicleAngle(
        0);
  }

  public float getVehicleSpeed() {

    return getVehicleSpeed(
        0);
  }

  public int getCurrentWaypointNumber() {

    return getCurrentWaypointNumber(
        0);
  }
}