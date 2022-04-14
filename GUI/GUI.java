package GUI;

import Networking.Callback;
import Networking.Request;

public interface GUI {
    public Callback requestCallback(Request request);
    public void closeWindow();
}
