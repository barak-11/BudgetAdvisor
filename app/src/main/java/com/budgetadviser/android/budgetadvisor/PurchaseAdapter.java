package com.budgetadviser.android.budgetadvisor;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.exblr.dropdownmenu.DropdownMenu;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class PurchaseAdapter extends
        RecyclerView.Adapter<PurchaseAdapter.ViewHolder> {

    // ... view holder defined above...

    // Store a member variable for the contacts
    public List<Purchase> mPurchases;
    public Context context;
    OnItemClickListener mItemClickListener;
    private EditText input_price;
    private DropdownMenu mDropdownMenu;
    // Pass in the contact array into the constructor
    public PurchaseAdapter(List<Purchase> prchases) {
        mPurchases = prchases;
        //Log.d("mPurchases.size(): ",String.valueOf(mPurchases.size()));
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

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        TextView nameTextView;
        TextView priceTextView;
        TextView addressTextView;
        TextView dateTextView;
        ImageView imgView;


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
            imgView = itemView.findViewById(R.id.item_image);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {


            mItemClickListener.onItemClick(v, getAdapterPosition(), "id"); //OnItemClickListener mItemClickListener;
        }

    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(PurchaseAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        final Purchase purchase = mPurchases.get(position);
        //Log.d("PurchaseAdaper:",purchase.toString());
        // Set item views based on your views and data model

        ImageView img = viewHolder.imgView;
        String dropDownItem=purchase.getName();
        switch(dropDownItem) {
            case "Beer":
                img.setImageResource(R.drawable.beer);
                break;
            case "Food":
                img.setImageResource(R.drawable.food);
                break;
            case "Tickets":
                img.setImageResource(R.drawable.tickets);
                break;
            case "Breakfast":
                img.setImageResource(R.drawable.breakfast);
                break;
            case "Lunch":
                img.setImageResource(R.drawable.lunch);
                break;
            case "Snacks":
                img.setImageResource(R.drawable.snack);
                break;
            case "Transportation":
                img.setImageResource(R.drawable.transportation);
                break;
            case "Groceries":
                img.setImageResource(R.drawable.groceries);
                break;
            case "Cloths":
                img.setImageResource(R.drawable.clothes);
                break;
            case "Gifts":
                img.setImageResource(R.drawable.gift);
                break;
            case "Baby Stuff":
                img.setImageResource(R.drawable.baby);
                break;
            case "Other":
                img.setImageResource(R.drawable.other);
                break;
            default:

        }


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
