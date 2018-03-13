package com.bridgefy.samples.alerts;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bridgefy.sdk.samples.alerts.R;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 * <p/>
 */
public class AlertFragment extends Fragment {


    private static final String ARG_ALERTS = "alerts";
    private ArrayList<Alert> alerts = new ArrayList<>();
    private AlertAdapter alertsAdapter;
    RecyclerView recyclerView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AlertFragment() {
    }

    @SuppressWarnings("unused")
    public static AlertFragment newInstance(ArrayList<Alert> alertData) {
        AlertFragment fragment = new AlertFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_ALERTS, alertData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            ArrayList<Alert> alertCount = getArguments().getParcelableArrayList(ARG_ALERTS);
            alerts.addAll(alertCount);
        }

        alertsAdapter = new AlertAdapter(alerts);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            recyclerView = (RecyclerView) view;
            recyclerView.setAdapter(alertsAdapter);
        }

        return view;
    }

    public void updateList(ArrayList<Alert> alertsData) {
        alertsAdapter.updateData(alertsData);
        recyclerView.scrollToPosition(alertsData.size() - 1);
    }
}
