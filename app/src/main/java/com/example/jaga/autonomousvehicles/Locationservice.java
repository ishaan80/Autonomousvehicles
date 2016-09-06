package com.example.jaga.autonomousvehicles;

import android.Manifest;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogRecord;

public class Locationservice extends Service {
    Context _context;
    Messenger mUiHandler;
    Handler delayhandler;
    Handler handler = new Handler();
    boolean isstop =false;
    public Locationservice(Context context) {
        _context = context;
    }
    public Locationservice() {

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Bundle bundle = intent.getExtras();
        if(bundle != null)
        {
            mUiHandler =  (Messenger)bundle.get("handler");
            mStatusChecker.run();
        }

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // TODO Auto-generated method stub
                //curloction = location;
                //Toast.makeText(getApplicationContext(),location.toString(),Toast.LENGTH_SHORT).show();
                //new updateloc().execute();
                if(ServerUtility.currentcall != null) {
                    DownloadTask downloadTask = new DownloadTask();
                    downloadTask.execute(getDirectionsUrl(ServerUtility.currentcall.DropLoc,  new LatLng(location.getLatitude(), location.getLongitude())));
                }
            }

            @Override
            public void onProviderDisabled(String provider) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProviderEnabled(String provider) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // TODO Auto-generated method stub
            }
        });



        return super.onStartCommand(intent, flags, startId);
    }
    private static final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
    void someMethod() {
        Runnable task = new Runnable() {
            public void run() {
                new GetVehicles().execute();
            }
        };
        worker.schedule(task, 5, TimeUnit.SECONDS);
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            new GetVehicles().execute();
            if(!isstop)
            handler.postDelayed(mStatusChecker, 5000);
        }
    };


    JSONParser jParser = new JSONParser();
    JSONArray products = null;
    String Type;
    List<vehicle> values;

    class GetVehicles extends AsyncTask<String, String, String>
    {
        boolean reachable;
        protected void onPreExecute() {
            super.onPreExecute();
            values = new ArrayList<vehicle>();

        }

        protected String doInBackground(String... args) {
            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair(ServerUtility.TAG_CAT, ServerUtility.ctype));
                JSONObject json = jParser.makeHttpRequest(ServerUtility.url_Vehicles(), "GET", params);

                    try {
                        products = json.getJSONArray(ServerUtility.TAG_ITEMS);
                        for (int i = 0; i < products.length(); i++) {
                            JSONObject c = products.getJSONObject(i);
                            vehicle itm = new vehicle();
                            itm.vid = c.getString("id");
                            itm.vname = c.getString("name");
                            itm.category = c.getString("cat");
                            itm.lat = c.getDouble("lat");
                            itm.lon = c.getDouble("lon");
                            itm.status = c.getString("status");
                            values.add(itm);
                        }
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                reachable = !json.equals("");
                Log.d("All Products: ", json.toString());

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String file_url) {
            sendMessage(values);
        }
        }

    public void sendMessage(Object val) {

        Message message = Message.obtain();
        message.obj = val;
        try {
            mUiHandler.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        worker.shutdown();
        isstop = true;
        super.onDestroy();
    }

    @Override
    public boolean stopService(Intent name) {

        return super.stopService(name);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    private String getDirectionsUrl(LatLng origin,LatLng dest){

        String str_origin = "origin="+origin.latitude+","+origin.longitude;
        String str_dest = "destination="+dest.latitude+","+dest.longitude;
        String sensor = "sensor=false";
        String parameters = str_origin+"&"+str_dest+"&"+sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;
        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb  = new StringBuffer();
            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        }catch(Exception e){
            Log.d("err downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class DownloadTask extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try{
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
            Log.d("All Products: ", result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            String distance = "";
            String duration = "";

            if(result.size()<1){
                //Toast.makeText(getActivity(), "No Points", Toast.LENGTH_SHORT).show();
                return;
            }

            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = result.get(i);
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);
                    if(j==0){	// Get distance from the list
                        distance = (String)point.get("distance");
                        continue;
                    }else if(j==1){ // Get duration from the list
                        duration = (String)point.get("duration");
                        continue;
                    }
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(2);
                lineOptions.color(Color.RED);

            }
            //googleMap .addPolyline(lineOptions);
            try {
                if (Double.parseDouble(distance.split(" ")[0]) > 3 && distance.split(" ")[1] == "km") {
                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    @SuppressWarnings("deprecation")
                    Notification notification = new Notification(R.drawable.ic_menu_share, "New Message", System.currentTimeMillis());
                    Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
                    //notification.setLatestEventInfo(getApplicationContext(), "Destination will reach", "your destination within 3Km", pendingIntent);
                    notificationManager.notify(9999, notification);
                }
            }catch (Exception e)
            {}

        }
    }

}
