package miraj.biid.com.pani_200;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import miraj.biid.com.pani_200.helpers.HTTPHelper;
import miraj.biid.com.pani_200.utils.PrefUtils;
import miraj.biid.com.pani_200.utils.Util;

/**
 * Created by Miraj on 20/6/2017.
 */

public class FarmerMainActivity extends AppCompatActivity implements View.OnClickListener,PopupMenu.OnMenuItemClickListener{

    Button addFieldsBtn,manageFieldsBtn,callLspBtn,msgLspBtn;
    TextView userNameTv;
    Toolbar toolbar;
    PrefUtils prefUtils;
    AsyncHttpClient httpClient;
    ProgressDialog progressDialog;
    boolean callBtn=false;
    List<Field> fieldList;
    private final int requestCode = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.farmer_main_layout);
        toolbar= (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        init();
        prefUtils=new PrefUtils(this);
        userNameTv.setText(this.getString(R.string.fml_welcome)+" "+User.getName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }


    /**
     * Initializing all the global variables
     */
    private void init() {
        progressDialog= Util.getProgressDialog(this,this.getString(R.string.loading));
        httpClient= HTTPHelper.getHTTPClient();
        addFieldsBtn= (Button) findViewById(R.id.addFieldsBtn);
        manageFieldsBtn= (Button) findViewById(R.id.manageFieldsBtn);
        callLspBtn= (Button) findViewById(R.id.farmerCallLspBtn);
        msgLspBtn= (Button) findViewById(R.id.farmerMsgLspBtn);
        userNameTv= (TextView) findViewById(R.id.userNameTv);
        addFieldsBtn.setOnClickListener(this);
        manageFieldsBtn.setOnClickListener(this);
        callLspBtn.setOnClickListener(this);
        msgLspBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        Intent intent=null;
        switch (view.getId()){
            case R.id.addFieldsBtn:
                intent=new Intent(this,AddFieldsActivity.class);
                startActivity(intent);
                break;
            case R.id.manageFieldsBtn:
                intent=new Intent(this,FarmerFieldListActivity.class);
                startActivity(intent);
                break;
            case R.id.farmerMsgLspBtn:
                getAllFields();
                callBtn=false;
                break;
            case R.id.farmerCallLspBtn:
                getAllFields();
                callBtn=true;
                break;
        }
    }

    private void getAllFields() {
        httpClient.get("http://bijoya.org/public/api/fields_by_farmer/"+User.getUserId(),new JsonHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                progressDialog.show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    fieldList=new ArrayList<Field>();
                    if(response.getInt("success")==1){
                        JSONArray fieldsArray=response.getJSONArray("fields");
                        for (int i=0;i<fieldsArray.length();i++){
                            JSONObject fieldObject=fieldsArray.getJSONObject(i);
                            Field field=new Field();
                            field.setFieldId(fieldObject.getString("field_id"));
                            field.setFieldName(fieldObject.getString("field_name"));
                            field.setCropName(fieldObject.getString("crop_name"));
                            field.setFieldLocation(fieldObject.getString("location"));
                            field.setFieldSowingDate(fieldObject.getString("field_sowing_date"));
                            field.setLspId(fieldObject.getString("lsp_id"));
                            field.setFieldLspPhoneNumber(fieldObject.getString("mobile_number"));
                            fieldList.add(field);
                        }
                    }
                    showFieldListDialog();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Util.printDebug("Farmer field json exception", e.getMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Util.printDebug("Farmer field fail", statusCode + "");
            }

            @Override
            public void onFinish() {
                super.onFinish();
                progressDialog.dismiss();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_more:
                View menuItemView = findViewById(R.id.menu_more); // SAME ID AS MENU ID
                PopupMenu popupMenu=new PopupMenu(this,menuItemView);
                popupMenu.inflate(R.menu.popup_menu);
                popupMenu.setOnMenuItemClickListener(this);
                popupMenu.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.popup_editprofile:
                Intent editProfileIntent=new Intent(this,UpdateUserActivity.class);
                editProfileIntent.putExtra("farmer",true);
                startActivity(editProfileIntent);
                break;
            case R.id.popup_logout:
                prefUtils.resetPref();
                startActivity(new Intent(this,StartActivity.class));
                finish();
                break;
        }
        return false;
    }

    /**
     * Showing all the field list in dialog
     */
    private void showFieldListDialog() {
        Util.printDebug("in dialog"," size "+fieldList.size());
        Dialog dialog=new Dialog(this);
        dialog.setContentView(R.layout.field_list_layout);
        dialog.setTitle("Field List");
        Toolbar toolbar= (Toolbar) dialog.findViewById(R.id.toolbar);
        toolbar.setVisibility(View.GONE);
        ListView fieldListView= (ListView) dialog.findViewById(R.id.fieldListView);
        TextView noFieldText= (TextView) dialog.findViewById(R.id.noFieldTextView);
        if(fieldList.size()>0){
            noFieldText.setVisibility(View.GONE);
            fieldListView.setAdapter(new FieldListAdapter());
        }
        dialog.show();
    }
    /**
     * Field list adapter
     */
    class FieldListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return fieldList.size();
        }

        @Override
        public Object getItem(int i) {
            return i;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            View row=getLayoutInflater().inflate(R.layout.field_list_call_sms_row,null,false);
            TextView fieldTitle= (TextView) row.findViewById(R.id.fieldCallSmsName);
            ImageButton callSmsBtn= (ImageButton) row.findViewById(R.id.fieldCallSmsImgBtn);
            fieldTitle.setText(fieldList.get(i).getFieldName());
            if(!callBtn){
                callSmsBtn.setImageResource(R.drawable.ic_communication_email);
            }
            callSmsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(callBtn){
                        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + fieldList.get(i).getFieldLspPhoneNumber()));
                        startActivity(callIntent);
                    }else {
                        String number =fieldList.get(i).getFieldLspPhoneNumber();
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", number, null)));
                    }
                }
            });
            return row;
        }
    }
}
