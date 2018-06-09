package com.budgetadviser.android.budgetadvisor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SetProjectActivity extends BaseActivity {
    private ProgressBar circular_progress;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private List<String> projectNames;
    private Map<String,String> projectNamesMap;
    SharedPreferences myDBfile;
    SharedPreferences.Editor myEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_project);
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
        projectNamesMap = new HashMap<>();
        projectNames = new ArrayList<String>();
        //Firebase
        try {
            initFirebase();
            addEventFirebaseListener();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new SetProjectAdapter(projectNames);
        mRecyclerView.setAdapter(mAdapter);
    }
    private void addEventFirebaseListener() {
        //Progressing
        circular_progress.setVisibility(View.VISIBLE);

        mDatabaseReference.child("purchase").child(getUid()).orderByChild("regularDate/time").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {

                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                        String project_str = postSnapshot.child("projectName").getValue().toString();
                        if (projectNamesMap.containsKey(postSnapshot.child("projectName").getValue().toString()))
                        continue;
                        else
                            projectNamesMap.put(postSnapshot.child("projectName").getValue().toString(),postSnapshot.child("projectName").getValue().toString());

                    }
                    projectNames = new ArrayList<String>(projectNamesMap.values());
                    SetProjectAdapter pAdapter = new SetProjectAdapter(projectNames);

                    LinearLayoutManager llm = new LinearLayoutManager(SetProjectActivity.this);
                    llm.setOrientation(LinearLayoutManager.VERTICAL);
                    llm.setReverseLayout(true); // in order to display the database records ordered by newest to oldest
                    llm.setStackFromEnd(true); // in order to display the database records ordered by newest to oldest
                    mRecyclerView.setLayoutManager(llm);
                    mRecyclerView.setAdapter(pAdapter);
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
    private void initFirebase() {

        FirebaseApp.initializeApp(this);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference  = mFirebaseDatabase.getReference();


    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int position = -1;
        try {
            position = ((SetProjectAdapter) mRecyclerView.getAdapter()).getPosition();
        } catch (Exception e) {
            return super.onContextItemSelected(item);
        }
        if(item.getTitle()=="Set Project"){
            myDBfile = getSharedPreferences("budgets", MODE_PRIVATE);
            myEditor = myDBfile.edit();
            myEditor.putString("projectName", projectNames.get(position));
            myEditor.apply();

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        else if(item.getTitle()==""){

        }else{
            return false;
        }
        return super.onContextItemSelected(item);
    }

}
