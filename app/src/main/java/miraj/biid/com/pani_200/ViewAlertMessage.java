package miraj.biid.com.pani_200;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import miraj.biid.com.pani_200.helpers.HTTPHelper;
import miraj.biid.com.pani_200.utils.Util;

/**
 * Created by Shahriar Miraj on 26/10/2017.
 */

public class ViewAlertMessage extends AppCompatActivity{

    ListView messageListView;
    Context context;
    AsyncHttpClient httpClient;
    ProgressDialog progressDialog;
    TextView noMessageListTextView;
    ArrayList<AlertMessages> messages;
    String fieldId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_list);
        Toolbar toolbar= (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fieldId = getIntent().getStringExtra("field");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        noMessageListTextView= (TextView) findViewById(R.id.noFieldTextView);
        context=this;
        progressDialog= Util.getProgressDialog(context,this.getString(R.string.loading));
        httpClient= HTTPHelper.getHTTPClient();
        messageListView= (ListView) findViewById(R.id.messageListView);
        getAllMessages();
    }

    private void getAllMessages() {
        RequestParams param = new RequestParams();
        param.add("field_id", fieldId);
        try {
            httpClient.post("http://www.pani-gca.net/public/index.php/api/notification_messages", param, new JsonHttpResponseHandler() {
                @Override
                public void onStart() {
                    super.onStart();
                    progressDialog.show();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    try {
                        messages = new ArrayList<AlertMessages>();
                        if (response.getInt("success") == 1) {
                            JSONArray messagesArray = response.getJSONArray("alert");
                            for (int i = 0; i < messagesArray.length(); i++) {
                                JSONObject messageObject = messagesArray.getJSONObject(i);
                                AlertMessages alertMessages = new AlertMessages();
                                String date = messageObject.getString("recieve_date");
                                alertMessages.setDate(date);
                                String signal = messageObject.getString("signal_irrgation");
                                //will change the text with required format
                                alertMessages.setText(signal);
                                messages.add(alertMessages);
                            }
                        }
                        messageListView.setAdapter(new MessageListAdapter());
                        if (messages.size() != 0)
                            noMessageListTextView.setVisibility(View.GONE);
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
        }catch(Exception e){
            Util.showToast(getApplicationContext(), getApplicationContext().getString(R.string.excuse));
        }
    }


    /**
     * Field list adapter
     */
    class MessageListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return messages.size();
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
        public View getView(int i, View view, ViewGroup viewGroup) {
            View row=getLayoutInflater().inflate(R.layout.message_list_row,null,false);
            TextView dateField= (TextView) row.findViewById(R.id.txtDate);
            dateField.setText(messages.get(i).getDate());
            TextView signalField= (TextView) row.findViewById(R.id.txtSignal);
            signalField.setText(messages.get(i).getText());
            return row;
        }
    }
}
