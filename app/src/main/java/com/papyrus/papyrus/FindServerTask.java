package com.papyrus.papyrus;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class FindServerTask extends AsyncTask<Void, Void, String> {
    private WifiManager wifi;
    private String TAG = "FindServer";
    private boolean notChecked = true;
    private String serverIp = "";
    private MainActivity mContext;
    private int ServerPort = 55555;
    private String ServerMessage = "IS_SERVER";

    public FindServerTask(MainActivity context) {
        mContext = context;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            findServer();
            return serverIp;
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch Server");
        }
        return serverIp;
    }

    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }

    private InetAddress getBroadcastAddress() throws IOException {
        DhcpInfo dhcp = wifi.getDhcpInfo();
        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }

    private void findServer() throws IOException {
        wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        DatagramSocket socket = new DatagramSocket(ServerPort);
        socket.setBroadcast(true);
        DatagramPacket sPacket = new DatagramPacket(ServerMessage.getBytes(), ServerMessage.length(),
                getBroadcastAddress(), mContext.getRemoteServerPort());
        socket.send(sPacket);

        byte[] buf = new byte[1024];

        while (notChecked) {
            DatagramPacket rPacket = new DatagramPacket(buf, buf.length);
            socket.receive(rPacket);
            serverIp = new String(rPacket.getData(), 0, rPacket.getLength());
            if (rPacket.getLength() > 0) {
                notChecked = false;
            }
        }
        socket.close();
    }
}