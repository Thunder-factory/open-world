package com.openworld.gameplay;

import com.openworld.render.Renderer;
import com.openworld.vehicle.Car;
import com.openworld.world.City;

public final class DeliveryManager {

    /*
     * ============================================================
     * CONFIGURACIÓN
     * ============================================================
     */

    private static final float ARRIVAL_RADIUS = 3.0f;

    private static final float MAX_DELIVERY_SPEED = 1.0f;

    /*
     * Cada destino se define mediante:
     *
     * { fila, columna }
     *
     * Las posiciones seleccionadas pertenecen a calles
     * y recorren distintas zonas de la ciudad.
     */
    private static final int[][] DESTINATION_CELLS = {

            {1, 1},
            {9, 9},
            {1, 9}
    };

    /*
     * ============================================================
     * DESTINOS EN COORDENADAS DEL MUNDO
     * ============================================================
     */

    private final float[] destinationX =
            new float[DESTINATION_CELLS.length];

    private final float[] destinationZ =
            new float[DESTINATION_CELLS.length];

    /*
     * ============================================================
     * ESTADO
     * ============================================================
     */

    private int currentDestinationIndex;

    private int completedDeliveries;

    private float elapsedTime;

    private boolean finished;


    public DeliveryManager(
            City city
    ) {

        initializeDestinations(
                city
        );

        reset();
    }

    /*
     * ============================================================
     * INICIALIZACIÓN
     * ============================================================
     */

    private void initializeDestinations(
            City city
    ) {

        for (int i = 0;
             i < DESTINATION_CELLS.length;
             i++) {

            int row =
                    DESTINATION_CELLS[i][0];

            int column =
                    DESTINATION_CELLS[i][1];


            if (!city.isRoadCell(
                    row,
                    column
            )) {

                throw new IllegalStateException(
                        "El destino "
                                + i
                                + " no se encuentra sobre una calle"
                );
            }

            destinationX[i] =
                    city.getCellCenterX(
                            column
                    );

            destinationZ[i] =
                    city.getCellCenterZ(
                            row
                    );

            System.out.printf(
                    "Destino %d -> "
                            + "celda [%d, %d] "
                            + "posición (%.1f, %.1f)%n",

                    i + 1,
                    row,
                    column,
                    destinationX[i],
                    destinationZ[i]
            );
        }
    }

    /*
     * ============================================================
     * ACTUALIZACIÓN
     * ============================================================
     */

    public void update(
            float deltaTime,
            Car car
    ) {

        if (finished) {
            return;
        }

        elapsedTime +=
                deltaTime;


        float targetX =
                destinationX[
                        currentDestinationIndex
                ];

        float targetZ =
                destinationZ[
                        currentDestinationIndex
                ];

        float deltaX =
                car.getX()
                        - targetX;

        float deltaZ =
                car.getZ()
                        - targetZ;

        float distanceSquared =
                deltaX * deltaX
                        + deltaZ * deltaZ;

        float arrivalRadiusSquared =
                ARRIVAL_RADIUS
                        * ARRIVAL_RADIUS;

        boolean insideDestination =
                distanceSquared
                        <= arrivalRadiusSquared;

        boolean slowEnough =
                Math.abs(
                        car.getSpeed()
                )
                        <= MAX_DELIVERY_SPEED;


        if (insideDestination
                && slowEnough) {

            completeCurrentDelivery();
        }
    }

    /*
     * ============================================================
     * FINALIZACIÓN DE ENTREGA
     * ============================================================
     */

    private void completeCurrentDelivery() {

        completedDeliveries++;


        System.out.printf(
                "Entrega %d/%d completada%n",
                completedDeliveries,
                getTotalDestinations()
        );

        if (completedDeliveries
                >= getTotalDestinations()) {

            finished =
                    true;

            currentDestinationIndex =
                    getTotalDestinations();


            System.out.printf(
                    "Recorrido completado en %.2f segundos%n",
                    elapsedTime
            );

            return;
        }

        currentDestinationIndex++;

        System.out.printf(
                "Nuevo destino -> %d%n",
                currentDestinationIndex + 1
        );
    }


    /*
     * ============================================================
     * RENDER
     * ============================================================
     */

    public void render(
            Renderer renderer
    ) {

        if (finished) {
            return;
        }


        float x =
                destinationX[
                        currentDestinationIndex
                ];

        float z =
                destinationZ[
                        currentDestinationIndex
                ];


        /*
         * Una superficie plana marca el área aproximada
         * donde se acepta la entrega.
         */
        renderer.box(
                x,
                0.075f,
                z,

                ARRIVAL_RADIUS * 2.0f,
                0.02f,
                ARRIVAL_RADIUS * 2.0f,

                0.95f,
                0.72f,
                0.08f
        );


        /*
         * Una baliza flotante facilita la identificación
         * del destino desde ambas cámaras.
         */
        float verticalOffset =
                (float) Math.sin(
                        elapsedTime * 3.0f
                )
                        * 0.25f;


        renderer.box(
                x,
                2.7f + verticalOffset,
                z,

                0.9f,
                0.9f,
                0.9f,

                1.0f,
                0.85f,
                0.15f
        );


        /*
         * Un segundo elemento aumenta la visibilidad
         * de la baliza desde largas distancias.
         */
        renderer.box(
                x,
                1.6f + verticalOffset,
                z,

                0.25f,
                2.0f,
                0.25f,

                1.0f,
                0.65f,
                0.05f
        );
    }


    /*
     * ============================================================
     * RESET
     * ============================================================
     */

    public void reset() {

        currentDestinationIndex =
                0;

        completedDeliveries =
                0;

        elapsedTime =
                0.0f;

        finished =
                false;
    }


    /*
     * ============================================================
     * CONSULTAS
     * ============================================================
     */

    public boolean hasActiveDestination() {

        return !finished;
    }


    public boolean isFinished() {

        return finished;
    }


    public int getCompletedDeliveries() {

        return completedDeliveries;
    }


    public int getTotalDestinations() {

        return destinationX.length;
    }


    public int getCurrentDestinationNumber() {

        if (finished) {
            return getTotalDestinations();
        }

        return currentDestinationIndex
                + 1;
    }


    public float getElapsedTime() {

        return elapsedTime;
    }


    public float getCurrentDestinationX() {

        ensureActiveDestination();

        return destinationX[
                currentDestinationIndex
        ];
    }


    public float getCurrentDestinationZ() {

        ensureActiveDestination();

        return destinationZ[
                currentDestinationIndex
        ];
    }


    public float getArrivalRadius() {

        return ARRIVAL_RADIUS;
    }


    private void ensureActiveDestination() {

        if (finished) {

            throw new IllegalStateException(
                    "No existe un destino activo"
            );
        }
    }
}