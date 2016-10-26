package com.papyrus.papyrus;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.google.gson.Gson;

import java.util.concurrent.BlockingQueue;

public class DrawingView extends View {
    //drawing path
    private Path drawPath;
    //drawing and canvas paint
    private Paint drawPaint, canvasPaint;
    //initial color
    private int paintColor = 0xFF660000;
    //canvas
    private Canvas drawCanvas;
    //canvas bitmap
    private Bitmap canvasBitmap;

    private boolean erase = false;
    public BlockingQueue<byte[]> queue;
    public long timeDiff = -1;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing() {
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(15);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//view given size
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
//draw view
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//detect user touch
        try {
            DrawCommand command = new DrawCommand();
            Log.i("DRAW", queue.toString());
            String message = "";
            String action = "";
            float touchX = event.getX();
            float touchY = event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    drawPath.moveTo(touchX, touchY);
                    action = "moveTo";
                    break;
                case MotionEvent.ACTION_HOVER_MOVE:
                case MotionEvent.ACTION_MOVE:
                    drawPath.lineTo(touchX, touchY);
                    action = "lineTo";
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    drawCanvas.drawPath(drawPath, drawPaint);
                    action = "drawPath";
                    drawPath.reset();
                    break;
                default:
                    //message += "NOT SHOWN EVENT:" + event.getAction();
                    return true;
            }
            if (action != "") {
                command.setAction(action);
                if (this.timeDiff == -1) {
                    this.timeDiff = System.currentTimeMillis();
                }
                command.setNanoDiff(System.currentTimeMillis() - timeDiff);
                command.setPointX(touchX);
                command.setPointY(touchY);
                Gson gson = new Gson();
                message = gson.toJson(command);
                queue.put(message.getBytes());
            }
            invalidate();
        } catch (InterruptedException e) {
        }
        return true;
    }

    public void setColor(String newColor) {
        invalidate();
        paintColor = Color.parseColor(newColor);
        drawPaint.setColor(paintColor);
    }

    public void startNew() {
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
    }

    public void setErase(boolean isErase) {
        erase = isErase;
        if (erase) drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        else drawPaint.setXfermode(null);
    }


    public void setQueue(BlockingQueue<byte[]> queue) {
        this.queue = queue;
    }
}
