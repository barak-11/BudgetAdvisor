package com.budgetadviser.android.budgetadvisor;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsActivity extends BaseActivity {
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private List<Purchase> list_purchases = new ArrayList<>();
    private ProgressBar circular_progress;
    private GraphView graph;
    SharedPreferences myDBfile; // create a file or return a reference to an exist file
    SharedPreferences.Editor myEditor;
    private String projectName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        circular_progress = (ProgressBar)findViewById(R.id.circular_progress);
        graph = (GraphView) findViewById(R.id.graph);
        myDBfile = getSharedPreferences("budgets", MODE_PRIVATE);
        Integer savedBudget = myDBfile.getInt("Budget", -1);
        projectName = myDBfile.getString("projectName", "Default Project");

        try {
            initFirebase();
            addEventFirebaseListener();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }




    }
    private void initFirebase() {
        FirebaseApp.initializeApp(this);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference  = mFirebaseDatabase.getReference();
    }
    private void addEventFirebaseListener() {
        //Progressing
        graph.setVisibility(View.INVISIBLE);
        circular_progress.setVisibility(View.VISIBLE);


        mDatabaseReference.child("purchase").child(getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {
                    if (list_purchases.size() > 0)
                        list_purchases.clear();


                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        Purchase purchase = new Purchase(postSnapshot.child("name").getValue().toString(), postSnapshot.child("uid").getValue().toString(), Integer.valueOf(postSnapshot.child("price").getValue().toString()), postSnapshot.child("address").getValue().toString(),postSnapshot.child("date").getValue().toString(),projectName);
                        list_purchases.add(purchase);
                    }

                    Map<String,List<Purchase>> datesWithPurchasesMap = new HashMap<>();
                    Map<String,Integer> datesWithPurchasesCounterMap = new HashMap<>();
                    Integer countPurchases=0;
                    Integer currentPurchases=0;
                    Calendar calendar = Calendar.getInstance();
                    String dayOfTheWeek,day,monthString,monthNumber,year;
                    String mapKey;

                    for (Purchase pur : list_purchases){
                        dayOfTheWeek = (String) DateFormat.format("EEEE", pur.getRegularDate()); // Thursday
                        day          = (String) DateFormat.format("dd",   pur.getRegularDate()); // 20
                        monthString  = (String) DateFormat.format("MMM",  pur.getRegularDate()); // Jun
                        monthNumber  = (String) DateFormat.format("MM",   pur.getRegularDate()); // 06
                        year         = (String) DateFormat.format("yyyy", pur.getRegularDate()); // 2013

                        mapKey = day+"/"+monthString+"/"+year;

                        if (!datesWithPurchasesMap.containsKey(mapKey)){
                            countPurchases=1;
                            datesWithPurchasesCounterMap.put(mapKey,countPurchases);
                            datesWithPurchasesMap.put(mapKey,new ArrayList<Purchase>());
                            datesWithPurchasesMap.get(mapKey).add(pur);
                        }
                        else {
                            currentPurchases=0;
                            currentPurchases=datesWithPurchasesCounterMap.get(mapKey);
                            currentPurchases++;
                            datesWithPurchasesCounterMap.put(mapKey,currentPurchases);

                            datesWithPurchasesMap.get(mapKey).add(pur);
                        }
                    }
                    System.out.println("datesWithPurchasesCounterMap values are:"+datesWithPurchasesCounterMap.values());
                    System.out.println("datesWithPurchasesCounterMap keys are:"+datesWithPurchasesCounterMap.keySet());
                    LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
                    //DataPoint datapoints [] = new DataPoint[];

                    for (String d : datesWithPurchasesCounterMap.keySet()){
                        series.resetData(new DataPoint[] {});
                        DataPoint point = new DataPoint(getRegularDate(d), datesWithPurchasesCounterMap.get(d));
                        System.out.println("getRegularDate(d): "+getRegularDate(d));
                        series.appendData(point, true,datesWithPurchasesCounterMap.get(d));
                    }
                    Date d1 = calendar.getTime();
                    System.out.println("d1 calendar: "+d1);



                    graph.addSeries(series);

// set date label formatter
                    graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(StatsActivity.this));
                    //graph.getGridLabelRenderer().setNumHorizontalLabels(5); // only 4 because of the space

// set manual x bounds to have nice steps
                    //graph.getViewport().setMinX(d1.getTime());
                    //graph.getViewport().setMaxX(d3.getTime());
                    //graph.getViewport().setXAxisBoundsManual(true);

// as we use dates as labels, the human rounding to nice readable numbers
// is not necessary
                    graph.getGridLabelRenderer().setHumanRounding(false);



                    circular_progress.setVisibility(View.INVISIBLE);
                    graph.setVisibility(View.VISIBLE);


                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "onDataChange() error:" + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("Budget", "onDataChange() error", e);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public Date getRegularDate(String dateTime) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MMM/yyyy");
        Date date = new Date();
        try {
            date = format.parse(dateTime);
            System.out.println(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

}