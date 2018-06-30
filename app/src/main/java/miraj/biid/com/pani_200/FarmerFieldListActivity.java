package miraj.biid.com.pani_200;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import miraj.biid.com.pani_200.exceptions.BaseException;
import miraj.biid.com.pani_200.helpers.HTTPHelper;
import miraj.biid.com.pani_200.utils.Util;

/**
 * Created by Miraj on 21/6/2017.
 */

public class FarmerFieldListActivity extends AppCompatActivity {

    ListView fieldListView;
    Context context;
    AsyncHttpClient httpClient;
    ProgressDialog progressDialog;
    ArrayList<Field> fieldList;
    TextView noFieldListTextView;

    private int FIELD_REQUEST_VALUE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.field_list_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        noFieldListTextView = (TextView) findViewById(R.id.noFieldTextView);
        context=this;
        progressDialog = Util.getProgressDialog(context,this.getString(R.string.loading));
        httpClient = HTTPHelper.getHTTPClient();
        fieldListView = (ListView) findViewById(R.id.fieldListView);
        getAllFields();
        fieldListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                FieldDetailsInputActivity.existField=fieldList.get(i);
                Intent fieldDetailsIntent=new Intent(FarmerFieldListActivity.this,FieldDetailsInputActivity.class);
                startActivityForResult(fieldDetailsIntent, FIELD_REQUEST_VALUE);
            }
        });
    }

    /**
     * Getting all the user field list from the user
     */
    private void getAllFields() {
        try{
            httpClient.get("http://www.pani-gca.net/public/index.php/api/fields_by_farmer/" + User.getUserId(), new JsonHttpResponseHandler() {
                @Override
                public void onStart() {
                    super.onStart();
                    progressDialog.show();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    try {
                        fieldList = new ArrayList<Field>();
                        if(response.getInt("success") == 1){
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
                                field.setIrrigationDone(fieldObject.getString("irrigation_done").equals("1") ? true : false);
                                if(fieldObject.getString("prev_irrigation_date") != "null")
                                    field.setFieldPrevIrrigationDate(fieldObject.getString("prev_irrigation_date"));
                                if(fieldObject.getString("next_irrigation_date") != "null")
                                    field.setFieldNextIrrigationDate(fieldObject.getString("next_irrigation_date"));
                                fieldList.add(field);
                            }
                        }
                        fieldListView.setAdapter(new FieldListAdapter());
                        if(fieldList.size()!=0)
                            noFieldListTextView.setVisibility(View.GONE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                }

                @Override
                public void onFinish() {
                    super.onFinish();
                    progressDialog.dismiss();
                }
            });
        }catch(BaseException b){
            Util.showToast(getApplicationContext(), b.getMessage().toString());
        }

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
            View row = getLayoutInflater().inflate(R.layout.field_list_row,null,false);
            TextView fieldTitle = (TextView) row.findViewById(R.id.fieldTitle);
            fieldTitle.setText(fieldList.get(i).getFieldName());
            ImageButton notifications = (ImageButton) row.findViewById(R.id.FarmerFieldListCallImgBtn);
            ImageButton imageAnalysis = (ImageButton) row.findViewById(R.id.update_field_with_image);
            notifications.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(),ViewAlertMessage.class);
                    intent.putExtra("field",fieldList.get(i).getFieldId());
                    startActivity(intent);
                }
            });
            imageAnalysis.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(),ImageUploading.class);
                    intent.putExtra("fieldId",fieldList.get(i).getFieldId());
                    startActivity(intent);
                }
            });
            return row;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == FIELD_REQUEST_VALUE && resultCode==RESULT_OK){
            getAllFields();
        }
    }
}
