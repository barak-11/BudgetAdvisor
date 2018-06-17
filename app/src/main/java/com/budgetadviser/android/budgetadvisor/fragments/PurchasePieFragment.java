package com.budgetadviser.android.budgetadvisor.fragments;

import android.content.SharedPreferences;
import android.graphics.Color;
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
import com.budgetadviser.android.budgetadvisor.model.MyValueFormatter;
import com.budgetadviser.android.budgetadvisor.model.Purchase;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class PurchasePieFragment extends Fragment {
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private List<Purchase> list_purchases = new ArrayList<>();
    private ProgressBar circular_progress;
    SharedPreferences myDBfile; // create a file or return a reference to an exist file
    SharedPreferences.Editor myEditor;
    private String projectName;

    public PurchasePieFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myDBfile = this.getActivity().getSharedPreferences("budgets", MODE_PRIVATE);
        Integer savedBudget = myDBfile.getInt("Budget", -1);
        projectName = myDBfile.getString("projectName", "Default Project");

        try {
            initFirebase();
            addEventFirebaseListener();
        } catch (Exception e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.purchase_pie_fragment, container, false);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void initFirebase() {
        FirebaseApp.initializeApp(getContext());
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
    }

    private void addEventFirebaseListener() {
        //Progressing


        mDatabaseReference.child("purchase").child(getUid()).orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {
                    if (list_purchases.size() > 0)
                        list_purchases.clear();


                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        if (postSnapshot.child("projectName").getValue().toString().matches(projectName)) {
                            Purchase purchase = new Purchase(postSnapshot.child("name").getValue().toString(), postSnapshot.child("uid").getValue().toString(), Integer.valueOf(postSnapshot.child("price").getValue().toString()), postSnapshot.child("address").getValue().toString(), postSnapshot.child("date").getValue().toString(), projectName, Double.valueOf(postSnapshot.child("latitude").getValue().toString()), Double.valueOf(postSnapshot.child("longitude").getValue().toString()), postSnapshot.child("timestamp").getValue().toString());
                            list_purchases.add(purchase);
                        }
                    }

                    Map<String,Integer> productsWithCounter = new HashMap<>();
                    int tempCounter=0;
                    for (Purchase pur : list_purchases){
                        tempCounter=0;
                        if (productsWithCounter.containsKey(pur.getName())){
                            tempCounter = productsWithCounter.get(pur.getName());
                            tempCounter++;
                            productsWithCounter.put(pur.getName(),tempCounter);
                        }
                        else {
                            tempCounter=1;
                            productsWithCounter.put(pur.getName(),tempCounter);
                        }
                    }
                    tempCounter=0;
                    List<PieEntry> yvalues = new ArrayList<>();
                    ArrayList<String> xVals = new ArrayList<>();
                    for (String str : productsWithCounter.keySet()) {
                        xVals.add(str);
                        yvalues.add(new PieEntry(productsWithCounter.get(str),str));
                        tempCounter++;
                    }
                    PieChart pieChart = getActivity().findViewById(R.id.piechart);
                    PieDataSet dataSet = new PieDataSet(yvalues, "Purchases");
                    dataSet.setValueFormatter(new MyValueFormatter());

                    dataSet.setColors(ColorTemplate.VORDIPLOM_COLORS);
                    PieData data = new PieData(dataSet);
                    pieChart.setDrawHoleEnabled(false);
                    data.setValueTextSize(13f);
                    data.setValueTextColor(Color.DKGRAY);
                    pieChart.animateXY(1400, 1400);
                    pieChart.setData(data);
                    pieChart.invalidate(); // refresh

                    System.out.println("productsWithCounter values are:"+productsWithCounter.values());
                    System.out.println("productsWithCounter keys are:"+productsWithCounter.keySet());


                } catch (Exception e) {
                    Toast.makeText(getContext(), "onDataChange() error:" + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("Budget", "onDataChange() error", e);

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

