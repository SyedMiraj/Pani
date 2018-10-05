package miraj.biid.com.pani_200;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.IOException;

import miraj.biid.com.pani_200.exceptions.BaseException;
import miraj.biid.com.pani_200.utils.Util;

public class ImageAnalysisInternalValue extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_STORAGE = 1000;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_analysis_value);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        textView = (TextView) findViewById(R.id.textView);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
        }
        try {
            String text = "";
            String filename = "gca_value.txt";
            FileInputStream fio = openFileInput(filename);
            if(fio == null){
                throw new BaseException("File not found");
            }
            int size = fio.available();
            byte[] buffer = new byte[size];
            fio.read(buffer);
            fio.close();
            text = new String(buffer);
            textView.setText(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private String readText(String input){
//        File file = new File(input);
//        StringBuilder text = new StringBuilder();
//        try{
//            BufferedReader br = new BufferedReader(new FileReader(file));
//            String line;
//            while((line = br.readLine()) != null){
//                text.append(line);
//                text.append("\n");
//            }
//            br.close();
//        }catch(IOException e){
//            e.printStackTrace();
//        }
//        return text.toString();
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == PERMISSION_REQUEST_STORAGE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Util.showToast(getApplicationContext(), "Permission Granted");
            }else{
                Util.showToast(getApplicationContext(), "Permission Not Granted");
                finish();
            }
        }
    }
}
