package miraj.biid.com.pani_200;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.kosalgeek.android.photoutil.CameraPhoto;
import com.kosalgeek.android.photoutil.GalleryPhoto;
import com.kosalgeek.android.photoutil.PhotoLoader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shahriar Miraj on 15/11/2017.
 */

public class ImageUploading extends AppCompatActivity implements View.OnClickListener{
    public static TextView camera,addImg;
    Toolbar toolbar;
    GalleryPhoto galleryPhoto;
    CameraPhoto cameraPhoto;
    LinearLayout linearImage;
    ImageView imageView;
    private final int GALLERY_REQUEST = 1200;
    private final int CAMERA_REQUEST = 1500;
    List<String> imageList =  new ArrayList<String>();

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
        camera = (TextView) findViewById(R.id.camera);
        addImg = (TextView) findViewById(R.id.addimg);
        cameraPhoto = new CameraPhoto(getApplicationContext());
        galleryPhoto = new GalleryPhoto(getApplicationContext());
        linearImage = (LinearLayout) findViewById(R.id.linearImage);
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
        try {
            startActivityForResult(cameraPhoto.takePhotoIntent(),CAMERA_REQUEST);
            cameraPhoto.addToGallery();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addImageInList() {
        Intent intent=galleryPhoto.openGalleryIntent();
        startActivityForResult(intent,GALLERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == CAMERA_REQUEST){
                String photoPath = cameraPhoto.getPhotoPath();
                addImageToInterface(photoPath);
            }
            if(requestCode == GALLERY_REQUEST && data.getData() != null){
                Uri uri = data.getData();
                galleryPhoto.setPhotoUri(uri);
                String photoPath = galleryPhoto.getPath();
                addImageToInterface(photoPath);
            }
        }
    }

    public void addImageToInterface(String imagePath){
        if(imagePath != null){
            imageList.add(imagePath);
            try {
                Bitmap bitmap = PhotoLoader.init().from(imagePath).requestSize(512, 512).getBitmap();
                imageView = new ImageView(getApplicationContext());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                imageView.setLayoutParams(layoutParams);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setPadding(20, 0, 20, 10);
                imageView.setAdjustViewBounds(true);
                imageView.setImageBitmap(bitmap);
                linearImage.addView(imageView);
                imageView.setOnClickListener(this);
                playTheAudio();
            }catch(Exception e){

            }
        }
    }

    private void playTheAudio()
    {
        try {
            if (imageList.size() == 1) {
                MediaPlayer mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.one);
                mPlayer.start();
            } else if (imageList.size() == 2) {
                MediaPlayer mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.two);
                mPlayer.start();
            } else if (imageList.size() == 3) {
                MediaPlayer mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.three);
                mPlayer.start();
            } else if (imageList.size() == 4) {
                MediaPlayer mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.four);
                mPlayer.start();
            } else if (imageList.size() == 5) {
                MediaPlayer mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.five);
                mPlayer.start();
            } else if (imageList.size() == 6) {
                MediaPlayer mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.six);
                mPlayer.start();
            } else if (imageList.size() == 7) {
                MediaPlayer mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.seven);
                mPlayer.start();
            } else if (imageList.size() == 8) {
                MediaPlayer mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.eight);
                mPlayer.start();
            } else if (imageList.size() == 9) {
                MediaPlayer mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.nine);
                mPlayer.start();
            } else if (imageList.size() == 10) {
                MediaPlayer mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.ten);
                mPlayer.start();
            } else if (imageList.size() == 11) {
                MediaPlayer mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.eleven);
                mPlayer.start();
            } else if (imageList.size() == 12) {
                MediaPlayer mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.twelve);
                mPlayer.start();
            } else if (imageList.size() == 13) {
                MediaPlayer mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.thirtin);
                mPlayer.start();
            } else if (imageList.size() == 14) {
                MediaPlayer mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.fortin);
                mPlayer.start();
            } else if (imageList.size() == 15) {
                MediaPlayer mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.fiftin);
                mPlayer.start();
            }
        }catch (Exception e)
        {
            Log.i("GCA", "Exception: "+e);
        }
    }

}
