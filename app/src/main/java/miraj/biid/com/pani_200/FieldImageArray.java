package miraj.biid.com.pani_200;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by DELL on 15/11/2017.
 */

public class FieldImageArray implements Serializable {

    private ArrayList<FieldImageModel> imageModelList;

    public ArrayList<FieldImageModel> getImageModelList() {
        return imageModelList;
    }

    public void setImageModelList(ArrayList<FieldImageModel> imageModelList) {
        this.imageModelList = imageModelList;
    }
}
