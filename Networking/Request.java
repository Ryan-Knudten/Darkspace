package Networking;

import java.io.Serializable;
import java.util.ArrayList;

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
