package com.budgetadviser.android.budgetadvisor;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.exblr.dropdownmenu.DropdownMenu;

import java.util.ArrayList;
import java.util.List;

public class PurchaseAdapter extends
        RecyclerView.Adapter<PurchaseAdapter.ViewHolder> {

    // ... view holder defined above...

    // Store a member variable for the contacts
    private List<Purchase> mPurchases;
    public Context context;
    OnItemClickListener mItemClickListener;
    private EditText input_price;
    private DropdownMenu mDropdownMenu;
    // Pass in the contact array into the constructor
    public PurchaseAdapter(List<Purchase> prchases) {
        mPurchases = prchases;
        //Log.d("mPurchases.size(): ",String.valueOf(mPurchases.size()));
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
         TextView nameTextView;
         TextView priceTextView;
         TextView addressTextView;
        TextView dateTextView;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            nameTextView = (TextView) itemView.findViewById(R.id.purchase_name);
            priceTextView = (TextView) itemView.findViewById(R.id.purchase_price);
            addressTextView = (TextView) itemView.findViewById(R.id.purchase_address);
            dateTextView = (TextView) itemView.findViewById(R.id.purchase_date);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {


            mItemClickListener.onItemClick(v, getAdapterPosition(), "id"); //OnItemClickListener mItemClickListener;
        }
    }
    public interface OnItemClickListener {
        public void onItemClick(View view, int position, String id);
    }

    public void SetOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }
    @Override
    public PurchaseAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)  {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View purchaseView = inflater.inflate(R.layout.item_post, parent, false);

        // Return a new holder instance
        PurchaseAdapter.ViewHolder viewHolder = new PurchaseAdapter.ViewHolder(purchaseView);
        return viewHolder;
    }



    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(PurchaseAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        final Purchase purchase = mPurchases.get(position);

        // Set item views based on your views and data model

        TextView textView = viewHolder.nameTextView;
        textView.setText(purchase.getName());
        TextView textViewPrice = viewHolder.priceTextView;
        textViewPrice.setText(purchase.getPrice().toString());
        TextView textViewAddress = viewHolder.addressTextView;
        textViewAddress.setText(purchase.getAddress().toString());
        TextView dateViewAddress = viewHolder.dateTextView;
        dateViewAddress.setText(purchase.getDate());

    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mPurchases.size();
    }
}
