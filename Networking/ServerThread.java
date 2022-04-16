package Networking;

import java.io.*;
import java.net.Socket;

import DataControl.AppData;
import DataControl.Controller;

public class ServerThread extends Thread {
    private Socket socket;
    private Controller controller;

    public ServerThread(Socket socket, Controller controller) {
        this.socket = socket;
        this.controller = controller;
    }

    public void run() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.flush();
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            while (true) {
                Request request = (Request)ois.readObject();
                Callback callback = controller.handleRequest(request);
                oos.writeObject(callback);
                oos.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }
}
