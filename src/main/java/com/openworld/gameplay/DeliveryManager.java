package com.openworld.gameplay;

import com.openworld.vehicle.Car;
import com.openworld.world.City;

import java.util.Random;

public final class DeliveryManager {

  private static final float ARRIVAL_RADIUS = 3.0f;
  private static final float MAX_DELIVERY_SPEED = 1.0f;
  private static final float MIN_DESTINATION_DISTANCE = 30.0f;
  private static final float DESTINATION_CLEARANCE = 0.35f;
  private static final int MAX_GENERATION_ATTEMPTS = 5000;

  private final City city;
  private final Random random = new Random();
  private final float spawnX;
  private final float spawnZ;
  private final float[] destinationX;
  private final float[] destinationZ;
  private int currentDestinationIndex;
  private int completedDeliveries;
  private float elapsedTime;
  private boolean finished;

  public DeliveryManager(City city, float spawnX, float spawnZ,
                         int destinationCount) {
    if (city == null) {
      throw new IllegalArgumentException("La ciudad no puede ser null");
    }
    if (destinationCount <= 0) {
      throw new IllegalArgumentException(
          "La cantidad de destinos debe ser mayor que 0");
    }
    if (!city.canDrive(spawnX, spawnZ, 0.0f)) {
      throw new IllegalArgumentException(
          "El punto inicial del vehiculo no es transitable");
    }

    this.city = city;
    this.spawnX = spawnX;
    this.spawnZ = spawnZ;
    destinationX = new float[destinationCount];
    destinationZ = new float[destinationCount];
    reset();
  }

  private void generateDestinations() {
    float referenceX = spawnX;
    float referenceZ = spawnZ;

    for (int i = 0; i < getTotalDestinations(); i++) {
      generateDestination(i, referenceX, referenceZ);
      referenceX = destinationX[i];
      referenceZ = destinationZ[i];
    }

    printGeneratedDestinations();
  }

  private void generateDestination(int destinationIndex,
                                   float referenceX,
                                   float referenceZ) {
    float halfWorldSize = city.getWorldSize() / 2.0f;
    float requiredRadius = getRequiredDestinationRadius();
    float minimumCoordinate = -halfWorldSize + requiredRadius;
    float maximumCoordinate = halfWorldSize - requiredRadius;

    for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
      float candidateX = randomRange(minimumCoordinate, maximumCoordinate);
      float candidateZ = randomRange(minimumCoordinate, maximumCoordinate);

      if (!hasMinimumDistance(referenceX, referenceZ,
          candidateX, candidateZ)) {
        continue;
      }
      if (!city.canDrive(candidateX, candidateZ, requiredRadius)) {
        continue;
      }

      destinationX[destinationIndex] = candidateX;
      destinationZ[destinationIndex] = candidateZ;
      return;
    }

    throw new IllegalStateException(
        "No se pudo generar el destino " + (destinationIndex + 1)
            + " despues de " + MAX_GENERATION_ATTEMPTS + " intentos");
  }

  private boolean hasMinimumDistance(float referenceX, float referenceZ,
                                     float candidateX, float candidateZ) {
    float deltaX = candidateX - referenceX;
    float deltaZ = candidateZ - referenceZ;
    return deltaX * deltaX + deltaZ * deltaZ
        >= MIN_DESTINATION_DISTANCE * MIN_DESTINATION_DISTANCE;
  }

  private float randomRange(float minimum, float maximum) {
    return minimum + random.nextFloat() * (maximum - minimum);
  }

  private void printGeneratedDestinations() {
    System.out.printf("Ruta de entregas generada: %d destinos%n",
        getTotalDestinations());
    float referenceX = spawnX;
    float referenceZ = spawnZ;
    for (int i = 0; i < getTotalDestinations(); i++) {
      float deltaX = destinationX[i] - referenceX;
      float deltaZ = destinationZ[i] - referenceZ;
      float distance = (float) Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
      System.out.printf("Destino %d/%d -> (%.2f, %.2f)"
              + " | distancia desde referencia: %.2f%n",
          i + 1, getTotalDestinations(), destinationX[i], destinationZ[i],
          distance);
      referenceX = destinationX[i];
      referenceZ = destinationZ[i];
    }
  }

  public void update(float deltaTime, Car car) {
    if (finished) {
      return;
    }
    if (car == null) {
      throw new IllegalArgumentException("El vehiculo no puede ser null");
    }
    if (deltaTime > 0.0f) {
      elapsedTime += deltaTime;
    }

    float deltaX = car.getX() - destinationX[currentDestinationIndex];
    float deltaZ = car.getZ() - destinationZ[currentDestinationIndex];
    float distanceSquared = deltaX * deltaX + deltaZ * deltaZ;
    boolean insideDestination = distanceSquared
        <= ARRIVAL_RADIUS * ARRIVAL_RADIUS;
    boolean slowEnough = Math.abs(car.getSpeed()) <= MAX_DELIVERY_SPEED;
    if (insideDestination && slowEnough) {
      completeCurrentDelivery();
    }
  }

  private void completeCurrentDelivery() {
    completedDeliveries++;
    System.out.printf("Entrega %d/%d completada%n",
        completedDeliveries, getTotalDestinations());
    if (completedDeliveries >= getTotalDestinations()) {
      finished = true;
      currentDestinationIndex = getTotalDestinations();
      System.out.printf("Recorrido completado en %.2f segundos%n", elapsedTime);
      return;
    }
    currentDestinationIndex++;
    System.out.printf("Nuevo destino -> %d/%d%n",
        currentDestinationIndex + 1, getTotalDestinations());
  }

  public void reset() {
    generateDestinations();
    currentDestinationIndex = 0;
    completedDeliveries = 0;
    elapsedTime = 0.0f;
    finished = false;
  }

  public boolean hasActiveDestination() { return !finished; }

  public boolean isFinished() { return finished; }

  public int getCompletedDeliveries() { return completedDeliveries; }

  public int getTotalDestinations() { return destinationX.length; }

  public int getCurrentDestinationNumber() {
    return finished ? getTotalDestinations() : currentDestinationIndex + 1;
  }

  public float getElapsedTime() { return elapsedTime; }

  public float getCurrentDestinationX() {
    ensureActiveDestination();
    return destinationX[currentDestinationIndex];
  }

  public float getCurrentDestinationZ() {
    ensureActiveDestination();
    return destinationZ[currentDestinationIndex];
  }

  public float getDestinationX(int index) {
    validateDestinationIndex(index);
    return destinationX[index];
  }

  public float getDestinationZ(int index) {
    validateDestinationIndex(index);
    return destinationZ[index];
  }

  public float getArrivalRadius() { return ARRIVAL_RADIUS; }

  public float getMinimumDestinationDistance() {
    return MIN_DESTINATION_DISTANCE;
  }

  public float getRequiredDestinationRadius() {
    return ARRIVAL_RADIUS + DESTINATION_CLEARANCE;
  }

  public float getSpawnX() { return spawnX; }

  public float getSpawnZ() { return spawnZ; }

  private void ensureActiveDestination() {
    if (finished) {
      throw new IllegalStateException("No existe un destino activo");
    }
  }

  private void validateDestinationIndex(int index) {
    if (index < 0 || index >= getTotalDestinations()) {
      throw new IndexOutOfBoundsException(
          "Indice de destino invalido: " + index);
    }
  }
}
