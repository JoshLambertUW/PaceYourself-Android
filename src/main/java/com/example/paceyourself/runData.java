package com.example.paceyourself;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import java.util.List;

/**
 * Created by Admin on 5/30/2018.
 */
public class runData extends AppCompatActivity {

    List<Run> runHistory;
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
        Map<String, Object> data = new HashMap<>();
        data.put("timestamp", run.getDate());
        data.put("totalTime", run.getTotalTime());
        data.put("totalDistance", run.getTotalDistance());
        data.put("mapPreview", run.getMapPreview());
        data.put("coordList", run.getCoordList());

*/
        userDocRef.collection("runHistory").add(run);
    }

    public void deleteRun(Context context, String docID){
        userDocRef.collection("runHistory").document(docID).delete();
    }

    public List<Run> getRunHistory(Context context){
        int maxRuns =  getMaxRunHistorySize(context);

        Query runHistoryQuery = userDocRef.collection
                ("runHistory").orderBy("timestamp").limit(maxRuns);

        runHistoryQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e)
            {
                for (DocumentSnapshot doc : queryDocumentSnapshots)
                {
                    runHistory.add(doc.toObject(Run.class));
                }
            }
        });

        return runHistory;
    }
}
