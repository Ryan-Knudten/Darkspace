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
        switch (request.getRequestType()) {
            case CREATE_USER:
                String userName = (String) request.getData().get(0);
                String password = (String) request.getData().get(1);
                String userType = (String) request.getData().get(2);
                return createUser(userName, password, userType);

            case LOGIN_USER:
                userName = (String) request.getData().get(0);
                password = (String) request.getData().get(1);
                return loginUser(userName, password);

            case DELETE_USER:
                return null;

            case CREATE_COURSE:
                String name = (String) request.getData().get(0);
                String teacher = (String) request.getData().get(1);
                return createCourse(name, teacher);

            case DELETE_COURSE:
                name = (String) request.getData().get(0);
                teacher = (String) request.getData().get(1);
                return deleteCourse(name, teacher);

            case CREATE_QUIZ: // unsure
                name = (String) request.getData().get(0);
                String courseName = (String) request.getData().get(1);
                teacher = (String) request.getData().get(2);
                ArrayList<String> questions = (ArrayList<String>) request.getData().get(3);
                return createQuiz(name, courseName, teacher, questions);

            case EDIT_QUIZ:
                name = (String) request.getData().get(0);
                courseName = (String) request.getData().get(1);
                teacher = (String) request.getData().get(2);
                questions = (ArrayList<String>) request.getData().get(3);
                return editQuiz(name, courseName, teacher, questions);

            case DELETE_QUIZ:
                name = (String) request.getData().get(0);
                courseName = (String) request.getData().get(1);
                teacher = (String) request.getData().get(2);
                return deleteQuiz(name, courseName, teacher);

            case GRADE_QUIZ:
                return null;

            case TAKE_QUIZ:
                return null;

            default:
                return null;
        }
    }

    // must make multiple fails at every place it could fail

    public Callback createCourse(String name, String teacher) {
        // synchronized (model) {

        for (Course course : model.getCourses()) {
            if (course.getName().equals(name)) {
                return new Callback(model, false, "Course already exists");
            }
        }
        model.getCourses().add(new Course(name, teacher));
        return new Callback(model, true, "Course Created");
        // }
    }

    public Callback createUser(String userName, String password, String userType) {
        // synchronized (model) {
        // creates variable lists that contains the keys of the hashtables
        Set<String> studentKeys = model.getStudents().keySet();
        Iterator<String> studentItr = studentKeys.iterator();
        Set<String> teacherKeys = model.getTeachers().keySet();
        Iterator<String> teacherItr = teacherKeys.iterator();
        // loops through the keys
        while (studentItr.hasNext() || teacherItr.hasNext()) {
            // must search thru hashmap for username
            if (model.getStudents().containsKey(userName)) {
                return new Callback(model, false, "Student account already exists");
            } else if (model.getTeachers().containsKey(userName)) {
                return new Callback(model, false, "Teacher account already exists");
            }
        }
        if (userType.equals("Student")) {
            model.getStudents().put(userName, password);
            return new Callback(model, true, "Account successfully created.");
        } else {
            model.getTeachers().put(userName, password);
            return new Callback(model, true, "Account successfully created.");
        }

        // }
    }

    public Callback loginUser(String userName, String password) {
        // make incorrect user, password, and both
        Set<String> studentKeys = model.getStudents().keySet();
        Iterator<String> studentItr = studentKeys.iterator();
        Set<String> teacherKeys = model.getTeachers().keySet();
        Iterator<String> teacherItr = teacherKeys.iterator();
        Callback callback = new Callback(model, false, "");
        // loops through the keys
        while (studentItr.hasNext() || teacherItr.hasNext()) {
            if (model.getStudents().containsKey(userName) && model.getStudents().containsValue(password)) {
                callback = new Callback(model, true, "Login Successful");
            } else if (!model.getStudents().containsKey(userName) || !model.getTeachers().containsKey(userName)) {
                callback = new Callback(model, false, "Incorrect Username");
            } else if (!model.getStudents().containsValue(password) || !model.getTeachers().containsValue(password)) {
                callback = new Callback(model, false, "Incorrect Password");
            } else if (!model.getStudents().containsKey(userName) && !model.getStudents().containsValue(password)
                    || !model.getTeachers().containsKey(userName) && !model.getTeachers().containsValue(password)) {
                callback = new Callback(model, false, "Incorrect Username and Password");
            }
        }
        return callback;
    }

    public Callback deleteUser(String userName, String name, String teacher) {
        // for student must delete from list, the courses, and their quizzes inside
        // those courses
        // for teachers must delete from has and their course
        // only neg call back is for when user doesn't exist

        // creates variable lists that contains the keys of the hashtables

        // loops through the keys
        Set<String> studentKeys = model.getStudents().keySet();
        Iterator<String> studentItr = studentKeys.iterator();
        Set<String> teacherKeys = model.getTeachers().keySet();
        Iterator<String> teacherItr = teacherKeys.iterator();
        Course courses = new Course(name, teacher);
        while (studentItr.hasNext() || teacherItr.hasNext()) {
            if (model.getStudents().containsKey(userName)) {
                model.getStudents().remove(userName);
            } else if (model.getTeachers().containsKey(userName)) {
                model.getTeachers().remove(userName);
            }
            for (int i = 0; i < model.getCourses().size(); i++) {
                if (courses.getStudents().get(i).equals(userName)) {
                    courses.getStudents().remove(i);
                    if (courses.getQuizzes().get(i).getName().equals(userName)) {
                        courses.getQuizzes().remove(i);
                    }
                    if (model.getCourses().get(i).getName().equals(userName)) {
                        model.getCourses().remove(i);
                    }
                }
            }
        }
    }

    public Callback deleteCourse(String name, String teacher) {
        for (Course course : model.getCourses()) {
            if (course.getName().equals(name) && course.getTeacher().equals(teacher)) {
                model.getCourses().remove(course);
                return new Callback(model, true, "Course removed");
            }
        }
        return new Callback(model, false, "Course does not exist");
    }

    public Callback createQuiz(String name, String courseName, String teacher, ArrayList<String> questions) {
        // do i add the submissions in this class?
        Quiz quiz = new Quiz(name);
        Course course = new Course(courseName, teacher);
        for (int i = 0; i < course.getQuizzes().size(); i++) {
            if (course.getQuizzes().get(i).getName().equals(name)) {
                return new Callback(model, false, "Quiz already exists");
            }
        }
        quiz.setQuestions(questions);
        course.getQuizzes().add(new Quiz(name));
        return new Callback(model, true, "Quiz Created");
    }

    public Callback editQuiz(String name, String courseName, String teacher, ArrayList<String> questions) {
        Quiz quiz = new Quiz(name);
        Course course = new Course(courseName, teacher);
        for (int i = 0; i < course.getQuizzes().size(); i++) {
            if (!course.getQuizzes().get(i).getName().equals(name)) {
                return new Callback(model, false, "Quiz does not exist");
            } else {
                quiz.setName(name);
                quiz.setQuestions(questions);
            }
        }
        // should i make a switch in here for the different options?
        return new Callback(model, true, "Quiz modified");
    }

    public Callback deleteQuiz(String name, String courseName, String teacher) {
        Course course = new Course(courseName, teacher);
        for (int i = 0; i < course.getQuizzes().size(); i++) {
            if (!course.getQuizzes().get(i).getName().equals(name)) {
                return new Callback(model, false, "Quiz does not exist");
            } else {
                course.getQuizzes().remove(i);
            }
        }
        return new Callback(model, true, "Quiz deleted");
    }

    public Callback gradeQuiz(String courseName, String studentName, String quizName, Submission submissions) {
        //oof
    }
}
