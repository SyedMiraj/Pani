package miraj.biid.com.pani_200;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import cz.msebera.android.httpclient.Header;
import miraj.biid.com.pani_200.helpers.HTTPHelper;
import miraj.biid.com.pani_200.utils.Util;

/**
 * Created by Shahriar Miraj on 10/8/2017.
 */

public class LSPScheduleActivity extends AppCompatActivity{

    AsyncHttpClient httpClient;
    ProgressDialog progressDialog;
    public static ArrayList<Field> fieldArrayList;
    ViewPager pager;
    PagerSlidingTabStrip tabs;
    TextView noScheduleTv;
    SimpleDateFormat weekFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lsp_schedule_layout);
        Toolbar toolbar= (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        weekFormat=new SimpleDateFormat("EEE");
        httpClient= HTTPHelper.getHTTPClient();
        progressDialog= Util.getProgressDialog(this,this.getString(R.string.loading));
        noScheduleTv= (TextView) findViewById(R.id.noScheduleTextView);
        pager= (ViewPager) findViewById(R.id.pager);
        getAllFieldsByLSP();
    }

    /**
     * Schedule view pager adapter
     */
    class ViewPagerAdapter extends FragmentPagerAdapter {

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            SingleScheduleFragment fragment=new SingleScheduleFragment();
            Bundle bundle=new Bundle();
            bundle.putInt("position",position);
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public int getCount() {
            return 7;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Calendar calendar=Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR,position);
            return weekFormat.format(calendar.getTime());
        }
    }

    /**
     * Getting lsp's all fields
     */
    private void getAllFieldsByLSP() {
        RequestParams params=new RequestParams();
        params.put("lsp_id",User.getUserId());
        params.put("irrigation_done","0");
        httpClient.post("http://www.pani-gca.net/public/index.php/api/fields_by_lsp_for_schedule", params, new JsonHttpResponseHandler() {
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
                            field.setFieldNextIrrigationDate(fieldObject.getString("next_irrigation_date"));
                            
                            fieldArrayList.add(field);
                        }
                        pager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
                        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
                        tabs.setViewPager(pager);
                        if(fieldArrayList.size()!=0)
                            noScheduleTv.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
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
}
