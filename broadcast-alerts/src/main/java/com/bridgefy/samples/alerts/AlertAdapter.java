package com.bridgefy.samples.alerts;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bridgefy.sdk.samples.alerts.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Alert}
 * TODO: Replace the implementation with code for your data type.
 */
public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.ViewHolder> {

    private List<Alert> mValues;

    public AlertAdapter(List<Alert> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_device, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        String id = holder.mItem.getId();

        holder.mIdView.setText(holder.mItem.getName() + " ("+id.substring(25)+")");
        holder.mContentView.setText(String.valueOf(holder.mItem.getCount()));

        long dateLong = holder.mItem.getDate();
        Date date = new Date(dateLong);
        SimpleDateFormat format=new SimpleDateFormat("hh:mm");
        holder.mDateView.setText(format.format(date));

        holder.mView.setOnClickListener(v -> {
        });
    }



    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void updateData(ArrayList<Alert> alertsData) {
        mValues=alertsData;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public final TextView mDateView;
        public Alert mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = view.findViewById(R.id.txt_user);
            mContentView = view.findViewById(R.id.txt_count);
            mDateView = view.findViewById(R.id.txt_date);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }



}
