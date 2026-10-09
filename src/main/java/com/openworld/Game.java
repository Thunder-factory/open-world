package com.openworld;

import com.openworld.core.Input;
import com.openworld.core.Window;

import com.openworld.render.Camera;
import com.openworld.render.Renderer;

import com.openworld.vehicle.Car;
import com.openworld.world.City;
import com.openworld.gameplay.DeliveryManager;
import com.openworld.lighting.LightingSystem;
import com.openworld.world.TrafficLightSystem;
import com.openworld.render.Minimap;
import com.openworld.validation.LogicValidator;

import static org.lwjgl.glfw.GLFW.*;

public class Game {

  private static final float MAX_DELTA_TIME = 0.05f;

  private static final float TITLE_UPDATE_INTERVAL = 0.20f;

  private Window window;
  private Input input;

  private Renderer renderer;
  private Camera camera;

  private City city;
  private Car car;

  private DeliveryManager deliveryManager;
  private LightingSystem lighting;

  private TrafficLightSystem trafficLights;

  private Minimap minimap;

  private double previousTime;

  private float titleUpdateAccumulator;

  public void run() {

    try {

      init();
      loop();

    } finally {

      cleanup();
    }
  }

  private void init() {

    window = new Window(
        1280,
        720,
        "Open World");

    window.create();

    input = new Input(
        window.getHandle());

    renderer = new Renderer();

    city = new City();

    trafficLights = new TrafficLightSystem(
        city);

    lighting = new LightingSystem(city);

    /*
     * ========================================================
     * AUTO
     * ========================================================
     */

    float spawnX = city.getCellCenterX(5);

    float spawnZ = city.getCellCenterZ(5);

    car = new Car(
        spawnX,
        spawnZ,
        0.0f);

    if (!city.canDrive(
        car.getX(),
        car.getZ(),
        car.getCollisionRadius())) {
      throw new IllegalStateException(
          "La posicion inicial del vehiculo no es transitable");
    }

    lighting.update(car);

    deliveryManager = new DeliveryManager(city);

    camera = new Camera();
    /**
     * La posicion inicial se calcula a partir
     * del estado del vehiculo.
     */
    camera.setMode(Camera.Mode.FOLLOW);

    camera.update(car, city);

    /*
     * El minimapa mantiene su propio estado de visibilidad
     * y utiliza una vista superior independiente.
     */
    minimap = new Minimap();

    /*
     * Las comprobaciones se realizan después de inicializar
     * todos los sistemas que contienen lógica verificable.
     */
    LogicValidator.run(
        city,
        car,
        deliveryManager,
        lighting,
        trafficLights,
        camera,
        minimap);

    titleUpdateAccumulator = 0.0f;

    previousTime = glfwGetTime();

    refreshWindowTitle();

    System.out.println(
        "Open World iniciado correctamente");
  }

  private void loop() {

    while (!window.shouldClose()) {

      float deltaTime = calculateDeltaTime();

      input.update();

      processInput();

      update(
          deltaTime);

      render();

      window.update();
    }
  }

  private float calculateDeltaTime() {

    double currentTime = glfwGetTime();

    float deltaTime = (float) (currentTime
        - previousTime);

    previousTime = currentTime;

    return Math.min(
        deltaTime,
        MAX_DELTA_TIME);
  }

  private void processInput() {

    if (input.wasPressed(
        GLFW_KEY_ESCAPE)) {

      window.requestClose();
    }

    if (input.wasPressed(
        GLFW_KEY_C)) {

      System.out.println(
          "C -> cámara");
      camera.toggleMode();
      System.out.println("Camara ->  " + camera.getMode());
    }

    if (input.wasPressed(
        GLFW_KEY_N)) {

      lighting.toggleDayNight();
      System.out.println(
          "Iluminacion -> " + lighting.getModeName());
    }

    if (input.wasPressed(
        GLFW_KEY_F)) {
      lighting.toggleHeadlights();

      System.out.println(
          "Faros -> " + lighting.getHeadlightStateName());
    }

    if (input.wasPressed(
        GLFW_KEY_M)) {

      minimap.toggle();

      System.out.println(
          "Minimapa -> " + minimap.getStateName());
    }

    if (input.wasPressed(
        GLFW_KEY_R)) {

      resetGame();
    }
  }

