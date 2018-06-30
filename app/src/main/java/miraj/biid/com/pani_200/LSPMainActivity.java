package miraj.biid.com.pani_200;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

import miraj.biid.com.pani_200.utils.PrefUtils;

/**
 * Created by Miraj on 20/6/2017.
 */

public class LSPMainActivity extends AppCompatActivity implements View.OnClickListener,PopupMenu.OnMenuItemClickListener{

    Button viewMapBtn, viewListBtn, viewScheduleBtn, callSupportBtn, msgSupportBtn;
    TextView userNameTv;
    PrefUtils prefUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lsp_main_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        init();
        prefUtils = new PrefUtils(this);

        userNameTv.setText(this.getString(R.string.fml_welcome)+" "+User.getName());
    }

    /**
     * Initializing all the global variables
     */
    private void init() {
        viewMapBtn = (Button) findViewById(R.id.lspViewMapBtn);
        viewScheduleBtn = (Button) findViewById(R.id.lspViewScheduleBtn);
        callSupportBtn = (Button) findViewById(R.id.lspCallSupportBtn);
        msgSupportBtn = (Button) findViewById(R.id.lspMsgSupportBtn);
        viewListBtn = (Button) findViewById(R.id.lspViewFieldListBtn);
        userNameTv = (TextView) findViewById(R.id.userNameTv);

        viewListBtn.setOnClickListener(this);
        viewMapBtn.setOnClickListener(this);
        viewScheduleBtn.setOnClickListener(this);
        callSupportBtn.setOnClickListener(this);
        msgSupportBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.lspViewMapBtn:
                startActivity(new Intent(this, LspViewMapActivity.class));
                break;
            case R.id.lspViewFieldListBtn:
                startActivity(new Intent(this, LSPFieldListActivity.class));
                break;
            case R.id.lspViewScheduleBtn:
                startActivity(new Intent(this, LSPScheduleActivity.class));
                break;
            case R.id.lspCallSupportBtn:
                Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "01911190527"));
                startActivity(callIntent);
                break;
            case R.id.lspMsgSupportBtn:
                String[] choices = {"Call", "SMS"};
                new AlertDialog.Builder(this)
                        .setSingleChoiceItems(choices, 0, null)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                                int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                                switch (selectedPosition) {
                                    case 0:
                                        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "01746774281"));
                                        startActivity(callIntent);
                                        break;
                                    case 1:
                                        String number = "01746774281";  // The number on which you want to send SMS
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", number, null)));
                                        break;
                                }
                            }
                        })
                        .show();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_more:
                View menuItemView = findViewById(R.id.menu_more); // SAME ID AS MENU ID
                PopupMenu popupMenu=new PopupMenu(this,menuItemView);
                popupMenu.inflate(R.menu.popup_menu);
                popupMenu.setOnMenuItemClickListener(this);
                popupMenu.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()){
//            case R.id.popup_editprofile:
//                Intent editProfileIntent=new Intent(this,UpdateUserActivity.class);
//                editProfileIntent.putExtra("farmer",true);
//                startActivity(editProfileIntent);
//                break;
            case R.id.popup_logout:
                prefUtils.resetPref();
                startActivity(new Intent(this,StartActivity.class));
                finish();
                break;
        }
        return false;
    }

}
