package com.papyrus.papyrus;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;


public class UDP_Client implements Runnable {

    private final int clientPort;
    private final int serverPort;
    private final String serverIp;
    private final BlockingQueue<byte[]> messageQueue;

    public UDP_Client(int clientPort, int serverPort, String serverIp, BlockingQueue<byte[]> messageQueue) {
        this.clientPort = clientPort;
        this.serverPort = serverPort;
        this.serverIp = serverIp;
        this.messageQueue = messageQueue;
    }

    @Override
    public void run() {
        try (DatagramSocket serverSocket = new DatagramSocket(serverPort)) {

            while (true) {
                byte[] item = this.messageQueue.take();
                DatagramPacket datagramPacket = new DatagramPacket(
                        item,
                        item.length,
                        InetAddress.getByName(serverIp),
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

