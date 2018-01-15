package miraj.biid.com.pani_200.helpers;


import com.loopj.android.http.AsyncHttpClient;

import miraj.biid.com.pani_200.utils.AppConst;

public class HTTPHelper {

    /**
     * @return httpclient with any basic authentication
     */
    public static AsyncHttpClient getHTTPClient(){
        AsyncHttpClient httpClient=new AsyncHttpClient();
        httpClient.setMaxRetriesAndTimeout(AppConst.MAX_CONNECTION_TRY, AppConst.TIME_OUT);
        return httpClient;
    }

    /**
     * @param username for basic authentication
     * @param password for basic authentication
     * @return httpclient with the basic authentication
     */
    public static AsyncHttpClient getHTTPClient(String username, String password){
        AsyncHttpClient httpClient=getHTTPClient();
        httpClient.setBasicAuth(username,password);
        return httpClient;
    }
}
