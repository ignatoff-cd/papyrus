package com.papyrus.papyrus;

import android.os.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;

/**
 * Created by cyrillignatov on 26.10.16.
 */

public class UDP_Server implements Runnable {
    /**
     * The port where the client is listening.
     */
    private final int clientPort;
    private final BlockingQueue<byte[]> messageQueue;

    public UDP_Server(int port, BlockingQueue<byte[]> messageQueue) {
        this.clientPort = port;
        this.messageQueue = messageQueue;
    }

    @Override
    public void run() {
        /**
         * Create a new server socket and bind it to a free port. I have chosendr
         * one in the 49152 - 65535 range, which are allocated for internal applications
         */
        try (DatagramSocket serverSocket = new DatagramSocket(50000)) {
            // The server will generate 3 messages and send them to the client
            while (true) {
                byte[] item = this.messageQueue.take();
                //String message = "Message number " + i;
                DatagramPacket datagramPacket = new DatagramPacket(
                        //message.getBytes(),
                        item,
                        item.length,
                        InetAddress.getByName("192.168.1.5"),
                        clientPort);
                serverSocket.send(datagramPacket);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

