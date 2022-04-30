package GUI;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import javax.swing.*;

import GUI.Colors.Colors;
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

    private Font titleFont;
    private Font textFont;
    private Color backgroundColor = Colors.SPACE_CADET;
    private Color textColor = Colors.WHITE;
    private Color buttonColor = Colors.MANATEE;

    private ActionListener actionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == createAccountButton) {
                if (usernameBox.getText().equals("") || passwordBox.getText().equals("")) {
                    JOptionPane.showMessageDialog(frame, "Entries cannot be blank.", "Darkspace", JOptionPane.ERROR_MESSAGE);
                } else {
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
            }
            if (e.getSource() == loginButton) {
                ArrayList<Object> data = new ArrayList<Object>();
                data.add(usernameBox.getText());
                data.add(passwordBox.getText());
                Request request = new Request(RequestType.LOGIN_USER, data);

                Callback callback = requestCallback(request);
                model = callback.getModel();
                if(callback.getDidRequestWork()) {
                    if(model.getStudents().containsKey(usernameBox.getText())) {
                        StudentGUI studentGUI = new StudentGUI(usernameBox.getText(), model, oos, ois);
                        SwingUtilities.invokeLater(studentGUI);
                    } else if (model.getTeachers().containsKey(usernameBox.getText())) {
                        TeacherGUI teacherGUI = new TeacherGUI(usernameBox.getText(), model, oos, ois);
                        SwingUtilities.invokeLater(teacherGUI);
                    }
                    frame.dispose();
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

        try {
            File file = new File("GUI/Fonts/Landasans-Medium.otf");
            Font font = Font.createFont(Font.TRUETYPE_FONT, file);
            titleFont = font.deriveFont(50.0f);
            textFont = font.deriveFont(18.0f);
        } catch (Exception e) {
            titleFont = new Font("Serif", Font.BOLD, 50);
            textFont = new Font("Serif", Font.BOLD, 20);
        }
    }

    public void run() {
        frame = new JFrame("Sign In");
        Container content = frame.getContentPane();
        content.setLayout(new BorderLayout());
        LoginGUI loginGUI = new LoginGUI(null, null);
        content.add(loginGUI, BorderLayout.CENTER);

        JLabel titleLabel = new JLabel("Darkspace");
        //#region Title
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(new Color(255, 255, 255));
        titleLabel.setVerticalAlignment(SwingConstants.CENTER);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        //#endregion

        JPanel textBoxPanel = new JPanel(new GridLayout(5, 0));
        //#region Text Boxes
        textBoxPanel.setForeground(new Color(255,255,255));
        textBoxPanel.setBackground(backgroundColor);
        usernameBox = new JTextField();
        usernameBox.setFont(textFont);
        passwordBox = new JTextField();
        passwordBox.setFont(textFont);
        userTypeBox = new JComboBox<>();
        userTypeBox.setFont(textFont);
        userTypeBox.setAlignmentY(-5);
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
        labelPanel.setBackground(backgroundColor);
        JLabel usernameLabel = new JLabel("Username: ");
        usernameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        usernameLabel.setVerticalAlignment(SwingConstants.CENTER);
        usernameLabel.setVerticalTextPosition(SwingConstants.CENTER);
        usernameLabel.setFont(textFont);
        usernameLabel.setForeground(textColor);
        JLabel passwordLabel = new JLabel("Password: ");
        passwordLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        passwordLabel.setVerticalAlignment(SwingConstants.CENTER);
        passwordLabel.setVerticalTextPosition(SwingConstants.CENTER);
        passwordLabel.setFont(textFont);
        passwordLabel.setForeground(textColor);
        JLabel userTypeLabel = new JLabel("Sign in as: ");
        userTypeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        userTypeLabel.setVerticalAlignment(SwingConstants.CENTER);
        userTypeLabel.setVerticalTextPosition(SwingConstants.CENTER);
        userTypeLabel.setFont(textFont);
        userTypeLabel.setForeground(textColor);
        userTypeLabel.setBackground(backgroundColor);
        labelPanel.add(userTypeLabel);
        labelPanel.add(new JLabel());
        labelPanel.add(usernameLabel);
        labelPanel.add(passwordLabel);
        labelPanel.add(new JLabel());
        
        //#endregion

        JPanel buttonPanel = new JPanel(new GridLayout(6, 0));
        //#region Buttons
        buttonPanel.setBackground(backgroundColor);
        createAccountButton = new JButton("Create Account");
        createAccountButton.addActionListener(actionListener);
        createAccountButton.setBackground(buttonColor);
        createAccountButton.setFont(textFont);
        loginButton = new JButton("Login");
        loginButton.addActionListener(actionListener);
        loginButton.setFont(textFont);
        loginButton.setBackground(buttonColor);
        buttonPanel.add(loginButton);
        buttonPanel.add(new JLabel());
        buttonPanel.add(createAccountButton);
        buttonPanel.add(new JLabel());
        buttonPanel.add(new JLabel());
        buttonPanel.add(new JLabel());
        //#endregion
        
        JPanel gridPanel = new JPanel(new GridLayout(3, 3));
        //#region Main Grid
        gridPanel.setBackground(backgroundColor);
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

        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);
    }

    public Callback requestCallback(Request request) {
        try {
            oos.writeObject(request);
            oos.flush();
            return (Callback)ois.readObject();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Server Connection Ended.", "Darkspace", JOptionPane.ERROR_MESSAGE);
            frame.dispose();
            return null;
        }
    }
}
