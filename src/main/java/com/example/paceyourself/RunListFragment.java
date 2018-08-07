package com.example.paceyourself;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import java.util.List;

public class RunListFragment extends Fragment implements
        OnItemClickListener, OnItemLongClickListener {

        public static final String ARG_ITEM_ID = "run_list";

        Activity activity;
        ListView runListView;
        List<Run> runHistory;
        RunListAdapter runlistAdapter;

        runData rundata;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            activity = getActivity();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_run_list, container,
                    false);

            rundata = new runData();
            runHistory = rundata.getRunHistory(activity);
            findViewsById(view);

            runListView.setOnItemClickListener(this);
            runListView.setOnItemLongClickListener(this);

            if (runHistory != null && runHistory.size() > 0) {
                runListView = (ListView) view.findViewById(R.id.run_list);
                runlistAdapter = new RunListAdapter(activity, runHistory);
                runListView.setAdapter(runlistAdapter);

                runListView.setOnItemClickListener(this);
                runListView.setOnItemLongClickListener(this);
            }
            return view;
        }

    private void findViewsById(View view) {
       runListView = (ListView) view.findViewById(R.id.run_list);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), MapsActivity.class);
        String runString = rundata.getRunString(activity, position);
        intent.putExtra("run", runString);
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View view, int position, long arg3) {
            //ToDo: Implement deleteRun
        //rundata.deleteRun(activity, runHistory.get(position));
        runlistAdapter.remove(runHistory.get(position));
        return true;
    }


    @Override
    public void onResume() {
        getActivity().setTitle(R.string.app_name);
        runlistAdapter.clear();
        runlistAdapter.addAll(rundata.getRunHistory(activity));
        runlistAdapter.notifyDataSetChanged();
        //getActivity().getActionBar().setTitle(R.string.app_name);
        super.onResume();
    }

    SharedPreferences.OnSharedPreferenceChangeListener spChanged = new
            SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                                      String key) {
                    runlistAdapter.clear();
                    runlistAdapter.addAll(rundata.getRunHistory(activity));
                    runlistAdapter.notifyDataSetChanged();
                }
            };
}