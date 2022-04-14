package Executables;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

import Networking.Callback;
import Networking.Request;
import Networking.RequestType;

public class Client {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Socket socket = new Socket("localhost", 4242);

        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.flush();

        ArrayList<Object> data = new ArrayList<Object>();
        data.add("Math");
        data.add("Ryan");
        Request request = new Request(RequestType.CREATE_COURSE, data);

        oos.writeObject(request);
        oos.flush();

        Callback callback = (Callback)ois.readObject();
        System.out.println(callback.getMessage());

        System.out.println("Waiting for object...");
        Object obj = ois.readObject();
        System.out.println("Client close");

        oos.close();
        ois.close();
    }
}
