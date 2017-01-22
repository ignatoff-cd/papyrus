package com.papyrus.papyrus;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.gson.Gson;

import java.util.concurrent.BlockingQueue;

public class PapSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private PapyrusThread thread;
    public SurfaceHolder holder;
    private Path drawPath, remotePath;
    private int action, lastAction;
    private float lastX, lastY, touchX, touchY;
    public long timeDiff = -1;
    public BlockingQueue<byte[]> queue;
    private String message;
    private Paint drawPaint, canvasPaint, rDrawPaint;
    private int paintColor = 0xFF000000;
    private boolean erase = false;


    public PapSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        holder = getHolder();
        holder.addCallback(this);
        thread = new PapyrusThread(holder);
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(10);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public PapSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
    }

    public PapSurfaceView(Context context) {
        super(context);
        getHolder().addCallback(this);
    }

    public void surfaceCreated(SurfaceHolder holder) {
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            DrawCommand command = new DrawCommand();
            touchX = event.getX();
            touchY = event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    drawPath.moveTo(touchX, touchY);
                    action = DrawCommand.MOVE_TO;
                    break;
                case MotionEvent.ACTION_HOVER_MOVE:
                case MotionEvent.ACTION_MOVE:
                    drawPath.quadTo(lastX, lastY, (touchX + lastX) / 2, (touchY + lastY) / 2);
                    //drawPath.lineTo(touchX, touchY);
                    action = DrawCommand.LINE_TO;
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    Canvas canvas = getHolder().lockCanvas();
                    canvas.drawPath(drawPath, drawPaint);
                    getHolder().unlockCanvasAndPost(canvas);
                    action = DrawCommand.DRAW_PATH;
                    break;
                default:
                    return true;
            }

            lastAction = action;
            if (action != 0) {
                command.setAction(action);
                if (this.timeDiff == -1) {
                    this.timeDiff = System.currentTimeMillis();
                }
                command.setNanoDiff(timeDiff);
                command.setPointX(touchX);
                command.setPointY(touchY);
                command.setLastX(lastX);
                command.setLastY(lastY);
                Gson gson = new Gson();
                message = gson.toJson(command);
                queue.put(message.getBytes());
                lastX = touchX;
                lastY = touchY;
            }
        } catch (InterruptedException e) {
        }
        return true;
    }

    public void setQueue(BlockingQueue<byte[]> setQueue) {
        queue = setQueue;
    }

    public void setColor(String newColor) {
        invalidate();
        paintColor = Color.parseColor(newColor);
        drawPaint.setColor(paintColor);
    }

    public void startNew() {
        Canvas canvas = holder.lockCanvas();
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        holder.unlockCanvasAndPost(canvas);
    }

    public void setErase(boolean isErase) {
        erase = isErase;
        if (erase) drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        else drawPaint.setXfermode(null);
    }
}
