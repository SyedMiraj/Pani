package miraj.biid.com.pani_200;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    List<String> imageList =  new ArrayList<String>();
    AsyncHttpClient httpClient;
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

    private void uploadImageActivity(List<String> imageList) {
        String url = "http://bijoya.org/public/api/fields_image";
        if(imageList != null){
            for(String imagePath : imageList){
                try{
                    Bitmap bitmap = PhotoLoader.init().from(imagePath).requestSize(512,512).getBitmap();
                    final String encodedString = ImageBase64.encode(bitmap);
                    StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Toast.makeText(getApplicationContext(),"Image Upload Successful",Toast.LENGTH_SHORT).show();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(),"Image Upload Failed",Toast.LENGTH_SHORT).show();
                        }
                    }){
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params =  new HashMap<String, String>();
                            params.put("image",encodedString);
                            params.put("field_id",fieldId);
                            params.put("farmer_id",User.getUserId());
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

    public void addImageToInterface(final String imagePath){
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

            httpClient.post("http://bijoya.org/public/api/field_value", params, new JsonHttpResponseHandler() {
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


}
