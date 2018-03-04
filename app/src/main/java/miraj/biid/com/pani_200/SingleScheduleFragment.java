package miraj.biid.com.pani_200;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import cz.msebera.android.httpclient.Header;
import miraj.biid.com.pani_200.helpers.HTTPHelper;
import miraj.biid.com.pani_200.utils.Util;

/**
 * Created by Shahriar Miraj on 10/8/2017.
 */

public class SingleScheduleFragment extends Fragment{

    ArrayList<Field> fields;
    AsyncHttpClient httpClient;
    ProgressDialog progressDialog;
    String lspNextIrrigationDate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ListView listView = new ListView(getActivity());
        int pos = getArguments().getInt("position");
        fields = new ArrayList<>();
        httpClient = HTTPHelper.getHTTPClient();
        progressDialog = Util.getProgressDialog(getActivity(), this.getString(R.string.loading));
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, pos);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy'-'MM'-'dd");
        for (int i = 0; i < LSPScheduleActivity.fieldArrayList.size(); i++) {
            try {
                Date date = dateFormat.parse(LSPScheduleActivity.fieldArrayList.get(i).getFieldNextIrrigationDate());
                Date currentDate = calendar.getTime();
                if (dateFormat.format(currentDate).equals(dateFormat.format(date))) {
                    fields.add(LSPScheduleActivity.fieldArrayList.get(i));
                }
            } catch (ParseException e) {
                e.printStackTrace();
                Util.printDebug("Date parse error", e.getMessage());
            }
        }
        if (fields != null) {
            listView.setAdapter(new FieldListAdapter());
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    showFieldDetails(fields.get(i));
                }
            });
        }
        if (fields.size() == 0) {
            LinearLayout linearLayout = new LinearLayout(getActivity());
            linearLayout.setGravity(Gravity.CENTER);
            TextView emptyText = new TextView(getActivity());
            emptyText.setText("No Field is assigned");
            emptyText.setTextSize(20);
            linearLayout.addView(emptyText);
            return linearLayout;
        }
        return listView;
    }

    private void showFieldDetails(final Field field) {

        final Dialog dialog=new Dialog(getActivity());
        dialog.setContentView(R.layout.field_details_layout);
        dialog.setTitle("Field Details");

        TextView fieldName= (TextView) dialog.findViewById(R.id.fieldDetailsFieldName);
        TextView farmerName= (TextView) dialog.findViewById(R.id.fieldDetailsFarmerName);
        TextView farmerAddress= (TextView) dialog.findViewById(R.id.fieldDetailsFarmerAddress);
        TextView farmerPhnNum= (TextView) dialog.findViewById(R.id.fieldDetailsFarmerPhn);
        TextView cropName= (TextView) dialog.findViewById(R.id.fieldDetailsCropName);
        TextView sowingDate= (TextView) dialog.findViewById(R.id.fieldDetailsSowingDate);
        Button showLocBtn= (Button) dialog.findViewById(R.id.fieldDetailsShowLocationBtn);
        Button callFarmerBtn= (Button) dialog.findViewById(R.id.fieldDetailsCallFarmerBtn);
        LinearLayout scheduleLayout= (LinearLayout) dialog.findViewById(R.id.fieldDetailsScheduleBtnLayout);
        Button irrigationDoneBtn= (Button) dialog.findViewById(R.id.fieldDetailsIrrigationDoneBtn);
//        Button rescheduleBtn= (Button) dialog.findViewById(R.id.fieldDetailsRescheduleBtn);
        TextView suggestionTv= (TextView) dialog.findViewById(R.id.lspSuggestionTv);
        LinearLayout setIrrigationDateLayout= (LinearLayout) dialog.findViewById(R.id.lspSetIrrigationLayout);
        final Button setIrriDateBtn= (Button) dialog.findViewById(R.id.lspNextIrrigationDateBtn);
        Button saveIrriDateBtn= (Button) dialog.findViewById(R.id.lspSaveNextIrrigationBtn);

        fieldName.setText(field.getFieldName());
        farmerName.setText(field.getFarmerName());
        farmerAddress.setText(field.getFarmerAddress());
        farmerPhnNum.setText(field.getFarmerPhoneNumber());
        cropName.setText(field.getCropName());
        sowingDate.setText(field.getFieldSowingDate());
        scheduleLayout.setVisibility(View.VISIBLE);

        showLocBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), LspViewMapActivity.class);
                intent.putExtra("singlefield", true);
                intent.putExtra("field_location", field.getFieldLocation());
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

        irrigationDoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                showNewIrrigationDialog(field);
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
                    Util.showToast(getActivity(),"Please select the next irrigation date first");
                    return;
                }
                setIrrigationDate(field);
            }
        });

        dialog.show();
    }

    class FieldListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return fields.size();
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
            View row=getActivity().getLayoutInflater().inflate(R.layout.lsp_list_row,null,false);
            TextView fieldTitle= (TextView) row.findViewById(R.id.fieldTitle);
            fieldTitle.setText(fields.get(i).getFieldName());
            ImageButton callBtn= (ImageButton) row.findViewById(R.id.lspFieldListCallImgBtn);
            callBtn.setVisibility(View.VISIBLE);
            callBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + fields.get(i).getFarmerPhoneNumber()));
                    startActivity(callIntent);
                }
            });
            return row;
        }
    }

    private void showNewIrrigationDialog(final Field field) {
        final Dialog dialog=new Dialog(getActivity());
        dialog.setContentView(R.layout.new_irrigation_dialog_layout);
        dialog.setTitle("New Irrigation");
        TextView dateTv= (TextView) dialog.findViewById(R.id.newIrrigationDateTv);
        dateTv.setText("Date: "+Util.getTodayDate("dd/MM/yyyy"));
        final EditText amountEt= (EditText) dialog.findViewById(R.id.newIrrigationAmountEt);
        Button saveBtn= (Button) dialog.findViewById(R.id.newIrrigationSaveBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TextUtils.isEmpty(amountEt.getText().toString())){
                    sendNewIrrigation(field,amountEt.getText().toString());
                    dialog.dismiss();
                }else Util.showToast(getActivity(),"Please enter the amount");
            }
        });
        dialog.show();
    }

    private void sendNewIrrigation(Field field, String amount){
        RequestParams params=new RequestParams();
        params.add("field_id",field.getFieldId());
        params.add("lsp_id",User.getUserId());
        params.add("amount",amount);
        params.add("irri_date",Util.getTodayDate("yyyy-MM-dd"));

        httpClient.post("",params,new JsonHttpResponseHandler(){
            @Override
            public void onStart() {
                super.onStart();
                progressDialog.show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    Util.showToast(getActivity(),response.getString("message"));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Util.printDebug("JSon error",e.getMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Util.printDebug("new Irrigation done response fail", statusCode + "");
            }

            @Override
            public void onFinish() {
                super.onFinish();
                progressDialog.dismiss();
            }
        });
    }

    private void requestReschedule(String fieldId) {
        RequestParams params=new RequestParams();
        params.add("field_id",fieldId);
        httpClient.post("http://argha.xyz/_biid/request-reschedule.php",params,new JsonHttpResponseHandler(){
            @Override
            public void onStart() {
                super.onStart();
                progressDialog.show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Util.printDebug("Irrigation done response",response.toString());
                try {
                    if(response.getInt("success")==1){
                        Util.showToast(getActivity(),response.getString("message"));
                        getActivity().finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Util.printDebug("Json error",e.getMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Util.printDebug("Irrigation done response fail", statusCode + "");
            }

            @Override
            public void onFinish() {
                super.onFinish();
                progressDialog.dismiss();
            }
        });
    }

    private void setIrrigationDate(Field field) {
        RequestParams params=new RequestParams();
        params.add("field_id",field.getFieldId());
        params.add("irri_date",lspNextIrrigationDate);
        httpClient.put("http://bijoya.org/public/api/update/fields_date",params,new JsonHttpResponseHandler(){
            @Override
            public void onStart() {
                super.onStart();
                progressDialog.show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    Util.showToast(getActivity(),response.getString("message"));
                    getActivity().finish();
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

        DatePickerDialog dpd = new DatePickerDialog(getActivity(),
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
