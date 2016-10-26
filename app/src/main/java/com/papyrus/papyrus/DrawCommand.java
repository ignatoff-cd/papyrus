package com.papyrus.papyrus;

public class DrawCommand {
    private String action;
    private float pointX;
    private float pointY;
    private long nanoDiff;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
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
