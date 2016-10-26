package com.papyrus.papyrus;

import android.util.Log;

import java.util.concurrent.BlockingQueue;

/**
 * Created by cyrillignatov on 26.10.16.
 */

public class DataProcessor implements Runnable {
    private final BlockingQueue<byte[]> messageQueue;
    private static final String TAG = "DataProcessor";

    public DataProcessor(BlockingQueue<byte[]> messageQueue, DrawingView drawView) {
        this.messageQueue = messageQueue;
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
                /**
                 * Simulate a 3 ms delay
                 */
                Thread.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}