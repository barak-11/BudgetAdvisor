package com.budgetadviser.android.budgetadvisor;

import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ProjectActivity extends BaseActivity {
    private ProgressBar circular_progress;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private List<Project> projectNames = new ArrayList<>();
    private Map<String,String> projectNamesMap;
    SharedPreferences myDBfile;
    SharedPreferences.Editor myEditor;
    String newProjectName;
    int newProjectBudget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_project);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Project Settings");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //adds the back arrow to the toolbar
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                //createProject();
                ProjectCreateDialogFragment newFragment = new ProjectCreateDialogFragment();
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.replace(R.id.placeholder_layout, newFragment).addToBackStack(null).commit(); // newInstance() is a static factory method.


            }
        });
        circular_progress = (ProgressBar)findViewById(R.id.circular_progress);
        projectNamesMap = new HashMap<>();
        //projectNames = new ArrayList<String>();
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
        mAdapter = new ProjectAdapter(projectNames);
        mRecyclerView.setAdapter(mAdapter);
    }
    private void addEventFirebaseListener() {
        //Progressing
        circular_progress.setVisibility(View.VISIBLE);

        mDatabaseReference.child("project").child(getUid()).orderByChild("createdDate").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {
                    if (projectNames==null){
                        projectNames=new ArrayList<>();
                    }
                    if(projectNames.size() > 0)
                        projectNames.clear();

                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {


                        String name_str = postSnapshot.child("name").getValue().toString();
                        String uid_str = postSnapshot.child("uid").getValue().toString();
                        String createdDate_str = postSnapshot.child("createdDate").getValue().toString();
                        int budget_int = Integer.valueOf(postSnapshot.child("budget").getValue().toString());


                        Project project = new Project(name_str,budget_int , createdDate_str,uid_str );
                        projectNames.add(project);

                    }
                    //projectNames = new ArrayList<String>(projectNamesMap.values());
                    ProjectAdapter pAdapter = new ProjectAdapter(projectNames);

                    LinearLayoutManager llm = new LinearLayoutManager(ProjectActivity.this);
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
            position = ((ProjectAdapter) mRecyclerView.getAdapter()).getPosition();
        } catch (Exception e) {
            return super.onContextItemSelected(item);
        }
        if(item.getTitle()=="Set Project"){
            myDBfile = getSharedPreferences("budgets", MODE_PRIVATE);
            myEditor = myDBfile.edit();
            myEditor.putString("projectName", projectNames.get(position).getName());
            myEditor.putInt("Budget", projectNames.get(position).getBudget());
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

    /**
     *
     * this method overrides the back button on the mobile device, when a user
     * is on the fragment view and taps 'back' this method will call ProjectActivity so the float button will reappear

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {

            Intent intent = new Intent(this, ProjectActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
*/
}
