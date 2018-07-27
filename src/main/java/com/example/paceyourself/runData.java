package com.example.paceyourself;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Admin on 5/30/2018.
 */
public class runData extends AppCompatActivity {

    private String runHistory;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    Gson gson;

    public static final String PREFS_NAME = "SAVED_PREFS";
    public static final String RUNS = "RUN_HISTORY";

    private DocumentReference userDocRef;

    public runData(){
        String UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersCollectionRef = db.collection("users");
        DocumentReference userDocRef = usersCollectionRef.document(UID);
    }

    public int getMaxRunHistorySize(Context context){
        int maxNum = 25;
        SharedPreferences settings = context.getSharedPreferences("prefs",
                Context.MODE_PRIVATE);
        return Integer.parseInt(settings.getString("runNumber", "25"));
    }

    // Unnecessary with Firebase
/*
    public void saveRunHistory(Context context, List<Run> runHistory){

        settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);

        editor = settings.edit();
        gson = new Gson();
        String jsonHistory = gson.toJson(runHistory);
        editor.putString(RUNS, jsonHistory);
        editor.commit();

    }
*/
    public void addRun(Context context, Run run){

        /*
        private Timestamp runTimestamp;
    private long totalTime;
    private float totalDistance;
    private String mapPreview = "";
    private Map coordList;
         */

        Map<String, Object> data = new HashMap<>();
        data.put("timestamp", run.getDate());
        data.put("totalTime", run.getTotalTime());
        data.put("totalDistance", run.getTotalDistance());
        data.put("mapPreview", run.getMapPreview());
        data.put("coordList", run.getCoordList());

        userDocRef.collection("runHistory").add(data);
    }

    public void deleteRun(Context context, String docID){
        userDocRef.collection("runHistory").document(docID).delete();
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
