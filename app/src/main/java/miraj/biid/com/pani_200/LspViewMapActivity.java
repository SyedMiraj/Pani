package miraj.biid.com.pani_200;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;
import miraj.biid.com.pani_200.helpers.GPSTracker;
import miraj.biid.com.pani_200.helpers.HTTPHelper;
import miraj.biid.com.pani_200.utils.Util;

/**
 * Created by Shahriar Miraj on 9/8/2017.
 */

public class LspViewMapActivity extends AppCompatActivity implements GoogleMap.OnMarkerClickListener,OnMapReadyCallback{
    private GoogleMap googleMap;
    GPSTracker gpsTracker;
    AsyncHttpClient httpClient;
    ProgressDialog progressDialog;
    ArrayList<Field> fieldArrayList;
    HashMap<Marker,Field> markerFieldList;
    boolean singleField=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lsp_viewmap_layout);
        gpsTracker=new GPSTracker(this);
        httpClient= HTTPHelper.getHTTPClient();
        progressDialog= Util.getProgressDialog(this,this.getString(R.string.loading));
        singleField=getIntent().getBooleanExtra("singlefield",false);

        try {
            // Loading map
            initializeMap();
        } catch (Exception e) {
            e.printStackTrace();
            Util.printDebug("Map error",e.getMessage());
        }
    }

    /**
     * Initializing the google map
     */
    private void initializeMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            // check if map is created successfully or not
