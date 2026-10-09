package com.openworld.validation;

import com.openworld.gameplay.DeliveryManager;
import com.openworld.lighting.LightingSystem;
import com.openworld.render.Camera;
import com.openworld.render.Minimap;
import com.openworld.vehicle.Car;
import com.openworld.world.City;
import com.openworld.world.TrafficLightSystem;

import java.util.ArrayDeque;
import java.util.Queue;

public final class LogicValidator {

  private static int passedChecks;

  private LogicValidator() {

  }

  public static void run(
      City city,
      Car car,
      DeliveryManager deliveryManager,
      LightingSystem lighting,
      TrafficLightSystem trafficLights,
      Camera camera,
      Minimap minimap) {

    passedChecks = 0;

    System.out.println(
        "----------------------------------------");

    System.out.println(
        "Iniciando validaciones logicas");

    validateCityDimensions(
        city);

    validateCellCounts(
        city);

    validateRoadConnectivity(
        city);

    validateCollisionMap(
        city);

    validateCarSpawn(
        city,
        car);

    validateCarMovementAndReset(
        city,
        car);

    validateDeliveries(
        city,
        deliveryManager);

    validateLighting(
        lighting);

    validateTrafficLights(
        trafficLights);

    validateCamera(
        camera);

    validateMinimap(
        minimap);

    System.out.printf(
        "Validaciones logicas: %d OK%n",
        passedChecks);

    System.out.println(
        "----------------------------------------");
  }

  /*
   * ============================================================
   * CIUDAD
   * ============================================================
   */

  private static void validateCityDimensions(
      City city) {

    require(
        city.getRows() >= 11,
        "La ciudad debe tener al menos 11 filas");

    require(
        city.getColumns() >= 11,
        "La ciudad debe tener al menos 11 columnas");

    require(
        city.getWorldSize() > 0.0f,
        "El tamaño del mundo debe ser positivo");

    pass(
        "Dimensiones de ciudad");
  }

  private static void validateCellCounts(
      City city) {

    int buildings = 0;

    int parks = 0;

    int roads = 0;

    for (int row = 0; row < city.getRows(); row++) {

      for (int column = 0; column < city.getColumns(); column++) {

        int cell = city.getCell(
            row,
            column);

        switch (cell) {

          case City.BUILDING ->
            buildings++;

          case City.PARK ->
            parks++;

          case City.ROAD ->
            roads++;

          default ->
            throw new IllegalStateException(
                "Tipo de celda invalido");
        }
      }
    }

    require(
        buildings >= 12, // 12,
        "Se requieren al menos 12 edificios");

    require(
        parks >= 4,
        "Se requieren al menos 4 parques");

    require(
        roads > 0,
        "La ciudad debe contener calles");

    pass(
        "Cantidad de edificios, parques y calles");
  }

  /*
   * ============================================================
   * CONECTIVIDAD DE CALLES
   * ============================================================
   */

  private static void validateRoadConnectivity(
      City city) {

    int rows = city.getRows();

    int columns = city.getColumns();

    boolean[][] visited = new boolean[rows][columns];

    int startRow = -1;

    int startColumn = -1;

    int totalRoadCells = 0;

    for (int row = 0; row < rows; row++) {

      for (int column = 0; column < columns; column++) {

        if (!city.isRoadCell(
            row,
            column)) {

          continue;
        }

        totalRoadCells++;

        if (startRow == -1) {

          startRow = row;

          startColumn = column;
        }
      }
    }

    require(
        startRow != -1,
        "No existe ninguna calle en la ciudad");

    Queue<int[]> queue = new ArrayDeque<>();

    queue.add(
        new int[] {
            startRow,
            startColumn
        });

    visited[startRow][startColumn] = true;

    int visitedRoadCells = 0;

    int[][] directions = {

        { -1, 0 },
        { 1, 0 },
        { 0, -1 },
        { 0, 1 }
    };

    while (!queue.isEmpty()) {

      int[] current = queue.remove();

      int row = current[0];

      int column = current[1];

      visitedRoadCells++;

      for (int[] direction : directions) {

        int nextRow = row + direction[0];

        int nextColumn = column + direction[1];

        if (!city.isValidCell(
            nextRow,
            nextColumn)) {

          continue;
        }

        if (visited[nextRow][nextColumn]) {
          continue;
        }

        if (!city.isRoadCell(
            nextRow,
            nextColumn)) {

          continue;
        }

        visited[nextRow][nextColumn] = true;

        queue.add(
            new int[] {
                nextRow,
                nextColumn
            });
      }
    }

    require(
        visitedRoadCells == totalRoadCells,

        "La red vial contiene calles desconectadas");

    pass(
        "Conectividad completa de calles");
  }

  /*
   * ============================================================
   * COLISIONES
   * ============================================================
   */

  private static void validateCollisionMap(
      City city) {

    boolean foundRoad = false;

    boolean foundBlockedCell = false;

    for (int row = 0; row < city.getRows(); row++) {

      for (int column = 0; column < city.getColumns(); column++) {

        float x = city.getCellCenterX(
            column);

        float z = city.getCellCenterZ(
            row);

        if (city.isRoadCell(
            row,
            column)) {

          require(
              city.canDrive(
                  x,
                  z,
                  0.1f),

              "El centro de una calle no es transitable");

          foundRoad = true;

        } else {

          require(
              !city.canDrive(
                  x,
                  z,
                  0.1f),

              "Una celda bloqueada permite circulacion");

          foundBlockedCell = true;
        }
      }
    }

    require(
        foundRoad,
        "No se encontraron calles transitables");

    require(
        foundBlockedCell,
        "No se encontraron celdas bloqueadas");

    float outside = city.getHalfSize()
        + 10.0f;

    require(
        !city.canDrive(
            outside,
            outside,
            0.1f),

        "Se permite circular fuera de los limites");

    pass(
        "Mapa de colisiones");
  }

