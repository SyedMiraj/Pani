package miraj.biid.com.pani_200;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by Miraj on 19/6/2017.
 */

public class IntroActivity  extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_layout);
    }

    /**
     * On click for the enter button at the start
     * @param view the button view- calling from layout
     */
    public void enterBtn(View view){
        startActivity(new Intent(this,StartActivity.class));
        finish();
    }
}
