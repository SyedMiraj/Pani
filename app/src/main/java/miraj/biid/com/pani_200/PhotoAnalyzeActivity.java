package miraj.biid.com.pani_200;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PhotoAnalyzeActivity extends AppCompatActivity implements View.OnClickListener {

    private static int RESULT_LOAD_IMAGE = 1;
    private Button photoAnalyzeBtn, showResultBtn,buttonLoadImage;
    private Toolbar toolbar;

    private String picturePath;
    private Context ctx = this;
    private int bucketsize;
    private ProgressDialog progressDialog;
    private boolean checkStatus = true, imageUpload = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_analyze_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        progressDialog=new ProgressDialog(this);
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(100);
        progressDialog.setMessage("Computing...");
        progressDialog.setProgressNumberFormat(null);
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        buttonLoadImage = (Button) findViewById(R.id.PhotoSelect);
        photoAnalyzeBtn = (Button) findViewById(R.id.Analyze);
        showResultBtn = (Button) findViewById(R.id.showResult);
        initCustomSpinner();

        buttonLoadImage.setOnClickListener(this);
        photoAnalyzeBtn.setOnClickListener(this);
        showResultBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.PhotoSelect:
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
                break;
            case R.id.Analyze:
                if (checkStatus) {
                    showResult("Warning", "Currently no model is selected. Please select a valid model no.");
                } else {
                    if (imageUpload) {
                        showResult("Warning", "No picture is selected.");
                    } else {
                        progressDialog.setProgress(0);
                        progressDialog.show();

                        final Handler handler = new Handler() {
                            public void handleMessage(Message msg) {
                                switch(msg.getData().getString("Command")) {
                                    case "Message":
                                        showResult(msg.getData().getString("Title"), msg.getData().getString("Message"));
                                        break;
                                    case "Progress":
                                        progressDialog.setProgress(msg.getData().getInt("Progress"));
                                        String message = msg.getData().getString("Message");
                                        if (message != "")
                                            progressDialog.setMessage(message);
                                        break;
                                    case "HideProgress":
                                        progressDialog.dismiss();
                                }
                            }
                        };
                        new Thread() {
                            private ProgressMonitor progressMonitor = new ProgressMonitor() {

                                @Override
                                protected void setProgress(int progress) {
                                    currentProgress = progress;
                                    Message msg = handler.obtainMessage();
                                    Bundle bundle = new Bundle();
                                    bundle.putString("Command", "Progress");
                                    bundle.putInt("Progress", currentProgress);
                                    bundle.putString("Message", "");
                                    msg.setData(bundle);
                                    handler.sendMessage(msg);
                                }

                                protected void setMessage(String message) {
                                    Message msg = handler.obtainMessage();
                                    Bundle bundle = new Bundle();
                                    bundle.putString("Command", "Progress");
                                    bundle.putInt("Progress", currentProgress);
                                    bundle.putString("Message", message);
                                    msg.setData(bundle);
                                    handler.sendMessage(msg);
                                }
                            };

                            private void showResult(String title, String message) {
                                Message msg = handler.obtainMessage();
                                Bundle bundle = new Bundle();
                                bundle.putString("Command", "Message");
                                bundle.putString("Title", title);
                                bundle.putString("Message", message);
                                msg.setData(bundle);
                                handler.sendMessage(msg);
                            }

                            private void hideProgress() {
                                Message msg = handler.obtainMessage();
                                Bundle bundle = new Bundle();
                                bundle.putString("Command", "HideProgress");
                                msg.setData(bundle);
                                handler.sendMessage(msg);
                            }

                            public void run() {
                                final DecimalFormat df = new DecimalFormat("#.##");
                                progressMonitor.setMessage("Computing SVM Index...");
                                Map<String, Double> results = new HashMap<String, Double>();
                                SvmComputer svmcomp = new SvmComputer(bucketsize, getResources());
                                String message = svmcomp.computeFcover(picturePath, results, progressMonitor);
                                if (message.length() > 0)
                                    showResult("Warning", message);
                                progressMonitor.setMessage("Complete!");
                                hideProgress();
                                String text = "";
                                for (String index : results.keySet()) {
                                    double value = results.get(index);
                                    text += index + " = " + df.format(100.0 * value) + " %\n";
                                }
                                showResult("RESULT", text);
                                text = "";
                                for (String index : results.keySet()) {
                                    double value = results.get(index);
                                    text += "\t" + index + "=" + value;
                                }
                                String filename = picturePath.substring(picturePath.lastIndexOf("/") + 1);
                                generateNoteOnSD(ctx, "Result", filename + text);
                            }
                        }.start();
                    }
                }
                break;
            case R.id.showResult:
                StringBuilder text = new StringBuilder();
                try {
                    File sdcard = Environment.getExternalStorageDirectory();
                    File file = new File(sdcard, BuildConfig.APPLICATION_ID + "/Notes/Result.txt");

                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String line;
                    while ((line = br.readLine()) != null) {
                        text.append(line);
                        text.append("\n\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                }
                showAllResult("Result", text.toString());
                break;
        }
    }

    public void generateNoteOnSD(Context context, String sFileName, String sBody) {
        try {
            File root = new File(Environment.getExternalStorageDirectory(), BuildConfig.APPLICATION_ID + "/Notes");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName + ".txt");
            FileWriter writer = new FileWriter(gpxfile, true);
            writer.append(sBody + "\r\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            picturePath = cursor.getString(columnIndex);
            cursor.close();

            ImageView imageView = (ImageView) findViewById(R.id.CropImage);
            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            imageUpload = false;

        }
    }

    public void showResult(String title, String message) {

        final TextView myView = new TextView(getApplicationContext());
        myView.setTextSize(22);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });
        builder.show();

    }

    public void showAllResult(String title, String message) {

        final TextView myView = new TextView(getApplicationContext());
        myView.setTextSize(22);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });
        builder.show();

    }

    public void chooseModel() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Model Choice");

        final View view = getLayoutInflater().inflate(R.layout.choose_model_layout, null);
        final EditText titleEt = (EditText) view.findViewById(R.id.chooseModelET);

        alertDialog.setView(view);
        alertDialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //DO Nothing here
            }
        });
        final AlertDialog dialog = alertDialog.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (titleEt.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please Enter the Model No.", Toast.LENGTH_LONG).show();
                    return;
                } else {
                    bucketsize = Integer.parseInt(titleEt.getText().toString());
                    checkStatus = false;
                }
                dialog.dismiss();
            }
        });

    }

    private void initCustomSpinner() {

        Spinner spinnerCustom= (Spinner) findViewById(R.id.chooseModel);

        ArrayList<String> model = new ArrayList<String>();
        model.add("SVM 2");
        model.add("SVM 4");
        model.add("SVM 8");
        for (int i : SvmComputer.additionalModels())
            model.add("SVM " + i);
        CustomSpinnerAdapter customSpinnerAdapter=new CustomSpinnerAdapter(PhotoAnalyzeActivity.this,model);
        spinnerCustom.setAdapter(customSpinnerAdapter);
        spinnerCustom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String item = parent.getItemAtPosition(position).toString();
                if (item.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please Enter the Model No.", Toast.LENGTH_LONG).show();
                } else {
                    bucketsize = Integer.parseInt(item.substring(4));
                    checkStatus = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public class CustomSpinnerAdapter extends BaseAdapter implements SpinnerAdapter {

        private final Context activity;
        private ArrayList<String> asr;

        public CustomSpinnerAdapter(Context context,ArrayList<String> asr) {
            this.asr=asr;
            activity = context;
        }

        public int getCount()
        {
            return asr.size();
        }

        public Object getItem(int i)
        {
            return asr.get(i);
        }

        public long getItemId(int i)
        {
            return (long)i;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            TextView txt = new TextView(PhotoAnalyzeActivity.this);
            txt.setPadding(16, 16, 16, 16);
            txt.setTextSize(18);
            txt.setGravity(Gravity.CENTER_VERTICAL);
            txt.setText(asr.get(position));
            txt.setTextColor(Color.parseColor("#000000"));
            return  txt;
        }

        public View getView(int i, View view, ViewGroup viewgroup) {
            TextView txt = new TextView(PhotoAnalyzeActivity.this);
            txt.setGravity(Gravity.CENTER);
            txt.setPadding(16, 16, 16, 16);
            txt.setTextSize(16);
            txt.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.dropdownicon, 0);
            txt.setText(asr.get(i));
            txt.setTextColor(Color.parseColor("#FFFFFF"));
            return  txt;
        }

    }

}
