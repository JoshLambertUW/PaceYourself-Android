package com.example.paceyourself;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.maps.model.LatLng;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Created by Admin on 6/4/2018.
 */
public class Run {

    private Date runDate;
    private long totalTime;
    private float totalDistance;
    private String mapPreview = "";
    private List<LatLng> coordList;
    private TaskCallback mCallback;

    private String STATIC_MAP_API_ENDPOINT = "http://maps.googleapis.com/maps/api/staticmap?size=230x200&path=";
    private String STATE_MAP_API_FINISH = "&sensor=false";
    public static final String PREFS_NAME = "SAVED_PREFS";

    public Run(){
        super();
    }

    public Run(Date runDate){
        super();
        this.runDate = runDate;
        coordList = new ArrayList<LatLng>();
    }

    public Date getDate(){
        return runDate;
    }

    public String getDateString(){
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss");
        return sdf.format(runDate);
    }

    public void setDate(Date runDate){
        this.runDate = runDate;
    }

    public void setTotalTime(long totalTime){this.totalTime = totalTime; }

    public long getTotalTime() {
        return totalTime;
    }

    public String getTotalTimeText(){
        long totalTimeMillis = getTotalTime();
        if (totalTimeMillis == 0) return "";

        long hours = TimeUnit.MILLISECONDS.toHours(totalTimeMillis) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(totalTimeMillis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(totalTimeMillis) % 60;
        long milliseconds = totalTimeMillis % 1000;

        return String.format("%d:%02d:%02d:%02d",
                hours, minutes, seconds, milliseconds);
    }


    public void addCoord(LatLng latlng){
        coordList.add(latlng);
    }

    public Date getRunDate() {
        return runDate;
    }

    public void setRunDate(Date runDate) {
        this.runDate = runDate;
    }

    public float getTotalDistance(){
        return totalDistance;
    }

    public String getTotalDistanceText(Context context) {
        float distance = totalDistance;
        SharedPreferences settings = context.getSharedPreferences("prefs",
                Context.MODE_PRIVATE);

        int unit = Integer.parseInt(settings.getString("unit_list", "0"));

        if (unit == 1) return Float.toString(distance * (float)0.001) + " km";
        else return Float.toString(distance * (float)0.000621371) + " miles";
    }

    public void setTotalDistance(float totalDistance) {
        this.totalDistance = totalDistance;
    }

    public void setMapPreview(MapsActivity m){
        final MapsActivity activity = m;
        String startMarker = "color:green|" + String.valueOf(coordList.get(0).latitude) + "," + String.valueOf(coordList.get(0).longitude);
        String finishMarker = "color:red|" + String.valueOf(coordList.get(coordList.size() - 1).latitude) + "," + String.valueOf(coordList.get(coordList.size() - 1).longitude);
        String path = "color:blue|weight:3";
        for (int i = 0; i < coordList.size(); i += 30){
            path += "|" + String.valueOf(coordList.get(i).latitude) + "," + String.valueOf(coordList.get(i).longitude);
        }
        String last = "|" + String.valueOf(coordList.get(coordList.size() - 1).latitude) + "," + String.valueOf(coordList.get(coordList.size() - 1).longitude);

        try {
            startMarker = URLEncoder.encode(startMarker, "UTF-8");
            finishMarker =  URLEncoder.encode(finishMarker, "UTF-8");
            path = URLEncoder.encode(path, "UTF-8");
            last = URLEncoder.encode(last, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        STATIC_MAP_API_ENDPOINT = STATIC_MAP_API_ENDPOINT + path + "&markers=" + startMarker + "&markers=" + finishMarker + last + STATE_MAP_API_FINISH;

        AsyncTask<Void, Void, byte[]> setImageFromUrl = new AsyncTask<Void, Void, byte[]>(){
            @Override
            protected byte[] doInBackground(Void... params) {
                Bitmap bmp = null;
                HttpClient httpclient = new DefaultHttpClient();
                HttpGet request = new HttpGet(STATIC_MAP_API_ENDPOINT);

                InputStream in = null;
                try {
                    in = httpclient.execute(request).getEntity().getContent();
                    bmp = BitmapFactory.decodeStream(in);
                    in.close();
                    ByteArrayOutputStream bStream = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, bStream);
                    byte[] b = bStream.toByteArray();
                    return b;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
            protected void onPostExecute(byte[] b) {
                setMapPreviewString(Base64.encodeToString(b, Base64.DEFAULT));
                activity.done();
            }
        };
        setImageFromUrl.execute();
    }

    public void setMapPreviewString(String mapPreviewString){
            this.mapPreview = mapPreviewString;
    }

    public Bitmap getMapPreview(){
        if(!mapPreview.equalsIgnoreCase("") ){
            byte[] decodedByte = Base64.decode(mapPreview, 0);
            return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
        }
        return null;
    }

    public List<LatLng> getCoordList() {
        return coordList;
    }

    public void setCoordList(List<LatLng> coordList) {
        this.coordList = coordList;
    }
}
