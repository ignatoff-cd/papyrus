package com.papyrus.papyrus;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.MotionEvent;

import com.google.gson.Gson;
import java.util.concurrent.BlockingQueue;


public class DataProcessor implements Runnable {
    private final BlockingQueue<byte[]> messageQueue;
    private final DrawingView drawView;
    private static final String TAG = "DataProcessor";
    protected Path drawPath;
    protected Paint drawPaint;
    protected Canvas drawCanvas;



    public DataProcessor(BlockingQueue<byte[]> messageQueue, DrawingView drawView) {
        this.messageQueue = messageQueue;
        this.drawView = drawView;
        this.drawPath = drawView.getDrawPath();
        this.drawPaint = drawView.getDrawPaint();
        this.drawCanvas = drawView.getDrawCanvas();
    }

    @Override
    public void run() {
        int counter = 0;
        while (true) {
            try {
                /**
                 * Try and take a message from the queue. Will block if the
                 * message queue is empty, until an element becomes available.
                 */
                byte[] rawData = this.messageQueue.take();
                /**
                 * Increase message counter after processing
                 */
                counter++;
                Log.i(TAG, "Data processor handled " + counter + " messages");
                String message = new String(rawData);
                Log.i("TAG", message);
                Gson gson = new Gson();
                DrawCommand command = gson.fromJson(message, DrawCommand.class);
                drawRemote(command);

                /**
                 * Simulate a 3 ms delay
                 */
                //Thread.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                Log.e("REMOTE_DRAW", e.toString());
                e.printStackTrace();
            }
        }
    }


    protected boolean drawRemote(DrawCommand command) {
        try {
            switch (command.getAction()) {
                case DrawCommand.MOVE_TO:
                    drawPath.moveTo(command.getPointX(), command.getPointY());
                    break;
                case DrawCommand.LINE_TO:
                    drawPath.lineTo(command.getPointX(), command.getPointY());
                    break;
                case DrawCommand.DRAW_PATH:
                    drawCanvas.drawPath(drawPath, drawPaint);
                    //drawPath.reset();
                    break;
                default:
                    return true;
            }
        } catch (Exception e) {
            Log.e("REMOTE_DRAW", e.toString());
            e.printStackTrace();
        }
        return true;
    }
}