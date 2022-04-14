package GUI;

import java.awt.*;
import java.awt.event.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.swing.*;

import Model.Model;
import Networking.Callback;
import Networking.Request;
import Networking.RequestType;

public class LoginGUI extends JComponent implements Runnable, GUI {
    private Model model;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private JButton testButton;
    private JButton windowButton;
    private JFrame frame;

    private ActionListener actionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == testButton) {
                ArrayList<Object> data = new ArrayList<Object>();
                Request request = new Request(RequestType.CREATE_COURSE, data);
                Callback callback = requestCallback(request);
                testButton.setText(callback.getMessage());
            }
            if (e.getSource() == windowButton) {
                LoginGUI guiNEW = new LoginGUI(ois, oos);
                SwingUtilities.invokeLater(guiNEW);
                closeWindow();
                
            }
        }
    };

    public LoginGUI(ObjectInputStream ois, ObjectOutputStream oos) {
        this.model = new Model();
        this.ois = ois;
        this.oos = oos;
    }

    public void run() {
        frame = new JFrame("Login");
        Container content = frame.getContentPane();
        content.setLayout(new BorderLayout());
        LoginGUI loginGUI = new LoginGUI(null, null);
        content.add(loginGUI, BorderLayout.CENTER);


        JPanel panel = new JPanel();
        testButton = new JButton("Hello");
        testButton.addActionListener(actionListener);
        windowButton = new JButton("New Window");
        windowButton.addActionListener(actionListener);
        panel.add(testButton);
        panel.add(windowButton);
        content.add(panel, BorderLayout.CENTER);

        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public void closeWindow() {
        frame.dispose();
    }

    public Callback requestCallback(Request request) {
        try {
            oos.writeObject(new Request(request.getRequestType(), request.getData()));
            oos.flush();
            return (Callback)ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
