package DataControl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import Model.Course;
import Model.Model;
import Model.Quiz;
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
                return null;

            case CREATE_QUIZ:
                return null;

            case EDIT_QUIZ:
                return null;

            case DELETE_QUIZ:
                return null;

            case GRADE_QUIZ:
                return null;

            case TAKE_QUIZ:
                return null;

            case REFRESH:
                Model model = new Model();
                ArrayList<String> students = new ArrayList<String>();
                students.add("Ryan");
                for(int i = 0; i < 20; i++) {
                    Course course = new Course("Course: " + i, "yomama");
                    course.setStudents(students);
                    if (i % 2 == 0) {
                        Quiz quiz = new Quiz("Math");
                        ArrayList<String> questions = new ArrayList<String>();
                        questions.add("What day is it?\nA. Mon\nB. Tues\nC. Wed\nD. Thur");
                        questions.add("What day is it?\nA. Mon\nB. Tues\nC. Wed\nD. Thur");
                        questions.add("What day is it?\nA. Mon\nB. Tues\nC. Wed\nD. Thur");
                        questions.add("What day is it?\nA. Mon\nB. Tues\nC. Wed\nD. Thur");
                        quiz.setQuestions(questions);
                        course.getQuizzes().add(quiz);

                    }
                    model.getCourses().add(course);
                }
                return new Callback(model, true, "WOOPEPE");

            default:
                return null;
        }
    }

    //#region Functions
    public Callback createCourse(String name, String teacher) {
        // synchronized (model) {

        for (Course course : model.getCourses()) {
            if (course.getName().equals(name)) {
                return new Callback(model, false, "Course already exists");
            }
        }
        model.getCourses().add(new Course(name, teacher));
        return new Callback(model, true, "success");
        // }
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

    public Callback loginUser(String userName, String password) {
        Set<String> studentKeys = model.getStudents().keySet();
        var students = model.getStudents();

        Set<String> teacherKeys = model.getTeachers().keySet();
        var teachers = model.getTeachers();

        for(String key : studentKeys) {
            if (studentKeys.contains(key)) {
                if (password.equals(students.get(key))) {
                    return new Callback(model, true, "success");
                } else {
                    return new Callback(model, false, "Incorrect password.");
                }
            }
        }
        for(String key : teacherKeys) {
            if (teacherKeys.contains(key)) {
                if (password.equals(teachers.get(key))) {
                    return new Callback(model, true, "success");
                } else {
                    return new Callback(model, false, "Incorrect password.");
                }
            }
        }
        return new Callback(model, false, "Invalid username");
    }

    // public Callback deleteUser(String userName) {
    //     // for student must delete from list, the courses, and their quizzes inside of
    //     // those courses
    //     // for teachers must delete from has and their course
    //     // only neg call back is for when user doesn't exist

    //     // creates variable lists that contains the keys of the hashtables
    //     Set<String> studentKeys = model.getStudents().keySet();
    //     Iterator<String> studentItr = studentKeys.iterator();
    //     Set<String> teacherKeys = model.getTeachers().keySet();
    //     Iterator<String> teacherItr = teacherKeys.iterator();
    //     // loops through the keys
    //     while (studentItr.hasNext() || teacherItr.hasNext()) {
    //         if (model.getStudents().equals(userName)) {
    //             return new Callback(model, true, "Student deleted");
    //         } else if (model.getTeachers().equals(userName)) {
    //             model.getTeachers().put(null, null);
    //             // iterate through courses and if it matches teacher name delete it
    //             for (int i = 0; i < model.getCourses().size(); i++) {
    //                 if (model.getCourses().get(i).contains(userName)) {
    //                     model.getCourses().get(i).remove(o)
    //                 }
    //             }
    //             return new Callback(model, true, "Teacher deleted");
    //         }
    //     }
    // }
    //#endregion
}
