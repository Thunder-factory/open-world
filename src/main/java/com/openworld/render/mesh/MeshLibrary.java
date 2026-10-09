package com.openworld.render.mesh;

public final class MeshLibrary {

  private static final int CYLINDER_SEGMENTS = 12;

  private static final int SPHERE_LATITUDE_SEGMENTS = 8;

  private static final int SPHERE_LONGITUDE_SEGMENTS = 12;

  private static final int CONE_SEGMENTS = 12;

  private static final float WINDOW_FRAME_BORDER_RATIO = 0.12f;

  private final Mesh cube;

  private final Mesh quad;

  private final Mesh cylinder;

  private final Mesh sphere;

  private final Mesh cone;

  private final Mesh windowFrame;

  private final Mesh vehicleBody;

  private final Mesh vehicleGlass;

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

    vehicleBody = new Mesh(
        MeshFactory.createVehicleBody());

    vehicleGlass = new Mesh(
        MeshFactory.createVehicleGlass());
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

  public Mesh getVehicleBody() {

    return vehicleBody;
  }

  public Mesh getVehicleGlass() {

    return vehicleGlass;
  }

  public void cleanup() {

    vehicleGlass.cleanup();
    vehicleBody.cleanup();
    cone.cleanup();
    sphere.cleanup();
    cylinder.cleanup();
    windowFrame.cleanup();
    quad.cleanup();
    cube.cleanup();
  }
}
