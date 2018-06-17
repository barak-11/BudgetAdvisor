package com.budgetadviser.android.budgetadvisor.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.budgetadviser.android.budgetadvisor.R;

public class AmountOfPurchasesPerDayFragment extends Fragment {

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.amount_of_purchases_per_day_fragment, container, false);
    }

}