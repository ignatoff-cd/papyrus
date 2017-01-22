package com.papyrus.papyrus;

import android.view.SurfaceHolder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;


public class PapyrusThread implements Runnable {

    private final SurfaceHolder holder;
    //private final BlockingQueue<byte[]> incomeMessageQueue;

    public PapyrusThread(SurfaceHolder sHolder) {
        holder = sHolder;
        //incomeMessageQueue = messageQueue;
    }

    @Override
    public void run() {

    }
}

