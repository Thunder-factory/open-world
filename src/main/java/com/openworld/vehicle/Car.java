package com.openworld.vehicle;

import com.openworld.render.Renderer;
import com.openworld.world.City;

public final class Car {

    /*
     * ============================================================
     * MOVIMIENTO
     * ============================================================
     */

    private static final float MAX_FORWARD_SPEED =
            22.0f;

    private static final float MAX_REVERSE_SPEED =
            -8.0f;

    private static final float FORWARD_ACCELERATION =
            12.0f;

    private static final float REVERSE_ACCELERATION =
            8.0f;

    private static final float BRAKE_DECELERATION =
            20.0f;

    private static final float ROLLING_RESISTANCE =
            4.0f;


    /*
     * Velocidad de giro máxima.
     *
     * Internamente se utilizan radianes.
     */
    private static final float TURN_SPEED =
            (float) Math.toRadians(100.0);


    /*
     * A esta velocidad ya se permite
     * prácticamente todo el giro disponible.
     */
    private static final float STEERING_REFERENCE_SPEED =
            7.0f;


    /**
     *
     * La colisión será deliberadamente
     * simplificada mediante un círculo.
     */
    private static final float COLLISION_RADIUS =
            1.4f;


    /*
     * ============================================================
     * SPAWN
     * ============================================================
     */

    private final float spawnX;
    private final float spawnZ;
    private final float spawnAngle;


    /*
     * ============================================================
     * ESTADO ACTUAL
     * ============================================================
     */

    private float x;
    private float z;

    /*
     * Ángulo en radianes.
     *
     * Convención:
     *
     * angle = 0
     *     ↓
     * frente hacia -Z
     *
     * ángulo positivo
     *     ↓
     * gira hacia -X
     */
    private float angle;

    /*
     * unidades del mundo / segundo
     *
     * positivo → avance
     * negativo → reversa
     */
    private float speed;


    public Car(
            float spawnX,
            float spawnZ,
            float spawnAngle
    ) {

        this.spawnX =
                spawnX;

        this.spawnZ =
                spawnZ;

        this.spawnAngle =
                spawnAngle;

        reset();
    }


    /*
     * ============================================================
     * UPDATE
     * ============================================================
     */

