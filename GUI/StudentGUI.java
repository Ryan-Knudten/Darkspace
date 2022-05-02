package GUI;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.DimensionUIResource;

import GUI.Colors.Colors;

import java.awt.Container;
import java.awt.event.*;
import java.awt.*;

import Model.Course;
import Model.Model;
import Model.Quiz;
import Model.Submission;
import Networking.Callback;
import Networking.Request;
import Networking.RequestType;

public class StudentGUI extends JComponent implements Runnable, GUI { //TODO: Implement GUI.java
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

    private JPanel takeQuizGrid;
    private JPanel viewQuizGrid;

    private JPanel quizNavGrid;
    private String selectedCourse;

    private JButton joinCourseButton;

    private JPanel dashboardHeader;
    //
    private JButton refreshButton;
    private JButton accountButton;
    private JLabel dashboardLabel;
    private JMenuItem deleteUserItem;

    private Font header1Font;
    private Font header2Font;
    private Font textFont;

    //#region Listeners
    private ActionListener mainListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == refreshButton) {
                Request request = new Request(RequestType.REFRESH, null);
                Callback callback = requestCallback(request);
                model = callback.getModel();

                courseNav.setViewportView(createCourseNavGrid()); 
                workspace.setViewportView(createQuizPanel(selectedCourse));
            }
            if(e.getSource() == accountButton) {
                int option = JOptionPane.showConfirmDialog(frame, "Would you like to log out?", "Darkspace", JOptionPane.YES_NO_OPTION);
                if(option == JOptionPane.YES_OPTION) {
                    frame.dispose();
                    LoginGUI gui = new LoginGUI(ois, oos);
                    SwingUtilities.invokeLater(gui);
                }
            }
            if (e.getSource() == deleteUserItem) {
                ArrayList<Object> data = new ArrayList<Object>();
                data.add(username);
                Request request = new Request(RequestType.DELETE_USER, data);
                Callback callback = requestCallback(request);
                model = callback.getModel();

                JOptionPane.showMessageDialog(frame, callback.getMessage(), "Darkspace", JOptionPane.INFORMATION_MESSAGE);
                frame.dispose();
                LoginGUI gui = new LoginGUI(ois, oos);
                SwingUtilities.invokeLater(gui);
            }
            if(e.getSource() == joinCourseButton) {
                ArrayList<String> availCourses = new ArrayList<String>();
                for (Course course : model.getCourses()) {
                    if (!course.getStudents().contains(username)) {
                        availCourses.add(course.getName());
                    }
                }
                if (availCourses.size() != 0) {
                    int option = JOptionPane.showOptionDialog(frame, "Choose a course to join:", "Darkspace", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, availCourses.toArray(), null);
                    if (option != -1) {
                        var data = new ArrayList<Object>();
                        data.add(availCourses.get(option));
                        data.add(username);
                        Request request = new Request(RequestType.JOIN_COURSE, data);
                        Callback callback = requestCallback(request);
                        model = callback.getModel();
                        courseNav.setViewportView(createCourseNavGrid()); 
                        workspace.setViewportView(createQuizPanel(selectedCourse));
                        JOptionPane.showMessageDialog(frame, callback.getMessage(), "Darkspace", JOptionPane.INFORMATION_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "No courses available to join", "Darkspace", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
    };
    
    private ActionListener quizActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JButton button = (JButton)e.getSource();
            String quizName = ((JLabel)button.getParent().getComponent(0)).getText();
            for (Course course : model.getCourses()) {
                if (course.getName().equals(selectedCourse)) {
                    for (Quiz quiz : course.getQuizzes()) {
                        if (quiz.getName().equals(quizName)) {
                            if (button.getText().equals("Take Quiz")) {
                                workspace.setViewportView(createTakeQuizPanel(quiz));
                                return;
                            } else {
                                if (!quiz.getSubmissions().get(username).getIsGraded()) {
                                    JOptionPane.showMessageDialog(frame, "Quiz not graded. Check back later.", "Darkspace", JOptionPane.ERROR_MESSAGE);
                                    return;
                                } else {
                                    workspace.setViewportView(createViewQuizPanel(quiz));
                                    return;
                                }
                            }
                            
                        }
                    }
                }
            }
            JOptionPane.showMessageDialog(frame, "Either the Course or Quiz was deleted.", "Darkspace", JOptionPane.ERROR_MESSAGE);
        }
    };

    private ActionListener courseListener = new ActionListener() {
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

    private ActionListener submitListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            var panel = ((JButton)e.getSource()).getParent();

            Submission submission = new Submission();
            ArrayList<String> responses = new ArrayList<String>();
            ArrayList<Integer> points = new ArrayList<Integer>();

            for (Component comp : panel.getComponents()) {
                if (comp instanceof JComboBox) {
                    var cb = (JComboBox<String>)comp;
                    responses.add((String)cb.getSelectedItem());
                    points.add(-1);
                }
            }

            submission.setResponses(responses);
            submission.setPoints(points);
            submission.setIsTaken(true);
            submission.setSubmissionTime(LocalDateTime.now());

            ArrayList<Object> data = new ArrayList<Object>();
            data.add(selectedCourse);
            data.add(((JLabel)panel.getComponent(0)).getText());
            data.add(username);
            data.add(submission);
            Request request = new Request(RequestType.TAKE_QUIZ, data);
            Callback callback = requestCallback(request);
            model = callback.getModel();

            if(!callback.getDidRequestWork()) {
                JOptionPane.showMessageDialog(frame, callback.getMessage(), "Darkspace", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, callback.getMessage(), "Darkspace", JOptionPane.INFORMATION_MESSAGE);
            }

            courseNav.setViewportView(createCourseNavGrid()); 
            workspace.setViewportView(createQuizPanel(selectedCourse));
        }
    };

    private ActionListener leaveCourseListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JMenuItem item = (JMenuItem)e.getSource();
            JPopupMenu popup = (JPopupMenu)item.getParent();
            ArrayList<Object> data = new ArrayList<Object>();
            data.add(popup.getName());
            data.add(username);
            Request request = new Request(RequestType.LEAVE_COURSE, data);
            Callback callback = requestCallback(request);
            model = callback.getModel();
            JOptionPane.showMessageDialog(frame, callback.getMessage(), "Darkspace", JOptionPane.INFORMATION_MESSAGE);
            if (selectedCourse != null && selectedCourse.equals(popup.getName())) {
                workspace.setViewportView(createSelectCoursePanel());
            }
            courseNav.setViewportView(createCourseNavGrid());
        }
    };
    //#endregion

    public StudentGUI(String username, Model model, ObjectOutputStream oos, ObjectInputStream ois) {
        this.username = username;
        this.model = model;
        this.oos = oos;
        this.ois = ois;

        // Callback callback = requestCallback(new Request(RequestType.REFRESH, null));
        // this.model = callback.getModel();

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

        courseNav.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        courseNav.setViewportView(createCourseNavGrid());
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

        //#region selectCourseLabel

        //#endregion

        workspace.setViewportView(createSelectCoursePanel());
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 7;
        gbc.weighty = 7;
        //#endregion
        mainGrid.add(workspace, gbc);

        //#region joinCourseButton
        joinCourseButton = new JButton("Join Course");
        joinCourseButton.setBackground(Colors.OCEAN_BLUE);
        joinCourseButton.setForeground(Colors.WHITE);
        joinCourseButton.setFont(textFont);
        joinCourseButton.setFocusPainted(false);
        joinCourseButton.addActionListener(mainListener);
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.weightx = 0;
        gbc.weighty = 0;
        //#endregion
        mainGrid.add(joinCourseButton, gbc);

        //#region usernameLabel
        JLabel usernameLabel = new JLabel(username + " ");

        JPopupMenu popup = new JPopupMenu();
        deleteUserItem = new JMenuItem("Delete User");
        deleteUserItem.addActionListener(mainListener);
        popup.add(deleteUserItem);
        usernameLabel.setComponentPopupMenu(popup);

        usernameLabel.setForeground(Colors.WHITE);
        usernameLabel.setFont(header2Font);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridy = 2;
        mainGrid.add(usernameLabel, gbc);
        //#endregion

        content.add(mainGrid, BorderLayout.CENTER);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);
    }

    private JPanel createCourseNavGrid() {
        //CourseNavGrid
        var gbc = new GridBagConstraints();
        courseNavGrid = new JPanel(new GridBagLayout());
        int i = 0;
        for(Course course : model.getCourses()) {
            for (String student : course.getStudents()) {
                if (student.equals(username)) {
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
                    button.addActionListener(courseListener);
                    JPopupMenu popup = new JPopupMenu();
                    JMenuItem leaveCourseItem = new JMenuItem();
                    leaveCourseItem.setText("Leave Course");
                    leaveCourseItem.addActionListener(leaveCourseListener);
                    popup.add(leaveCourseItem);
                    popup.setName(course.getName());
                    gbc = new GridBagConstraints();
                    gbc.fill = GridBagConstraints.HORIZONTAL;
                    gbc.gridx = 0;
                    gbc.weightx = 1;
                    gbc.gridy = i;
                    i++;
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
        return courseNavGrid;
    }

    private JPanel createViewQuizPanel(Quiz quiz) {
        GridBagLayout gbl = new GridBagLayout();

        takeQuizGrid = new JPanel(new GridBagLayout());
        takeQuizGrid.setBorder(new LineBorder(Colors.WHITE, 30));
        takeQuizGrid.setBackground(Colors.WHITE);
        var gbc = new GridBagConstraints();
        int i = 0;

        //#region Quiz Title
        JLabel quizTitle = new JLabel(quiz.getName());
        quizTitle.setFont(header2Font);
        gbc.gridy = i;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        i++;
        gbc.weightx = 1;
        takeQuizGrid.add(quizTitle, gbc);
        //#endregion

        Submission submission = quiz.getSubmissions().get(username);

        //#region Submission Time
        LocalDateTime time = submission.getSubmissionTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        JLabel timeLabel = new JLabel("Submitted at: " + formatter.format(time));
        timeLabel.setFont(textFont);
        gbc.gridy = i;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        i++;
        gbc.weightx = 1;
        takeQuizGrid.add(timeLabel, gbc);
        //#endregion

        //#region Total Score
        int score = 0;
        for(Integer s : submission.getPoints()) {
            score += s;
        }
        JLabel scoreLabel = new JLabel("Total Score: " + score);
        scoreLabel.setFont(textFont);
        gbc.gridy = i;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        i++;
        gbc.weightx = 1;
        takeQuizGrid.add(scoreLabel, gbc);
        //#endregion

        int questionIndex = 0;

        for (String question : quiz.getQuestions()) {
            gbc = new GridBagConstraints();
            gbc.gridy = i;
            i++;
            takeQuizGrid.add(Box.createVerticalStrut(30), gbc);

            String[] lines = question.split("\n"); 
            JLabel questionText = new JLabel("Question " + ((i - 1) / 7 + 1) + ": " + lines[0], SwingConstants.LEFT);
            questionText.setFont(header2Font);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            i++;
            takeQuizGrid.add(questionText, gbc);

            //#region Answer Choices
            JLabel aText = new JLabel(lines[1]);
            aText.setFont(textFont);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            i++;
            takeQuizGrid.add(aText, gbc);

            JLabel bText = new JLabel(lines[2]);
            bText.setFont(textFont);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            i++;
            takeQuizGrid.add(bText, gbc);

            JLabel cText = new JLabel(lines[3]);
            cText.setFont(textFont);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            i++;
            takeQuizGrid.add(cText, gbc);

            JLabel dText = new JLabel(lines[4]);
            dText.setFont(textFont);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            i++;
            takeQuizGrid.add(dText, gbc);

    
            //#endregion

            JLabel responseLabel = new JLabel("Your Response: " + submission.getResponses().get(questionIndex));
            responseLabel.setFont(textFont);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            i++;
            takeQuizGrid.add(responseLabel, gbc);

            JLabel pointsLabel = new JLabel("Points Earned: " + submission.getPoints().get(questionIndex));
            questionIndex++;
            pointsLabel.setFont(textFont);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            i++;
            takeQuizGrid.add(pointsLabel, gbc);
        }

        //#region Button Gap
        gbc = new GridBagConstraints();
        gbc.gridy = i;
        i++;
        takeQuizGrid.add(Box.createVerticalStrut(30), gbc);
        //#endregion

        
        //#endregion
        return takeQuizGrid;
    }

    private JPanel createSelectCoursePanel() {
        JPanel selectCoursePanel = new JPanel(new BorderLayout());
        JLabel selectCourseLabel = new JLabel("Select a Course.");
        selectCourseLabel.setFont(textFont);
        selectCourseLabel.setHorizontalAlignment(JLabel.CENTER);
        selectCourseLabel.setHorizontalTextPosition(JLabel.CENTER);
        selectCoursePanel.setBackground(Colors.SILVER_SAND);
        selectCoursePanel.add(selectCourseLabel, BorderLayout.CENTER);
        return selectCoursePanel;
    }

    private JPanel createTakeQuizPanel(Quiz quiz) {
        GridBagLayout gbl = new GridBagLayout();

        takeQuizGrid = new JPanel(new GridBagLayout());
        takeQuizGrid.setBorder(new LineBorder(Colors.WHITE, 30));
        takeQuizGrid.setBackground(Colors.WHITE);
        var gbc = new GridBagConstraints();
        int i = 0;

        //#region Quiz Title
        JLabel quizTitle = new JLabel(quiz.getName());
        quizTitle.setFont(header2Font);
        gbc.gridy = i;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        i++;
        gbc.weightx = 1;
        takeQuizGrid.add(quizTitle, gbc);
        //#endregion

        for (String question : quiz.getQuestions()) {
            gbc = new GridBagConstraints();
            gbc.gridy = i;
            i++;
            takeQuizGrid.add(Box.createVerticalStrut(30), gbc);

            String[] lines = question.split("\n"); 
            JLabel questionText = new JLabel("Question " + ((i - 1) / 7 + 1) + ": " + lines[0], SwingConstants.LEFT);
            questionText.setFont(header2Font);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            i++;
            takeQuizGrid.add(questionText, gbc);

            //#region Answer Choices
            JLabel aText = new JLabel(lines[1]);
            aText.setFont(textFont);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            i++;
            takeQuizGrid.add(aText, gbc);

            JLabel bText = new JLabel(lines[2]);
            bText.setFont(textFont);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            i++;
            takeQuizGrid.add(bText, gbc);

            JLabel cText = new JLabel(lines[3]);
            cText.setFont(textFont);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            i++;
            takeQuizGrid.add(cText, gbc);

            JLabel dText = new JLabel(lines[4]);
            dText.setFont(textFont);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            i++;
            takeQuizGrid.add(dText, gbc);

    
            //#endregion

            JComboBox<String> choices = new JComboBox<String>();
            choices.addItem("A");
            choices.addItem("B");
            choices.addItem("C");
            choices.addItem("D");
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            i++;

            takeQuizGrid.add(choices, gbc);
        }

        //#region Button Gap
        gbc = new GridBagConstraints();
        gbc.gridy = i;
        i++;
        takeQuizGrid.add(Box.createVerticalStrut(30), gbc);
        //#endregion

        //#region Submit button
        JButton submit = new JButton("Submit");
        //submit.addActionListener(submitListener);
        submit.setFont(header1Font);
        submit.setFocusPainted(false);
        submit.setBackground(Colors.OCEAN_BLUE);
        submit.setForeground(Colors.WHITE);
        submit.addActionListener(submitListener);
        gbc = new GridBagConstraints();
        gbc.gridy = i;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        takeQuizGrid.add(submit, gbc);
        //#endregion
        return takeQuizGrid;
    }

    private JPanel createQuizPanel(String courseName) {
        //QuizGrid
        boolean noQuiz = true;
        var quizGrid = new JPanel(new GridBagLayout());
        var gbc = new GridBagConstraints();
        int i = 0;
        for(Course course : model.getCourses()) {
            if (course.getName().equals(courseName)) {
                for (Quiz quiz : course.getQuizzes()) {
                    noQuiz = false;
                    JPanel quizPanel = new JPanel(new GridBagLayout());
                    quizPanel.setBackground(Colors.SILVER_SAND);
                    quizPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                    JLabel quizLabel = new JLabel(quiz.getName());
                    quizLabel.setFont(header2Font);
                    JButton quizAction = new JButton();
                    quizAction.setForeground(Colors.WHITE);
                    quizAction.setFocusPainted(false);
                    quizAction.setFont(header1Font);
                    quizAction.addActionListener(quizActionListener);
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
                    quizPanel.add(quizLabel, gbc);

                    //Add Button
                    gbc = new GridBagConstraints();
                    gbc.gridx = 1;
                    quizPanel.add(quizAction, gbc);

                    gbc = new GridBagConstraints();
                    gbc.fill = GridBagConstraints.HORIZONTAL;
                    gbc.gridx = 0;
                    gbc.weightx = 1;
                    gbc.gridy = i;
                    i++;
                    quizGrid.add(quizPanel, gbc);
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

        if (noQuiz) {
            quizGrid = createNoQuizPanel();
        }

        return quizGrid;
    }

    private JPanel createNoQuizPanel() {
        JPanel noQuizPanel = new JPanel(new BorderLayout());
        noQuizPanel.setBackground(Colors.SILVER_SAND);
        JLabel noQuizLabel = new JLabel("No Quizzes Available");
        noQuizLabel.setFont(textFont);
        noQuizLabel.setHorizontalAlignment(JLabel.CENTER);
        noQuizLabel.setHorizontalTextPosition(JLabel.CENTER);
        noQuizLabel.setBackground(Colors.SILVER_SAND);
        noQuizPanel.add(noQuizLabel);
        return noQuizPanel;
    }

    public Callback requestCallback(Request request) {
        try {
            oos.writeUnshared(request);
            oos.flush();
            return (Callback)ois.readUnshared();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Server Connection Ended.", "Darkspace", JOptionPane.ERROR_MESSAGE);
            frame.dispose();
            return null;
        }
    }
}
