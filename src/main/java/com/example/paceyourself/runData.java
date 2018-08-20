package com.example.paceyourself;

import com.google.gson.Gson;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Add DynamoDBMapper and AmazonDynamoDBClient to support data access methods

/**
 * Created by Admin on 5/30/2018.
 */
public class runData extends AppCompatActivity {

    Gson gson;

    DynamoDBMapper dynamoDBMapper;
    AmazonDynamoDBClient dynamoDBClient;
    final IdentityManager identityManager;

    double lastRun;

    public static final String PREFS_NAME = "SAVED_PREFS";
    public static final String RUNS = "RUN_HISTORY";

    public runData(){
        super();
        identityManager = AWSProvider.getInstance().getIdentityManager();

        AWSCredentialsProvider credentialsProvider = AWSMobileClient.getInstance().getCredentialsProvider();
        AWSConfiguration configuration = AWSMobileClient.getInstance().getConfiguration();

        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(credentialsProvider);

        this.dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(configuration)
                .build();

    }

    public void getMaxRunHistorySize(Context context) {
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

    public void saveRunHistory(Context context, List<Run> runHistory){

        gson = new Gson();
        String jsonHistory = gson.toJson(runHistory);

        final RunHistoryDO runHistoryDO = new RunHistoryDO();

        runHistoryDO.setUserId(identityManager.getCachedUserID());

        runHistoryDO.setLastRun(lastRun);
        runHistoryDO.setRunsString(jsonHistory);

        new Thread(new Runnable() {
            @Override
            public void run() {
                dynamoDBMapper.save(runHistoryDO);
                // Item saved
            }
        }).start();
    }

    public void addRun(Context context, Run run){

        List<Run> runHistory = getRunHistory(context);
        if (runHistory == null) runHistory = new ArrayList<Run>();
        lastRun = run.getRunTimestamp();
        runHistory.add(0, run);
        getMaxRunHistorySize(context);
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

        RunHistoryDO runHistoryDO = dynamoDBMapper.load(
                RunHistoryDO.class,
                identityManager.getCachedUserID(),
                "runsString");

        String history = runHistoryDO.getRunsString();
        gson = new Gson();
        Run[] runList = gson.fromJson(history, Run[].class);
        List<Run> runHistory = Arrays.asList(runList);
        runHistory = new ArrayList<Run>(runHistory);

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
