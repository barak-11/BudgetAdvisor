package com.budgetadviser.android.budgetadvisor;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class SetProjectAdapter extends RecyclerView.Adapter<SetProjectAdapter.ViewHolder> {

    OnItemClickListener mItemClickListener;
    private List<String> mProject;
    public SetProjectAdapter(List<String> projects) {
        mProject = projects;
    }
    @Override
    public SetProjectAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View projectView = inflater.inflate(R.layout.setproject, parent, false);

        // Return a new holder instance
        SetProjectAdapter.ViewHolder viewHolder = new SetProjectAdapter.ViewHolder(projectView);
        return viewHolder;
    }
    public interface OnItemClickListener {
        public void onItemClick(View view, int position, String id);
    }

    public void SetOnItemClickListener(final SetProjectAdapter.OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }
    @Override
    public void onBindViewHolder(SetProjectAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        String currProject = mProject.get(position);
        //Log.d("currProject",currProject);
        Log.d("SetProjectAdaper:",currProject);

        // Set item views based on your views and data model

        TextView textView = viewHolder.project_nameTextView;
        textView.setText(currProject);
    }
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        TextView project_nameTextView;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            project_nameTextView = (TextView) itemView.findViewById(R.id.project_name);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {


            mItemClickListener.onItemClick(v, getAdapterPosition(), "id"); //OnItemClickListener mItemClickListener;
        }
    }

    @Override
    public int getItemCount() {
        return mProject.size();
    }
}
