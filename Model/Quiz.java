package Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

public class Quiz implements Serializable {
    private ArrayList<String> questions;
    private Hashtable<String, Submission> submissions;
    private String name;

    public Quiz(String name) {
        this.name = name;
        this.questions = new ArrayList<String>();
        this.submissions = new Hashtable<String, Submission>();
    }
    
    //#region Get/Set
    public ArrayList<String> getQuestions() {
        return this.questions;
    }

    public void setQuestions(ArrayList<String> questions) {
        this.questions = questions;
    }

    public Hashtable<String, Submission> getSubmissions() {
        return this.submissions;
    }

    public void setSubmissions(Hashtable<String, Submission> submissions) {
        this.submissions = submissions;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
    //#endregion
}


