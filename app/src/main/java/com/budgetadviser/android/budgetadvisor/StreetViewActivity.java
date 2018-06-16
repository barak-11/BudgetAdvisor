package com.budgetadviser.android.budgetadvisor;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;

public class StreetViewActivity extends AppCompatActivity implements OnStreetViewPanoramaReadyCallback {
    private static final LatLng SYDNEY = new LatLng(-33.87365, 151.20689);
    double latitude;
    double longitude;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_street_view);
        try{
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_new);
            toolbar.setTitle("Street View");
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); //adds the back arrow to the toolbar
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            Bundle b = getIntent().getExtras();
            latitude = b.getDouble("latitude");
            longitude = b.getDouble("longitude");

            StreetViewPanoramaFragment streetViewPanoramaFragment =(StreetViewPanoramaFragment) getFragmentManager().findFragmentById(R.id.streetviewpanorama);
            streetViewPanoramaFragment.getStreetViewPanoramaAsync(this);
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), "StreetView error:" + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("StreetView", "oStreetView error", e);
        }


    }

    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama streetViewPanorama) {
        try{
            streetViewPanorama.setPosition(new LatLng(latitude,longitude));
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), "StreetView error:" + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("StreetView", "oStreetView error", e);
        }

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

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
