package com.budgetadviser.android.budgetadvisor;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static android.util.Log.d;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;


public class MainActivity extends BaseActivity implements AdapterView.OnItemSelectedListener,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private EditText input_price;
    private DropdownMenu mDropdownMenu;
    private ProgressBar circular_progress;
    private TextView projectTVtitle;
    private TextView projectTVoutput;
    private TextView budgetTVtitle;
    private TextView budgetTVoutput;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private List<Purchase> list_purchases = new ArrayList<>();
    private Purchase selectedPurchase;
    private  String product="";
    private DropdownViewAdapter mDropdownViewAdapter;
    private ArrayList list = new ArrayList() {{
        add(new DropdownListItem(1, "Item 1"));
        add(new DropdownListItem(2, "Item 2"));
    }};
    private ArrayList productsList = createProductsList(15, true,false);
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;

    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */
    private String address="";
    private Integer budget;
    private Integer currentSpendings;
    private Integer remainedBudget;
    private String projectName;

    RecyclerView rvContacts;
    SharedPreferences myDBfile; // create a file or return a reference to an exist file
    SharedPreferences.Editor myEditor;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
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

        projectTVtitle = findViewById(R.id.project_title);
        projectTVoutput = findViewById(R.id.project_title_name);
        budgetTVtitle = findViewById(R.id.budget_title);
        budgetTVoutput = findViewById(R.id.budget_title_name);

        Typeface myFont = Typeface.createFromAsset(getAssets(),"fonts/Abel-Regular.ttf");
        projectTVtitle.setTypeface(myFont);
        projectTVoutput.setTypeface(myFont);
        budgetTVtitle.setTypeface(myFont);
        budgetTVoutput.setTypeface(myFont);
        TextInputLayout textInputLayout= (TextInputLayout) findViewById(R.id.textInputLayoutId);
        textInputLayout.setTypeface(myFont);


        myDBfile = getSharedPreferences("budgets", MODE_PRIVATE);
        Integer savedBudget = myDBfile.getInt("Budget", -1);
        projectName = myDBfile.getString("projectName", "Default Project");




        projectTVoutput.setText(projectName);

        //Control
        budget= savedBudget == -1 ? 2000 : savedBudget;
        currentSpendings=0;
        circular_progress = (ProgressBar)findViewById(R.id.circular_progress);
        input_price = (EditText)findViewById(R.id.price_tag);

    //Set items in dropdown
            mDropdownViewAdapter = new DropdownViewAdapter(this, productsList);
            View customContentView = getLayoutInflater().inflate(R.layout.ddm_custom_content, null, false);
            GridView gridView = (GridView) customContentView.findViewById(R.id.ddm_custom_content_gv);
            gridView.setAdapter(mDropdownViewAdapter);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    DropdownListItem item = mDropdownViewAdapter.setSelectedItem(position);
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

        rvContacts = (RecyclerView) findViewById(R.id.rvContacts);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rvContacts.setLayoutManager(llm);
        PurchaseAdapter pAdapter = new PurchaseAdapter(list_purchases);
        rvContacts.setAdapter( pAdapter );

        try {
            runLocationSettings();
            getLastLocation();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
        //Call the method below if you wish to get constant location updates
        //startLocationUpdates();

    }
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }



    private void addEventFirebaseListener() {
        //Progressing
        circular_progress.setVisibility(View.VISIBLE);

        mDatabaseReference.child("purchase").child(getUid()).orderByChild("regularDate/time").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {
                    if (list_purchases.size() > 0)
                        list_purchases.clear();

                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        if (postSnapshot.child("projectName").getValue().toString().matches(projectName)){
                            String date_str = postSnapshot.child("date").getValue().toString();
                            String name_str = postSnapshot.child("name").getValue().toString();
                            String uid_str = postSnapshot.child("uid").getValue().toString();
                            String address_str = postSnapshot.child("address").getValue().toString();
                            String price_str = postSnapshot.child("price").getValue().toString();
                            String project_str = postSnapshot.child("projectName").getValue().toString();

                            Purchase purchase = new Purchase(name_str, uid_str, Integer.valueOf(price_str), address_str,date_str,project_str);
                            list_purchases.add(purchase);
                            currentSpendings+=Integer.valueOf(postSnapshot.child("price").getValue().toString());
                        }

                    }
                    final TextView budget_tv = (TextView) findViewById(R.id.budget_title_name);
                    remainedBudget = budget - currentSpendings;
                    currentSpendings=0;
                    budget_tv.setText(remainedBudget.toString());

                    rvContacts = (RecyclerView) findViewById(R.id.rvContacts);
                    PurchaseAdapter pAdapter = new PurchaseAdapter(list_purchases);
                    // Attach the adapter to the recyclerview to populate items

                    pAdapter.SetOnItemClickListener(new PurchaseAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(View v, int position, String id) {

                            Purchase purchase = list_purchases.get(position);
                            selectedPurchase=purchase;
                            product=purchase.getName();
                            input_price.setText(String.valueOf(purchase.getPrice()));
                            mDropdownMenu.setCurrentTitle(purchase.getName());
                           // DropdownListItem item = mDropdownViewAdapter.setSelectedItem(position);
                            mDropdownMenu.dismissCurrentPopupWindow();
                        }
                    });

                    LinearLayoutManager llm = new LinearLayoutManager(MainActivity.this);
                    llm.setOrientation(LinearLayoutManager.VERTICAL);
                    llm.setReverseLayout(true); // in order to display the database records ordered by newest to oldest
                    llm.setStackFromEnd(true); // in order to display the database records ordered by newest to oldest
                    rvContacts.setLayoutManager(llm);
                    rvContacts.setAdapter(pAdapter);
                    // Set layout manager to position the items

                    circular_progress.setVisibility(View.INVISIBLE);


                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "onDataChange() error:" + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("Budget", "onDataChange() error", e);

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void deletePurchase(Purchase selectedPurchase) {
        mDatabaseReference.child("purchase").child(getUid()).child(selectedPurchase.getUid()).removeValue();
        clearEditText();
    }

    private void updatePurchase(Purchase purchase) {
        mDatabaseReference.child("purchase").child(getUid()).child(purchase.getUid()).child("name").setValue(purchase.getName());
        mDatabaseReference.child("purchase").child(getUid()).child(purchase.getUid()).child("price").setValue(purchase.getPrice());
        mDatabaseReference.child("purchase").child(getUid()).child(purchase.getUid()).child("address").setValue(purchase.getAddress());
        mDatabaseReference.child("purchase").child(getUid()).child(purchase.getUid()).child("date").setValue(purchase.getDate());
        mDatabaseReference.child("purchase").child(getUid()).child(purchase.getUid()).child("projectName").setValue(purchase.getProjectName());
        clearEditText();
    }

    private void createPurchase() {
        //final TextView latlngNew = (TextView)findViewById(R.id.latLng);
        String sInput_Price="";
        if (product.matches(""))
            product="";
        if (input_price.getText().toString().matches(""))
            sInput_Price="0";
        else
            sInput_Price=input_price.getText().toString();
        if (address.matches(""))
            address="";

        Purchase purchase = new Purchase(product,UUID.randomUUID().toString(), Integer.valueOf(sInput_Price),address,Calendar.getInstance().getTime().toString(),projectName);
        mDatabaseReference.child("purchase").child(getUid()).child(purchase.getUid()).setValue(purchase);
        clearEditText();
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
                Log.e("Budget", "create error", e);
                Toast.makeText(getApplicationContext(),"Error:"+e.getMessage(), Toast.LENGTH_LONG).show();
            }


        }
        else if(item.getItemId() == R.id.menu_save)
        {
            try {
                if ( !input_price.getText().toString().matches("")) {

                    Purchase purchase = new Purchase( product,selectedPurchase.getUid(), Integer.valueOf(input_price.getText().toString()),address, selectedPurchase.getDate(),projectName);
                    updatePurchase(purchase);
                }
                else {

                    Toast.makeText(getApplicationContext(),"Please choose a value from the list", Toast.LENGTH_LONG).show();
                }
            }catch (Exception e){

                Toast.makeText(getApplicationContext(),"Error: "+e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("Budget", "save error", e);

            }

        }
        else if(item.getItemId() == R.id.menu_remove){
            try{
                deletePurchase(selectedPurchase);
            }
            catch (Exception e){
                Toast.makeText(getApplicationContext(),"Error: "+e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("Budget", "delete error", e);
            }

        }
        else if(item.getItemId() == R.id.menu_graph){
            try{
                Intent intent = new Intent(this, StatsActivity.class);
                startActivity(intent);
            }catch (Exception e){
                Toast.makeText(getApplicationContext(),"Error: "+e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("Budget", "stats error", e);
            }
        }
        else if(item.getItemId() == R.id.menu_setBudget){
            try{
               // Intent intent = new Intent(this, SettingsActivity.class);
                //startActivity(intent);
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(MainActivity.this);
                }
                final EditText input = new EditText(MainActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                builder.setView(input); // uncomment this line
                builder.setTitle("Budget Adviser Settings")
                        .setMessage("Please set the amount of budget for this project")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                myDBfile = getSharedPreferences("budgets", MODE_PRIVATE);
                                myEditor = myDBfile.edit();
                                Integer savedBudget = myDBfile.getInt("Budget", -1);

                                myEditor.putInt("Budget", Integer.valueOf(input.getText().toString()));
                                myEditor.apply();

                                currentSpendings=0;
                                for (Purchase purchase : list_purchases){
                                    if (purchase.getPrice()!=null) {
                                        currentSpendings +=  purchase.getPrice();
                                    }
                                }

                                TextView budget_tv = (TextView) findViewById(R.id.budget_title_name);
                                budget=Integer.valueOf(input.getText().toString());
                                String budgeString = String.valueOf(Integer.valueOf(input.getText().toString())-currentSpendings);
                                budget_tv.setText(budgeString);

                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_dialer)
                        .show();
            }catch (Exception e){
                Toast.makeText(getApplicationContext(),"Error: "+e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("Budget", "stats error", e);
            }
        }
        else if(item.getItemId() == R.id.menu_setProject){
            try{
                Intent intent = new Intent(this, SetProjectActivity.class);
                startActivity(intent);
            }catch (Exception e){
                Toast.makeText(getApplicationContext(),"Error: "+e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("Budget", "stats error", e);
            }
/*            try{

                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(MainActivity.this);
                }
                final EditText input = new EditText(MainActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                builder.setView(input); // uncomment this line
                builder.setTitle("Budget Adviser Settings")
                        .setMessage("Please select a name for the project")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                myDBfile = getSharedPreferences("budgets", MODE_PRIVATE);
                                myEditor = myDBfile.edit();
                                //Integer savedBudget = myDBfile.getInt("Budget", -1);

                                myEditor.putString("projectName", input.getText().toString());
                                myEditor.apply();
                                startActivity(getIntent());
                                finish();

                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_dialer)
                        .show();
            }catch (Exception e){
                Toast.makeText(getApplicationContext(),"Error: "+e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("Budget", "stats error", e);
            }*/
        }
        return true;
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
        //Asking permission to access device's location
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                1);
        mFusedLocationClient = getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
    }
    private ArrayList createProductsList(int count, boolean hasEmpty, boolean mock) {
        ArrayList list = new ArrayList();
        if (mock==false){
            list.add(new DropdownListItem(0, "Beer", false, false));
            list.add(new DropdownListItem(1, "Food"));
            list.add(new DropdownListItem(2, "Tickets"));
            list.add(new DropdownListItem(3, "Breakfast"));
            list.add(new DropdownListItem(4, "Lunch"));
            list.add(new DropdownListItem(5, "Dinner"));
            list.add(new DropdownListItem(6, "Snacks"));
            list.add(new DropdownListItem(7, "Transportation"));
            list.add(new DropdownListItem(8, "Groceries"));
            list.add(new DropdownListItem(9, "Cloths"));
            list.add(new DropdownListItem(10, "Gifts"));
            list.add(new DropdownListItem(11, "Drinks"));
            list.add(new DropdownListItem(12, "Baby Stuff"));
            list.add(new DropdownListItem(13, "Other"));

        }

        return list;
    }

    private void clearEditText() {

        input_price.setText(String.valueOf(""));
        mDropdownMenu.setCurrentTitle("Select Product");
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onConnected( Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

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

}
