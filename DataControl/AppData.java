package DataControl;
import java.io.*;

import Model.Model;

public class AppData {
    public static Model loadData(String filename) {
        try {
            FileInputStream fis = new FileInputStream(filename);
            ObjectInputStream ois = new ObjectInputStream(fis);

            Model model = (Model)ois.readObject();
            ois.close();
            return model;
        } catch (Exception e) {
            return new Model();
        }
    }

    public static void saveData(Model model, String filename) {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
    
            oos.writeObject(model);
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
