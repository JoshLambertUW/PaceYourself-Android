package com.example.paceyourself;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class StartMenu extends android.support.v4.app.Fragment implements View.OnClickListener {

    public static final String TAG = "StartMenu";

    @Override
    public View onCreateView(  LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.start_menu, container, false);
        FloatingActionButton but = (FloatingActionButton) view.findViewById(R.id.runButton);
        but.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        RunMenu RunFragment = new RunMenu();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, RunFragment);
        transaction.commit();
    }

}
