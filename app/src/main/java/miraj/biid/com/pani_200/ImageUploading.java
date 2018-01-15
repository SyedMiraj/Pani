package miraj.biid.com.pani_200;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.kosalgeek.android.photoutil.GalleryPhoto;
import java.util.ArrayList;

/**
 * Created by Shahriar Miraj on 15/11/2017.
 */

public class ImageUploading extends AppCompatActivity implements View.OnClickListener{
    public static TextView camera,addImg;
    Toolbar toolbar;
    GalleryPhoto galleryPhoto;
    ArrayList<FieldImageModel> fieldImageModelList=new ArrayList<>();
    private final int GALLERY_REQUEST=1200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_upload_layout);
        toolbar= (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        init();
        camera.setOnClickListener(this);
        addImg.setOnClickListener(this);
    }

    private void init(){
        camera= (TextView) findViewById(R.id.camera);
        addImg= (TextView) findViewById(R.id.addimg);
        galleryPhoto=new GalleryPhoto(getApplicationContext());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.camera:
                imageCapturing();
                break;
            case R.id.addimg:
                addImageInList();
                break;
        }
    }

    private void imageCapturing(){

    }

    private void addImageInList() {

        Intent intent=galleryPhoto.openGalleryIntent();
        startActivityForResult(intent,GALLERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==0){
            switch (resultCode){
                case Activity.RESULT_OK:

                    break;
                case Activity.RESULT_CANCELED:
                    Toast.makeText(getApplicationContext(),"Image captured failed",Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
        if(requestCode==GALLERY_REQUEST && data.getData() != null){

        }
    }


}
