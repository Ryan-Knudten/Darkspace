package DataControl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import Model.Course;
import Model.Model;
import Model.Quiz;
import Model.Submission;
import Networking.Callback;
import Networking.Request;
import Networking.RequestType;

public class Controller {
    private Model model;

    public Controller(Model model) {
        this.model = model;
    }

    // #region Get/Set
    public Model getModel() {
        return this.model;
    }

    public void setModel(Model model) {
        this.model = model;
    }
    // #endregion

    public synchronized Callback handleRequest(Request request) {
        Callback callback;
        switch (request.getRequestType()) {
            case CREATE_USER:
                String userName = (String) request.getData().get(0);
                String password = (String) request.getData().get(1);
                String userType = (String) request.getData().get(2);
                callback = createUser(userName, password, userType);
                break;

            case LOGIN_USER:
                userName = (String) request.getData().get(0);
                password = (String) request.getData().get(1);
                callback = loginUser(userName, password);
                break;

            case DELETE_USER:
                userName = (String) request.getData().get(0);
                callback = deleteUser(userName);
                break;

            case CREATE_COURSE:
                String name = (String) request.getData().get(0);
                String teacher = (String) request.getData().get(1);
                callback = createCourse(name, teacher);
                break;

            case DELETE_COURSE:
                String courseName = (String) request.getData().get(0);
                callback = deleteCourse(courseName);
                break;

            case CREATE_QUIZ:
                courseName = (String) request.getData().get(0);
                Quiz quiz = (Quiz) request.getData().get(1);
                callback = createQuiz(courseName, quiz);
                break;

            case JOIN_COURSE:
                courseName = (String) request.getData().get(0);
                name = (String) request.getData().get(1);
                callback = joinCourse(courseName, name);
                break;

            case LEAVE_COURSE:
                courseName = (String) request.getData().get(0);
                name = (String) request.getData().get(1);
                callback = leaveCourse(courseName, name);
                break;

            case EDIT_QUIZ:
                courseName = (String) request.getData().get(0);
                quiz = (Quiz) request.getData().get(1);
                callback = editQuiz(courseName, quiz);
                break;

            case DELETE_QUIZ:
                courseName = (String) request.getData().get(0);
                var quizName = (String) request.getData().get(1);
                callback = deleteQuiz(courseName, quizName);
                break;

            case GRADE_QUIZ:
                courseName = (String) request.getData().get(0);
                quizName = (String) request.getData().get(1);
                var studentName = (String) request.getData().get(2);
                var points = (ArrayList<Integer>) request.getData().get(3);
                callback = gradeQuiz(courseName, quizName, studentName, points);
                break;

            case TAKE_QUIZ:
                courseName = (String) request.getData().get(0);
                quizName = (String) request.getData().get(1);
                userName = (String) request.getData().get(2);
                Submission submission = (Submission) request.getData().get(3);
                callback = takeQuiz(courseName, quizName, userName, submission);
                break;

            case REFRESH:
                callback = new Callback(model, true, "Refresh Successful");
                break;

            default:
                return null;
        }
        AppData.saveData(model, "AppData.txt");
        return callback;
    }

    //#region Functions
    public Callback createCourse(String name, String teacher) {
        for (Course course : model.getCourses()) {
            if (course.getName().equals(name)) {
                return new Callback(model, false, "Course already exists");
            }
        }
        model.getCourses().add(new Course(name, teacher));
        return new Callback(model, true, "Course Created!");
    }

    public Callback deleteUser(String username) {
        for (String student : model.getStudents().keySet()) {
            if (student.equals(username)) {
                model.getStudents().remove(username);
                for (Course course : model.getCourses()) {
                    for (String studentName : course.getStudents()) {
                        if (studentName.equals(username)) {
                            course.getStudents().remove(studentName);
                        }
                    }
                    for (Quiz quiz : course.getQuizzes()) {
                        if (quiz.getSubmissions().containsKey(username)) {
                            quiz.getSubmissions().remove(username);
                        }
                    }
                }
                return new Callback(model, true, "User account \"" + username + "\" deleted successsfully.");
            }
        }
        for (String teacher : model.getTeachers().keySet()) {
            if (teacher.equals(username)) {
                model.getTeachers().remove(username);
                for (Course course : model.getCourses()) {
                    if (course.getTeacher().equals(username)) {
                        model.getCourses().remove(course);
                    }
                }
                return new Callback(model, true, "User account \"" + username + "\" deleted successsfully.");
            }
        }
        return new Callback(model, false, "This user does not exist.");
    }

    public Callback createUser(String userName, String password, String userType) {
        Set<String> studentKeys = model.getStudents().keySet();
        Set<String> teacherKeys = model.getTeachers().keySet();

        for(String key : studentKeys) {
            if(userName.equals(key)) {
                return new Callback(model, false, "Account already exists!");
            }
        }
        for(String key : teacherKeys) {
            if(userName.equals(key)) {
                return new Callback(model, false, "Account already exists!");
            }
        }
        if (userType.equals("Student")) {
            model.getStudents().put(userName, password);
            return new Callback(model, true, "Account Created!");
        } else {
            model.getTeachers().put(userName, password);
            return new Callback(model, true, "Account Created!");
        }
    }

