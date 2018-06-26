package com.example.paceyourself;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class RunMenu extends android.support.v4.app.Fragment {

    private TextView timerTextView, distanceTextView, resultsTextView, prevDistanceTextView;
    private FloatingActionButton startFab, pauseFab, stopFab;

    public void setTimerTextView(String text){
        timerTextView.setText(text);
    }

    public void setDistanceTextView(String text){
        distanceTextView.setText(text);
    }

    public void setPrevDistanceTextView(String text){
        prevDistanceTextView.setText(text);
    }

    public void setResultsTextView(String text){
        resultsTextView.setText(text);
    }

    @Override
    public View onCreateView(  LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.run_menu, container, false);
        startRun(0);

        timerTextView = (TextView) view.findViewById(R.id.textTime);
        distanceTextView = (TextView) view.findViewById(R.id.textDistance);
        resultsTextView = (TextView) view.findViewById(R.id.resultsTextDistance);
        prevDistanceTextView = (TextView) view.findViewById(R.id.prevTextDistance);

        startFab = (FloatingActionButton) view.findViewById(R.id.startFab);
        pauseFab = (FloatingActionButton) view.findViewById(R.id.pauseFab);
        stopFab = (FloatingActionButton) view.findViewById(R.id.stopFab);


        startFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRun(1);
                startFab.setVisibility(v.INVISIBLE);
                pauseFab.setVisibility(v.VISIBLE);
                stopFab.setVisibility(v.INVISIBLE);
            }
        });

        pauseFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRun(2);
                startFab.setVisibility(v.VISIBLE);
                pauseFab.setVisibility(v.INVISIBLE);
                stopFab.setVisibility(v.VISIBLE);
            }
        });

        stopFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRun(3);
                startFab.setVisibility(v.INVISIBLE);
                pauseFab.setVisibility(v.INVISIBLE);
                stopFab.setVisibility(v.INVISIBLE);
                timerTextView.setVisibility(v.INVISIBLE);
                distanceTextView.setVisibility(v.INVISIBLE);
                resultsTextView.setVisibility(v.VISIBLE);
            }
        });

        return view;
    }

    public interface startRun {
        void startRun(int status);
    }

    startRun dataPasser;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dataPasser = (startRun) context;
    }

    public void startRun(int status) {
        dataPasser.startRun(status);
    }

}