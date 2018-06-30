package miraj.biid.com.pani_200;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import miraj.biid.com.pani_200.helpers.GPSTracker;
import miraj.biid.com.pani_200.helpers.HTTPHelper;
import miraj.biid.com.pani_200.utils.Util;

/**
 * Created by Miraj on 19/6/2017.
 */

public class RegisterActivity extends AppCompatActivity {

    EditText mobilNumberEt,passwordEt,confirmPasswordEt,nameEt,addressEt,nationalIdEt,pumpTypeEt,pumpCapacityEt;
    boolean farmer;
    AsyncHttpClient httpClient;
    ProgressDialog progressDialog;
    Button submitBtn;
    Context context;
    boolean editProfile=false;
    LinearLayout extraLspInfoLayout;
    GPSTracker gpsTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_layout);
        context=this;
        farmer=getIntent().getBooleanExtra("farmer",false);
        init();
        gpsTracker=new GPSTracker(this);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String toastText=null;
                if((toastText=checkTextField(mobilNumberEt.getText().toString(), getApplicationContext().getString(R.string.reg_mobile)))==null){
                    if((toastText=checkTextField(passwordEt.getText().toString(),getApplicationContext().getString(R.string.reg_password)))==null){
                        if(passwordEt.getText().toString().equals(confirmPasswordEt.getText().toString())){
                            if((toastText=checkTextField(nameEt.getText().toString(),getApplicationContext().getString(R.string.reg_name)))==null){
                                if((toastText=checkTextField(addressEt.getText().toString(),"Please enter address"))==null){
                                    if(farmer){
                                        registerUser(mobilNumberEt.getText().toString(), passwordEt.getText().toString(), nameEt.getText().toString(), addressEt.getText().toString(),null,null,null);
                                    }
                                    else{
                                        if((toastText=checkTextField(pumpTypeEt.getText().toString(),getApplicationContext().getString(R.string.reg_pumpType)))==null){
                                            if((toastText=checkTextField(pumpCapacityEt.getText().toString(),getApplicationContext().getString(R.string.reg_pumpCapacity)))==null){
                                                registerUser(mobilNumberEt.getText().toString(), passwordEt.getText().toString(), nameEt.getText().toString(), addressEt.getText().toString()
                                                        ,nationalIdEt.getText().toString(),pumpTypeEt.getText().toString(),pumpCapacityEt.getText().toString());
                                            }
                                        }
                                    }
                                }
                            }
                        }else toastText= getApplicationContext().getString(R.string.regi_not_match);
                    }
                }
                if(toastText!=null)
                    Util.showToast(context, toastText);
            }
        });
    }

    private void registerUser(String mobile, String password, String name, String address,
                              String nationalId,String pumpType,String pumpCapacity) {
        RequestParams params=new RequestParams();
        params.put("mobile_number",mobile);
        params.put("password",password);
        params.put("user_name",name);
        params.put("address",address);
        params.put("position",User.getPosition());

        if(!farmer){
            params.put("national_id",nationalId);
            params.put("pump_type",pumpType);
            params.put("pump_capacity",pumpCapacity);
            params.put("lsp","1");
        }else{
            params.put("lsp","0");
        }
        httpClient.post("http://www.pani-gca.net/public/index.php/api/users",params,new JsonHttpResponseHandler(){
            @Override
            public void onStart() {
                super.onStart();
                progressDialog.show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try{
                    if(response.getInt("status")==200 && response.getInt("success")==1){
                        Util.showToast(context,context.getString(R.string.success_reg));
                        Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
                        if(farmer){
                            intent.putExtra("farmer",true);
                        }else{
                            intent.putExtra("farmer",false);
                        }
                        startActivity(intent);
                    }
                }catch(JSONException ex){
                    Util.showToast(context,ex.getMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Util.showToast(context,"Registration error Response"+" and status code: "+statusCode);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                progressDialog.dismiss();
            }
        });
    }

    private String checkTextField(String value, String returnText) {
        if(TextUtils.isEmpty(value))return  returnText;
        else return null;
    }

    private void init() {
        extraLspInfoLayout= (LinearLayout) findViewById(R.id.registerLspExtraInformationLayout);
        progressDialog = Util.getProgressDialog(this, getApplicationContext().getString(R.string.loading));
        httpClient = HTTPHelper.getHTTPClient();
        submitBtn = (Button) findViewById(R.id.registerSubmitBtn);
        mobilNumberEt = (EditText) findViewById(R.id.registerMblNumber);
        passwordEt = (EditText) findViewById(R.id.registerPassword);
        confirmPasswordEt = (EditText) findViewById(R.id.registerConfirmPassword);
        nameEt = (EditText) findViewById(R.id.registerName);
        addressEt = (EditText) findViewById(R.id.registerAddress);
        nationalIdEt = (EditText) findViewById(R.id.registerIDCard);
        pumpTypeEt = (EditText) findViewById(R.id.registerPumpType);
        pumpCapacityEt = (EditText) findViewById(R.id.registerPumpCapacity);

        if (farmer) {
            extraLspInfoLayout.setVisibility(View.GONE);
        }
    }

}