    public Callback leaveCourse(String courseName, String username) {
        for (Course course : model.getCourses()) {
            if (course.getName().equals(courseName)) {
                for (String student : course.getStudents()) {
                    if (student.equals(username)) {
                        course.getStudents().remove(student);
                        for (Quiz quiz : course.getQuizzes()) {
                            if (quiz.getSubmissions().containsKey(username)) {
                                quiz.getSubmissions().remove(username);
                            }
                        }
                        return new Callback(model, true, "You have left the course.");
                    }
                }
                return new Callback(model, false, "You were not in this course.");
            }
        }
        return new Callback(model, false, "This course does not exist.");
    }

    public Callback loginUser(String userName, String password) {
        Set<String> studentKeys = model.getStudents().keySet();
        var students = model.getStudents();

        Set<String> teacherKeys = model.getTeachers().keySet();
        var teachers = model.getTeachers();

        if (studentKeys.contains(userName)) {
            if (password.equals(students.get(userName))) {
                return new Callback(model, true, "success");
            } else {
                return new Callback(model, false, "Incorrect password.");
            }
        }
        if (teacherKeys.contains(userName)) {
            if (password.equals(teachers.get(userName))) {
                return new Callback(model, true, "success");
            } else {
                return new Callback(model, false, "Incorrect password.");
            }
        }
        return new Callback(model, false, "Invalid username");
    }

    public Callback joinCourse(String courseName, String userName) {
        for (Course course : model.getCourses()) {
            if (course.getName().equals(courseName)) {
                for (String student : course.getStudents()) {
                    if (student.equals(userName)) {
                        return new Callback(model, false, "You are already in this class.");
                    }
                }
                course.getStudents().add(userName);
                course.setStudents(course.getStudents());
                return new Callback(this.model, true, "Joined class successfully");
            }
        }
        return new Callback(model, false, "Course does not exist.");
    }

    public Callback deleteCourse(String name) {
        for (Course course : model.getCourses()) {
            if (course.getName().equals(name)) {
                model.getCourses().remove(course);
                return new Callback(model, true, "Course successfully deleted.");
            }
        }
        return new Callback(model, false, "Course does not exist");
    }

    public Callback createQuiz(String courseName, Quiz quiz) {
        for (Course course : model.getCourses()) {
            if (course.getName().equals(courseName)) {
                for (Quiz courseQuiz : course.getQuizzes()) {
                    if (courseQuiz.getName().equals(quiz.getName())) {
                        return new Callback(model, false, "Quiz already exists");
                    }
                }
                course.getQuizzes().add(quiz);
                return new Callback(model, true, "Quiz added successfully");
            }
        }
        return new Callback(model, false, "Course does not exist.");
    }

    public Callback editQuiz(String courseName, Quiz newQuiz) {
        for (Course course : model.getCourses()) {
            if (course.getName().equals(courseName)) {
                for (Quiz prevQuiz : course.getQuizzes()) {
                    if (prevQuiz.getName().equals(newQuiz.getName())) {
                        course.getQuizzes().remove(prevQuiz);
                        course.getQuizzes().add(newQuiz);
                        return new Callback(model, true, "Quiz has been changed.");
                    }
                }
                return new Callback(model, false, "This quiz does not exist.");
            }
        }
        return new Callback(model, false, "This course does not exist.");
    } 

    public Callback deleteQuiz(String courseName, String quizName) {
        for (Course course : model.getCourses()) {
            if (course.getName().equals(courseName)) {
                for (Quiz quiz : course.getQuizzes()){
                    if (quiz.getName().equals(quizName)) {
                        course.getQuizzes().remove(quiz);
                        return new Callback(model, true, "Quiz removed successfully.");
                    }
                }
                return new Callback(model, false, "Quiz does not exist.");
            }
        }
        return new Callback(model, false, "Course does not exist.");
    }

    public Callback gradeQuiz(String courseName, String quizName, String studentName, ArrayList<Integer> points) {
        for (Course course : model.getCourses()) {
            if (course.getName().equals(courseName)) {
                for (Quiz quiz : course.getQuizzes()) {
                    if (quiz.getName().equals(quizName)) {
                        if (quiz.getSubmissions().containsKey(studentName)) {
                            var submission = quiz.getSubmissions().get(studentName);
                            if (submission.getIsTaken()) {
                                submission.setIsGraded(true);
                                submission.setPoints(points);
                                return new Callback(model, true, "Quiz graded.");
                            } else {
                                return new Callback(model, false, "Quiz not taken yet.");
                            }
                        } else {
                            return new Callback(model, false, "This student does not exist.");
                        }
                    }
                }
                return new Callback(model, false, "Quiz does not exist.");
            }
        }
        return new Callback(model, false, "Course does not exist.");
    }

    public Callback takeQuiz(String courseName, String quizName, String studentName, Submission submission) {
        for (Course course : model.getCourses()) {
            if (course.getName().equals(courseName)) {
                for (Quiz quiz : course.getQuizzes()) {
                    if (quiz.getName().equals(quizName)) {
                        if (!quiz.getSubmissions().containsKey(studentName)) {
                            quiz.getSubmissions().put(studentName, submission);
                            return new Callback(model, true, "Quiz Submitted!");
                        } else { 
                            return new Callback(model, false, "Quiz already taken.");
                        }
                    }
                }
                return new Callback(model, false, "Quiz does not exist.");
            }
        }
        return new Callback(model, false, "Course does not exist.");
    }
}
