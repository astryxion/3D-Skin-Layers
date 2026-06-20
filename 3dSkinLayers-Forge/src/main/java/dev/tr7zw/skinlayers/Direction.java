package dev.tr7zw.skinlayers;

import org.lwjgl.util.vector.Vector3f;

public enum Direction {
    DOWN(0, -1, 0), UP(0, 1, 0), NORTH(0, 0, -1), SOUTH(0, 0, 1),
    WEST(-1, 0, 0), EAST(1, 0, 0);

    private Direction(int x, int y, int z) {
        this.normalX = x;
        this.normalY = y;
        this.normalZ = z;
    }

    private final int normalX;
    private final int normalY;
    private final int normalZ;

    public int getStepX() {
        return this.normalX;
    }

    public int getStepY() {
        return this.normalY;
    }

    public int getStepZ() {
        return this.normalZ;
    }

    public Vector3f step() {
        return new Vector3f(getStepX(), getStepY(), getStepZ());
    }

}
