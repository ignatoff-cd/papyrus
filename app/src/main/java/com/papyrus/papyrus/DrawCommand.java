package com.papyrus.papyrus;

public class DrawCommand {
    public static final int LINE_TO = 1;
    public static final int MOVE_TO = 2;
    public static final int DRAW_PATH = 3;

    private int action;
    private float pointX;
    private float pointY;
    private long nanoDiff;

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public float getPointX() {
        return pointX;
    }

    public void setPointX(float pointX) {
        this.pointX = pointX;
    }

    public float getPointY() {
        return pointY;
    }

    public void setPointY(float pointY) {
        this.pointY = pointY;
    }

    public void setNanoDiff(long nanoDiff) {
        this.nanoDiff = nanoDiff;
    }

    public long getNanoDiff() {
        return nanoDiff;
    }
}
