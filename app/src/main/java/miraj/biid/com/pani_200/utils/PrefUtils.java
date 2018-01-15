package miraj.biid.com.pani_200.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Miraj on 19/6/2017.
 */

public class PrefUtils {
    SharedPreferences preferences;

    /**
     * Main constructor to save the user information
     * @param context activity context
     */
    public PrefUtils(Context context){
        preferences=context.getSharedPreferences(AppConst.MAIN_PREF,Context.MODE_PRIVATE);
    }

    public void savePhoneNumber(String phoneNumber){
        SharedPreferences.Editor editor=preferences.edit();
        editor.putString("PREF_PHONE",phoneNumber);
        editor.commit();
    }

    public void savePassword(String password){
        SharedPreferences.Editor editor=preferences.edit();
        editor.putString("PREF_PASSWORD",password);
        editor.commit();
    }

    public String getPhoneNumber(){
        return preferences.getString("PREF_PHONE",null);
    }

    public String getPassword(){
        return preferences.getString("PREF_PASSWORD",null);
    }

    public void setFarmer(boolean farmer){
        SharedPreferences.Editor editor=preferences.edit();
        editor.putBoolean("PREF_FARMER", farmer);
        editor.commit();
    }
    public void setLoggedIn(boolean loggedIn){
        SharedPreferences.Editor editor=preferences.edit();
        editor.putBoolean("PREF_LOGGEDIN", loggedIn);
        editor.commit();
    }

    public boolean isFarmer(){
        return preferences.getBoolean("PREF_FARMER", false);
    }
    public boolean isLoggedIn(){
        return preferences.getBoolean("PREF_LOGGEDIN", false);
    }

    public void resetPref(){
        savePhoneNumber(null);
        savePassword(null);
        setLoggedIn(false);
    }

}