    public void update(
            float deltaTime,
            float throttle,
            float steering,
            boolean brake,
            City city
    ) {

        /*
         * Protección por si posteriormente
         * se recibe valores fuera de rango.
         */
        throttle =
                clamp(
                        throttle,
                        -1.0f,
                        1.0f
                );

        steering =
                clamp(
                        steering,
                        -1.0f,
                        1.0f
                );


        /*
         * ========================================================
         * VELOCIDAD
         * ========================================================
         */

        if (brake) {

            /*
             * SPACE:
             *
             * frenado fuerte independientemente
             * de la dirección.
             */
            speed =
                    moveTowardZero(
                            speed,
                            BRAKE_DECELERATION
                                    * deltaTime
                    );

        } else if (throttle > 0.0f) {

            /*
             * El vehículo debe avanzar.
             *
             * Si veníamos en reversa,
             * primero frenamos hasta 0.
             */
            if (speed < 0.0f) {

                speed =
                        moveTowardZero(
                                speed,
                                BRAKE_DECELERATION
                                        * deltaTime
                        );

            } else {

                speed +=
                        FORWARD_ACCELERATION
                                * throttle
                                * deltaTime;
            }

        } else if (throttle < 0.0f) {

            /*
             * El vehículo debe retroceder.
             *
             * Si veníamos avanzando,
             * primero frenamos hasta 0.
             */
            if (speed > 0.0f) {

                speed =
                        moveTowardZero(
                                speed,
                                BRAKE_DECELERATION
                                        * deltaTime
                        );

            } else {

                speed -=
                        REVERSE_ACCELERATION
                                * (-throttle)
                                * deltaTime;
            }

        } else {

            /*
             * Sin acelerador:
             *
             * resistencia natural.
             */
            speed =
                    moveTowardZero(
                            speed,
                            ROLLING_RESISTANCE
                                    * deltaTime
                    );
        }


        /*
         * Límites.
         */
        speed =
                clamp(
                        speed,
                        MAX_REVERSE_SPEED,
                        MAX_FORWARD_SPEED
                );


        /*
         * ========================================================
         * GIRO
         * ========================================================
         *
         * No se permite que el auto rote sobre su propio eje
         * estando completamente detenido.
         */

        if (Math.abs(speed) > 0.05f
                && Math.abs(steering) > 0.001f) {

            float steeringFactor =
                    Math.min(
                            Math.abs(speed)
                                    / STEERING_REFERENCE_SPEED,
                            1.0f
                    );


            /*
             * En reversa el efecto del giro
             * se invierte naturalmente.
             */
            float direction =
                    Math.signum(speed);


            angle +=
                    steering
                            * TURN_SPEED
                            * steeringFactor
                            * direction
                            * deltaTime;


            angle =
                    normalizeAngle(angle);
        }


        /*
         * ========================================================
         * MOVIMIENTO
         * ========================================================
         *
         * Convención:
         *
         * angle = 0
         *     ↓
         * forward = (0, 0, -1)
         */

        float forwardX =
                -(float) Math.sin(angle);

        float forwardZ =
                -(float) Math.cos(angle);


        /*
         * ========================================================
         * MOVIMIENTO + COLISIONES
         * ========================================================
         */

        float movementX =
                forwardX
                        * speed
                        * deltaTime;

        float movementZ =
                forwardZ
                        * speed
                        * deltaTime;

        boolean blockedX = false;
        boolean blockedZ = false;

        if (Math.abs(movementX) > 0.00001f) {

            float candidateX =
                    x + movementX;

            if (city.canDrive(
                    candidateX,
                    z,
                    COLLISION_RADIUS
            )) {

                x = candidateX;

            } else {

                blockedX = true;
            }
        }

        if (Math.abs(movementZ) > 0.00001f) {

            float candidateZ =
                    z + movementZ;

            if (city.canDrive(
                    x,
                    candidateZ,
                    COLLISION_RADIUS
            )) {

                z = candidateZ;

            } else {

                blockedZ = true;
            }
        }

        if (blockedX && blockedZ) {

            speed =
                    moveTowardZero(
                            speed,
                            BRAKE_DECELERATION
                                    * deltaTime
                    );
        }
    }


    /*
     * ============================================================
     * RENDER
     * ============================================================
     */

