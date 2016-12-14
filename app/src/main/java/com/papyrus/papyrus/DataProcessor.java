package com.papyrus.papyrus;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;

import java.util.concurrent.BlockingQueue;


public class DataProcessor implements Runnable {
    private final BlockingQueue<byte[]> incomeMessageQueue;
    private static final String TAG = "DataProcessor";
    private Path drawPath;
    private Paint drawPaint;
    private DrawingView drawView;
    protected Canvas drawCanvas;
    private DrawCommand command;
    private Handler mHandler = new Handler();
    private int lastCommand;


    public DataProcessor(BlockingQueue<byte[]> messageQueue, Context context, DrawingView dView) {
        incomeMessageQueue = messageQueue;
        drawView = dView;
        drawPath = drawView.getDrawPath();
        drawPaint = drawView.getDrawPaint();
        drawCanvas = drawView.getDrawCanvas();
    }

    public void run() {
        while (true) {
            try {
                byte[] rawData = incomeMessageQueue.take();
                String message = new String(rawData);
                Gson gson = new Gson();
                command = gson.fromJson(message, DrawCommand.class);
            } catch (InterruptedException e) {
                Log.e("REMOTE_DRAW", e.toString());
                e.printStackTrace();
            } catch (Exception e) {
                Log.e("REMOTE_DRAW", e.toString());
                e.printStackTrace();
            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    switch (command.getAction()) {
                        case DrawCommand.MOVE_TO:
                            drawView.rMoveTo(command.getPointX(), command.getPointY());
                            break;
                        case DrawCommand.LINE_TO:
                            if (lastCommand != DrawCommand.LINE_TO) {
                                drawView.rMoveTo(command.getPointX(), command.getPointY());
                            } else {
                                drawView.rLineTo(command.getPointX(), command.getPointY());
                            }
                            break;
                        case DrawCommand.DRAW_PATH:
                            drawView.rDrawPath();
                            break;
                        case DrawCommand.BUTTON_COLOR:
                            drawView.setColor(command.getColor());
                            break;
                        case DrawCommand.BUTTON_DRAW:
                            drawView.setErase(false);
                            break;
                        case DrawCommand.BUTTON_ERASE:
                            drawView.setErase(true);
                            break;
                        case DrawCommand.BUTTON_NEW:
                            drawView.setErase(false);
                            drawView.startNew();
                            break;
                        default:
                    }
                    lastCommand = command.getAction();
                    drawView.invalidate();
                }
            });
        }
    }
}