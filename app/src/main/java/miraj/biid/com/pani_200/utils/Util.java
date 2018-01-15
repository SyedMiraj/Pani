package miraj.biid.com.pani_200.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Miraj on 19/6/2017.
 */

public class Util {


    /**
     * Checking for all possible internet providers
     * **/
    public static boolean isConnectedToInternet(Context con){
        ConnectivityManager connectivity = (ConnectivityManager) con.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }

        }
        return false;
    }



    public static void showNoInternetDialog(final Context con) {

        AlertDialog.Builder build=new AlertDialog.Builder(con);
        build.setTitle("No Internet");
        build.setMessage("Internet is not available. Please check your connection");
        build.setCancelable(true);
        build.setPositiveButton("Settings", new Dialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                con.startActivity(intent);
            }
        });

        build.setNegativeButton("Cancel", new Dialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alert=build.create();
        alert.show();
    }

    public static boolean isValidEmail(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

    public static void showToast(Context con,String message){
        Toast.makeText(con, message, Toast.LENGTH_SHORT).show();
    }

    public static void printDebug(String key,String message){
        Log.d(AppConst.DEBUG_KEY, key + " - " + message);
    }


    public static boolean isGPSOn(Context context){
        return ((LocationManager) context.getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled("gps");
    }

    public static ProgressDialog getProgressDialog(Context context, String message){
        ProgressDialog progressDialog=new ProgressDialog(context);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(message);
        return progressDialog;
    }

    public static String toSHA256(String base) {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public static String getTodayDate(String format){
        SimpleDateFormat dateFormat=new SimpleDateFormat(format);
        return dateFormat.format(new Date());
    }

    public static List<String> getTempLsp(){
        List<String> list=new ArrayList<>();
        list.add("LSP-1");
        list.add("LSP-2");
        list.add("LSP-3");
        list.add("LSP-4");

        return list;
    }
}
