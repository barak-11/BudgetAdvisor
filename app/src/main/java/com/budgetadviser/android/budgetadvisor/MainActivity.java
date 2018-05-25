package com.budgetadviser.android.budgetadvisor;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.exblr.dropdownmenu.DropdownListItem;
import com.exblr.dropdownmenu.DropdownMenu;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static android.util.Log.d;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private EditText input_price;
    private ListView list_data;
    private ProgressBar circular_progress;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private List<Purchase> list_purchases = new ArrayList<>();
    private Purchase selectedPurchase;
    private  String product;
    private DropdownMenu mDropdownMenu;
    private GridViewAdapter mGridViewAdapter;
    private ArrayList list = new ArrayList() {{
        add(new DropdownListItem(1, "Item 1"));
        add(new DropdownListItem(2, "Item 2"));
    }};
    private ArrayList productsList = createMockList(15, true,false);
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;

    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */
    private String address="";
    //FusedLocationProviderClient locationClient;
    private Integer budget;
    private Integer currentSpendings;
    private Integer remainedBudget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //line below makes sure that the widgets won't move when the keyboard visible
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        //Add toolbar
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("Budget Adviser");
        setSupportActionBar(toolbar);


        //Control
        budget=2000;
        currentSpendings=0;
        circular_progress = (ProgressBar)findViewById(R.id.circular_progress);
        input_price = (EditText)findViewById(R.id.price_tag);
        list_data = (ListView)findViewById(R.id.list_data);
        list_data.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Purchase purchase = (Purchase)adapterView.getItemAtPosition(i);
                selectedPurchase = purchase;
                input_price.setText(String.valueOf(purchase.getPrice()));
            }
        });

            mGridViewAdapter = new GridViewAdapter(this, productsList);

            View customContentView = getLayoutInflater().inflate(R.layout.ddm_custom_content, null, false);
            GridView gridView = (GridView) customContentView.findViewById(R.id.ddm_custom_content_gv);
            gridView.setAdapter(mGridViewAdapter);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    DropdownListItem item = mGridViewAdapter.setSelectedItem(position);
                    Log.d("item Text:",item.getText());
                    product = item.getText();
                    mDropdownMenu.setCurrentTitle(item.isEmptyItem() ? "Select Product" : item.getText());
                    mDropdownMenu.dismissCurrentPopupWindow();
                }
            });

            mDropdownMenu = (DropdownMenu) findViewById(R.id.dropdown_menu);
            mDropdownMenu.add("Items", customContentView);

        //Firebase
        try {
            initFirebase();
            addEventFirebaseListener();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }

        Integer negetive_count=0;
        Integer positive_count=0;
        for (Purchase purchase : list_purchases){
           if (purchase.getPrice()!=null) {
               currentSpendings = +purchase.getPrice();
               positive_count++;
           }
           else
               negetive_count++;

        }
        final TextView budget_tv = (TextView) findViewById(R.id.budget);
        remainedBudget = budget - currentSpendings;
        budget_tv.setText(remainedBudget.toString());
       // Toast.makeText(getApplicationContext(),"list_purchases "+list_purchases.size(), Toast.LENGTH_LONG).show();
       // Toast.makeText(getApplicationContext(),"negetive_count "+negetive_count.toString(), Toast.LENGTH_LONG).show();
       // Toast.makeText(getApplicationContext(),"positive_count "+positive_count.toString(), Toast.LENGTH_LONG).show();


        try {
            runLocationSettings();
            getLastLocation();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
        //Call the method below if you wish to get constant location updates
        //startLocationUpdates();

    }

    private void getLastLocation() throws IOException{

        //final TextView latlngNew = (TextView)findViewById(R.id.latLng);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {



                            Geocoder geocoder;
                            List<Address> addresses;
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            geocoder = new Geocoder(MainActivity.this, Locale.getDefault());

                            try {
                                addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                                address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                                String city = addresses.get(0).getLocality();
                                String state = addresses.get(0).getAdminArea();
                                String country = addresses.get(0).getCountryName();
                                String postalCode = addresses.get(0).getPostalCode();
                                String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
                                Log.d("debug",address+","+city+","+state+","+country+","+postalCode+","+knownName);
                                //latlngNew.setText(address);
                            }
                            catch (Exception e){
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            }


                        }
                        else {

                            Toast.makeText(getApplicationContext(),"location is null", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    private  void runLocationSettings(){
        //final TextView latlngNew = (TextView)findViewById(R.id.latLng);
        //Asking permission to access device's location
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                1);
        mFusedLocationClient = getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
    }
    private ArrayList createMockList(int count, boolean hasEmpty, boolean mock) {
        ArrayList list = new ArrayList();
        if (mock==false){
            list.add(new DropdownListItem(0, "Beer", true, true));
            list.add(new DropdownListItem(1, "Food"));
            list.add(new DropdownListItem(2, "Tickets"));
            list.add(new DropdownListItem(3, "Breakfast"));
            list.add(new DropdownListItem(3, "Lunch"));
            list.add(new DropdownListItem(3, "Dinner"));
            list.add(new DropdownListItem(4, "Snacks"));
            list.add(new DropdownListItem(5, "Transportation"));
            list.add(new DropdownListItem(6, "Groceries"));
            list.add(new DropdownListItem(7, "Cloths"));
            list.add(new DropdownListItem(8, "Gifts"));
            list.add(new DropdownListItem(9, "Other"));

        }
        else{
            if (hasEmpty) {
                list.add(new DropdownListItem(0, "不限", true, true));
            }
            for (int i = 1; i <= count; i++) {
                list.add(new DropdownListItem(10 + i, "Item-1-" + i));
            }
        }


        return list;
    }
    // This method checks for location updates
    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        mFusedLocationClient = getFusedLocationProviderClient(this);
        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }

    private void addEventFirebaseListener() {
        //Progressing
        circular_progress.setVisibility(View.VISIBLE);
        list_data.setVisibility(View.INVISIBLE);

        mDatabaseReference.child("purchase").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {


                    if (list_purchases.size() > 0)
                        list_purchases.clear();
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        Purchase purchase = new Purchase(postSnapshot.child("name").getValue().toString(), postSnapshot.child("uid").getValue().toString(), Integer.valueOf(postSnapshot.child("price").getValue().toString()), postSnapshot.child("address").getValue().toString());
                        list_purchases.add(purchase);
                        //Toast.makeText(getApplicationContext(),"currentSpendings "+currentSpendings.toString(), Toast.LENGTH_LONG).show();
                        currentSpendings+=Integer.valueOf(postSnapshot.child("price").getValue().toString());

                        //Toast.makeText(getApplicationContext(),"currentSpending"+Integer.valueOf(postSnapshot.child("price").getValue().toString()), Toast.LENGTH_LONG).show();
                    }
                    final TextView budget_tv = (TextView) findViewById(R.id.budget);
                    remainedBudget = budget - currentSpendings;
                    currentSpendings=0;
                    budget_tv.setText(remainedBudget.toString());
                    //Toast.makeText(getApplicationContext(),"currentSpendings "+currentSpendings.toString(), Toast.LENGTH_LONG).show();
                    //for (Purchase purchase : list_purchases){
                     //   if (purchase.getPrice()!=null)
                     //   currentSpendings=+purchase.getPrice();
                    //}
                    //Toast.makeText(getApplicationContext(),"currentSpending"+currentSpendings.toString(), Toast.LENGTH_LONG).show();
                    ListViewAdapter adapter = new ListViewAdapter(MainActivity.this, list_purchases, getApplicationContext());
                    list_data.setAdapter(adapter);

                    circular_progress.setVisibility(View.INVISIBLE);
                    list_data.setVisibility(View.VISIBLE);


                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "public void onDataChange:" + e.getMessage(), Toast.LENGTH_LONG).show();
                    //    e.printStackTrace();
                }
            }




            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initFirebase() {
        FirebaseApp.initializeApp(this);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference  = mFirebaseDatabase.getReference();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_add)
        {
            try{

                createPurchase();
            }
            catch (Exception e){
                Toast.makeText(getApplicationContext(),"Error:"+e.getMessage(), Toast.LENGTH_LONG).show();
            }


        }
        else if(item.getItemId() == R.id.menu_save)
        {
            try {
                if ( !input_price.getText().toString().matches("")) {

                    Purchase purchase = new Purchase( product,selectedPurchase.getUid(), Integer.valueOf(input_price.toString()),address);
                    updatePurchase(purchase);
                }
                else {

                    Toast.makeText(getApplicationContext(),"Please choose a value from the list", Toast.LENGTH_LONG).show();
                }
            }catch (Exception e){

                Toast.makeText(getApplicationContext(),"Error: "+e.getMessage(), Toast.LENGTH_LONG).show();

            }

        }
        else if(item.getItemId() == R.id.menu_remove){
            try{
                deletePurchase(selectedPurchase);
            }
            catch (Exception e){
                Toast.makeText(getApplicationContext(),"Error: "+e.getMessage(), Toast.LENGTH_LONG).show();
            }

        }
        return true;
    }

    private void deletePurchase(Purchase selectedPurchase) {
        mDatabaseReference.child("purchase").child(selectedPurchase.getUid()).removeValue();
        clearEditText();
    }

    private void updatePurchase(Purchase purchase) {
        mDatabaseReference.child("purchase").child(purchase.getUid()).child("name").setValue(purchase.getName());
        mDatabaseReference.child("purchase").child(purchase.getUid()).child("price_tag").setValue(purchase.getPrice());
        mDatabaseReference.child("purchase").child(purchase.getUid()).child("address").setValue(purchase.getAddress());
        clearEditText();
    }

    private void createPurchase() {
        //final TextView latlngNew = (TextView)findViewById(R.id.latLng);
            Purchase purchase = new Purchase(product,UUID.randomUUID().toString(), Integer.valueOf(input_price.getText().toString()),address);
            mDatabaseReference.child("purchase").child(purchase.getUid()).setValue(purchase);
            clearEditText();
    }
    private void clearEditText() {
        input_price.setText(String.valueOf(""));
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {

        String output;
        output = parent.getItemAtPosition(i).toString();
        d("Output",output);


    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        // New location has now been determined
        /*
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        final TextView latlngNew = (TextView)findViewById(R.id.latLng);
        latlngNew.setText(location.getLatitude()+","+location.getLongitude());
*/
        // You can now create a LatLng Object for use with maps
        //LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
    }
}
