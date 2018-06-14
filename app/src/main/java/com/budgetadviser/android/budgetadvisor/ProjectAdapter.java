package com.budgetadviser.android.budgetadvisor;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Random;

import static android.content.Context.MODE_PRIVATE;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ViewHolder> {


    private int position;

    private List<Project> mProject;
    SharedPreferences myDBfile; // create a file or return a reference to an exist file
    SharedPreferences.Editor myEditor;
    public ProjectAdapter(List<Project> projects) {
        mProject = projects;
    }
    @Override
    public ProjectAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View projectView = inflater.inflate(R.layout.set_project, parent, false);

        // Return a new holder instance
        ProjectAdapter.ViewHolder viewHolder = new ProjectAdapter.ViewHolder(projectView);
        return viewHolder;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener, View.OnCreateContextMenuListener{
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        TextView project_nameTextView;
        TextView project_budgetTextView;
        Button setButton;
        Button editButton;
        ImageView cardImageView;
        int index;
        Random rand;


        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            project_nameTextView = (TextView) itemView.findViewById(R.id.project_name);
            project_budgetTextView = (TextView) itemView.findViewById(R.id.project_budget);
            setButton = itemView.findViewById(R.id.setButton);
            editButton = itemView.findViewById(R.id.editButton);

            int resId[]={R.drawable.idea_cardview,R.drawable.monkey_selfie_cardview,R.drawable.iron_melt_cardview,R.drawable.other_cardview};
            rand = new Random();
            index = rand.nextInt((resId.length- 1) - 0 + 1) + 0;
            cardImageView = itemView.findViewById(R.id.imageView_card);
            cardImageView.setImageResource(resId[index]);

            itemView.setOnLongClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View v) {

        }

        @Override
        public boolean onLongClick(View v) {

            return true;
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Select The Action");
            menu.add(0, v.getId(), 0, "Set Project");//groupId, itemId, order, title
        }
    }
    @Override
    public void onBindViewHolder(ProjectAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        final Project currProject = mProject.get(position);

        // Set item views based on your views and data model
        TextView textView = viewHolder.project_nameTextView;
        textView.setText(currProject.getName());
        TextView textViewBudget = viewHolder.project_budgetTextView;
        textViewBudget.setText(String.valueOf(currProject.getBudget()));
        final ViewHolder finalViewHolder=viewHolder;
        finalViewHolder.setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDBfile = view.getContext().getSharedPreferences("budgets", MODE_PRIVATE);
                myEditor = myDBfile.edit();
                myEditor.putInt("Budget", currProject.getBudget());
                myEditor.putString("projectName", currProject.getName());
                myEditor.apply();
                Snackbar.make(view, "Success", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });
        finalViewHolder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        //uncomment snippet below to allow onlongclick
/*        finalViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setPosition(finalViewHolder.getAdapterPosition());
                return false;
            }
        });*/
    }
    @Override
    public int getItemCount() {
        if (mProject==null)
            return 0;
        else
            return mProject.size();
    }
    public int getPosition() {
        return position;
    }
    public void setPosition(int position) {
        this.position = position;
    }
}
