package Model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Course
 *
 * Contains array of quizzes and submissions
 * Contains one teacher user
 *
 * @author Ryan Knudten, 22
 *
 * @version 5/1/22
 *
 */
public class Course implements Serializable {
    ArrayList<String> students;
    String teacher;
    ArrayList<Quiz> quizzes;
    String name;

    public Course(String name, String teacher) {
        this.name = name;
        this.teacher = teacher;
        this.quizzes = new ArrayList<Quiz>();
        this.students = new ArrayList<String>();
    }
    
    //#region Get/Set
    public ArrayList<String> getStudents() {
        return students;
    }

    public void setStudents(ArrayList<String> students) {
        this.students = students;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public ArrayList<Quiz> getQuizzes() {
        return quizzes;
    }

    public void setQuizzes(ArrayList<Quiz> quizzes) {
        this.quizzes = quizzes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    //#endregion
}
