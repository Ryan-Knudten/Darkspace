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

    private JTextField usernameBox;
    private JTextField passwordBox;
    private JComboBox<String> userTypeBox;
    private JButton createAccountButton;
    private JButton loginButton;

    private JFrame frame;

    private ActionListener actionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == createAccountButton) {
                ArrayList<Object> data = new ArrayList<Object>();
                data.add(usernameBox.getText());
                data.add(passwordBox.getText());
                data.add(userTypeBox.getSelectedItem());
                Request request = new Request(RequestType.CREATE_USER, data);

                Callback callback = requestCallback(request);
                model = callback.getModel();
                JOptionPane.showMessageDialog(frame, callback.getMessage(), "Darkspace", JOptionPane.INFORMATION_MESSAGE);
                usernameBox.setText("");
                passwordBox.setText("");
            }
            if (e.getSource() == loginButton) {
                ArrayList<Object> data = new ArrayList<Object>();
                data.add(usernameBox.getText());
                data.add(passwordBox.getText());
                Request request = new Request(RequestType.LOGIN_USER, data);

                Callback callback = requestCallback(request);
                model = callback.getModel();
                if(callback.getDidRequestWork()) {
                    frame.dispose();
                    if(userTypeBox.getSelectedItem().equals("Student")) {
                        System.out.println("OPEN STUDENT WINDOW");
                    } else {
                        System.out.println("OPEN TEACHER WINDOW");
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, callback.getMessage(), "Darkspace", JOptionPane.ERROR_MESSAGE);
                    usernameBox.setText("");
                    passwordBox.setText("");
                }
            }
        }
    };

    public LoginGUI(ObjectInputStream ois, ObjectOutputStream oos) {
        this.model = new Model();
        this.ois = ois;
        this.oos = oos;
    }

    public void run() {
        frame = new JFrame("Sign In");
        Container content = frame.getContentPane();
        content.setLayout(new BorderLayout());
        LoginGUI loginGUI = new LoginGUI(null, null);
        content.add(loginGUI, BorderLayout.CENTER);

        JLabel titleLabel = new JLabel("Darkspace");
        //#region Title
        titleLabel.setFont(new Font("Serif", Font.BOLD, 20));
        titleLabel.setVerticalAlignment(SwingConstants.CENTER);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        //#endregion

        JPanel textBoxPanel = new JPanel(new GridLayout(5, 0));
        //#region Text Boxes
        usernameBox = new JTextField();
        passwordBox = new JTextField();
        userTypeBox = new JComboBox<>();
        userTypeBox.addItem("Student");
        userTypeBox.addItem("Teacher");
        textBoxPanel.add(userTypeBox);
        textBoxPanel.add(new JLabel());
        textBoxPanel.add(usernameBox);
        textBoxPanel.add(passwordBox);
        textBoxPanel.add(new JLabel());
        //#endregion

        JPanel labelPanel = new JPanel(new GridLayout(5, 0));
        //#region Labels
        JLabel usernameLabel = new JLabel("Username: ");
        usernameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        usernameLabel.setVerticalAlignment(SwingConstants.CENTER);
        usernameLabel.setVerticalTextPosition(SwingConstants.CENTER);
        JLabel passwordLabel = new JLabel("Password: ");
        passwordLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        passwordLabel.setVerticalAlignment(SwingConstants.CENTER);
        passwordLabel.setVerticalTextPosition(SwingConstants.CENTER);
        JLabel userTypeLabel = new JLabel("Sign in as: ");
        userTypeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        userTypeLabel.setVerticalAlignment(SwingConstants.CENTER);
        userTypeLabel.setVerticalTextPosition(SwingConstants.CENTER);
        labelPanel.add(userTypeLabel);
        labelPanel.add(new JLabel());
        labelPanel.add(usernameLabel);
        labelPanel.add(passwordLabel);
        labelPanel.add(new JLabel());
        //#endregion

        JPanel buttonPanel = new JPanel(new GridLayout(6, 0));
        //#region Buttons
        createAccountButton = new JButton("Create Account");
        createAccountButton.addActionListener(actionListener);
        createAccountButton.setBackground(new Color(46,168,245));
        loginButton = new JButton("Login");
        loginButton.addActionListener(actionListener);
        buttonPanel.add(loginButton);
        buttonPanel.add(new JLabel());
        buttonPanel.add(createAccountButton);
        buttonPanel.add(new JLabel());
        buttonPanel.add(new JLabel());
        buttonPanel.add(new JLabel());
        //#endregion
        
        JPanel gridPanel = new JPanel(new GridLayout(3, 3));
        //#region Main Grid
        gridPanel.add(new JLabel());
        gridPanel.add(titleLabel);
        gridPanel.add(new JLabel());
        gridPanel.add(labelPanel);
        gridPanel.add(textBoxPanel);
        gridPanel.add(new JLabel());
        gridPanel.add(new JLabel());
        gridPanel.add(buttonPanel);
        gridPanel.add(new JLabel());
        //#endregion
        
        content.add(gridPanel, BorderLayout.CENTER);

        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public Callback requestCallback(Request request) {
        try {
            oos.writeObject(new Request(request.getRequestType(), request.getData()));
            oos.flush();
            return (Callback)ois.readObject();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Server Connection Ended.", "Darkspace", JOptionPane.ERROR_MESSAGE);
            frame.dispose();
            return null;
        }
    }
}
