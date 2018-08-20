package com.example.paceyourself;

import com.google.android.gms.maps.model.LatLng;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Admin on 6/4/2018.
 */
public class Run {

    private long runTimestamp;
    private long totalTime;
    private float totalDistance;
    private String mapPreview = "";
    private Map coordList;

    private String STATIC_MAP_API_ENDPOINT = "http://maps.googleapis.com/maps/api/staticmap?size=230x200&path=";
    private String STATE_MAP_API_FINISH = "&sensor=false";
    public static final String PREFS_NAME = "SAVED_PREFS";

    public Run(){
        super();
    }

    public Run(long runTimestamp){
        super();
        this.runTimestamp = runTimestamp;
        Map<String, Map> coordList;
    }

    public Run(long runTimestamp, long totalTime, float totalDistance, String mapPreview, Map coordList){
        this.runTimestamp = runTimestamp;
        this.totalTime = totalTime;
        this.totalDistance = totalDistance;
        this.mapPreview = mapPreview;
        this.coordList = coordList;
    }

    public long getDate(){
        return runTimestamp;
    }

    public void setTotalTime(long totalTime){this.totalTime = totalTime; }

    public long getTotalTime() {
        return totalTime;
    }

    public String getDateString(){
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss");
        return sdf.format(runTimestamp);
    }

    public String getTotalTimeText(){

        long hours = TimeUnit.MILLISECONDS.toHours(totalTime) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(totalTime) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(totalTime) % 60;
        long milliseconds = totalTime % 1000;

        return String.format("%d:%02d:%02d:%02d",
                hours, minutes, seconds, milliseconds);
    }

    public String getTotalDistanceText(Context context) {
        SharedPreferences settings = context.getSharedPreferences("prefs",
                Context.MODE_PRIVATE);

        int unit = Integer.parseInt(settings.getString("unit_list", "0"));

        if (unit == 1) return Float.toString(totalDistance * (float)0.001) + " km";
        else return Float.toString(totalDistance * (float)0.000621371) + " miles";
    }

    public Bitmap getMapPreviewBmp(){
        if(!mapPreview.equalsIgnoreCase("") ){
            byte[] decodedByte = Base64.decode(mapPreview, 0);
            return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
        }
        return null;
    }

    public void addCoord(LatLng latlng){
        long mLastUpdateTime = System.currentTimeMillis();
        coordList.put("timestamp", mLastUpdateTime);
        HashMap<String, Double> mCoordinate = new HashMap<>();

        mCoordinate.put("latitude", latlng.latitude);
        mCoordinate.put("longitude", latlng.longitude);

        coordList.put("location", mCoordinate);
    }

    public float getTotalDistance(){
        return totalDistance;
    }

    public void setTotalDistance(float totalDistance) {
        this.totalDistance = totalDistance;
    }

    public void setMapPreview(MapsActivity m){
        final MapsActivity activity = m;
        String path = "color:blue|weight:3";

        Iterator it = coordList.entrySet().iterator();

        Map.Entry pair = (Map.Entry) it.next();
        Map data = (Map) pair.getValue();
        Map mCoordinate = (HashMap)data.get("location");
        double latitude = (double) (mCoordinate.get("latitude"));
        double longitude = (double) (mCoordinate.get("longitude"));
        it.remove();

        String startMarker = "color:green|" + String.valueOf(latitude) + "," + String.valueOf(longitude);

        while (it.hasNext()){
            pair = (Map.Entry) it.next();
            data = (Map) pair.getValue();
            mCoordinate = (HashMap)data.get("location");
            latitude = (double) (mCoordinate.get("latitude"));
            longitude = (double) (mCoordinate.get("longitude"));
            path += "|" + String.valueOf(latitude) + "," + String.valueOf(longitude);
            it.remove();
        }

        String finishMarker = "color:red|" + String.valueOf(latitude) + "," + String.valueOf(longitude);

        try {
            startMarker = URLEncoder.encode(startMarker, "UTF-8");
            finishMarker =  URLEncoder.encode(finishMarker, "UTF-8");
            path = URLEncoder.encode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        STATIC_MAP_API_ENDPOINT = STATIC_MAP_API_ENDPOINT + path + "&markers=" + startMarker + "&markers=" + finishMarker + STATE_MAP_API_FINISH;

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

    public String getMapPreview(){
        return mapPreview;
    }

    public Map getCoordList() {
        return coordList;
    }

    public void setCoordList(Map coordList) {
        this.coordList = coordList;
    }

    public long getRunTimestamp() {
        return runTimestamp;
    }

    public void setRunTimestamp(long runTimestamp) {
        this.runTimestamp = runTimestamp;
    }

    public void setMapPreview(String mapPreview) {
        this.mapPreview = mapPreview;
    }
}
