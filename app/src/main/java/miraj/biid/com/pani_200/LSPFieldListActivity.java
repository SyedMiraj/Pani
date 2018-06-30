package miraj.biid.com.pani_200;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import cz.msebera.android.httpclient.Header;
import miraj.biid.com.pani_200.helpers.HTTPHelper;
import miraj.biid.com.pani_200.utils.Util;

/**
 * Created by Shahriar Miraj on 9/8/2017.
 */

public class LSPFieldListActivity extends AppCompatActivity{
    ListView fieldListView;
    Context context;
    AsyncHttpClient httpClient;
    ProgressDialog progressDialog;
    ArrayList<Field> fieldArrayList;
    TextView noFieldListTextView;

    String lspNextIrrigationDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.field_list_layout);
        Toolbar toolbar= (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        context=this;
        progressDialog= Util.getProgressDialog(context, context.getString(R.string.loading));
        httpClient= HTTPHelper.getHTTPClient();
        fieldListView= (ListView) findViewById(R.id.fieldListView);
        noFieldListTextView= (TextView) findViewById(R.id.noFieldTextView);
        getAllFieldsByLSP();

        fieldListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Util.showToast(LSPFieldListActivity.this,"Clicked");
                showFieldDetails(fieldArrayList.get(i));
            }
        });
    }

    /**
     * Callign all the lsp's fields list
     */
    private void getAllFieldsByLSP() {
        RequestParams params=new RequestParams();
        params.add("lsp_id",User.getUserId());
        params.add("irrigation_done","0");
        httpClient.post("http://www.pani-gca.net/public/index.php/api/fields_by_lsp_for_schedule", params,new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                progressDialog.show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    fieldArrayList=new ArrayList<>();
                    if(response.getInt("success")==1){
                        JSONArray fieldsArray=response.getJSONArray("fields");
                        for (int i=0;i<fieldsArray.length();i++){
                            JSONObject fieldObject=fieldsArray.getJSONObject(i);
                            Field field=new Field();
                            field.setFieldId(fieldObject.getString("field_id"));
                            field.setFieldName(fieldObject.getString("field_name"));
                            field.setFarmerName(fieldObject.getString("user_name"));
                            field.setFarmerPhoneNumber(fieldObject.getString("mobile_number"));
                            field.setFarmerAddress(fieldObject.getString("address"));
                            field.setCropName(fieldObject.getString("crop_name"));
                            field.setLspId(fieldObject.getString("lsp_id"));
                            field.setFieldLocation(fieldObject.getString("location"));
                            field.setFieldSowingDate(fieldObject.getString("field_sowing_date"));
//                            if(fieldObject.has("suggestion"))  field.setSuggestion(fieldObject.getString("suggestion"));
                            fieldArrayList.add(field);
                        }
                    }
                    fieldListView.setAdapter(new FieldListAdapter());
                    if(fieldArrayList.size()!=0)
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
    }

    /**
     * Field list adapter
     */
    class FieldListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return fieldArrayList.size();
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
            View row=getLayoutInflater().inflate(R.layout.lsp_list_row,null,false);
            TextView fieldTitle= (TextView) row.findViewById(R.id.fieldTitle);
            fieldTitle.setText(fieldArrayList.get(i).getFieldName());
            ImageButton callBtn= (ImageButton) row.findViewById(R.id.lspFieldListCallImgBtn);
            callBtn.setVisibility(View.VISIBLE);
            callBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + fieldArrayList.get(i).getFarmerPhoneNumber()));
                    startActivity(callIntent);
                }
            });
            return row;
        }
    }

    /**
     * Field details dialog for the lsp
     * @param field the field model object
     */
    public void showFieldDetails(final Field field){
        Dialog dialog=new Dialog(this);
        dialog.setContentView(R.layout.field_details_layout);
        dialog.setTitle(this.getString(R.string.lvla_field_det));

        TextView fieldName= (TextView) dialog.findViewById(R.id.fieldDetailsFieldName);
        TextView farmerName= (TextView) dialog.findViewById(R.id.fieldDetailsFarmerName);
        TextView farmerAddress= (TextView) dialog.findViewById(R.id.fieldDetailsFarmerAddress);
        TextView farmerPhnNum= (TextView) dialog.findViewById(R.id.fieldDetailsFarmerPhn);
        TextView cropName= (TextView) dialog.findViewById(R.id.fieldDetailsCropName);
        TextView sowingDate= (TextView) dialog.findViewById(R.id.fieldDetailsSowingDate);
        Button showLocBtn= (Button) dialog.findViewById(R.id.fieldDetailsShowLocationBtn);
        Button callFarmerBtn= (Button) dialog.findViewById(R.id.fieldDetailsCallFarmerBtn);
        TextView suggestionTv= (TextView) dialog.findViewById(R.id.lspSuggestionTv);
        final Button setIrriDateBtn= (Button) dialog.findViewById(R.id.lspNextIrrigationDateBtn);
        Button saveIrriDateBtn= (Button) dialog.findViewById(R.id.lspSaveNextIrrigationBtn);
        fieldName.setText(field.getFieldName());
        farmerName.setText(field.getFarmerName());
        farmerAddress.setText(field.getFarmerAddress());
        farmerPhnNum.setText(field.getFarmerPhoneNumber());
        cropName.setText(field.getCropName());
        sowingDate.setText(field.getFieldSowingDate());

        showLocBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(LSPFieldListActivity.this,LspViewMapActivity.class);
                intent.putExtra("singlefield",true);
                intent.putExtra("field_location",field.getFieldLocation());
                startActivity(intent);
            }
        });

        callFarmerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + field.getFarmerPhoneNumber()));
                startActivity(callIntent);
            }
        });

        setIrriDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDateDialog(setIrriDateBtn);
            }
        });

        saveIrriDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(lspNextIrrigationDate)){
                    Util.showToast(LSPFieldListActivity.this,"Please select the next irrigation date first");
                    return;
                }

                setIrrigationDate(field);
            }
        });

        dialog.show();
    }

    private void setIrrigationDate(Field field) {
        RequestParams params=new RequestParams();
        params.add("field_id",field.getFieldId());
        params.add("irri_date",lspNextIrrigationDate);
        httpClient.post("http://bijoya.org/public/api/update/fields_date",params,new JsonHttpResponseHandler(){
            @Override
            public void onStart() {
                super.onStart();
                progressDialog.show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    Util.showToast(LSPFieldListActivity.this,response.getString("message"));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Util.printDebug("Json error",e.getMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Util.printDebug("Set irrigation date failed",statusCode+"");
            }

            @Override
            public void onFinish() {
                super.onFinish();
                if(progressDialog.isShowing()) progressDialog.dismiss();
            }
        });
    }

    private void showDateDialog(final Button btn) {
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        btn.setText(dayOfMonth + "/" + ++monthOfYear + "/" + year);

                        lspNextIrrigationDate = year + "-" + (monthOfYear) + "-" + dayOfMonth;
                    }
                }, mYear, mMonth, mDay);
        dpd.show();
    }

}
