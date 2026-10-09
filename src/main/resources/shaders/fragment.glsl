#version 330 core

#define MAX_LAMPS 16


in vec3 vWorldPosition;
in vec3 vNormal;


uniform vec3 uColor;


/*
 * ================================================================
 * ILUMINACIÓN AMBIENTAL
 * ================================================================
 */

uniform vec3 uAmbientColor;
uniform float uAmbientStrength;


/*
 * ================================================================
 * ILUMINACIÓN DIRECCIONAL
 * ================================================================
 */

uniform vec3 uDirectionalColor;
uniform float uDirectionalStrength;

uniform vec3 uDirectionalDirection;


/*
 * ================================================================
 * FAROLAS
 * ================================================================
 */

uniform int uLampCount;

uniform vec3 uLampPositions[MAX_LAMPS];

uniform vec3 uLampColor;

uniform float uLampStrength;

uniform float uLampConstant;
uniform float uLampLinear;
uniform float uLampQuadratic;

/*
 * ================================================================
 * FAROS DEL VEHÍCULO
 * ================================================================
 */

#define HEADLIGHT_COUNT 2

uniform int uHeadlightsEnabled;

uniform vec3 uHeadlightPositions[HEADLIGHT_COUNT];

uniform vec3 uHeadlightDirection;

uniform vec3 uHeadlightColor;

uniform float uHeadlightStrength;

uniform float uHeadlightMaxDistance;

uniform float uHeadlightConstant;
uniform float uHeadlightLinear;
uniform float uHeadlightQuadratic;

uniform float uHeadlightInnerCutoff;
uniform float uHeadlightOuterCutoff;

/*
 * ================================================================
 * EMISIÓN VISUAL
 * ================================================================
 */

uniform float uEmissionStrength;


out vec4 fragColor;


void main() {

    vec3 normal =
        normalize(vNormal);


    /*
     * ============================================================
     * COMPONENTE AMBIENTAL
     * ============================================================
     */

    vec3 ambient =
        uAmbientColor
        * uAmbientStrength;


    /*
     * ============================================================
     * COMPONENTE DIRECCIONAL
     * ============================================================
     */

    vec3 directionalLightDirection =
        normalize(
            -uDirectionalDirection
        );


    float directionalDiffuseFactor =
        max(
            dot(
                normal,
                directionalLightDirection
            ),
            0.0
        );


    vec3 directionalDiffuse =
        uDirectionalColor
        * uDirectionalStrength
        * directionalDiffuseFactor;


    /*
     * ============================================================
     * FAROLAS
     * ============================================================
     */

    vec3 streetLighting =
        vec3(0.0);


    for (int i = 0;
         i < uLampCount;
         i++) {

        vec3 toLamp =
            uLampPositions[i]
            - vWorldPosition;


        float distanceToLamp =
            length(toLamp);


        if (distanceToLamp > 0.0001) {

            vec3 lampDirection =
                toLamp
                / distanceToLamp;


            float lampDiffuseFactor =
                max(
                    dot(
                        normal,
                        lampDirection
                    ),
                    0.0
                );


            float attenuation =
                1.0
                /
                (
                    uLampConstant
                    + uLampLinear
                    * distanceToLamp
                    + uLampQuadratic
                    * distanceToLamp
                    * distanceToLamp
                );


            streetLighting +=
                uLampColor
                * uLampStrength
                * lampDiffuseFactor
                * attenuation;
        }
    }


    /*
    * ============================================================
    * FAROS DEL VEHÍCULO
    * ============================================================
    */

    vec3 headlightLighting =
        vec3(0.0);


    if (uHeadlightsEnabled == 1) {

        vec3 normalizedHeadlightDirection =
            normalize(
                uHeadlightDirection
            );


        for (int i = 0;
            i < HEADLIGHT_COUNT;
            i++) {

            /*
            * Vector que parte del faro
            * hacia el fragmento.
            */
            vec3 fromHeadlight =
                vWorldPosition
                - uHeadlightPositions[i];


            float distanceToHeadlight =
                length(
                    fromHeadlight
                );


            if (distanceToHeadlight > 0.0001
                    &&
                distanceToHeadlight
                    < uHeadlightMaxDistance) {

                vec3 lightToFragment =
                    fromHeadlight
                    / distanceToHeadlight;


                /*
                * ====================================================
                * CONO
                * ====================================================
                */

                float theta =
                    dot(
                        lightToFragment,
                        normalizedHeadlightDirection
                    );


                float epsilon =
                    uHeadlightInnerCutoff
                    - uHeadlightOuterCutoff;


                float coneIntensity =
                    clamp(
                        (
                            theta
                            - uHeadlightOuterCutoff
                        )
                        / epsilon,
                        0.0,
                        1.0
                    );


                /*
                * ====================================================
                * DIFUSA
                * ====================================================
                *
                * La dirección utilizada por la superficie
                * apunta desde el fragmento hacia el faro.
                */

                vec3 fragmentToLight =
                    -lightToFragment;


                float diffuseFactor =
                    max(
                        dot(
                            normal,
                            fragmentToLight
                        ),
                        0.0
                    );


                /*
                * ====================================================
                * ATENUACIÓN
                * ====================================================
                */

                float attenuation =
                    1.0
                    /
                    (
                        uHeadlightConstant
                        + uHeadlightLinear
                        * distanceToHeadlight
                        + uHeadlightQuadratic
                        * distanceToHeadlight
                        * distanceToHeadlight
                    );


                /*
                * El borde final del alcance se suaviza.
                */

                float rangeFade =
                    1.0
                    - smoothstep(
                        uHeadlightMaxDistance
                            * 0.75,
                        uHeadlightMaxDistance,
                        distanceToHeadlight
                    );


                headlightLighting +=
                    uHeadlightColor
                    * uHeadlightStrength
                    * diffuseFactor
                    * attenuation
                    * coneIntensity
                    * rangeFade;
            }
        }
    }


    /*
     * ============================================================
     * ILUMINACIÓN TOTAL
     * ============================================================
     */

    vec3 lighting =
        ambient
        + directionalDiffuse
        + streetLighting
        + headlightLighting;


    /*
     * ============================================================
     * COLOR BASE
     * ============================================================
     */

    vec3 finalColor =
        uColor
        * lighting;


    /*
     * La emisión modifica directamente la apariencia
     * visual del objeto.
     *
     * No ilumina por sí misma otros fragmentos.
     */
    finalColor +=
        uColor
        * uEmissionStrength;


    fragColor =
        vec4(
            finalColor,
            1.0
        );
}