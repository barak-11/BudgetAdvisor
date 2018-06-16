package com.budgetadviser.android.budgetadvisor.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.budgetadviser.android.budgetadvisor.R;
import com.exblr.dropdownmenu.DropdownListItem;

import java.util.List;

/**
 * Created by lwang on 2017/2/28.
 */

public class DropdownViewAdapter extends BaseAdapter {

    private Context mContext;
    private List<DropdownListItem> mList;

    public DropdownViewAdapter(Context context, List<DropdownListItem> list) {
        mContext=context;
        mList = list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public DropdownListItem getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView != null) {
            viewHolder = (ViewHolder) convertView.getTag();
        } else {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.ddm_custom_content_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        viewHolder.bind(position);
        return convertView;
    }

    public DropdownListItem setSelectedItem(int position) {
        for (int i = 0; i < mList.size(); i++) {
            mList.get(i).setSelected(position == i);
        }
        notifyDataSetChanged();
        return mList.get(position);
    }

    private class ViewHolder {
        private TextView mTextView;

        public ViewHolder(View view) {
            mTextView = (TextView) view.findViewById(R.id.ddm_custom_content_item_tv);
        }

        public void bind(int position) {
            DropdownListItem item = mList.get(position);
            mTextView.setText(item.getText());
            if (item.isSelected()) {
                mTextView.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
               // mTextView.setBackgroundResource(R.drawable.common_google_signin_btn_icon_light_focused);
                GradientDrawable gd = new GradientDrawable();
                gd.setColor(mContext.getResources().getColor(R.color.colorWhite)); // Changes this drawbale to use a single color instead of a gradient
                gd.setCornerRadius(5);
                gd.setStroke(1, 0xFF000000);
                //TextView tv = (TextView)findViewById(R.id.textView1);
                mTextView.setBackground(gd);
            } else {
                mTextView.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
                mTextView.setBackgroundResource(R.drawable.common_full_open_on_phone);
            }
        }
    }
}
