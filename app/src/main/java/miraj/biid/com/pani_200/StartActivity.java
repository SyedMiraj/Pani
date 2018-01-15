package miraj.biid.com.pani_200;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

import miraj.biid.com.pani_200.utils.PrefUtils;

/**
 * Created by Miraj on 19/6/2017.
 */

public class StartActivity  extends AppCompatActivity implements View.OnClickListener{

    ImageButton farmerButton;
    ImageButton lspButton;
    PrefUtils prefUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_layout);
        init();
        prefUtils=new PrefUtils(this);
        if(prefUtils.isLoggedIn() && prefUtils.isFarmer()){
            farmerButton.performClick();
        }else if(prefUtils.isLoggedIn() && !prefUtils.isFarmer()){
            lspButton.performClick();
        }
    }

    private void init() {
        farmerButton= (ImageButton) findViewById(R.id.farmerImgBtn);
        lspButton= (ImageButton) findViewById(R.id.lspImgBtn);
        farmerButton.setOnClickListener(this);
        lspButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent=new Intent(this,LoginActivity.class);
        switch (view.getId()){
            case R.id.farmerImgBtn:
                intent.putExtra("farmer",true);
                finish();
                break;
            case R.id.lspImgBtn:
                intent.putExtra("farmer",false);
                finish();
                break;
        }
        startActivity(intent);
    }
}