    public void render(
            Renderer renderer
    ) {

        /*
         * ========================================================
         * RUEDAS
         * ========================================================
         */

        drawPart(
                renderer,

                -1.05f,
                0.30f,
                1.25f,

                0.38f,
                0.48f,
                0.75f,

                0.04f,
                0.04f,
                0.04f
        );

        drawPart(
                renderer,

                1.05f,
                0.30f,
                1.25f,

                0.38f,
                0.48f,
                0.75f,

                0.04f,
                0.04f,
                0.04f
        );

        drawPart(
                renderer,

                -1.05f,
                0.30f,
                -1.25f,

                0.38f,
                0.48f,
                0.75f,

                0.04f,
                0.04f,
                0.04f
        );

        drawPart(
                renderer,

                1.05f,
                0.30f,
                -1.25f,

                0.38f,
                0.48f,
                0.75f,

                0.04f,
                0.04f,
                0.04f
        );


        /*
         * ========================================================
         * CHASIS
         * ========================================================
         */

        drawPart(
                renderer,

                0.0f,
                0.62f,
                0.0f,

                2.20f,
                0.68f,
                4.20f,

                0.88f,
                0.20f,
                0.08f
        );


        /*
         * ========================================================
         * CABINA
         * ========================================================
         *
         * localForward negativo:
         *
         * ligeramente desplazada hacia
         * la parte trasera.
         */

        drawPart(
                renderer,

                0.0f,
                1.22f,
                -0.35f,

                1.75f,
                0.65f,
                2.05f,

                0.95f,
                0.48f,
                0.12f
        );


        /*
         * ========================================================
         * PARABRISAS
         * ========================================================
         *
         * Todavía es solo un detalle visual.
         */

        drawPart(
                renderer,

                0.0f,
                1.28f,
                0.70f,

                1.50f,
                0.42f,
                0.08f,

                0.10f,
                0.20f,
                0.28f
        );


        /*
         * ========================================================
         * FAROS VISUALES
         * ========================================================
         *
         * Estas piezas representan las ópticas delanteras.
         *
         * La iluminación real se calcula mediante
         * LightingSystem
         */

        drawPart(
                renderer,

                -0.65f,
                0.68f,
                2.12f,

                0.35f,
                0.22f,
                0.08f,

                1.0f,
                0.95f,
                0.50f
        );

        drawPart(
                renderer,

                0.65f,
                0.68f,
                2.12f,

                0.35f,
                0.22f,
                0.08f,

                1.0f,
                0.95f,
                0.50f
        );


        /*
         * ========================================================
         * LUCES TRASERAS
         * ========================================================
         */

        drawPart(
                renderer,

                -0.65f,
                0.65f,
                -2.12f,

                0.30f,
                0.20f,
                0.08f,

                0.75f,
                0.02f,
                0.02f
        );

        drawPart(
                renderer,

                0.65f,
                0.65f,
                -2.12f,

                0.30f,
                0.20f,
                0.08f,

                0.75f,
                0.02f,
                0.02f
        );
    }


    /*
     * Convierte coordenadas locales del auto
     * a coordenadas del mundo.
     *
     * localX:
     *     izquierda / derecha
     *
     * localForward:
     *     positivo hacia el frente
     */
    private void drawPart(
            Renderer renderer,

            float localX,
            float worldY,
            float localForward,

            float sizeX,
            float sizeY,
            float sizeZ,

            float red,
            float green,
            float blue
    ) {

        float sin =
                (float) Math.sin(angle);

        float cos =
                (float) Math.cos(angle);


        /*
         * Vector derecho local:
         *
         * ( cos(angle), -sin(angle) )
         *
         * Vector frontal local:
         *
         * ( -sin(angle), -cos(angle) )
         */

        float worldX =
                x
                        + cos * localX
                        - sin * localForward;

        float worldZ =
                z
                        - sin * localX
                        - cos * localForward;


        renderer.rotatedBox(
                worldX,
                worldY,
                worldZ,

                sizeX,
                sizeY,
                sizeZ,

                red,
                green,
                blue,

                angle
        );
    }


    /*
     * ============================================================
     * RESET
     * ============================================================
     */

    public void reset() {

        x =
                spawnX;

        z =
                spawnZ;

        angle =
                spawnAngle;

        speed =
                0.0f;
    }


    /*
     * ============================================================
     * HELPERS
     * ============================================================
     */

    private float moveTowardZero(
            float value,
            float amount
    ) {

        if (value > 0.0f) {

            return Math.max(
                    0.0f,
                    value - amount
            );
        }

        if (value < 0.0f) {

            return Math.min(
                    0.0f,
                    value + amount
            );
        }

        return 0.0f;
    }


    private float normalizeAngle(
            float value
    ) {

        float fullTurn =
                (float) (Math.PI * 2.0);

        while (value > Math.PI) {
            value -= fullTurn;
        }

        while (value < -Math.PI) {
            value += fullTurn;
        }

        return value;
    }


    private float clamp(
            float value,
            float minimum,
            float maximum
    ) {

        return Math.max(
                minimum,
                Math.min(
                        maximum,
                        value
                )
        );
    }


    /*
     * ============================================================
     * GETTERS
     * ============================================================
     */

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