  private void update(
      float deltaTime) {

    /*
     * ========================================================
     * ACELERADOR
     * ========================================================
     */

    boolean forward = input.isDown(GLFW_KEY_W)
        ||
        input.isDown(GLFW_KEY_UP);

    boolean backward = input.isDown(GLFW_KEY_S)
        ||
        input.isDown(GLFW_KEY_DOWN);

    float throttle = 0.0f;

    if (forward) {
      throttle += 1.0f;
    }

    if (backward) {
      throttle -= 1.0f;
    }

    /*
     * ========================================================
     * DIRECCIÓN
     * ========================================================
     */

    boolean left = input.isDown(GLFW_KEY_A)
        ||
        input.isDown(GLFW_KEY_LEFT);

    boolean right = input.isDown(GLFW_KEY_D)
        ||
        input.isDown(GLFW_KEY_RIGHT);

    float steering = 0.0f;

    if (left) {
      steering += 1.0f;
    }

    if (right) {
      steering -= 1.0f;
    }

    /*
     * ========================================================
     * FRENO
     * ========================================================
     */

    boolean brake = input.isDown(
        GLFW_KEY_SPACE);

    /*
     * ========================================================
     * AUTO + COLISIONES
     * ========================================================
     */

    car.update(
        deltaTime,
        throttle,
        steering,
        brake,
        city);

    /*
     * Las fuentes de luz delanteras se actualizan
     * después del movimiento del vehículo.
     */
    lighting.update(
        car);

    deliveryManager.update(deltaTime, car);

    /*
     * El ciclo de los semáforos utiliza
     * el tiempo transcurrido entre frames.
     */
    trafficLights.update(
        deltaTime);

    /**
     * La camara se actualiza despues del automovil
     * para utilizar su posicion y orientacion actuales.
     */
    camera.update(car, city);

    updateWindowTitle(deltaTime);

    /*
     * ========================================================
     * DEBUG
     * ========================================================
     */

    if (input.isDown(
        GLFW_KEY_T)) {

      boolean valid = city.canDrive(
          car.getX(),
          car.getZ(),
          car.getCollisionRadius());

      System.out.printf(
          "dt: %.5f | "
              + "speed: %.2f | "
              + "x: %.2f | "
              + "z: %.2f | "
              + "angle: %.1f° | "
              + "camera: %s | "
              + "light: %s | "
              + "headlights: %s | "
              + "minimap: %s | "
              + "traffic: %s | "
              + "delivery: %d/%d | "
              + "time: %.1f | "
              + "finished: %s | "
              + "valid: %s%n",

          deltaTime,
          car.getSpeed(),
          car.getX(),
          car.getZ(),

          Math.toDegrees(
              car.getAngle()),

          camera.getMode(),

          lighting.getModeName(),

          lighting.getHeadlightStateName(),

          minimap.getStateName(),

          trafficLights.getPhaseName(),

          deliveryManager.getCompletedDeliveries(),
          deliveryManager.getTotalDestinations(),

          deliveryManager.getElapsedTime(),

          deliveryManager.isFinished(),

          valid);
    }
  }

  private void render() {

    renderer.beginFrame(
        camera,
        window.getAspectRatio(),
        lighting);

    city.render(renderer, lighting.isNight());

    lighting.render(renderer);

    trafficLights.render(renderer);

    deliveryManager.render(renderer);

    car.render(renderer);

    /*
     * El minimapa se dibuja como un segundo pase
     * después de completar la vista principal.
     */

    minimap.render(
        renderer,
        city,
        car,
        deliveryManager,
        lighting,

        window.getFramebufferWidth(),
        window.getFramebufferHeight());

    renderer.endFrame();
  }

  private void cleanup() {

    if (renderer != null) {

      renderer.cleanup();

      renderer = null;
    }

    if (input != null) {

      input.cleanup();

      input = null;
    }

    if (window != null) {

      window.destroy();

      window = null;
    }

    System.out.println(
        "Open World finalizado");
  }

  /*
   * ============================================================
   * TÍTULO DE VENTANA
   * ============================================================
   */

  private void updateWindowTitle(
      float deltaTime) {

    titleUpdateAccumulator += deltaTime;

    if (titleUpdateAccumulator < TITLE_UPDATE_INTERVAL) {

      return;
    }

    titleUpdateAccumulator = 0.0f;

    refreshWindowTitle();
  }

  private void refreshWindowTitle() {

    String deliveryText;

    if (deliveryManager.isFinished()) {

      deliveryText = deliveryManager.getTotalDestinations()
          + "/"
          + deliveryManager.getTotalDestinations()
          + " COMPLETADO";

    } else {

      deliveryText = deliveryManager.getCompletedDeliveries()
          + "/"
          + deliveryManager.getTotalDestinations();
    }

    String title = String.format(
        "Open World"
            + " | Vel: %.1f"
            + " | Ent: %s"
            + " | Tiempo: %.1fs"
            + " | %s"
            + " | Faros: %s"
            + " | Cam: %s"
            + " | Mapa: %s",

        car.getSpeed(),

        deliveryText,

        deliveryManager.getElapsedTime(),

        lighting.getModeName(),

        lighting.getHeadlightStateName(),

        camera.getMode(),

        minimap.getStateName());

    window.setTitle(
        title);
  }

  private void resetGame() {

    /*
     * ========================================================
     * VEHÍCULO
     * ========================================================
     */

    car.reset();

    /*
     * ========================================================
     * ACTIVIDAD DE ENTREGAS
     * ========================================================
     */

    deliveryManager.reset();

    /*
     * ========================================================
     * SEMÁFOROS
     * ========================================================
     */

    trafficLights.reset();

    /*
     * ========================================================
     * ILUMINACIÓN
     * ========================================================
     *
     * Se restaura:
     *
     * DAY
     * faros OFF
     * posición de faros sincronizada con el auto
     */

    lighting.reset(
        car);

    /*
     * ========================================================
     * CÁMARA
     * ========================================================
     */

    camera.setMode(
        Camera.Mode.FOLLOW);

    camera.update(
        car,
        city);

    /*
     * ========================================================
     * MINIMAPA
     * ========================================================
     */

    minimap.reset();

    refreshWindowTitle();

    System.out.println(
        "Estado reiniciado");
  }

}