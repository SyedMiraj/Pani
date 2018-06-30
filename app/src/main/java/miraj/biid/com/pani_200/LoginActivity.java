package miraj.biid.com.pani_200;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import miraj.biid.com.pani_200.callback.PermissionCallback;
import miraj.biid.com.pani_200.callback.TaskPerformCallback;
import miraj.biid.com.pani_200.exceptions.PositionNotFoundException;
import miraj.biid.com.pani_200.helpers.GPSTracker;
import miraj.biid.com.pani_200.helpers.HTTPHelper;
import miraj.biid.com.pani_200.utils.PrefUtils;
import miraj.biid.com.pani_200.utils.Util;

/**
 * Created by Miraj on 19/6/2017.
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int MY_PERMISSIONS_REQUEST = 10;
    boolean farmer;
    ImageView headerImg;
    EditText phnNumberEt,passwordEt;
    Button loginBtn,newUserBtn;
    AsyncHttpClient httpClient;
    ProgressDialog progressDialog;
    PrefUtils prefUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        farmer = getIntent().getBooleanExtra("farmer",false);
        prefUtils = new PrefUtils(this);
        init();

        checkReqPermissions(new PermissionCallback() {
            @Override
            public void hasAccess() {
                if((prefUtils.isFarmer() && farmer) || (!prefUtils.isFarmer() && !farmer) ){
                    if(prefUtils.getPhoneNumber()!=null){
                        phnNumberEt.setText(prefUtils.getPhoneNumber());
                        if(prefUtils.getPassword()!=null){
                            passwordEt.setText(prefUtils.getPassword());
                            loginBtn.performClick();
                        }
                    }
                }
            }
        });
    }

    /**
     * Initializing all the global variables
     */
    private void init() {
        progressDialog= Util.getProgressDialog(this, getApplicationContext().getString(R.string.loading));
        httpClient= HTTPHelper.getHTTPClient();
        headerImg= (ImageView) findViewById(R.id.loginHeaderImg);
        phnNumberEt= (EditText) findViewById(R.id.loginPhnNumberEt);
        passwordEt= (EditText) findViewById(R.id.loginPasswordEt);
        loginBtn= (Button) findViewById(R.id.loginBtn);
        newUserBtn= (Button) findViewById(R.id.loginNewUserBtn);
//        forgotPasswordBtn= (Button) findViewById(R.id.forgotPasswordBtn);

//        forgotPasswordBtn.setOnClickListener(this);
        loginBtn.setOnClickListener(this);
        newUserBtn.setOnClickListener(this);
        if(farmer)
            headerImg.setImageResource(R.drawable.farmer_green);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.loginBtn:
                if(TextUtils.isEmpty(phnNumberEt.getText().toString()) || TextUtils.isEmpty(phnNumberEt.getText().toString())){
                    Util.showToast(this,this.getString(R.string.login_form_fillup));
                    return;
                }
                loginUser(phnNumberEt.getText().toString(),passwordEt.getText().toString());
                break;
            case R.id.loginNewUserBtn:
                Intent intent=new Intent(this,RegisterActivity.class);
                intent.putExtra("farmer",farmer);
                startActivity(intent);
                break;
//            case R.id.forgotPasswordBtn:
//                showForgotPasswordDialog();
//                break;
        }
    }

    //method for showing forget password
    private void showForgotPasswordDialog() {
        Util.showToast(getApplicationContext(),"will work later");
    }

    //method for login user
    private void loginUser(String mobileNumber, String password) {
        RequestParams params=new RequestParams();
        params.put("phonenumber",mobileNumber);
        params.put("password",password);
        if(farmer){
            params.put("lsp","0");
        }else {
            params.put("lsp","1");
        }
        httpClient.post("http://www.pani-gca.net/public/index.php/api/users/login", params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                progressDialog.show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    if(response.getInt("status")==200 && response.getInt("success")==1){
                        Intent intent=null;
                        User.setUserId(response.getString("user_id"));
                        User.setName(response.getString("name"));
                        User.setAddress(response.getString("address"));
                        User.setNumber(response.getString("phonenumber"));

                        User.setNationalNumber(response.getString("national_id"));
                        User.setPumpCapacity(response.getString("pump_capacity"));
                        User.setPumpType(response.getString("pump_type"));
                        User.setPosition(response.getString("position"));

                        prefUtils.savePhoneNumber(phnNumberEt.getText().toString());
                        prefUtils.savePassword(passwordEt.getText().toString());

                        if(farmer){
                            intent=new Intent(LoginActivity.this,FarmerMainActivity.class);
                            prefUtils.setFarmer(true);
                            startActivity(intent);
                        }else {
                            intent=new Intent(LoginActivity.this,LSPMainActivity.class);
                            prefUtils.setFarmer(false);
                            startActivity(intent);
                        }
                        prefUtils.setLoggedIn(true);
                        final Intent finalIntent = intent;
                        sendUserPositionToServer(User.getUserId(),new TaskPerformCallback(){
                            @Override
                            public void onComplete() {
                                startActivity(finalIntent);
                                finish();
                            }
                            @Override
                            public void onError() {

                            }
                        });
                    }else {
                        Util.showToast(LoginActivity.this,getApplicationContext().getString(R.string.try_again));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Util.printDebug("Login json Error",e.getMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Util.showToast(LoginActivity.this,getApplicationContext().getString(R.string.try_again));
            }

            @Override
            public void onFinish() {
                super.onFinish();
                progressDialog.dismiss();
            }
        });
    }

    private void checkReqPermissions(PermissionCallback callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.CALL_PHONE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST);
            }else callback.hasAccess();
        }else callback.hasAccess();
    }

    private void sendUserPositionToServer(String userId, final TaskPerformCallback taskPerformCallback) {
        GPSTracker gpsTracker=new GPSTracker(this);
        RequestParams params=new RequestParams();
        params.add("user_id",userId);
        params.add("position",gpsTracker.getLatitude()+";"+gpsTracker.getLongitude());
        try{
            if(gpsTracker.getLatitude() == 0.0 && gpsTracker.getLongitude() == 0.0){
                throw new PositionNotFoundException(getApplication().getString(R.string.position_not_updated));
            }
            final ProgressDialog dialog=Util.getProgressDialog(this,"Please wait...");
            httpClient.put("http://www.pani-gca.net/public/index.php/api/user/updateposition",params,new JsonHttpResponseHandler(){

                @Override
                public void onStart() {
                    super.onStart();
                    dialog.show();
                }

                @Override
                public void onFinish() {
                    super.onFinish();
                    dialog.dismiss();
                    taskPerformCallback.onComplete();
                }
            });
        }catch(PositionNotFoundException e){
            Util.showToast(LoginActivity.this, e.getMessage().toString());
        }

    }
}
