package miraj.biid.com.pani_200;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

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
 * Created by Miraj on 21/6/2017.
 */

public class AddFieldsActivity extends AppCompatActivity implements View.OnClickListener,OnMapReadyCallback{

    private GoogleMap googleMap;
    GPSTracker gpsTracker;
    public static ArrayList<LatLng> selectedPoints;
    ArrayList<MarkerOptions> mapMarkers;
    PolygonOptions rectOptions;
    Polygon polygon;
    Button polygonClearBtn,polygonSubmitBtn,addCurrentMarkerBtn;
    AsyncHttpClient httpClient;
    ProgressDialog progressDialog;
    Toolbar toolbar;
    public static String fieldLocation;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_map_layout);
        init();
        try {
            initializeMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                addMarkerPoint(latLng);
            }
        });
    }

    private void init() {
        httpClient = HTTPHelper.getHTTPClient();
        progressDialog = Util.getProgressDialog(this,this.getString(R.string.loading));

        polygonClearBtn = (Button) findViewById(R.id.fieldLocationClearBtn);
        polygonSubmitBtn = (Button) findViewById(R.id.fieldLocationSubmitBtn);
        addCurrentMarkerBtn = (Button) findViewById(R.id.fieldLocationCurrentBtn);
        addCurrentMarkerBtn.setOnClickListener(this);
        polygonClearBtn.setOnClickListener(this);
        polygonSubmitBtn.setOnClickListener(this);
        gpsTracker = new GPSTracker(this);
        selectedPoints = new ArrayList<>();
        rectOptions = new PolygonOptions();
        mapMarkers = new ArrayList<>();

    }

    private void initializeMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        this.getString(R.string.lvma_unable_map), Toast.LENGTH_SHORT)
                        .show();
            }else{
                googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    centerMapOnMyLocation();
            }

            //set the map location on the map
            if(fieldLocation != null){
                String locations[] = fieldLocation.split(";");
                for(int n=0;n<locations.length;n++){
                    String latitude = locations[n].split(":")[0];
                    String longitude = locations[n].split(":")[1];
                    selectedPoints.add(new LatLng(Double.parseDouble(latitude),Double.parseDouble(longitude)));
                    if(latitude != null && longitude != null){
                        LatLng latlan = new LatLng(Double.parseDouble(latitude),Double.parseDouble(longitude));
                        rectOptions.add(latlan);
                        googleMap.addPolygon(rectOptions);
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void centerMapOnMyLocation() {
        googleMap.setMyLocationEnabled(true);
        final Location location = gpsTracker.getLocation();
        if (location != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()),
                    18)); // animating camera to that location
            Toast.makeText(this, this.getString(R.string.zooming), Toast.LENGTH_SHORT).show();
        }else Util.showToast(this,"Problem show current location");
    }

    @Override
    protected void onResume() {
        super.onResume();
        initializeMap();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fieldLocationClearBtn:
                if(polygon != null)polygon.remove();
                selectedPoints.clear();
                googleMap.clear();
                rectOptions = new PolygonOptions();
                break;
            case R.id.fieldLocationSubmitBtn:
                if(selectedPoints.size() >= 3){
                    Intent intent = new Intent(this,FieldDetailsInputActivity.class);
                    startActivity(intent);
                    finish();
                }else
                    Util.showToast(this,this.getString(R.string.three_point));
                break;
            case R.id.fieldLocationCurrentBtn:
                addMarkerPoint(new LatLng(gpsTracker.getLatitude(),gpsTracker.getLongitude()));
                break;
        }
    }

    private void addMarkerPoint(LatLng latLng) {
        if(selectedPoints.contains(latLng)) return;
        selectedPoints.add(latLng);
        rectOptions.add(latLng);
        MarkerOptions markerOptions=new MarkerOptions().position(latLng);
        mapMarkers.add(markerOptions);
        googleMap.addMarker(markerOptions);
        if(polygon != null)
            polygon.remove();
        polygon = googleMap.addPolygon(rectOptions);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap=googleMap;
    }
}
