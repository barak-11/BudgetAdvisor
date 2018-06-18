package com.budgetadviser.android.budgetadvisor.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.budgetadviser.android.budgetadvisor.R;
import com.budgetadviser.android.budgetadvisor.model.Purchase;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class AmountOfPurchasesPerDayFragment extends Fragment {
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private List<Purchase> list_purchases = new ArrayList<>();
    private ProgressBar circular_progress;
    SharedPreferences myDBfile; // create a file or return a reference to an exist file
    SharedPreferences.Editor myEditor;
    private String projectName;

    public AmountOfPurchasesPerDayFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.amount_of_purchases_per_day_fragment, container, false);
        myDBfile = rootView.getContext().getSharedPreferences("budgets", MODE_PRIVATE);
        Integer savedBudget = myDBfile.getInt("Budget", -1);
        projectName = myDBfile.getString("projectName", "Default Project");

        try {
            initFirebase();
            addEventFirebaseListener(rootView);
        }catch (Exception e){
            Toast.makeText(rootView.getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return rootView;

    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
    private void initFirebase() {
        FirebaseApp.initializeApp(getActivity());
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference  = mFirebaseDatabase.getReference();
    }
    private void addEventFirebaseListener(final View rootView) {
        //Progressing


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


                    BarChart chart = rootView.findViewById(R.id.chart);
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
                    bottomAxis.setGranularity(1f);

                    bottomAxis.setDrawGridLines(false);
                    bottomAxis.setDrawAxisLine(true);


                    YAxis rightYAxis = chart.getAxisRight();

                    YAxis leftYAxis = chart.getAxisLeft();
                    leftYAxis.setAxisMinimum(0);

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
                    chart.animateY(1000);
                    chart.getData().setValueTextSize(10);
                    chart.getLegend().setEnabled(true);

                    chart.invalidate(); // refresh

                    //System.out.println("datesWithPurchasesCounterMap values are:"+datesWithPurchasesCounterMap.values());
                    //System.out.println("datesWithPurchasesCounterMap keys are:"+datesWithPurchasesCounterMap.keySet());




                } catch (Exception e) {
                    Log.e("Budget", "onDataChange() error", e);
                    Toast.makeText(rootView.getContext(), "onDataChange() error:" + e.getMessage(), Toast.LENGTH_LONG).show();


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

}