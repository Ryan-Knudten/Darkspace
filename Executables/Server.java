package Executables;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import DataControl.AppData;
import DataControl.Controller;
import Networking.ServerThread;

/**
 * Server
 *
 * Run this class to start the server
 * Port: 4242
 * Spawns new threads for each user connection through the ServerThread.java class
 *
 * @author Ryan Knudten, 22
 *
 * @version 5/1/22
 *
 */
public class Server {
    public static void main(String[] args) throws IOException {
        Controller controller = new Controller(AppData.loadData("DataControl/AppData.txt"));

        ServerSocket serverSocket = new ServerSocket(4242);
        System.out.println("Server created");

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("Client connected");
            ServerThread serverThread = new ServerThread(socket, controller);
            serverThread.start();
        }
    }
}
