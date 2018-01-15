package miraj.biid.com.pani_200;

import java.io.Serializable;

/**
 * Created by Shahriar Miraj on 15/11/2017.
 */

public class FieldImageModel implements Serializable{

    private String imageUri;

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
}
