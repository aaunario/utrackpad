package com.aaunario.utrackpad;

import android.app.IntentService;
import android.content.Intent;
import android.net.Network;
import android.support.annotation.Nullable;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Created by aaunario on 12/9/17.
 */

public class NetworkService extends IntentService {

    private static final String HOST = "192.168.47.10";
    private static final int PORT = 50047;

    private DataOutputStream outputStream;

    private Socket socket;

    public NetworkService() { super("NetworkService"); }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public NetworkService(String name) {
        super(name);
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String data = intent.getDataString();
        String [] data_arr = data.split("/\\W/");

        if (data.split("\\s")[0]== "close") {
            try {
                System.out.println("closing");
                close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            send(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void send(String msg) throws IOException {

        connect();

        if (outputStream == null) return;

        // Semd exactly 1024 bytes
        while (msg.getBytes().length - 1022 > 0) {
            msg += " ";
//            System.out.println("msg = " + msg);
        }

        outputStream.writeUTF(msg);
        outputStream.flush();
    }

    private void close() throws IOException {
        if (socket != null && socket.isConnected() && !socket.isClosed())
            socket.close();
    }
    private void connect() throws IOException {
        InetAddress serverAddr = InetAddress.getByName(HOST);
        if (socket == null)
        {
            socket = new Socket();
        }

        //Set connection options here
        socket.setKeepAlive(true);

        if (socket.isBound()) {
            socket.setReuseAddress(true);
        }

        if (!socket.isConnected()) {
            socket.connect(new InetSocketAddress(HOST, PORT), 0);
            outputStream = new DataOutputStream(socket.getOutputStream());
        }
    }
}
