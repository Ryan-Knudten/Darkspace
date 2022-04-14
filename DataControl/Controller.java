package DataControl;

import Model.Model;
import Networking.Callback;
import Networking.Request;

public class Controller {
    private Model model;

    public Controller(Model model) {
        this.model = model;
    }

    //#region Get/Set
    public Model getModel() {
        return this.model;
    }

    public void setModel(Model model) {
        this.model = model;
    }
    //#endregion

    public synchronized Callback handleRequest(Request request) {
        switch(request.getRequestType()) {
            case CREATE_USER:
                return null;

            case LOGIN_USER:
                return null;

            case DELETE_USER:
                return null;

            case CREATE_COURSE:
                return new Callback(model, true, "Changed text omg!");

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
            
            default:
                return null;
        }
    }
}
