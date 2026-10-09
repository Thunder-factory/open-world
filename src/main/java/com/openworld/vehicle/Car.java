package com.openworld.vehicle;

import com.openworld.render.Renderer;
import com.openworld.render.Material;
import com.openworld.traffic.AutonomousTrafficSystem;
import com.openworld.visual.VehicleVisual;
import com.openworld.world.City;

public final class Car {

  private static final float MAX_FORWARD_SPEED = 22.0f;
  private static final float MAX_REVERSE_SPEED = -8.0f;
  private static final float FORWARD_ACCELERATION = 12.0f;
  private static final float REVERSE_ACCELERATION = 8.0f;
  private static final float BRAKE_DECELERATION = 20.0f;
  private static final float ROLLING_RESISTANCE = 4.0f;
  private static final float TURN_SPEED = (float) Math.toRadians(100.0);
  private static final float STEERING_REFERENCE_SPEED = 7.0f;
  private static final float COLLISION_RADIUS = 1.4f;

  private static final float VISUAL_SCALE = 1.0f;
    private static final Material BODY_MATERIAL =
            new Material(0.88f, 0.20f, 0.08f, 0.0f, 0.65f, 48.0f);
  private static final VehicleVisual VEHICLE_VISUAL = new VehicleVisual();

  private final float spawnX;
  private final float spawnZ;
  private final float spawnAngle;
  private float x;
  private float z;
  private float angle;
  private float speed;

  public Car(float spawnX, float spawnZ, float spawnAngle) {
    this.spawnX = spawnX;
    this.spawnZ = spawnZ;
    this.spawnAngle = spawnAngle;
    reset();
  }

  public void update(float deltaTime, float throttle, float steering,
      boolean brake, City city,
      AutonomousTrafficSystem autonomousTraffic) {
    throttle = clamp(throttle, -1.0f, 1.0f);
    steering = clamp(steering, -1.0f, 1.0f);

    if (brake) {
      speed = moveTowardZero(speed, BRAKE_DECELERATION * deltaTime);
    } else if (throttle > 0.0f) {
      if (speed < 0.0f) {
        speed = moveTowardZero(speed, BRAKE_DECELERATION * deltaTime);
      } else {
        speed += FORWARD_ACCELERATION * throttle * deltaTime;
      }
    } else if (throttle < 0.0f) {
      if (speed > 0.0f) {
        speed = moveTowardZero(speed, BRAKE_DECELERATION * deltaTime);
      } else {
        speed -= REVERSE_ACCELERATION * (-throttle) * deltaTime;
      }
    } else {
      speed = moveTowardZero(speed, ROLLING_RESISTANCE * deltaTime);
    }

    speed = clamp(speed, MAX_REVERSE_SPEED, MAX_FORWARD_SPEED);

    if (Math.abs(speed) > 0.05f && Math.abs(steering) > 0.001f) {
      float steeringFactor = Math.min(
          Math.abs(speed) / STEERING_REFERENCE_SPEED, 1.0f);
      angle = normalizeAngle(angle + steering * TURN_SPEED
          * steeringFactor * Math.signum(speed) * deltaTime);
    }

    float movementX = -(float) Math.sin(angle) * speed * deltaTime;
    float movementZ = -(float) Math.cos(angle) * speed * deltaTime;
    boolean blockedX = false;
    boolean blockedZ = false;

    if (Math.abs(movementX) > 0.00001f) {
      float candidateX = x + movementX;
      if (canDrive(city, autonomousTraffic, candidateX, z)) {
        x = candidateX;
      } else {
        blockedX = true;
      }
    }

    if (Math.abs(movementZ) > 0.00001f) {
      float candidateZ = z + movementZ;
      if (canDrive(city, autonomousTraffic, x, candidateZ)) {
        z = candidateZ;
      } else {
        blockedZ = true;
      }
    }

    if (blockedX && blockedZ) {
      speed = moveTowardZero(speed, BRAKE_DECELERATION * deltaTime);
    }
  }

  private boolean canDrive(City city,
      AutonomousTrafficSystem autonomousTraffic,
      float candidateX, float candidateZ) {
    return city.canDrive(candidateX, candidateZ, COLLISION_RADIUS)
        && (autonomousTraffic == null
            || autonomousTraffic.canDrive(
                candidateX, candidateZ, COLLISION_RADIUS));
  }

  public void update(float deltaTime, float throttle, float steering,
      boolean brake, City city) {
    update(deltaTime, throttle, steering, brake, city, null);
  }

  public void render(Renderer renderer) {
        VEHICLE_VISUAL.render(renderer, x, z, angle, VISUAL_SCALE,
                BODY_MATERIAL);
  }

  public void reset() {
    x = spawnX;
    z = spawnZ;
    angle = spawnAngle;
    speed = 0.0f;
  }

  private float moveTowardZero(float value, float amount) {
    if (value > 0.0f) {
      return Math.max(0.0f, value - amount);
    }
    if (value < 0.0f) {
      return Math.min(0.0f, value + amount);
    }
    return 0.0f;
  }

  private float normalizeAngle(float value) {
    float fullTurn = (float) (Math.PI * 2.0);
    while (value > Math.PI) {
      value -= fullTurn;
    }
    while (value < -Math.PI) {
      value += fullTurn;
    }
    return value;
  }

  private float clamp(float value, float minimum, float maximum) {
    return Math.max(minimum, Math.min(maximum, value));
  }

  public float getX() {
    return x;
  }

  public float getZ() {
    return z;
  }

  public float getAngle() {
    return angle;
  }

  public float getSpeed() {
    return speed;
  }

  public float getCollisionRadius() {
    return COLLISION_RADIUS;
  }
}
