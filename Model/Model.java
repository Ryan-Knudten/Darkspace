package Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Model
 *
 * All application data is packaged into this class
 * Contains arrays of courses, teacher users, and student users.
 *
 * @author Ryan Knudten, 22
 *
 * @version 5/1/22
 *
 */
public class Model implements Serializable {
    private Hashtable<String, String> teachers;
    private Hashtable<String, String> students;
    private ArrayList<Course> courses;
    
    public Model() {
        this.teachers = new Hashtable<String, String>();
        this.students = new Hashtable<String, String>();
        this.courses = new ArrayList<Course>();
    }
    
    //#region Get/Set
    public Hashtable<String, String> getStudents() {
        return this.students;
    }

    public void setUsers(Hashtable<String, String> students) {
        this.students = students;
    }

    public Hashtable<String, String> getTeachers() {
        return this.teachers;
    }

    public void setTeachers(Hashtable<String, String> teachers) {
        this.teachers = teachers;
    }

    public ArrayList<Course> getCourses() {
        return courses;
    }

    public void setCourses(ArrayList<Course> courses) {
        this.courses = courses;
    }
    //#endregion
}