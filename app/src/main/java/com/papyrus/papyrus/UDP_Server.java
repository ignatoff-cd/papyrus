package com.papyrus.papyrus;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;

public class UDP_Server implements Runnable {
    private final int port;
    private final BlockingQueue<byte[]> messageQueue;

    public UDP_Server(int port, BlockingQueue<byte[]> messageQueue) {
        this.port = port;
        this.messageQueue = messageQueue;
    }

    @Override
    public void run() {
        try {
            DatagramSocket clientSocket = new DatagramSocket(port);
            clientSocket.setSoTimeout(0);
            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket datagramPacket = new DatagramPacket(buffer, 0, buffer.length);
                clientSocket.receive(datagramPacket);
                String str = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
                this.messageQueue.put(str.getBytes());
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Timeout. Client is closing.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}