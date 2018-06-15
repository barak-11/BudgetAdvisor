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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
                    List<Date> myList = new ArrayList<>();
                    for (Purchase pur : list_purchases){
                        dayOfTheWeek = (String) DateFormat.format("EEEE", pur.getRegularDate()); // Thursday
                        day          = (String) DateFormat.format("dd",   pur.getRegularDate()); // 20
                        monthString  = (String) DateFormat.format("MMM",  pur.getRegularDate()); // Jun
                        monthNumber  = (String) DateFormat.format("MM",   pur.getRegularDate()); // 06
                        year         = (String) DateFormat.format("yyyy", pur.getRegularDate()); // 2013

                        Calendar cal = Calendar.getInstance();
                        cal.setTime(pur.getRegularDate());


                        mapKey = cal.getTime().toString();

                        if (!datesWithPurchasesMap.containsKey(mapKey)){
                            countPurchases=1;
                            datesWithPurchasesCounterMap.put(mapKey,countPurchases);
                            datesWithPurchasesMap.put(mapKey,new ArrayList<Purchase>());
                            datesWithPurchasesMap.get(mapKey).add(pur);
                            myList.add(pur.getRegularDate());
                        }
                        else {
                            currentPurchases=0;
                            currentPurchases=datesWithPurchasesCounterMap.get(mapKey);
                            currentPurchases++;
                            datesWithPurchasesCounterMap.put(mapKey,currentPurchases);

                            datesWithPurchasesMap.get(mapKey).add(pur);
                        }

                    }
                    Collections.sort(myList, new Comparator<Date>(){

                        @Override
                        public int compare(Date o1, Date o2) {
                            return o1.compareTo(o2);
                        }
                    });
                    System.out.println("datesWithPurchasesCounterMap values are:"+datesWithPurchasesCounterMap.values());
                    System.out.println("datesWithPurchasesCounterMap keys are:"+datesWithPurchasesCounterMap.keySet());
                    LineGraphSeries<DataPoint> series = new LineGraphSeries<>();

                    series.resetData(new DataPoint[] {});
                    for (String d : datesWithPurchasesCounterMap.keySet()){

                        //DataPoint point = new DataPoint(getRegularDate(d), datesWithPurchasesCounterMap.get(d));
                        System.out.println("getRegularDate(d): "+getRegularDate(d));
                        series.appendData(new DataPoint(getRegularDate(d), datesWithPurchasesCounterMap.get(d)), true,256);
                    }
                    Calendar calMin = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/EEE/yyyy", Locale.ENGLISH);
                    calMin.setTime(myList.get(0));
                    Date dT1 = calMin.getTime();
                    Calendar calMax = Calendar.getInstance();
                    calMax.setTime(myList.get(myList.size()-1));
                    Date dT2 = calMax.getTime();
                   /* Date d1 = calendar.getTime();
                    System.out.println("d1 calendar: "+d1);*/
                    //System.out.println("series:"+series);



                    graph.addSeries(series);

// set date label formatter
                    graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(StatsActivity.this));
                    graph.getGridLabelRenderer().setNumHorizontalLabels(5); // only 4 because of the space

// set manual x bounds to have nice steps

                    graph.getViewport().setMinX(dT1.getTime());
                    graph.getViewport().setMaxX(dT2.getTime());
                    graph.getViewport().setXAxisBoundsManual(true);

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
        SimpleDateFormat format = new SimpleDateFormat("dd/MMM/yyyy");
        Date date = new Date();
        try {
            date = format.parse(dateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

}