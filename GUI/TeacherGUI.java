package GUI;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.plaf.DimensionUIResource;

import GUI.Colors.Colors;
import Model.Course;
import Model.Model;
import Model.Quiz;
import Networking.Callback;
import Networking.Request;
import Networking.RequestType;

public class TeacherGUI extends JComponent implements Runnable, GUI { //TODO: Implement GUI.java
    private Model model;
    private String username;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    private JFrame frame;

    private JPanel mainGrid;

    private JScrollPane courseNav;
    //
    private JPanel courseNavGrid;
    private JButton createCourseButton;

    private JPanel dashboardHeader;

    private JScrollPane workspace;

    private JPanel quizNavGrid;
    private String selectedCourse;
    private JPanel createQuizGrid;
    private JPanel gradeQuizGrid;
    private JPanel editQuizGrid;

    private JButton addQuizButton;
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

                courseNav.setViewportView(createCourseNavGrid()); 
            }
            if(e.getSource() == accountButton) {
                int option = JOptionPane.showConfirmDialog(frame, "Would you like to log out?", "Darkspace", JOptionPane.YES_NO_OPTION);
                if(option == JOptionPane.YES_OPTION) {
                    frame.dispose();
                    LoginGUI gui = new LoginGUI(ois, oos);
                    SwingUtilities.invokeLater(gui);
                }
            }
            if (e.getSource() == createCourseButton) {
                String courseName = JOptionPane.showInputDialog(frame, "Enter Course Name:", "Darkspace", JOptionPane.INFORMATION_MESSAGE);
                if (!courseName.equals("") && courseName != null) {
                    var data = new ArrayList<Object>();
                    data.add(courseName);
                    data.add(username);
                    Request request = new Request(RequestType.CREATE_COURSE, data);
                    Callback callback = requestCallback(request);
                    JOptionPane.showMessageDialog(frame, callback.getMessage(), "Darkspace", JOptionPane.INFORMATION_MESSAGE);
                    model = callback.getModel();
                    courseNav.setViewportView(createCourseNavGrid());
                } else {
                    JOptionPane.showMessageDialog(frame, "Course name cannot be empty.", "Darkspace", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    };
    private ActionListener createQuizListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            workspace.setViewportView(createCreateQuizPanel());
        }
    };

    private ActionListener submitCreatedQuizListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean hasEmpty = false;
            for (Component comp : createQuizGrid.getComponents()) {
                if (comp instanceof JTextField) {
                    JTextField field = (JTextField)comp;
                    if (field.getText().equals("") || field.getText() == null) {
                        hasEmpty = true;
                    }
                }  
            }
            if (hasEmpty) {
                JOptionPane.showMessageDialog(frame, "All fields must have entries.", "Darkspace", JOptionPane.ERROR_MESSAGE);
            } else if (createQuizGrid.getComponentCount() <= 7) {
                JOptionPane.showMessageDialog(frame, "A quiz must have at least one question.", "Darkspace", JOptionPane.ERROR_MESSAGE);
            } else {
                int numComps = createQuizGrid.getComponentCount();
                String quizTitle = ((JTextField)createQuizGrid.getComponent(1)).getText();
                Quiz quiz = new Quiz(quizTitle);
                ArrayList<String> questions = new ArrayList<String>();

                for(int i = 4; i < numComps - 3; i += 11) {
                    String questionText = ((JTextField)createQuizGrid.getComponent(i)).getText();
                    String AText = ((JTextField)createQuizGrid.getComponent(i + 2)).getText();
                    String BText = ((JTextField)createQuizGrid.getComponent(i + 4)).getText();
                    String CText = ((JTextField)createQuizGrid.getComponent(i + 6)).getText();
                    String DText = ((JTextField)createQuizGrid.getComponent(i + 8)).getText();

                    String q = questionText + "\nA. " + AText + "\nB. " + BText + "\nC. " + CText + "\nD. " + DText;
                    questions.add(q);
                }
                quiz.setQuestions(questions);

                var data = new ArrayList<Object>();
                data.add(selectedCourse);
                data.add(quiz);
                Request request = new Request(RequestType.CREATE_QUIZ, data);
                Callback callback = requestCallback(request);
                model = callback.getModel();

                courseNav.setViewportView(createCourseNavGrid());
                workspace.setViewportView(createQuizPanel(selectedCourse));
            }
        }
    };

    private ActionListener submitEditedQuizListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JButton button = (JButton)e.getSource();
            String quizName = button.getName();
            int numComps = editQuizGrid.getComponentCount();
            Quiz newQuiz = null;
            ArrayList<String> questions = new ArrayList<String>();

            for (Course course : model.getCourses()) {
                if (course.getName().equals(selectedCourse)) {
                    for (Quiz quiz : course.getQuizzes()) {
                        if (quiz.getName().equals(quizName)) {
                            newQuiz = new Quiz(quiz.getName());
                        }
                    }
                }
            }
            boolean hasEmpty = false;
            for (Component comp : editQuizGrid.getComponents()) {
                if (comp instanceof JTextField) {
                    JTextField field = (JTextField)comp;
                    if (field.getText().equals("") || field.getText() == null) {
                        hasEmpty = true;
                    }
                }  
            }
            if (hasEmpty) {
                JOptionPane.showMessageDialog(frame, "All fields must have entries.", "Darkspace", JOptionPane.ERROR_MESSAGE);
            } else if (editQuizGrid.getComponentCount() <= 6) {
                JOptionPane.showMessageDialog(frame, "A quiz must have at least one question.", "Darkspace", JOptionPane.ERROR_MESSAGE);
            } else {
                for(int i = 3; i < numComps - 3; i += 11) {
                    String questionText = ((JTextField)editQuizGrid.getComponent(i)).getText();
                    String AText = ((JTextField)editQuizGrid.getComponent(i + 2)).getText();
                    String BText = ((JTextField)editQuizGrid.getComponent(i + 4)).getText();
                    String CText = ((JTextField)editQuizGrid.getComponent(i + 6)).getText();
                    String DText = ((JTextField)editQuizGrid.getComponent(i + 8)).getText();
    
                    String q = questionText + "\nA. " + AText + "\nB. " + BText + "\nC. " + CText + "\nD. " + DText;
                    questions.add(q);
                }
                newQuiz.setQuestions(questions);

                var data = new ArrayList<Object>();
                data.add(selectedCourse);
                data.add(newQuiz);
                Request request = new Request(RequestType.EDIT_QUIZ, data);
                Callback callback = requestCallback(request);
                model = callback.getModel();

                courseNav.setViewportView(createCourseNavGrid());
                workspace.setViewportView(createQuizPanel(selectedCourse));
                JOptionPane.showMessageDialog(frame, callback.getMessage(), "Darkspace", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    };

    private ActionListener submitGradeListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean invalidInt = false;
            String quizName = "";
            String studentName = "";
            var comps = ((JPanel)((JButton)e.getSource()).getParent()).getComponents();
            ArrayList<Integer> points = new ArrayList<Integer>();
            for (Component comp : comps) {
                if (comp instanceof JTextField) {
                    JTextField box = (JTextField)comp;
                    try {
                        int score = Integer.parseInt(box.getText());
                        points.add(score);
                        JButton button = (JButton)e.getSource();
                        String name = button.getName();
                        quizName = name.substring(0, name.lastIndexOf("~"));
                        studentName = name.substring(name.lastIndexOf("~") + 1);
                    } catch (NumberFormatException ex) {
                        invalidInt = true;
                    }
                }
            }
            if (invalidInt) {
                JOptionPane.showMessageDialog(frame, "Point values must be integers and all questions must be scored.", "Darkspace", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Grade Submitted!", "Darkspace", JOptionPane.INFORMATION_MESSAGE);

                var data = new ArrayList<Object>();
                data.add(selectedCourse);
                data.add(quizName);
                data.add(studentName);
                data.add(points);

                Request request = new Request(RequestType.GRADE_QUIZ, data);
                Callback callback = requestCallback(request);
                model = callback.getModel();

                courseNav.setViewportView(createCourseNavGrid());
                workspace.setViewportView(createQuizPanel(selectedCourse));
            }
        }
    };

    private ActionListener deleteCourseListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JMenuItem item = (JMenuItem)e.getSource();
            JPopupMenu popup = (JPopupMenu)item.getParent();
            ArrayList<Object> data = new ArrayList<Object>();
            data.add(popup.getName());
            Request request = new Request(RequestType.DELETE_COURSE, data);
            Callback callback = requestCallback(request);
            model = callback.getModel();
            JOptionPane.showMessageDialog(frame, callback.getMessage(), "Darkspace", JOptionPane.INFORMATION_MESSAGE);
            if (selectedCourse != null && selectedCourse.equals(popup.getName())) {
                workspace.setViewportView(createSelectCoursePanel());
            }
            courseNav.setViewportView(createCourseNavGrid());
        }
    };

    private ActionListener deleteQuizListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JPopupMenu popup = (JPopupMenu)(((JMenuItem)e.getSource()).getParent());
            ArrayList<Object> data = new ArrayList<Object>();
            data.add(selectedCourse);
            data.add(popup.getName());

            Request request = new Request(RequestType.DELETE_QUIZ, data);
            Callback callback = requestCallback(request);

            model = callback.getModel();
            JOptionPane.showMessageDialog(frame, callback.getMessage(), "Darkspace", JOptionPane.INFORMATION_MESSAGE);

            courseNav.setViewportView(createCourseNavGrid());
            workspace.setViewportView(createQuizPanel(selectedCourse));
        }
    };

    private ActionListener editQuizListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JPopupMenu popup = (JPopupMenu)(((JMenuItem)e.getSource()).getParent());
            String quizName = popup.getName();
            for (Course course : model.getCourses()) {
                if (course.getName().equals(selectedCourse)) {
                    for (Quiz quiz : course.getQuizzes()) {
                        if (quiz.getName().equals(quizName)) {
                            workspace.setViewportView(createEditQuizPanel(quiz));
                        }
                    }
                }
            }
        }
    };

    private ActionListener addQuestionCreateListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
           
            var gbc = new GridBagConstraints();

            int compCount = createQuizGrid.getComponentCount();
            createQuizGrid.remove(compCount - 1);
            createQuizGrid.remove(compCount - 2);
            createQuizGrid.remove(compCount - 3);
            createQuizGrid.remove(compCount - 4);
            createQuizGrid.remove(compCount - 5);


            int i = createQuizGrid.getComponentCount();
            int questionNumber = (i - 2) / 11 + 1;

            gbc.gridy = i;
            i++;
            createQuizGrid.add(Box.createVerticalStrut(30), gbc);

            JLabel questionLabel = new JLabel("Question " + questionNumber + ":");
            questionLabel.setFont(header2Font);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            i++;
            createQuizGrid.add(questionLabel, gbc);

            JTextField questionBox = new JTextField();
            questionBox.setFont(header2Font);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            i++;
            createQuizGrid.add(questionBox, gbc);

            JLabel ALabel = new JLabel("Answer A:");
            ALabel.setFont(header2Font);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            i++;
            createQuizGrid.add(ALabel, gbc);

            JTextField ABox = new JTextField();
            ABox.setFont(header2Font);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            i++;
            createQuizGrid.add(ABox, gbc);

            JLabel BLabel = new JLabel("Answer B:");
            BLabel.setFont(header2Font);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            i++;
            createQuizGrid.add(BLabel, gbc);

            JTextField BBox = new JTextField();
            BBox.setFont(header2Font);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            i++;
            createQuizGrid.add(BBox, gbc);

            JLabel CLabel = new JLabel("Answer C:");
            CLabel.setFont(header2Font);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            i++;
            createQuizGrid.add(CLabel, gbc);

            JTextField CBox = new JTextField();
            CBox.setFont(header2Font);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            i++;
            createQuizGrid.add(CBox, gbc);

            JLabel DLabel = new JLabel("Answer D:");
            DLabel.setFont(header2Font);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            i++;
            createQuizGrid.add(DLabel, gbc);

            JTextField DBox = new JTextField();
            DBox.setFont(header2Font);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            i++;
            createQuizGrid.add(DBox, gbc);

            addCreateQuizFunctions();
            
            workspace.setViewportView(createQuizGrid);
        }
    };

    private ActionListener addQuestionEditListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
           
            String quizName = ((JButton)e.getSource()).getName();

            var gbc = new GridBagConstraints();

            int compCount = editQuizGrid.getComponentCount();
            editQuizGrid.remove(compCount - 1);
            editQuizGrid.remove(compCount - 2);
            editQuizGrid.remove(compCount - 3);
            editQuizGrid.remove(compCount - 4);
            editQuizGrid.remove(compCount - 5);


            int i = editQuizGrid.getComponentCount();
            int questionNumber = (i - 2) / 11 + 1;

            gbc.gridy = i;
            i++;
            editQuizGrid.add(Box.createVerticalStrut(30), gbc);

            JLabel questionLabel = new JLabel("Question " + questionNumber + ":");
            questionLabel.setFont(header2Font);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            i++;
            editQuizGrid.add(questionLabel, gbc);

            JTextField questionBox = new JTextField();
            questionBox.setFont(header2Font);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            i++;
            editQuizGrid.add(questionBox, gbc);

            JLabel ALabel = new JLabel("Answer A:");
            ALabel.setFont(header2Font);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            i++;
            editQuizGrid.add(ALabel, gbc);

            JTextField ABox = new JTextField();
            ABox.setFont(header2Font);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            i++;
            editQuizGrid.add(ABox, gbc);

            JLabel BLabel = new JLabel("Answer B:");
            BLabel.setFont(header2Font);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            i++;
            editQuizGrid.add(BLabel, gbc);

            JTextField BBox = new JTextField();
            BBox.setFont(header2Font);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            i++;
            editQuizGrid.add(BBox, gbc);

            JLabel CLabel = new JLabel("Answer C:");
            CLabel.setFont(header2Font);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            i++;
            editQuizGrid.add(CLabel, gbc);

            JTextField CBox = new JTextField();
            CBox.setFont(header2Font);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            i++;
            editQuizGrid.add(CBox, gbc);

            JLabel DLabel = new JLabel("Answer D:");
            DLabel.setFont(header2Font);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            i++;
            editQuizGrid.add(DLabel, gbc);

            JTextField DBox = new JTextField();
            DBox.setFont(header2Font);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            i++;
            editQuizGrid.add(DBox, gbc);



            
            addEditQuizFunctions(quizName);
            
            workspace.setViewportView(editQuizGrid);
        }
    };

    private ActionListener deleteQuestionCreateListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            int compCount = createQuizGrid.getComponentCount();

            if (compCount > 7) {
                createQuizGrid.remove(compCount - 6);
                createQuizGrid.remove(compCount - 7);
                createQuizGrid.remove(compCount - 8);
                createQuizGrid.remove(compCount - 9);
                createQuizGrid.remove(compCount - 10);
                createQuizGrid.remove(compCount - 11);
                createQuizGrid.remove(compCount - 12);
                createQuizGrid.remove(compCount - 13);
                createQuizGrid.remove(compCount - 14);
                createQuizGrid.remove(compCount - 15);
                createQuizGrid.remove(compCount - 16);
            }

            workspace.setViewportView(createQuizGrid);
        }
    };

    private ActionListener deleteQuestionEditListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            int compCount = editQuizGrid.getComponentCount();

            if (compCount > 8) {
                editQuizGrid.remove(compCount - 6);
                editQuizGrid.remove(compCount - 7);
                editQuizGrid.remove(compCount - 8);
                editQuizGrid.remove(compCount - 9);
                editQuizGrid.remove(compCount - 10);
                editQuizGrid.remove(compCount - 11);
                editQuizGrid.remove(compCount - 12);
                editQuizGrid.remove(compCount - 13);
                editQuizGrid.remove(compCount - 14);
                editQuizGrid.remove(compCount - 15);
                editQuizGrid.remove(compCount - 16);
            }

            workspace.setViewportView(editQuizGrid);
        }
    };


    private ActionListener quizActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {

            String quizName = ((JLabel)(((JComponent)e.getSource()).getParent().getComponent(0))).getText();
            Object[] options = {};
            Quiz selectedQuiz = null;
            for (Course course : model.getCourses()) {
                if (course.getName().equals(selectedCourse)) {
                    for (Quiz quiz : course.getQuizzes()) {
                        if (quiz.getName().equals(quizName)) {
                            selectedQuiz = quiz;
                            options = quiz.getSubmissions().keySet().toArray();
                        }
                    }
                }
            }
            if (options.length == 0) {
                JOptionPane.showMessageDialog(frame, "No students have taken this quiz yet.", "Darkspace", JOptionPane.ERROR_MESSAGE);
            } else {
                JComboBox<Object> combo = new JComboBox<Object>(options);
                int option = JOptionPane.showConfirmDialog(frame, combo, "Choose a student:", JOptionPane.OK_CANCEL_OPTION);
                if(option == JOptionPane.YES_OPTION) {
                    workspace.setViewportView(gradeQuizPanel(selectedQuiz, (String)combo.getSelectedItem()));
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
                    selectedCourse = course.getName();
                    workspace.setViewportView(createQuizPanel(button.getText()));
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
        createCourseButton = new JButton("Create Course");
        createCourseButton.setBackground(Colors.OCEAN_BLUE);
        createCourseButton.setForeground(Colors.WHITE);
        createCourseButton.setFont(textFont);
        createCourseButton.setFocusPainted(false);
        createCourseButton.addActionListener(mainListener);
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.weightx = 0;
        gbc.weighty = 0;
        //#endregion
        mainGrid.add(createCourseButton, gbc);

        //#region usernameLabel
        JLabel usernameLabel = new JLabel(username + "  ");
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
            if (course.getTeacher().equals(username)) {
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
                leaveCourseItem.setText("Delete Course");
                leaveCourseItem.addActionListener(deleteCourseListener);
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

    private JPanel gradeQuizPanel(Quiz quiz, String studentName) {
        GridBagLayout gbl = new GridBagLayout();

        gradeQuizGrid = new JPanel(new GridBagLayout());
        gradeQuizGrid.setBorder(new LineBorder(Colors.WHITE, 30));
        gradeQuizGrid.setBackground(Colors.WHITE);
        var gbc = new GridBagConstraints();
        int i = 0;

        //#region Quiz Title
        JLabel quizTitle = new JLabel(quiz.getName());
        quizTitle.setFont(header2Font);
        gbc.gridy = i;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        i++;
        gbc.weightx = 1;
        gradeQuizGrid.add(quizTitle, gbc);
        //#endregion

        JLabel studentLabel = new JLabel("Student: " + studentName);
        studentLabel.setFont(header2Font);
        gbc.gridy = i;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        i++;
        gbc.weightx = 1;
        gradeQuizGrid.add(studentLabel, gbc);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        JLabel timeLabel = new JLabel("Submitted At: " + formatter.format(quiz.getSubmissions().get(studentName).getSubmissionTime()));
        timeLabel.setFont(header2Font);
        gbc.gridy = i;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        i++;
        gbc.weightx = 1;
        gradeQuizGrid.add(timeLabel, gbc);

        ArrayList<String> questions = quiz.getQuestions();
        var responses = quiz.getSubmissions().get(studentName).getResponses();
        for (int j = 0; j < questions.size(); j++) {
            gbc = new GridBagConstraints();
            gbc.gridy = i;
            i++;
            gradeQuizGrid.add(Box.createVerticalStrut(30), gbc);

            String[] lines = questions.get(j).split("\n"); 
            JLabel questionText = new JLabel("Question " + ((i - 1) / 7 + 1) + ": " + lines[0], SwingConstants.LEFT);
            questionText.setFont(header2Font);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            i++;
            gradeQuizGrid.add(questionText, gbc);

            //#region Answer Choices
            JLabel aText = new JLabel(lines[1]);
            aText.setFont(textFont);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            i++;
            gradeQuizGrid.add(aText, gbc);

            JLabel bText = new JLabel(lines[2]);
            bText.setFont(textFont);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            i++;
            gradeQuizGrid.add(bText, gbc);

            JLabel cText = new JLabel(lines[3]);
            cText.setFont(textFont);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            i++;
            gradeQuizGrid.add(cText, gbc);

            JLabel dText = new JLabel(lines[4]);
            dText.setFont(textFont);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            i++;
            gradeQuizGrid.add(dText, gbc);

            JLabel responseLabel = new JLabel("Student Response:");
            responseLabel.setFont(header2Font);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            i++;
            gradeQuizGrid.add(responseLabel, gbc);

            JLabel response = new JLabel(responses.get(j));
            response.setFont(header2Font);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            i++;
            gradeQuizGrid.add(response, gbc);

            //#endregion

            JLabel pointLabel = new JLabel("Points:");
            pointLabel.setFont(header2Font);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            i++;
            gradeQuizGrid.add(pointLabel, gbc);

            JTextField pointBox = new JTextField();
            pointBox.setFont(header2Font);
            pointBox.setPreferredSize(new Dimension(35, 35));
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            i++;

            gradeQuizGrid.add(pointBox, gbc);
        }

        //#region Button Gap
        gbc = new GridBagConstraints();
        gbc.gridy = i;
        i++;
        gradeQuizGrid.add(Box.createVerticalStrut(30), gbc);
        //#endregion

        //#region Submit button
        JButton submit = new JButton("Submit Grade");
        submit.setFont(header1Font);
        submit.setFocusPainted(false);
        submit.setBackground(Colors.OCEAN_BLUE);
        submit.setForeground(Colors.WHITE);
        submit.addActionListener(submitGradeListener);
        submit.setName(quiz.getName() + "~" + studentName);
        gbc = new GridBagConstraints();
        gbc.gridy = i;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gradeQuizGrid.add(submit, gbc);
        //#endregion
        return gradeQuizGrid;
    }

    private JPanel createCreateQuizPanel() {
        GridBagLayout gbl = new GridBagLayout();

        createQuizGrid = new JPanel(new GridBagLayout());
        createQuizGrid.setBorder(new LineBorder(Colors.WHITE, 30));
        createQuizGrid.setBackground(Colors.WHITE);
        var gbc = new GridBagConstraints();
        int i = 0;

        //#region Quiz Title
        JLabel quizTitleLabel = new JLabel("Enter Quiz Name:");
        quizTitleLabel.setFont(header2Font);
        gbc.gridy = i;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        i++;
        gbc.weightx = 1;
        createQuizGrid.add(quizTitleLabel, gbc);

        JTextField quizTitleBox = new JTextField();
        quizTitleBox.setFont(header2Font);
        gbc.gridy = i;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        i++;
        gbc.weightx = 1;
        createQuizGrid.add(quizTitleBox, gbc);
        //#endregion

        addCreateQuizFunctions();

        return createQuizGrid;
    }

    private JPanel createEditQuizPanel(Quiz quiz) {

        editQuizGrid = new JPanel(new GridBagLayout());
        editQuizGrid.setBorder(new LineBorder(Colors.WHITE, 30));
        editQuizGrid.setBackground(Colors.WHITE);
        var gbc = new GridBagConstraints();
        int j = 0;

        JLabel quizTitleLabel = new JLabel(quiz.getName());
        quizTitleLabel.setFont(header2Font);
        gbc.gridy = j;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        j++;
        gbc.weightx = 1;
        editQuizGrid.add(quizTitleLabel, gbc);

        addEditQuizFunctions(quiz.getName());

        for (String question : quiz.getQuestions()) {
            String quizName = quiz.getName();

            gbc = new GridBagConstraints();

            int compCount = editQuizGrid.getComponentCount();
            editQuizGrid.remove(compCount - 1);
            editQuizGrid.remove(compCount - 2);
            editQuizGrid.remove(compCount - 3);
            editQuizGrid.remove(compCount - 4);
            editQuizGrid.remove(compCount - 5);

            var text = question.split("\n");
            var questionText = text[0];
            var AText = text[1].substring(3);
            var BText = text[2].substring(3);
            var CText = text[3].substring(3);
            var DText = text[4].substring(3);


            int i = editQuizGrid.getComponentCount();
            int questionNumber = (i - 2) / 11 + 1;

            gbc.gridy = i;
            i++;
            editQuizGrid.add(Box.createVerticalStrut(30), gbc);

            JLabel questionLabel = new JLabel("Question " + questionNumber + ":");
            questionLabel.setFont(header2Font);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            i++;
            editQuizGrid.add(questionLabel, gbc);

            JTextField questionBox = new JTextField(questionText);
            questionBox.setFont(header2Font);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            i++;
            editQuizGrid.add(questionBox, gbc);

            JLabel ALabel = new JLabel("Answer A:");
            ALabel.setFont(header2Font);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            i++;
            editQuizGrid.add(ALabel, gbc);

            JTextField ABox = new JTextField(AText);
            ABox.setFont(header2Font);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            i++;
            editQuizGrid.add(ABox, gbc);

            JLabel BLabel = new JLabel("Answer B:");
            BLabel.setFont(header2Font);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            i++;
            editQuizGrid.add(BLabel, gbc);

            JTextField BBox = new JTextField(BText);
            BBox.setFont(header2Font);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            i++;
            editQuizGrid.add(BBox, gbc);

            JLabel CLabel = new JLabel("Answer C:");
            CLabel.setFont(header2Font);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            i++;
            editQuizGrid.add(CLabel, gbc);

            JTextField CBox = new JTextField(CText);
            CBox.setFont(header2Font);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            i++;
            editQuizGrid.add(CBox, gbc);

            JLabel DLabel = new JLabel("Answer D:");
            DLabel.setFont(header2Font);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            i++;
            editQuizGrid.add(DLabel, gbc);

            JTextField DBox = new JTextField(DText);
            DBox.setFont(header2Font);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridy = i;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            i++;
            editQuizGrid.add(DBox, gbc);



            
            addEditQuizFunctions(quizName);
        }

        return editQuizGrid;
    }

    private void addCreateQuizFunctions() {
        //#region Button Gap
        int i = createQuizGrid.getComponentCount();
        var gbc = new GridBagConstraints();
        gbc.gridy = i;
        i++;
        createQuizGrid.add(Box.createVerticalStrut(30), gbc);
        //#endregion

        JButton addQuestion = new JButton("Add Question");
        addQuestion.setFont(header1Font);
        addQuestion.setFocusPainted(false);
        addQuestion.setBackground(Colors.OCEAN_BLUE);
        addQuestion.setForeground(Colors.WHITE);
        addQuestion.addActionListener(addQuestionCreateListener);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = i;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        createQuizGrid.add(addQuestion, gbc);

        JButton deleteQuestion = new JButton("Delete Question");
        deleteQuestion.setFont(header1Font);
        deleteQuestion.setFocusPainted(false);
        deleteQuestion.setBackground(Colors.OCEAN_BLUE);
        deleteQuestion.setForeground(Colors.WHITE);
        deleteQuestion.addActionListener(deleteQuestionCreateListener);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = i;
        i++;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        createQuizGrid.add(deleteQuestion, gbc);

        //#region Button Gap
        gbc = new GridBagConstraints();
        gbc.gridy = i;
        i++;
        createQuizGrid.add(Box.createVerticalStrut(30), gbc);
        //#endregion

        //#region Submit button
        JButton submit = new JButton("Confirm");
        //submit.addActionListener(submitListener);
        submit.setFont(header1Font);
        submit.setFocusPainted(false);
        submit.setBackground(Colors.OCEAN_BLUE);
        submit.setForeground(Colors.WHITE);
        submit.addActionListener(submitCreatedQuizListener);
        gbc = new GridBagConstraints();
        gbc.gridy = i;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        createQuizGrid.add(submit, gbc);
    }

    private void addEditQuizFunctions(String quizName) {
        //#region Button Gap
        int i = editQuizGrid.getComponentCount();
        var gbc = new GridBagConstraints();
        gbc.gridy = i;
        i++;
        editQuizGrid.add(Box.createVerticalStrut(30), gbc);
        //#endregion

        JButton addQuestion = new JButton("Add Question");
        addQuestion.setFont(header1Font);
        addQuestion.setFocusPainted(false);
        addQuestion.setBackground(Colors.OCEAN_BLUE);
        addQuestion.setForeground(Colors.WHITE);
        addQuestion.addActionListener(addQuestionEditListener);
        addQuestion.setName(quizName);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = i;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        editQuizGrid.add(addQuestion, gbc);

        JButton deleteQuestion = new JButton("Delete Question");
        deleteQuestion.setFont(header1Font);
        deleteQuestion.setFocusPainted(false);
        deleteQuestion.setBackground(Colors.OCEAN_BLUE);
        deleteQuestion.setForeground(Colors.WHITE);
        deleteQuestion.addActionListener(deleteQuestionEditListener);
        deleteQuestion.setName(quizName);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = i;
        i++;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        editQuizGrid.add(deleteQuestion, gbc);

        //#region Button Gap
        gbc = new GridBagConstraints();
        gbc.gridy = i;
        i++;
        editQuizGrid.add(Box.createVerticalStrut(30), gbc);
        //#endregion

        //#region Submit button
        JButton submit = new JButton("Confirm");
        //submit.addActionListener(submitListener);
        submit.setFont(header1Font);
        submit.setFocusPainted(false);
        submit.setBackground(Colors.OCEAN_BLUE);
        submit.setForeground(Colors.WHITE);
        submit.addActionListener(submitEditedQuizListener);
        submit.setName(quizName);
        gbc = new GridBagConstraints();
        gbc.gridy = i;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        editQuizGrid.add(submit, gbc);
    }

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

                    JPopupMenu popup = new JPopupMenu();
                    JMenuItem editQuizItem = new JMenuItem();
                    editQuizItem.setText("Edit Quiz");
                    editQuizItem.addActionListener(editQuizListener);
                    JMenuItem deleteQuizItem = new JMenuItem();
                    deleteQuizItem.setText("Delete Quiz");
                    deleteQuizItem.addActionListener(deleteQuizListener);
                    popup.add(editQuizItem);
                    popup.add(deleteQuizItem);
                    popup.setName(quiz.getName());
                    quizPanel.setComponentPopupMenu(popup);                    
                
                    JLabel quizLabel = new JLabel(quiz.getName());
                    quizLabel.setFont(header2Font);
                    JButton quizAction = new JButton();
                    quizAction.setForeground(Colors.WHITE);
                    quizAction.setFocusPainted(false);
                    quizAction.setFont(header1Font);
                    quizAction.addActionListener(quizActionListener);
                    quizAction.setBackground(Colors.OCEAN_BLUE);
                    quizAction.setPreferredSize(new DimensionUIResource(200, 50));
                    quizAction.setText("Grade");

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
        //Add Quiz Button
        addQuizButton = new JButton("Create New Quiz");
        addQuizButton.setForeground(Colors.WHITE);
        addQuizButton.setFocusPainted(false);
        addQuizButton.setFont(header1Font);
        addQuizButton.addActionListener(createQuizListener);
        addQuizButton.setBackground(Colors.OCEAN_BLUE);
        addQuizButton.setPreferredSize(new DimensionUIResource(200, 50));
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.gridy = i + 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        quizGrid.add(addQuizButton, gbc);

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
