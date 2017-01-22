package com.papyrus.papyrus;

public class DrawCommand {
    public static final int LINE_TO = 1;
    public static final int MOVE_TO = 2;
    public static final int DRAW_PATH = 3;
    public static final int BUTTON_NEW = 4;
    public static final int BUTTON_DRAW = 5;
    public static final int BUTTON_ERASE = 6;
    public static final int BUTTON_COLOR = 7;

    private int action;
    private float pointX;
    private float pointY;
    private float lastX;
    private float lastY;
    private long nanoDiff;
    private String color;

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getAction() {
        return action;
    }

    public float getLastX() {
        return lastX;
    }

    public void setLastX(float lastX) {
        this.lastX = lastX;
    }

    public float getLastY() {
        return lastY;
    }

    public void setLastY(float lastY) {
        this.lastY = lastY;
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
        this.nanoDiff = System.currentTimeMillis() - nanoDiff;
    }

    public long getNanoDiff() {
        return nanoDiff;
    }
}
