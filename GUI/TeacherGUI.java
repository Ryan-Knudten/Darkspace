package GUI;
//#region imports

import java.io.*;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
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
import Model.Quiz;
import Networking.Callback;
import Networking.Request;
import Networking.RequestType;
//#endregion


public class TeacherGUI extends JComponent implements Runnable { //TODO: Implement GUI.java
    private Model model;
    private String username;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private ArrayList<Object> data = new ArrayList<Object>();
    private String selectedCourse;
    private JFrame frame;
    private Quiz quiz;

    private JPanel mainGrid;

    private JScrollPane courseNav;
    //
    private JPanel courseNavGrid;

    private JScrollPane workspace;

    private JPanel quizNavGrid;

    private JButton addCourseButton;

    private JPanel dashboardHeader;
    //
    private JButton refreshButton;
    private JButton accountButton;
    private JLabel dashboardLabel;

    private Font header1Font;
    private Font header2Font;
    private Font textFont;
    public static ArrayList<String> courses;
    public static ArrayList<String> quizzes;

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
    private ActionListener quizWindowListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JButton button = (JButton)e.getSource();
            for(Course course : model.getCourses()) {
                if(course.getName().equals(button.getText())) {
                    selectedCourse = course.getName();
                    if (course.getQuizzes().size() == 0) {
                        workspace.setViewportView(createNoQuizPanel());
                    } else {
                        workspace.setViewportView(createQuizPanel(button.getText()));
                    }
                }
            }
            for(Component comp : courseNavGrid.getComponents()) {
                if (comp instanceof JButton) {
                    JButton b = (JButton)comp;
                    if (b.getText().equals(selectedCourse)) {
                        b.setBackground(Colors.RHYTHM);
                        frame.repaint();
                    } else {
                        b.setBackground(Colors.MANATEE);
                        courseNavGrid.repaint();
                        frame.repaint();
                    }
                }
            }
        }
    };

    private JPanel createQuizPanel(String courseName) {
        //QuizGrid
        var quizGrid = new JPanel(new GridBagLayout());
        var gbc = new GridBagConstraints();
        int i = 0;
        for(Course course : model.getCourses()) {
            if (course.getName().equals(courseName)) {
                for (Quiz quiz : course.getQuizzes()) {
                    JPanel quizPanel = new JPanel(new GridBagLayout());
                    quizPanel.setBackground(Colors.SILVER_SAND);
                    quizPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                    //JLabel quizLabel = new JLabel(quiz.getName());
                    //quizLabel.setFont(header2Font);
                    JButton quizAction = new JButton();
                    quizAction.setForeground(Colors.WHITE);
                    quizAction.setFocusPainted(false);
                    quizAction.setFont(header1Font);
                    //quizAction.addActionListener(quizActionListener);
                    quizAction.setBackground(Colors.OCEAN_BLUE);
                    quizAction.setPreferredSize(new DimensionUIResource(200, 50));
                    if (quiz.getSubmissions().containsKey(username)) {
                        if (quiz.getSubmissions().get(username).getIsTaken()) {
                            quizAction.setText("View Score");
                        }
                    } else {
                        quizAction.setText("Take Quiz");
                    }

                    //Add Label
                    gbc = new GridBagConstraints();
                    gbc.gridx = 0;
                    gbc.weightx = 1;
                    //quizPanel.add(quizLabel, gbc);

                    //Add Button
                    gbc = new GridBagConstraints();
                    gbc.gridx = 1;
                    //quizPanel.add(quizAction, gbc);

                    gbc = new GridBagConstraints();
                    gbc.fill = GridBagConstraints.HORIZONTAL;
                    gbc.gridx = 0;
                    gbc.weightx = 1;
                    gbc.gridy = i;
                    i++;
                    //quizGrid.add(quizPanel, gbc);
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
        quizGrid.add(new JLabel(), gbc);
        return quizGrid;
    }

    private ActionListener leaveCourseListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JMenuItem item = (JMenuItem)e.getSource();
            JPopupMenu popup = (JPopupMenu)item.getParent();
            ArrayList<Object> data = new ArrayList<Object>();
            
            data.add(popup.getName());
            data.add(username);
            Request request = new Request(RequestType.DELETE_COURSE, data);
            Callback callback = requestCallback(request);
            model = callback.getModel();
            if(!callback.getDidRequestWork()) {
                JOptionPane.showMessageDialog(frame, callback.getMessage(), "Darkspace", JOptionPane.ERROR_MESSAGE);
            }
        }
    };

    private ActionListener courseListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            ArrayList<Object> data = new ArrayList<Object>();
            String newCourse = JOptionPane.showInputDialog(frame, "Enter name of course.");
                data.add(newCourse);
                data.add(username);
                Request request = new Request(RequestType.CREATE_COURSE, data);
                Callback callback = requestCallback(request);
                model = callback.getModel();
                if (!callback.getDidRequestWork()) {
                    JOptionPane.showMessageDialog(frame, callback.getMessage(), "Darkspace", JOptionPane.ERROR_MESSAGE); 
                } else {
                    courseNav.setViewportView(createCourseNavGrid());
                    var gbc = new GridBagConstraints();
                    gbc.fill = GridBagConstraints.BOTH;
                    gbc.gridheight = GridBagConstraints.RELATIVE;
                    gbc.gridx = 0;
                    gbc.gridy = 1;
                    gbc.anchor = GridBagConstraints.SOUTHWEST;
                    gbc.weightx = 1;
                    gbc.weighty = 1;
                    mainGrid.add(courseNav, gbc);
                }
        }
    }; 
    
    private JPanel createCourseNavGrid() {
        //CourseNavGrid
        var gbc = new GridBagConstraints();
        courseNavGrid = new JPanel(new GridBagLayout());
        int i = 0;
        for (Course course : model.getCourses()) {
            //if (course.getTeacher().equals(username)) {
                    JButton button = new JButton(course.getName());
                    if(selectedCourse != null && selectedCourse.equals(course.getName())) {
                        button.setBackground(Colors.RHYTHM);
                    } else {
                        button.setBackground(Colors.MANATEE);
                    }
                    button.setFont(textFont);
                    button.setMinimumSize(new Dimension(courseNav.getWidth(), 30));
                    button.setFocusPainted(false);
                    button.setHorizontalTextPosition(SwingConstants.LEFT);
                    //button.addActionListener(courseListener);
                    JPopupMenu popup = new JPopupMenu();
                    JMenuItem leaveCourseItem = new JMenuItem();
                    leaveCourseItem.setText("Delete Course");
                    leaveCourseItem.addActionListener(leaveCourseListener);
                    popup.add(leaveCourseItem);
                    gbc = new GridBagConstraints();
                    gbc.fill = GridBagConstraints.HORIZONTAL;
                    gbc.gridx = 0;
                    gbc.weightx = 1;
                    gbc.gridy = i;
                    i++;
                    button.setComponentPopupMenu(popup);
                    courseNavGrid.add(button, gbc);
                    
               // }
        }
        //Empty Space Filler
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.gridy = i + 1;
        gbc.weighty = 1;
        courseNavGrid.add(new JLabel(), gbc);
        System.out.println("15");
        //Empty Space Filler
        return courseNavGrid;
    }
    private JPanel createNoQuizPanel() {
        JPanel noQuizPanel = new JPanel(new BorderLayout());
        JLabel noQuizLabel = new JLabel("No Quizzes Available");
        noQuizLabel.setFont(textFont);
        noQuizLabel.setHorizontalAlignment(JLabel.CENTER);
        noQuizLabel.setHorizontalTextPosition(JLabel.CENTER);
        noQuizLabel.setBackground(Colors.SILVER_SAND);
        noQuizPanel.add(noQuizLabel);
        return noQuizPanel;
    }



    public TeacherGUI(String username, Model model, ObjectOutputStream oos, ObjectInputStream ois) {
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
        
        dashboardLabel = new JLabel(" Teacher Dashboard");
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
                    button.addActionListener(quizWindowListener);
                    JPopupMenu popup = new JPopupMenu();
                    gbc = new GridBagConstraints();
                    gbc.fill = GridBagConstraints.HORIZONTAL;
                    gbc.gridx = 0;
                    gbc.weightx = 1;
                    gbc.gridy = i;
                    i++;
                    popup.add("Delete Quiz");
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

        courseNav.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
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
        workspace.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
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
        addCourseButton = new JButton("Add Course");
        addCourseButton.addActionListener(courseListener);
        addCourseButton.setBackground(Colors.OCEAN_BLUE);
        addCourseButton.setForeground(Colors.WHITE);
        addCourseButton.setFont(textFont);
        addCourseButton.setFocusPainted(false);
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.weightx = 0;
        gbc.weighty = 0;
        //#endregion
        mainGrid.add(addCourseButton, gbc);


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