//            SupportMapFragment supportMapFragment= (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
//            supportMapFragment.getMapAsync(this);
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        this.getString(R.string.lvma_unable_map), Toast.LENGTH_SHORT)
                        .show();
            }else {
                googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                googleMap.setOnMarkerClickListener(this);
                centerMapOnMyLocation();
                if(!singleField)
                    getAllFieldsByLSP();
                else {
                    String location=getIntent().getStringExtra("field_location");
                    String locations[]=location.split(";");
                    PolygonOptions option=new PolygonOptions();
                    ArrayList<LatLng> latLangList=new ArrayList<>();
                    for(int n=0;n<locations.length;n++){
                        LatLng latlan=new LatLng(Double.parseDouble(locations[n].split(":")[0]),Double.parseDouble(locations[n].split(":")[1]));
                        option.add(latlan);
                        latLangList.add(latlan);
                    }
                    googleMap.addPolygon(option);
                    LatLng center=getPolygonCenterPoint(latLangList);
                    MarkerOptions markerOptions=new MarkerOptions().position(center);
                    googleMap.addMarker(markerOptions);
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(center,20));
                }
            }
        }
    }

    /**
     * Moving map to current location
     */
    private void centerMapOnMyLocation() {
        googleMap.setMyLocationEnabled(true);
        final Location location = gpsTracker.getLocation();
        if (location != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()),
                    18)); // animating camera to that location
            Toast.makeText(this, this.getString(R.string.zooming), Toast.LENGTH_SHORT).show();
        }else Util.showToast(this, this.getString(R.string.problem_current_location));
    }

    /**
     * Getting all the lsp's fields
     */
    public void getAllFieldsByLSP() {
        RequestParams params=new RequestParams();
        params.add("lsp_id",User.getUserId());
        params.add("irrigation_done","0");
        httpClient.post("http://www.pani-gca.net/public/index.php/api/fields_by_lsp_for_schedule",params,new JsonHttpResponseHandler(){
            @Override
            public void onStart() {
                super.onStart();
                progressDialog.show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    if(statusCode==200 && response.getInt("success")==1){
                        JSONArray fields=response.getJSONArray("fields");
                        fieldArrayList=new ArrayList<>();
                        markerFieldList=new HashMap<Marker, Field>();
                        for (int i=0;i<fields.length();i++){
                            JSONObject fieldObject=fields.getJSONObject(i);
                            Field field=new Field();
                            field.setFieldId(fieldObject.getString("field_id"));
                            field.setFieldName(fieldObject.getString("field_name"));
                            field.setFarmerName(fieldObject.getString("user_name"));
                            field.setFarmerPhoneNumber(fieldObject.getString("mobile_number"));
                            field.setFarmerAddress(fieldObject.getString("address"));
                            field.setCropName(fieldObject.getString("crop_name"));
                            field.setLspId(fieldObject.getString("lsp_id"));
                            field.setFieldLocation(fieldObject.getString("location"));
                            field.setFieldSowingDate(fieldObject.getString("field_sowing_date"));
                            fieldArrayList.add(field);

                            String location=fieldObject.getString("location");
                            String locations[]=location.split(";");
                            PolygonOptions option=new PolygonOptions();
                            ArrayList<LatLng> latLangList=new ArrayList<>();
                            for(int n=0;n<locations.length;n++){
                                LatLng latlan=new LatLng(Double.parseDouble(locations[n].split(":")[0]),Double.parseDouble(locations[n].split(":")[1]));
                                option.add(latlan);
                                latLangList.add(latlan);
                            }
                            googleMap.addPolygon(option);
                            LatLng center=getPolygonCenterPoint(latLangList);
                            MarkerOptions markerOptions=new MarkerOptions().position(center).title(fieldObject.getString("field_name"));
                            Marker marker=googleMap.addMarker(markerOptions);
                            marker.showInfoWindow();

                            markerFieldList.put(marker,field);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Util.printDebug("Json exception",e.getMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Util.showToast(getApplicationContext(),getApplicationContext().getString(R.string.data_not_displayed));
            }

            @Override
            public void onFinish() {
                super.onFinish();
                progressDialog.dismiss();
            }
        });
    }

    /**
     * @param polygonPointsList that want to generate polygon using
     * @return latlng point of the center
     */
    private LatLng getPolygonCenterPoint(ArrayList<LatLng> polygonPointsList){
        LatLng centerLatLng = null;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(int i = 0 ; i < polygonPointsList.size() ; i++){
            builder.include(polygonPointsList.get(i));
        }
        LatLngBounds bounds = builder.build();
        centerLatLng =  bounds.getCenter();
        return centerLatLng;
    }

    /**
     * Marker click listener
     * @param marker the marker that is clicked
     * @return click is processed or not
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        if(singleField)return false;
        final Field field=markerFieldList.get(marker);

        Dialog dialog=new Dialog(this);
        dialog.setContentView(R.layout.field_details_layout);
        dialog.setTitle("Field Details");

        TextView fieldName= (TextView) dialog.findViewById(R.id.fieldDetailsFieldName);
        TextView farmerName= (TextView) dialog.findViewById(R.id.fieldDetailsFarmerName);
        TextView farmerAddress= (TextView) dialog.findViewById(R.id.fieldDetailsFarmerAddress);
        TextView farmerPhnNum= (TextView) dialog.findViewById(R.id.fieldDetailsFarmerPhn);
        TextView cropName= (TextView) dialog.findViewById(R.id.fieldDetailsCropName);
        TextView sowingDate= (TextView) dialog.findViewById(R.id.fieldDetailsSowingDate);
        Button showLocBtn= (Button) dialog.findViewById(R.id.fieldDetailsShowLocationBtn);
        Button callFarmerBtn= (Button) dialog.findViewById(R.id.fieldDetailsCallFarmerBtn);

        fieldName.setText(field.getFieldName());
        farmerName.setText(field.getFarmerName());
        farmerAddress.setText(field.getFarmerAddress());
        farmerPhnNum.setText(field.getFarmerPhoneNumber());
        cropName.setText(field.getCropName());
        sowingDate.setText(field.getFieldSowingDate());

        showLocBtn.setVisibility(View.GONE);
        callFarmerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + field.getFarmerPhoneNumber()));
                startActivity(callIntent);
            }
        });
        dialog.show();
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap=googleMap;
    }
}
