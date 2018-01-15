package miraj.biid.com.pani_200;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by DELL on 22/8/2017.
 */

public class MyInstanceIdService extends FirebaseInstanceIdService {

    private static final String REG_TOKEN="reg_token";

    @Override
    public void onTokenRefresh() {
        String token_string = FirebaseInstanceId.getInstance().getToken();
        Log.d(REG_TOKEN,token_string);
        SharedPreferences sharedPreferences=getApplicationContext().getSharedPreferences(getString(R.string.FCM_PREF), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString(getString(R.string.FCM_TOKEN),token_string);
        editor.commit();
    }
}
