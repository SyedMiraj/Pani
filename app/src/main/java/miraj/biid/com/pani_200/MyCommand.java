package miraj.biid.com.pani_200;

import android.content.Context;

import com.android.volley.Request;

import java.util.ArrayList;

/**
 * Created by Shahriar Miraj on 18/1/2018.
 */

public class MyCommand<T> {

    private ArrayList<Request<T>> requests = new ArrayList<>();

    private Context context;

    public MyCommand(Context context) {
        this.context = context;
    }

    public void add(Request<T> request){
        requests.add(request);
    }

    public void remove(Request<T> request){
        requests.remove(request);
    }

    public void execute(){
        for(Request<T> request : requests){
            MySingleton.getInstance(context).addToRequestQueue(request);
        }
    }
}
