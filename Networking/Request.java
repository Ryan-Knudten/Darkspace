package Networking;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Request
 *
 * Holds a RequestType to determine what function is requested
 * and the necessary data to perform the function in an ArrayList of Object type
 *
 * @author Ryan Knudten, 22
 *
 * @version 5/1/22
 *
 */
public class Request implements Serializable {
    private RequestType requestType;
    private ArrayList<Object> data;

    public Request(RequestType requestType, ArrayList<Object> data) {
        this.requestType = requestType;
        this.data = data;
    }
    
    //#region Get/Set
    public RequestType getRequestType() {
        return this.requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public ArrayList<Object> getData() {
        return this.data;
    }

    public void setData(ArrayList<Object> data) {
        this.data = data;
    }
    //#endregion
}
