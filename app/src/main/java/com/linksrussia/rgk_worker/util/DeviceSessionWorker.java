package com.linksrussia.rgk_worker.util;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.linksrussia.rgk_worker.App;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Consumer;

public class DeviceSessionWorker {

    public static BluetoothSocket createBluetoothSocket(BluetoothDevice bluetoothDevice) {
        try {
            Method m = bluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
            return (BluetoothSocket) m.invoke(bluetoothDevice, 1);
        } catch (InvocationTargetException e) {
            Log.e("BluetoothSocket", "InvocationTargetException", e);
        } catch (NoSuchMethodException e) {
            Log.e("BluetoothSocket", "NoSuchMethodException", e);
        } catch (IllegalAccessException e) {
            Log.e("BluetoothSocket", "IllegalAccessException", e);
        }

        Log.e("ERROR_MESSAGE", "Can't create Bluetooth Socket");

        return null;
    }

    @SuppressLint("MissingPermission")
    public static BluetoothSocket getConnectedSocket(BluetoothDevice bluetoothDevice, int maxAttempts) {
        Thread currentThread = Thread.currentThread();
        int connectFailureCounter = 0;
        while (!currentThread.isInterrupted()) {
            BluetoothSocket bluetoothSocket = createBluetoothSocket(bluetoothDevice);
            try {
                bluetoothSocket.connect();
                Log.i("BluetoothSocket", "Connected!!!");
                App.Companion.setDeviceConnected(true);
                return bluetoothSocket;
            } catch (Exception e) {
                Log.e("BluetoothSocket", "Can't connect");
                closeSocket(bluetoothSocket);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Log.e("DATA", "Can't sleep", ie);
                    currentThread.interrupt();
                }

                if (maxAttempts <= ++connectFailureCounter) {
                    break;
                }
            }
        }

        return null;
    }

    public static void closeSocket(BluetoothSocket bluetoothSocket) {
        if (null != bluetoothSocket && bluetoothSocket.isConnected()) {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                Log.e("DATA", "Can't close socket", e);
            }
        }
        App.Companion.setDeviceConnected(false);
    }


    public static void receiveData(BluetoothSocket bluetoothSocket, Consumer<String> dataConsumer) {
        if (null == bluetoothSocket)
            return;

        Thread currentThread = Thread.currentThread();
        byte[] buff = new byte[128];
        try (DataInputStream dis = new DataInputStream(bluetoothSocket.getInputStream())) {
            StringBuffer buffer = new StringBuffer();
            while (!currentThread.isInterrupted()) {
                int length = dis.read(buff);
                String data = new String(Arrays.copyOfRange(buff, 0, length));
                buffer.append(data);

                if (data.contains("\r\n")) {
                    dataConsumer.accept(buffer.toString());
                    buffer.setLength(0);
                }
            }
        } catch (Exception e) {
            Log.e("DATA", "Read from socket error", e);
        }
    }
}
