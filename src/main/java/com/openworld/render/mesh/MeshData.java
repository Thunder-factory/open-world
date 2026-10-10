package com.openworld.render.mesh;

public final class MeshData {

    private final float[] vertices;

    private final int[] indices;

    public MeshData(
            float[] vertices,
            int[] indices
    ) {

        if (vertices == null
                || vertices.length == 0) {

            throw new IllegalArgumentException(
                    "Los vertices de la malla no pueden estar vacios"
            );
        }

        if (indices == null
                || indices.length == 0) {

            throw new IllegalArgumentException(
                    "Los indices de la malla no pueden estar vacios"
            );
        }

        this.vertices = vertices;

        this.indices = indices;
    }

    public float[] getVertices() {

        return vertices;
    }

    public int[] getIndices() {

        return indices;
    }
}
