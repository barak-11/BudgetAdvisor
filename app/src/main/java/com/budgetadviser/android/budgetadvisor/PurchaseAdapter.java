package com.budgetadviser.android.budgetadvisor;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.exblr.dropdownmenu.DropdownMenu;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class PurchaseAdapter extends
        RecyclerView.Adapter<PurchaseAdapter.ViewHolder> {

    // ... view holder defined above...

    // Store a member variable for the contacts
    public List<Purchase> mPurchases;
    public Context context;
    OnItemClickListener mItemClickListener;
    OnItemClickListener mItemLongClickListener;
    private EditText input_price;
    private DropdownMenu mDropdownMenu;
    SharedPreferences myDBfile; // create a file or return a reference to an exist file
    SharedPreferences.Editor myEditor;
    String currency;
    // Pass in the contact array into the constructor
    public PurchaseAdapter(List<Purchase> prchases) {
        mPurchases = prchases;
        //Log.d("mPurchases.size(): ",String.valueOf(mPurchases.size()));
    }


    public interface OnItemClickListener {
        public void onItemClick(View view, int position, String id);
        public void onLongItemClick(View view, int position, String id);
    }

    public void SetOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }
    public void SetOnLongItemClickListener(final OnItemClickListener mItemLongClickListener) {
        this.mItemLongClickListener = mItemLongClickListener;
    }
    @Override
    public PurchaseAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)  {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        myDBfile = context.getSharedPreferences("budgets", MODE_PRIVATE);
        currency = myDBfile.getString("currency", "$");
        // Inflate the custom layout
        View purchaseView = inflater.inflate(R.layout.item_post, parent, false);

        // Return a new holder instance
        PurchaseAdapter.ViewHolder viewHolder = new PurchaseAdapter.ViewHolder(purchaseView);
        return viewHolder;
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener,View.OnCreateContextMenuListener{
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        TextView nameTextView;
        TextView priceTextView;
        TextView currencyTextView;
        TextView addressTextView;
        TextView dateTextView;
        ImageView imgView;


        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            itemView.setOnCreateContextMenuListener(this); //REGISTER ONCREATE MENU LISTENER
            nameTextView = (TextView) itemView.findViewById(R.id.purchase_name);
            priceTextView = (TextView) itemView.findViewById(R.id.purchase_price);
            addressTextView = (TextView) itemView.findViewById(R.id.purchase_address);
            dateTextView = (TextView) itemView.findViewById(R.id.purchase_date);
            currencyTextView = itemView.findViewById(R.id.purchase_currency);
            imgView = itemView.findViewById(R.id.item_image);
            itemView.setOnLongClickListener(this);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {


            mItemClickListener.onItemClick(v, getAdapterPosition(), "id"); //OnItemClickListener mItemClickListener;
        }

        @Override
        public boolean onLongClick(View v) {
            mItemLongClickListener.onLongItemClick(v, getAdapterPosition(), "id");
            return false;
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            contextMenu.setHeaderTitle("Select an Action");
            contextMenu.add(0, view.getId(), 0, "Show in Street View");//groupId, itemId, order, title
            contextMenu.add(0, view.getId(), 0, "Show in Map");
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
            case "Clothes":
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
        String mytime=purchase.getDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");
        Date myDate = null;
        try {
            myDate = dateFormat.parse(mytime);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat timeFormat = new SimpleDateFormat("dd/MMM/yyyy - HH:mm");
        String finalDate = timeFormat.format(myDate);
        dateViewAddress.setText(finalDate);

        TextView textViewCurrency = viewHolder.currencyTextView;
        textViewCurrency.setText(currency);
        final ViewHolder finalViewHolder=viewHolder;
        finalViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setPosition(finalViewHolder.getAdapterPosition());
                return false;
            }
        });
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mPurchases.size();
    }

    private int position;
    public int getPosition() {
        return position;
    }
    public void setPosition(int position) {
        this.position = position;
    }


}
