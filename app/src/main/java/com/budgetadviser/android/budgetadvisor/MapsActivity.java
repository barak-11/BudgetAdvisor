package com.budgetadviser.android.budgetadvisor;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends AppCompatActivity  implements OnMapReadyCallback {

    private GoogleMap mMap;
    double latitude;
    double longitude;
    ProgressBar mProgressBar;
    MapFragment mMapView;
    SupportMapFragment mapFragment;
    FragmentManager fm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            setContentView(R.layout.activity_maps);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setTitle("Street View");
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); //adds the back arrow to the toolbar
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            Bundle b = getIntent().getExtras();
            latitude = b.getDouble("latitude");
            longitude = b.getDouble("longitude");



            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            fm = getSupportFragmentManager();
            fm.beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .hide(mapFragment)
                    .commit();
            mProgressBar = (ProgressBar) findViewById(R.id.circular_progress);
            mProgressBar.setVisibility(View.VISIBLE);
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), "MapsActivity error:" + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("MapsActivity", "oStreetView error", e);
        }



    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        try{
            mMap = googleMap;
            //mMap.setVisibility(View.INVISIBLE);
            // Add a marker in Sydney and move the camera
            LatLng selectedPlace = new LatLng(latitude, longitude);
            mMap.addMarker(new MarkerOptions().position(selectedPlace).title(""));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedPlace,14));

            fm.beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .show(mapFragment)
                    .commit();
            mProgressBar.setVisibility(View.INVISIBLE);
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), "MapsActivity error:" + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("MapsActivity", "oStreetView error", e);
        }

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onBackPressed() {

    }
}