  /*
   * ============================================================
   * VEHÍCULO
   * ============================================================
   */

  private static void validateCarSpawn(
      City city,
      Car car) {

    require(
        city.canDrive(
            car.getX(),
            car.getZ(),
            car.getCollisionRadius()),

        "El spawn del vehiculo no es transitable");

    require(
        Math.abs(
            car.getSpeed()) < 0.0001f,

        "El vehiculo debe iniciar detenido");

    pass(
        "Spawn del vehiculo");
  }

  private static void validateCarMovementAndReset(
      City city,
      Car liveCar) {

    float spawnX = liveCar.getX();

    float spawnZ = liveCar.getZ();

    float spawnAngle = liveCar.getAngle();

    Car testCar = new Car(
        spawnX,
        spawnZ,
        spawnAngle);

    testCar.update(
        0.10f,
        1.0f,
        0.0f,
        false,
        city);

    require(
        testCar.getSpeed() > 0.0f,
        "El vehiculo no acelera hacia delante");

    require(
        testCar.getZ() < spawnZ,

        "angle = 0 debe avanzar hacia -Z");

    require(
        Math.abs(
            testCar.getX()
                - spawnX) < 0.001f,

        "El avance recto modifica incorrectamente X");

    testCar.reset();

    require(
        Math.abs(
            testCar.getX()
                - spawnX) < 0.001f,

        "El reset no restaura X");

    require(
        Math.abs(
            testCar.getZ()
                - spawnZ) < 0.001f,

        "El reset no restaura Z");

    require(
        Math.abs(
            testCar.getAngle()
                - spawnAngle) < 0.001f,

        "El reset no restaura el angulo");

    require(
        Math.abs(
            testCar.getSpeed()) < 0.001f,

        "El reset no restaura la velocidad");

    pass(
        "Movimiento y reset del vehiculo");
  }

  /*
   * ============================================================
   * ENTREGAS
   * ============================================================
   */

  private static void validateDeliveries(
      City city,
      DeliveryManager deliveryManager) {

    require(
        deliveryManager.getTotalDestinations() > 0,

        "Debe existir al menos un destino");

    require(
        deliveryManager.getCompletedDeliveries() == 0,

        "Las entregas deben comenzar en 0");

    require(
        !deliveryManager.isFinished(),
        "La actividad no debe iniciar terminada");

    require(
        deliveryManager.hasActiveDestination(),
        "Debe existir un destino activo");

    require(
        Math.abs(
            deliveryManager.getElapsedTime()) < 0.001f,

        "El tiempo inicial debe ser 0");

    float destinationX = deliveryManager.getCurrentDestinationX();

    float destinationZ = deliveryManager.getCurrentDestinationZ();

    require(
        city.canDrive(
            destinationX,
            destinationZ,
            0.1f),

        "El destino activo no esta sobre una zona transitable");

    pass(
        "Estado inicial de entregas");
  }

  /*
   * ============================================================
   * ILUMINACIÓN
   * ============================================================
   */

  private static void validateLighting(
      LightingSystem lighting) {

    require(
        lighting.getLampCount() >= 9,
        "Se requieren al menos 9 farolas");

    require(
        !lighting.isNight(),
        "La iluminacion debe iniciar en DAY");

    require(
        !lighting.areHeadlightsOn(),
        "Los faros deben iniciar apagados");

    pass(
        "Iluminacion y farolas");
  }

  /*
   * ============================================================
   * SEMÁFOROS
   * ============================================================
   */

  private static void validateTrafficLights(
      TrafficLightSystem trafficLights) {

    trafficLights.reset();

    require(
        "NS_GREEN".equals(
            trafficLights.getPhaseName()),

        "El ciclo debe iniciar en NS_GREEN");

    trafficLights.update(
        5.0f);

    require(
        "NS_YELLOW".equals(
            trafficLights.getPhaseName()),

        "NS_GREEN debe avanzar a NS_YELLOW");

    trafficLights.update(
        1.5f);

    require(
        "EW_GREEN".equals(
            trafficLights.getPhaseName()),

        "NS_YELLOW debe avanzar a EW_GREEN");

    /*
     * El estado inicial se restaura después
     * de realizar la comprobación.
     */
    trafficLights.reset();

    require(
        "NS_GREEN".equals(
            trafficLights.getPhaseName()),

        "El reset del semaforo debe restaurar NS_GREEN");

    pass(
        "Ciclo de semaforos");
  }

  /*
   * ============================================================
   * CÁMARA
   * ============================================================
   */

  private static void validateCamera(
      Camera camera) {

    require(
        camera.getMode() == Camera.Mode.FOLLOW,

        "La camara debe iniciar en FOLLOW");

    pass(
        "Estado inicial de camara");
  }

  /*
   * ============================================================
   * MINIMAPA
   * ============================================================
   */

  private static void validateMinimap(
      Minimap minimap) {

    require(
        minimap.isVisible(),
        "El minimapa debe iniciar visible");

    pass(
        "Estado inicial del minimapa");
  }

  /*
   * ============================================================
   * RESULTADOS
   * ============================================================
   */

  private static void require(
      boolean condition,
      String message) {

    if (!condition) {

      throw new IllegalStateException(
          "Validacion logica fallida: "
              + message);
    }
  }

  private static void pass(
      String name) {

    passedChecks++;

    System.out.println(
        "[OK] "
            + name);
  }
}
