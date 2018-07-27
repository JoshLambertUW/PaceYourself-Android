package com.example.paceyourself;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Admin on 5/30/2018.
 */

public class runDataRemote extends AppCompatActivity {

    Gson gson;

    public runDataRemote(){
        super();
    }

    private DocumentReference getUserDoc(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference users = db.collection("users");
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return users.document(uid);
    }
/*
    public void maxRunHistorySize(Context context){

        int maxNum = 25;

        SharedPreferences settings = context.getSharedPreferences("prefs",
                Context.MODE_PRIVATE);
        maxNum = Integer.parseInt(settings.getString("runNumber", "25"));

        List<Run> runHistory = getRunHistory(context);
        while (runHistory.size() >= maxNum) {
            runHistory.remove(runHistory.size() - 1);
        }
        saveRunHistory(context, runHistory);
    }
*/
/*
    public void saveRunHistory(Context context, List<Run> runHistory){
        settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);

        editor = settings.edit();
        gson = new Gson();
        String jsonHistory = gson.toJson(runHistory);
        editor.putString(RUNS, jsonHistory);
        editor.commit();

        getUserDoc().collection

    }
*/
    public void addRun(Context context, Run run){
        List<Run> runHistory = getRunHistory(context);
        if (runHistory == null) runHistory = new ArrayList<Run>();
        runHistory.add(0, run);
        maxRunHistorySize(context);
        saveRunHistory(context, runHistory);
    }

    public void deleteRun(Context context, Run run){
        List<Run> runHistory = getRunHistory(context);
        if (runHistory != null) {
            runHistory.remove(run);
        }
        saveRunHistory(context,runHistory);
    }

    public ArrayList<Run> getRunHistory(Context context){
        settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        List<Run> runHistory;

        if (settings.contains(RUNS)){
            String jsonHistory = settings.getString(RUNS, null);
            gson = new Gson();
            Run[] runList = gson.fromJson(jsonHistory, Run[].class);

            runHistory = Arrays.asList(runList);
            runHistory = new ArrayList<Run>(runHistory);
        } else {
            return null;
        }

        return (ArrayList<Run>) runHistory;
    }

    public Run stringToRun(String runString){
        gson = new Gson();
        Run run = gson.fromJson(runString, Run.class);
        return run;
    }

    public Run getRun(Context context, int position){
        List<Run> runHistory = getRunHistory(context);
        return runHistory.get(position);
    }
}
