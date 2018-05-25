package com.budgetadviser.android.budgetadvisor;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.List;

public class ListViewAdapter extends BaseAdapter {

    Activity activity;
    List<Purchase> lstPurchases;
    LayoutInflater inflater;
    Context context;
    String address;


    public ListViewAdapter(Activity activity, List<Purchase> lstPurchases,Context context) {
        this.activity = activity;
        this.lstPurchases = lstPurchases;
        this.context=context;
    }

    @Override
    public int getCount() {
        return lstPurchases.size();
    }

    @Override
    public Object getItem(int i) {
        return lstPurchases.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        inflater = (LayoutInflater)activity.getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.listview_item,null);

        TextView txtUser = (TextView)itemView.findViewById(R.id.list_name);
        TextView txtEmail = (TextView)itemView.findViewById(R.id.list_email);
        TextView txtAddress = (TextView)itemView.findViewById(R.id.list_address);


        try{
            //Log.d("lstPurchases","-"+lstPurchases.get(i).getAddress());
            //Log.d("address",lstPurchases.get(i).getAddress().toString());
            //Log.d("price",String.valueOf(lstPurchases.get(i).getPrice()));
            //Log.d("name",lstPurchases.get(i).getName().toString());
        }
        catch (Exception e){
            Toast.makeText(context,"Error:"+e.getMessage(), Toast.LENGTH_LONG).show();
        }


        txtUser.setText(lstPurchases.get(i).getName());
        txtEmail.setText(String.valueOf(lstPurchases.get(i).getPrice()));
        txtAddress.setText(lstPurchases.get(i).getAddress());

        return itemView;
    }
}
