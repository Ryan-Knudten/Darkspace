package GUI;

import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.plaf.DimensionUIResource;

import GUI.Colors.Colors;

import java.awt.Container;
import java.awt.event.*;
import java.awt.*;

import Model.Course;
import Model.Model;
import Networking.Callback;
import Networking.Request;
import Networking.RequestType;

public class StudentGUI extends JComponent implements Runnable { //TODO: Implement GUI.java
    private Model model;
    private String username;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    private JFrame frame;

    private JPanel mainGrid;

    private JScrollPane courseNav;
    //
    private JPanel courseNavGrid;

    private JScrollPane workspace;

    private JPanel quizNavGrid;

    private JButton joinCourseButton;

    private JPanel dashboardHeader;
    //
    private JButton refreshButton;
    private JButton accountButton;
    private JLabel dashboardLabel;

    private Font header1Font;
    private Font header2Font;
    private Font textFont;

    private ActionListener mainListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == refreshButton) {
                Request request = new Request(RequestType.REFRESH, null);
                Callback callback = requestCallback(request);
                model = callback.getModel();
            }
            if(e.getSource() == accountButton) {
                int option = JOptionPane.showConfirmDialog(frame, "Would you like to log out?", "Darkspace", JOptionPane.YES_NO_OPTION);
                if(option == JOptionPane.YES_OPTION) {
                    frame.dispose();
                    LoginGUI gui = new LoginGUI(ois, oos);
                    SwingUtilities.invokeLater(gui);
                }
            }
        }
    };

    private ActionListener courseListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JButton button = (JButton)e.getSource();
            for(Course course : model.getCourses()) {
                if(course.getName().equals(button.getText())) {
                    System.out.println(course.getName() + ": " + course.getTeacher());
                }
            }
        }
    };

    public StudentGUI(String username, Model model, ObjectOutputStream oos, ObjectInputStream ois) {
        this.username = username;
        this.model = model;
        this.oos = oos;
        this.ois = ois;

        try {
            File file = new File("GUI/Fonts/Exo-Regular.ttf");
            Font font = Font.createFont(Font.TRUETYPE_FONT, file);
            header1Font = font.deriveFont(30.0f);
            header2Font = font.deriveFont(25.0f);
            textFont = font.deriveFont(20.0f);
        } catch (Exception e) {
            header1Font = new Font("Serif", Font.BOLD, 50);
            textFont = new Font("Serif", Font.BOLD, 20);
        }
    }

    public void run() {
        Callback callback = requestCallback(new Request(RequestType.REFRESH, null));
        this.model = callback.getModel();

        frame = new JFrame("Darkspace");
        Container content = frame.getContentPane();
        content.setLayout(new BorderLayout());
        StudentGUI studentGUI = new StudentGUI(null, null, null, null);

        mainGrid = new JPanel(new GridBagLayout());
        mainGrid.setBackground(Colors.SPACE_CADET);
        GridBagConstraints gbc = new GridBagConstraints();

        dashboardHeader = new JPanel(new GridBagLayout());
        //#region dashboardHeader

        //#region Left Margin
        dashboardHeader.setBackground(Colors.SPACE_CADET);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        //#endregion
        dashboardHeader.add(Box.createRigidArea(new Dimension(5, 0)), gbc);

        refreshButton = new JButton();
        //#region refreshButton
        Icon refreshIcon = new ImageIcon("GUI/Icons/refresh_icon.png");
        refreshButton.setIcon(refreshIcon);
        refreshButton.setPreferredSize(new Dimension(24,24));
        refreshButton.setBorderPainted(false);
        refreshButton.setBackground(Colors.MANATEE);
        refreshButton.addActionListener(mainListener);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0;
        //#endregion
        dashboardHeader.add(refreshButton, gbc);
        
        dashboardLabel = new JLabel(" Student Dashboard");
        //#region dashboardLabel
        dashboardLabel.setFont(header1Font);
        dashboardLabel.setForeground(Colors.WHITE);
        dashboardLabel.setVerticalAlignment(SwingConstants.CENTER);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1;
        //#endregion
        dashboardHeader.add(dashboardLabel, gbc);
        
        accountButton = new JButton();
        //#region accountButton
        Icon userIcon = new ImageIcon("GUI/Icons/user_icon.png");
        accountButton.setIcon(userIcon);
        accountButton.setPreferredSize(new Dimension(24,24));
        accountButton.setBorderPainted(false);
        accountButton.setBackground(Colors.MANATEE);
        accountButton.addActionListener(mainListener);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 0;
        //#endregion
        dashboardHeader.add(accountButton, gbc);

        //#region Right Margin
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.weightx = 0;
        dashboardHeader.add(Box.createRigidArea(new Dimension(5, 0)), gbc);
        //#endregion

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        //#endregion
        mainGrid.add(dashboardHeader, gbc);

        courseNav = new JScrollPane();
        //#region courseNav

        //Header
        JLabel courseNavHeader = new JLabel("  Course Navigator");
        JPanel courseNavHeaderPanel = new JPanel();
        courseNavHeaderPanel.setBackground(Colors.INDEPENDENCE);
        courseNavHeader.setFont(header2Font);
        courseNavHeader.setForeground(Colors.WHITE);
        courseNavHeader.setBackground(Colors.INDEPENDENCE);
        courseNavHeader.setHorizontalAlignment(SwingConstants.CENTER);
        courseNavHeaderPanel.add(courseNavHeader);
        courseNav.setColumnHeaderView(courseNavHeaderPanel);
        courseNav.getColumnHeader().getView().setBackground(Colors.INDEPENDENCE);
        courseNav.setBackground(Colors.INDEPENDENCE);
        
        //CourseNavGrid
        courseNavGrid = new JPanel(new GridBagLayout());
        int i = 0;
        for(Course course : model.getCourses()) {
            for (String student : course.getStudents()) {
                if (student.equals(username)) {
                    JButton button = new JButton(course.getName());
                    button.setBackground(Colors.MANATEE);
                    button.setFont(textFont);
                    button.setMinimumSize(new Dimension(courseNav.getWidth(), 30));
                    button.setFocusPainted(false);
                    button.setHorizontalTextPosition(SwingConstants.LEFT);
                    button.addActionListener(courseListener);
                    JPopupMenu popup = new JPopupMenu();
                    gbc = new GridBagConstraints();
                    gbc.fill = GridBagConstraints.HORIZONTAL;
                    gbc.gridx = 0;
                    gbc.weightx = 1;
                    gbc.gridy = i;
                    i++;
                    popup.add("Leave Course");
                    button.setComponentPopupMenu(popup);
                    courseNavGrid.add(button, gbc);
                }
            }
        }
        //Empty Space Filler
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.gridy = i + 1;
        gbc.weighty = 1;
        courseNavGrid.add(new JLabel(), gbc);
        //Empty Space Filler

        courseNav.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        courseNav.setViewportView(courseNavGrid);
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridheight = GridBagConstraints.RELATIVE;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        gbc.weightx = 1;
        gbc.weighty = 1;
        //#endregion
        mainGrid.add(courseNav, gbc);

        workspace = new JScrollPane();
        //#region workspace
        workspace.setBackground(Colors.RHYTHM);
        workspace.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        var workspaceHeader = new JPanel();
        var workspaceHeaderLabel = new JLabel("Workspace");
        workspaceHeaderLabel.setFont(header2Font);
        workspaceHeaderLabel.setForeground(Colors.WHITE);
        workspaceHeader.add(workspaceHeaderLabel);
        workspaceHeader.setBackground(Colors.RHYTHM);
        workspace.setColumnHeaderView(workspaceHeader);


        //#endregion

        JPanel selectCoursePanel = new JPanel(new BorderLayout());
        JLabel selectCourseLabel = new JLabel("Select a Course.");
        selectCourseLabel.setFont(textFont);
        selectCourseLabel.setHorizontalAlignment(JLabel.CENTER);
        selectCourseLabel.setHorizontalTextPosition(JLabel.CENTER);
        selectCoursePanel.setBackground(Colors.SILVER_SAND);
        selectCoursePanel.add(selectCourseLabel, BorderLayout.CENTER);
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 7;
        gbc.weighty = 7;
        workspace.setViewportView(selectCoursePanel);

        mainGrid.add(workspace, gbc);

        //#region joinCourseButton
        joinCourseButton = new JButton("Join Course");
        joinCourseButton.setBackground(Colors.OCEAN_BLUE);
        joinCourseButton.setForeground(Colors.WHITE);
        joinCourseButton.setFont(textFont);
        joinCourseButton.setFocusPainted(false);
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.weightx = 0;
        gbc.weighty = 0;
        //#endregion
        mainGrid.add(joinCourseButton, gbc);


        content.add(mainGrid, BorderLayout.CENTER);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);
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
