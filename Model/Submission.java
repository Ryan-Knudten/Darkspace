package Model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Submission implements Serializable {
    private ArrayList<String> responses;
    private ArrayList<Integer> points;
    private LocalDateTime submissionTime;
    private boolean isGraded;
    private boolean isTaken;

    public Submission() {
        this.responses = new ArrayList<String>();
    }
    
    //#region Get/Set
    public ArrayList<String> getResponses() {
        return this.responses;
    }

    public void setResponses(ArrayList<String> responses) {
        this.responses = responses;
    }

    public ArrayList<Integer> getPoints() {
        return this.points;
    }

    public void setPoints(ArrayList<Integer> points) {
        this.points = points;
    }

    public LocalDateTime getSubmissionTime() {
        return submissionTime;
    }

    public void setSubmissionTime(LocalDateTime submissionTime) {
        this.submissionTime = submissionTime;
    }

    public boolean getIsGraded() {
        return this.isGraded;
    }

    public void setIsGraded(boolean isGraded) {
        this.isGraded = isGraded;
    }

    public boolean getIsTaken() {
        return this.isTaken;
    }

    public void setIsTaken(boolean isTaken) {
        this.isTaken = isTaken;
    }
    //#endregion
}