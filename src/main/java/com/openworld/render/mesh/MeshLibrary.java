package com.openworld.render.mesh;

public final class MeshLibrary {

  private static final int CYLINDER_SEGMENTS = 12;

  private static final int SPHERE_LATITUDE_SEGMENTS = 8;

  private static final int SPHERE_LONGITUDE_SEGMENTS = 12;

  private static final int CONE_SEGMENTS = 12;

  private static final int CIRCLE_SEGMENTS = 24;

  private static final int RING_SEGMENTS = 48;

  private static final float RING_INNER_RADIUS_RATIO = 0.84f;

  private static final float WINDOW_FRAME_BORDER_RATIO = 0.12f;

  private final Mesh cube;

  private final Mesh quad;

  private final Mesh cylinder;

  private final Mesh sphere;

  private final Mesh cone;

  private final Mesh windowFrame;

  private final Mesh circle;

  private final Mesh ring;

  private final Mesh vehicleBody;

  private final Mesh vehicleGlassTrim;

  private final Mesh vehicleGlass;

  private final Mesh vehicleFrontLights;

  private final Mesh vehicleGrille;

  private final Mesh vehicleRearLights;

  private final Mesh vehicleRearPlate;

  public MeshLibrary() {

    cube = new Mesh(
        MeshFactory.createCube());

    quad = new Mesh(
        MeshFactory.createQuad());

    cylinder = new Mesh(
        MeshFactory.createCylinder(
            CYLINDER_SEGMENTS));

    sphere = new Mesh(
        MeshFactory.createSphere(
            SPHERE_LATITUDE_SEGMENTS,
            SPHERE_LONGITUDE_SEGMENTS));

    cone = new Mesh(
        MeshFactory.createCone(
            CONE_SEGMENTS));

    windowFrame = new Mesh(
        MeshFactory.createWindowFrame(
            WINDOW_FRAME_BORDER_RATIO));

    circle = new Mesh(
        MeshFactory.createCircle(CIRCLE_SEGMENTS));

    ring = new Mesh(
        MeshFactory.createRing(RING_SEGMENTS,
            RING_INNER_RADIUS_RATIO));

    vehicleBody = new Mesh(
        MeshFactory.createVehicleBody());

    vehicleGlassTrim = new Mesh(
        MeshFactory.createVehicleGlassTrim());

    vehicleGlass = new Mesh(
        MeshFactory.createVehicleGlass());

    vehicleFrontLights = new Mesh(
        MeshFactory.createVehicleFrontLights());

    vehicleGrille = new Mesh(
        MeshFactory.createVehicleGrille());

    vehicleRearLights = new Mesh(
        MeshFactory.createVehicleRearLights());

    vehicleRearPlate = new Mesh(
        MeshFactory.createVehicleRearPlate());
  }

  public Mesh getCube() {

    return cube;
  }

  public Mesh getQuad() {

    return quad;
  }

  public Mesh getCylinder() {

    return cylinder;
  }

  public Mesh getSphere() {

    return sphere;
  }

  public Mesh getCone() {

    return cone;
  }

  public Mesh getWindowFrame() {

    return windowFrame;
  }

  public Mesh getCircle() {

    return circle;
  }

  public Mesh getRing() {

    return ring;
  }

  public Mesh getVehicleBody() {

    return vehicleBody;
  }

  public Mesh getVehicleGlassTrim() {

    return vehicleGlassTrim;
  }

  public Mesh getVehicleGlass() {

    return vehicleGlass;
  }

  public Mesh getVehicleFrontLights() {

    return vehicleFrontLights;
  }

  public Mesh getVehicleGrille() {

    return vehicleGrille;
  }

  public Mesh getVehicleRearLights() {

    return vehicleRearLights;
  }

  public Mesh getVehicleRearPlate() {

    return vehicleRearPlate;
  }

  public void cleanup() {

    vehicleRearPlate.cleanup();
    vehicleRearLights.cleanup();
    vehicleGrille.cleanup();
    vehicleFrontLights.cleanup();
    vehicleGlass.cleanup();
    vehicleGlassTrim.cleanup();
    vehicleBody.cleanup();
    cone.cleanup();
    sphere.cleanup();
    cylinder.cleanup();
    circle.cleanup();
    ring.cleanup();
    windowFrame.cleanup();
    quad.cleanup();
    cube.cleanup();
  }
}
