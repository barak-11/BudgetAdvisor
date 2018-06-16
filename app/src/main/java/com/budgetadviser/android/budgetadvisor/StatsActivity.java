package com.budgetadviser.android.budgetadvisor;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.budgetadviser.android.budgetadvisor.model.Purchase;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsActivity extends BaseActivity {
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private List<Purchase> list_purchases = new ArrayList<>();
    private ProgressBar circular_progress;
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
        circular_progress.setVisibility(View.VISIBLE);


        mDatabaseReference.child("purchase").child(getUid()).orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {
                    if (list_purchases.size() > 0)
                        list_purchases.clear();


                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        if (postSnapshot.child("projectName").getValue().toString().matches(projectName)) {
                            Purchase purchase = new Purchase(postSnapshot.child("name").getValue().toString(), postSnapshot.child("uid").getValue().toString(), Integer.valueOf(postSnapshot.child("price").getValue().toString()), postSnapshot.child("address").getValue().toString(), postSnapshot.child("date").getValue().toString(), projectName,Double.valueOf(postSnapshot.child("latitude").getValue().toString()),Double.valueOf(postSnapshot.child("longitude").getValue().toString()), postSnapshot.child("timestamp").getValue().toString());
                            list_purchases.add(purchase);
                        }
                    }

                    Map<String,List<Purchase>> datesWithPurchasesMap = new HashMap<>();
                    Map<String,Integer> datesWithPurchasesCounterMap = new HashMap<>();
                    Integer countPurchases=0;
                    Integer currentPurchases=0;
                    Calendar calendar = Calendar.getInstance();
                    String dayOfTheWeek,day,monthString,monthNumber,year;
                    String mapKey;
                    List<String> myStringList = new ArrayList<>();
                    for (Purchase pur : list_purchases){
//                        dayOfTheWeek = (String) DateFormat.format("EEEE", pur.getRegularDate()); // Thursday
                        day          = (String) DateFormat.format("dd",   pur.getRegularDate()); // 20
//                        monthString  = (String) DateFormat.format("MMM",  pur.getRegularDate()); // Jun
                        monthNumber  = (String) DateFormat.format("MM",   pur.getRegularDate()); // 06
                        year         = (String) DateFormat.format("yyyy", pur.getRegularDate()); // 2013





                        mapKey = day+"/"+monthNumber+"/"+year;
                        System.out.println("mapkey:"+mapKey);

                        if (!datesWithPurchasesMap.containsKey(mapKey)){
                            countPurchases=1;
                            datesWithPurchasesCounterMap.put(mapKey,countPurchases);
                            datesWithPurchasesMap.put(mapKey,new ArrayList<Purchase>());
                            datesWithPurchasesMap.get(mapKey).add(pur);
                            myStringList.add(mapKey);
                        }
                        else {
                            currentPurchases=0;
                            currentPurchases=datesWithPurchasesCounterMap.get(mapKey);
                            currentPurchases++;
                            datesWithPurchasesCounterMap.put(mapKey,currentPurchases);

                            datesWithPurchasesMap.get(mapKey).add(pur);
                        }

                    }


                    BarChart chart = (BarChart) findViewById(R.id.chart);
                    List<BarEntry> entries = new ArrayList<>();
                    List<String> labels = new ArrayList<>();
                    Collections.sort(myStringList);
                    int counter = 0;
                    for (String str : myStringList){
                        labels.add(str);
                        entries.add(new BarEntry( counter,datesWithPurchasesCounterMap.get(str)));
                        counter++;
                    }

                    XAxis bottomAxis = chart.getXAxis();

                    bottomAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
                    bottomAxis.setLabelCount(entries.size());
                    bottomAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    bottomAxis.setDrawLabels(true);
                    bottomAxis.setDrawGridLines(false);
                    bottomAxis.setDrawAxisLine(true);


                    YAxis rightYAxis = chart.getAxisRight();

                    rightYAxis.setEnabled(false);
                    BarDataSet set = new BarDataSet(entries, "Amount of Purchases");
                    BarData data = new BarData(set);

                    data.setBarWidth(0.5f); // set custom bar width
                    chart.setData(data);
                    chart.setDescription(null);
                    chart.setPinchZoom(false);
                    chart.setScaleEnabled(false);
                    chart.setDrawBarShadow(false);
                    chart.setDrawGridBackground(false);
                    chart.animateY(2000);
                    chart.getData().setValueTextSize(10);
                    chart.getLegend().setEnabled(true);

                    chart.invalidate(); // refresh

                    System.out.println("datesWithPurchasesCounterMap values are:"+datesWithPurchasesCounterMap.values());
                    System.out.println("datesWithPurchasesCounterMap keys are:"+datesWithPurchasesCounterMap.keySet());

                    circular_progress.setVisibility(View.INVISIBLE);



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
    public Calendar getCalendar(int day, int month, int year) {
        Calendar date = Calendar.getInstance();
        date.set(Calendar.YEAR, year);

        // We will have to increment the month field by 1

        date.set(Calendar.MONTH, month+1);

        // As the month indexing starts with 0

        date.set(Calendar.DAY_OF_MONTH, day);

        return date;
    }
    public Date getRegularDate(String dateTime) {
        SimpleDateFormat format = new SimpleDateFormat("dd/EEE/yyyy");
        Date date = new Date();
        try {
            date = format.parse(dateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

}