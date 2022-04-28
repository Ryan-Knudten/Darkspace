package DataControl;
import java.util.ArrayList;
import Model.Model;
import Networking.Callback;
import Networking.Request;
import Networking.RequestType;

public class Tests {
    public static void main(String[] args) {
       Model model1 = new Model();
    ArrayList<Object> data = new ArrayList<>();
    data.add("nickA51");
    data.add("password");
    Request callback = new Request(RequestType.CREATE_COURSE, data);
        System.out.println("break");
    }

    //log in test
    // Model model1 = new Model();
    // ArrayList<Object> data = new ArrayList<>();
    // data.add("nickA51");
    // data.add("password");
    // Request callback = new Request(RequestType.LOGIN_USER, data);

    //course tests
    // Model model1 = new Model();
    // ArrayList<Object> data = new ArrayList<>();
    // data.add("math class");
    // data.add("nick teacher");
    // Request callback = new Request(RequestType.CREATE_COURSE, data);
    // Request callback1 = new Request(RequestType.DELETE_COURSE, data);
}
