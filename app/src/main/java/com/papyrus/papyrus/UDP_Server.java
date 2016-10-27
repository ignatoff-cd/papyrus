package com.papyrus.papyrus;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
        /**
         * Bind the client socket to the port on which you expect to
         * read incoming messages
         */
        try {
            DatagramSocket clientSocket = new DatagramSocket(port);
            // Set a timeout of 3000 ms for the client.
            clientSocket.setSoTimeout(0);
            while (true) {
                /**
                 * Create a byte array buffer to store incoming data. If the message length
                 * exceeds the length of your buffer, then the message will be truncated. To avoid this,
                 * you can simply instantiate the buffer with the maximum UDP packet size, which
                 * is 65506
                 */
                byte[] buffer = new byte[65507];
                DatagramPacket datagramPacket = new DatagramPacket(buffer, 0, buffer.length);
                /**
                 * The receive method will wait for 3000 ms for data.
                 * After that, the client will throw a timeout exception.
                 */
                clientSocket.receive(datagramPacket);
                /**
                 * Add the data contained in the datagram packet to the message
                 * queue.The 'put' method will block if the message queue is full,
                 * until there is space to store the new message.
                 */
                this.messageQueue.put(datagramPacket.getData());
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