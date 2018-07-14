package miraj.biid.com.pani_200;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.kosalgeek.android.photoutil.CameraPhoto;
import com.kosalgeek.android.photoutil.GalleryPhoto;
import com.kosalgeek.android.photoutil.ImageBase64;
import com.kosalgeek.android.photoutil.PhotoLoader;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import cz.msebera.android.httpclient.Header;
import miraj.biid.com.pani_200.helpers.HTTPHelper;
import miraj.biid.com.pani_200.utils.Util;

/**
 * Created by Shahriar Miraj on 15/11/2017.
 */

public class ImageUploading extends AppCompatActivity implements View.OnClickListener{
    public static TextView camera,addImg;
    Toolbar toolbar;
    GalleryPhoto galleryPhoto;
    CameraPhoto cameraPhoto;
    LinearLayout linearImage, linearUpRes;
    ImageView imageView;
    MyCommand myCommand;
    int bucketSize = 2;
    String fieldId = null;
    String finalResult = null;
    Button uploadBtn, resultBtn;
    private ProgressDialog progressDialog;
    private final int GALLERY_REQUEST = 1200;
    private final int CAMERA_REQUEST = 1500;
    private final int CAMERA_REQUEST_GREATER_VERSION_API = 900;
    List<String> imageList =  new ArrayList<String>();
    AsyncHttpClient httpClient;
    File fileProvide;
    Context context;

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
        uploadBtn.setOnClickListener(this);
        resultBtn.setOnClickListener(this);
    }

    private void init(){
        context = this;
        camera = (TextView) findViewById(R.id.camera);
        addImg = (TextView) findViewById(R.id.addimg);
        cameraPhoto = new CameraPhoto(getApplicationContext());
        galleryPhoto = new GalleryPhoto(getApplicationContext());
        linearImage = (LinearLayout) findViewById(R.id.linearImage);
        linearUpRes = (LinearLayout) findViewById(R.id.result_upload_layout);
        uploadBtn = (Button) findViewById(R.id.uploadBtn);
        resultBtn = (Button) findViewById(R.id.resultBtn);
        myCommand = new MyCommand(getApplicationContext());
        fieldId = getIntent().getExtras().getString("fieldId");
        httpClient= HTTPHelper.getHTTPClient();
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
            case R.id.uploadBtn:
                uploadImageActivity(imageList);
                break;
            case R.id.resultBtn:
                analyzeResultActivity(imageList);
                break;
        }
    }

    private void imageCapturing(){
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                        checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    captureImageForGreaterVersionAPI();
                }else{
                    if(shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
                        Util.showToast(getApplicationContext(), getApplicationContext().getString(R.string.camera_permission));
                    }
                    requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_REQUEST_GREATER_VERSION_API);
                }
            }else {
                startActivityForResult(cameraPhoto.takePhotoIntent(), CAMERA_REQUEST);
                cameraPhoto.addToGallery();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addImageInList() {
        Intent intent=galleryPhoto.openGalleryIntent();
        startActivityForResult(intent,GALLERY_REQUEST);
    }

    private void uploadImageActivity(final List<String> imageList) {
        uploadBtn.setClickable(false);
        String url = "http://www.pani-gca.net/public/index.php/api/fields_image";
        if(imageList != null){
            for(final String imagePath : imageList){
                try{
                    Bitmap bitmap = PhotoLoader.init().from(imagePath).requestSize(512,512).getBitmap();
                    final String encodedString = ImageBase64.encode(bitmap);
                    StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Toast.makeText(getApplicationContext(),(imageList.indexOf(imagePath) + 1)+"/"+imageList.size()+ getApplicationContext().getString(R.string.upload_image),Toast.LENGTH_SHORT).show();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.failed_image),Toast.LENGTH_SHORT).show();
                        }
                    }){
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params =  new HashMap<String, String>();
                            params.put("image",encodedString);
                            params.put("field_id",fieldId);
                            params.put("index", String.valueOf(imageList.indexOf(imagePath) + 1));
                            params.put("date", getMetaDateOfImage(imagePath));
                            return params;
                        }
                    };

                    myCommand.add(request);

                }catch(Exception e){
                    Toast.makeText(getApplicationContext(),"Upload Failed",Toast.LENGTH_SHORT).show();
                }
            }
            myCommand.execute();
        }
    }

    private void analyzeResultActivity(final List<String> imageList) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(100);
        progressDialog.setMessage("Computing...");
        progressDialog.setProgressNumberFormat(null);
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        if(imageList != null && !imageList.isEmpty()){
                progressDialog.setProgress(0);
                progressDialog.show();

                final Handler handler = new Handler() {
                    public void handleMessage(Message msg) {
                        switch(msg.getData().getString("Command")) {
                            case "Message":
                                showResult(msg.getData().getString("Title"), msg.getData().getString("Message"));
                                break;
                            case "Progress":
                                progressDialog.setProgress(msg.getData().getInt("Progress"));
                                String message = msg.getData().getString("Message");
                                if (message != "")
                                    progressDialog.setMessage(message);
                                break;
                            case "HideProgress":
                                progressDialog.dismiss();
                                saveImageValueToServer(finalResult,fieldId);
                        }
                    }
                };
                new Thread() {
                    private ProgressMonitor progressMonitor = new ProgressMonitor() {

                        @Override
                        protected void setProgress(int progress) {
                            currentProgress = progress;
                            Message msg = handler.obtainMessage();
                            Bundle bundle = new Bundle();
                            bundle.putString("Command", "Progress");
                            bundle.putInt("Progress", currentProgress);
                            bundle.putString("Message", "");
                            msg.setData(bundle);
                            handler.sendMessage(msg);
                        }

                        protected void setMessage(String message) {
                            Message msg = handler.obtainMessage();
                            Bundle bundle = new Bundle();
                            bundle.putString("Command", "Progress");
                            bundle.putInt("Progress", currentProgress);
                            bundle.putString("Message", message);
                            msg.setData(bundle);
                            handler.sendMessage(msg);
                        }
                    };

                    private void showResult(String title, String message) {
                        Message msg = handler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putString("Command", "Message");
                        bundle.putString("Title", title);
                        bundle.putString("Message", message);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    }

                    private void hideProgress() {
                        Message msg = handler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putString("Command", "HideProgress");
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    }

                    public void run() {
                        Double cumulativeValue = 0.0;
                        final DecimalFormat df = new DecimalFormat("#.##");
                        progressMonitor.setMessage("Computing SVM Index...");
                        Map<String, Double> results = new HashMap<String, Double>();
                        for(String imagepath : imageList){
                            SvmComputer svmcomp = new SvmComputer(bucketSize, getResources());
                            String message = svmcomp.computeFcover(imagepath, results, progressMonitor);
                            if (message.length() > 0)
                                showResult("Warning", message);
                        }
                        progressMonitor.setMessage("Complete!");
                        hideProgress();
                        String text = "";
                        String result = "";
                        for (String index : results.keySet()) {
                            double value = results.get(index);
                            cumulativeValue = cumulativeValue + value;
                            result = df.format(100.0 * cumulativeValue/results.keySet().size());
                            convertValue(result);
                        }
                        text = "svm" + " = " + result + " %\n";
                        showResult("RESULT", text);
                    }
                }.start();
        }else{
            Util.showToast(getApplicationContext(),"No image selected yet!!!");
        }
    }

    private void convertValue(String result) {
        this.finalResult = result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == CAMERA_REQUEST){
                String photoPath = cameraPhoto.getPhotoPath();
                addImageToInterface(photoPath, false);
            }
            if(requestCode == GALLERY_REQUEST && data.getData() != null){
                Uri uri = data.getData();
                galleryPhoto.setPhotoUri(uri);
                String photoPath = galleryPhoto.getPath();
                addImageToInterface(photoPath, false);
            }
            if(requestCode == CAMERA_REQUEST_GREATER_VERSION_API){
                addImageToInterface(fileProvide.getAbsolutePath(), true);
                fileProvide = null;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == CAMERA_REQUEST_GREATER_VERSION_API){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
            grantResults[1] == PackageManager.PERMISSION_GRANTED){
                captureImageForGreaterVersionAPI();
            }
        }else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void addImageToInterface(final String imagePath, boolean higherVersion){
        if(imagePath != null){
            imageList.add(imagePath);
            try {
                Bitmap bitmap = null;
                if(!higherVersion) {
                  bitmap =  PhotoLoader.init().from(imagePath).requestSize(512, 512).getBitmap();
                }else{
                    bitmap = BitmapFactory.decodeFile(imagePath);
                }
                imageView = new ImageView(getApplicationContext());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                imageView.setLayoutParams(layoutParams);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setPadding(20, 0, 20, 10);
                imageView.setAdjustViewBounds(true);
                imageView.setImageBitmap(bitmap);
                linearImage.addView(imageView);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                        alertDialog.setTitle("আপনি কি ছবিটি বাতিল করতে চান ?");
                        alertDialog.setPositiveButton("হ্যাঁ", new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {
                                linearImage.removeViewAt(imageList.indexOf(imagePath));
                                imageList.remove(imagePath);
                                if(imageList.size() < 16)
                                    addImg.setEnabled(true);
                           }
                       });
                        alertDialog.setNegativeButton("না", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        alertDialog.show();
                    }
                });
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
                linearUpRes.setVisibility(View.VISIBLE);
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
            }else if (imageList.size() == 16) {
                MediaPlayer mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.sixteen);
                mPlayer.start();
                addImg.setEnabled(false);
            }
        }catch (Exception e)
        {
            Log.i("GCA", "Exception: "+e);
        }
    }

    public void showResult(String title, String message) {

        final TextView myView = new TextView(getApplicationContext());
        myView.setTextSize(22);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message).setPositiveButton("OK", null);
        builder.show();
    }

    private void saveImageValueToServer(String value, String fieldId) {
        if(value != null){
            RequestParams params = new RequestParams();
            params.add("field_id", fieldId);
            params.add("gca_value", value);

            httpClient.post("http://www.pani-gca.net/public/index.php/api/field_value", params, new JsonHttpResponseHandler() {
                @Override
                public void onStart() {
                    super.onStart();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    try {
                        if (statusCode == 200 && response.getInt("success") == 1) {
                            Util.showToast(getApplicationContext(), "Data Update Successful");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    Util.showToast(getApplicationContext(), "Data Update failed");
                }

                @Override
                public void onFinish() {
                    super.onFinish();
                    progressDialog.dismiss();
                }
            });
        }
    }

    public String getMetaDateOfImage(String stringPath){
        DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy");
        if(stringPath != null){
            File file = new File(stringPath);
            if(file != null){
                Date date = new Date(file.lastModified());
                return dateFormat.format(date);
            }
        }
        return null;
    }

    public void captureImageForGreaterVersionAPI(){

        Uri picUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName()+".provider", createImageFile());
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, picUri);
        intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(intent, CAMERA_REQUEST_GREATER_VERSION_API);

    }

    private File createImageFile() {
        File picDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());

        fileProvide = new File(picDirectory, "field_"+fieldId+"_pic_"+timestamp+".jpg");
        return fileProvide;
    }


}
