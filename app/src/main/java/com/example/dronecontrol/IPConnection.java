package com.example.dronecontrol;

import static android.content.Context.WIFI_SERVICE;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiManager;
import android.view.View;
import android.widget.ImageView;

import androidx.core.widget.ImageViewCompat;

import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

public class IPConnection {
    InetAddress localIP;
    InetAddress droneIP;
    int localPort = 1234;
    int dronePort = 1234;
    DatagramSocket udp;
    Socket tcp;
    boolean connected = false;
    MainActivity mainActivity;
    ArrayDeque<byte[]> commandQueue = new ArrayDeque<>();
    Bitmap latestFrame;

    protected InetAddress wifiIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        int ipAddressInt = wifiManager.getConnectionInfo().getIpAddress();

        // Convert little-endian to big-endian if needed
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddressInt = Integer.reverseBytes(ipAddressInt);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddressInt).toByteArray();

        InetAddress ipAddress;
        try {
            ipAddress = InetAddress.getByAddress(ipByteArray);
        } catch (UnknownHostException ex) {
            ipAddress = null;
        }

        return ipAddress;
    }

    public Bitmap getLatestFrame() {
        return latestFrame;
    }

    public IPConnection(MainActivity activity) {
        mainActivity = activity;
        new Thread(() -> {
            try {
                localIP = wifiIpAddress(mainActivity.getApplicationContext());
                droneIP = InetAddress.getByName("192.168.1.6");
                udp = new DatagramSocket(localPort);
                tcp = new Socket(droneIP, dronePort);
                connected = true;
                startReceiving();
                startSending();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void setDronePort(int port) {
        dronePort = port;
    }

    public void setLocalPort(int port) {
        localPort = port;
    }

    public void setDroneIP(String ip) {
        try {
            droneIP = InetAddress.getByName(ip);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reconnect() {
        new Thread(() -> {
            try {
                connected = false;
                if (udp != null)
                    udp.close();
                if (tcp != null)
                    tcp.close();
                udp = new DatagramSocket(localPort);
                tcp = new Socket(droneIP, dronePort);
                connected = true;
                startReceiving();
                startSending();
                startStreaming();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    void sendCommand(byte[] command) {
        commandQueue.add(command);
    }

    public void startStreaming() {
        sendCommand(new byte[]{20});
    }

    public void stopStreaming() {
        sendCommand(new byte[]{21});
    }

    public void moveCamera(int a) {
        byte[] bytes = new byte[2];
        bytes[0] = 22;
        bytes[1] = (byte) a;
        sendCommand(bytes);
    }

    public void startRecording() {
        sendCommand(new byte[]{23});
    }

    public void stopRecording() {
        sendCommand(new byte[]{24});
    }

    public void takePicture() {
        sendCommand(new byte[]{25});
    }

    public void takeHDPicture() {
        sendCommand(new byte[]{26});
    }

    public void disconnect() {
        byte[] bytes = new byte[1];
        bytes[0] = 27;
        sendCommand(bytes);
        try {
            connected = false;
            tcp.close();
            udp.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startSending() {
        while (connected) {
            while (commandQueue.size() == 0) {
                if (!connected) {
                    return;
                }
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                tcp.getOutputStream().write(commandQueue.pop());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void startReceiving() {
        new Thread(() -> {
            byte[] buffer = new byte[65535];
            while (connected) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    udp.receive(packet);
                    latestFrame = BitmapFactory.decodeByteArray(packet.getData(), packet.getOffset(), packet.getLength());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
