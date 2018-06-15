package com.budgetadviser.android.budgetadvisor;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.UUID;

import static android.content.Context.MODE_PRIVATE;

public class ProjectCreateDialogFragment extends DialogFragment implements AdapterView.OnItemSelectedListener {

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    SharedPreferences myDBfile; // create a file or return a reference to an exist file
    SharedPreferences.Editor myEditor;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.full_screen_dialog, container, false);
        final FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
         final EditText projName = rootView.findViewById(R.id.project_name_set);
         final EditText projBudget = rootView.findViewById(R.id.project_budget_set);

        final Spinner spinner = (Spinner) rootView.findViewById(R.id.currency_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(rootView.getContext(),R.array.currency_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setOnItemSelectedListener(this);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        try {
            initFirebase();
        }catch (Exception e){
            Toast.makeText(rootView.getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
        (rootView.findViewById(R.id.button_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                fab.setVisibility(View.VISIBLE);

            }
        });
        (rootView.findViewById(R.id.createbutton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String projectName=projName.getText().toString();
                int budgetInt = Integer.valueOf(projBudget.getText().toString());
                String currency;
                if (spinner.getSelectedItem().toString()==null)
                    currency = "$";
                else
                    currency = spinner.getSelectedItem().toString();
                Project proj = new Project(projectName,budgetInt, Calendar.getInstance().getTime().toString(), UUID.randomUUID().toString(),currency);
                mDatabaseReference.child("project").child(getUid()).child(proj.getUid()).setValue(proj);

                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                CheckBox checkBox = rootView.findViewById(R.id.checkBox);
                if(checkBox.isChecked()){
                    myDBfile = getActivity().getSharedPreferences("budgets", MODE_PRIVATE);
                    myEditor = myDBfile.edit();
                    myEditor.putInt("Budget", budgetInt);
                    myEditor.putString("projectName", projectName);
                    myEditor.putString("currency", currency);
                    myEditor.apply();
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                }

                dismiss();
                fab.setVisibility(View.VISIBLE);

            }
        });



        return rootView;

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }
    private void initFirebase() {

        FirebaseApp.initializeApp(getActivity());
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference  = mFirebaseDatabase.getReference();
    }
    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        //parent.getItemAtPosition(pos)
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
