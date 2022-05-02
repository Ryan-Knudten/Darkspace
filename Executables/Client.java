package Executables;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import javax.swing.SwingUtilities;
import GUI.LoginGUI;

/**
 * Client
 *
 * Run this class to connect as a user
 * Host: localhost
 * Port: 4242
 *
 * @author Ryan Knudten, 22
 *
 * @version 5/1/22
 *
 */
public class Client {
    public static void main(String[] args) throws IOException, ClassNotFoundException, 
        InterruptedException, InvocationTargetException {
        Socket socket = new Socket("localhost", 4242);

        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.flush();

        LoginGUI gui = new LoginGUI(ois, oos);
        SwingUtilities.invokeLater(gui);
    }
}
