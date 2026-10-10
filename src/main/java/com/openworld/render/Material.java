package com.openworld.render;

public final class Material {

    private final float red;
    private final float green;
    private final float blue;
    private final float emissionStrength;
    private final float specularStrength;
    private final float shininess;

    public Material(float red, float green, float blue,
                    float emissionStrength,
                    float specularStrength,
                    float shininess) {
        this.red = clamp01(red);
        this.green = clamp01(green);
        this.blue = clamp01(blue);
        this.emissionStrength = Math.max(0.0f, emissionStrength);
        this.specularStrength = Math.max(0.0f, specularStrength);
        this.shininess = Math.max(1.0f, shininess);
    }

    public float getRed() { return red; }
    public float getGreen() { return green; }
    public float getBlue() { return blue; }
    public float getEmissionStrength() { return emissionStrength; }
    public float getSpecularStrength() { return specularStrength; }
    public float getShininess() { return shininess; }

    private float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }
}
