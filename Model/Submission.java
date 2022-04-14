package Model;

import java.io.Serializable;
import java.util.Hashtable;

public class Submission implements Serializable {
    private Hashtable<String, Integer> responses;
    private boolean isGraded;
    private boolean isTaken;

    public Submission() {
        this.responses = new Hashtable<String, Integer>();
    }
    
    //#region Get/Set
    public Hashtable<String, Integer> getResponses() {
        return this.responses;
    }

    public void setResponses(Hashtable<String, Integer> responses) {
        this.responses = responses;
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