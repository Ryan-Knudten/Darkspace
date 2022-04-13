import java.io.Serializable;

import Model.Model;

public class Callback implements Serializable {
    private Model model;
    private boolean didRequestWork;
    private String message;

    public Callback(Model model, boolean didRequestWork, String message) {
        this.model = model;
        this.didRequestWork = didRequestWork;
        this.message = message;
    }

    //#region Get/Set
    public Model getModel() {
        return this.model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public boolean getDidRequestWork() {
        return this.didRequestWork;
    }

    public void setDidRequestWork(boolean didRequestWork) {
        this.didRequestWork = didRequestWork;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    //#endregion
}
