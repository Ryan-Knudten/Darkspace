package GUI;

import Model.Model;
import Networking.Callback;
import Networking.Request;

public interface GUI {
    public Callback requestCallback(Request request);
}
