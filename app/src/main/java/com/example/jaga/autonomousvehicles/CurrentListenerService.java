package com.example.jaga.autonomousvehicles;


import android.Manifest;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GpsStatus;
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
import android.widget.Toast;

import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogRecord;

public class CurrentListenerService extends Service {
    Context _context;
    Messenger mUiHandler;
    Handler delayhandler;
    Handler handler = new Handler();
    boolean isstop = false, isstart = false;

    public CurrentListenerService(Context context) {
        _context = context;
    }

    public CurrentListenerService() {

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //getApplicationContext()

        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            mUiHandler = (Messenger) bundle.get("handler");
            mStatusChecker.run();
        }

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // TODO Auto-generated method stub
                curloction = location;
                Toast.makeText(getApplicationContext(),location.toString(),Toast.LENGTH_SHORT).show();
                new updateloc().execute();
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
    List<Call> values;
    Location curloction;

    class GetVehicles extends AsyncTask<String, String, String>
    {
        boolean reachable;
        protected void onPreExecute() {
            super.onPreExecute();
            values = new ArrayList<Call>();

        }

        protected String doInBackground(String... args) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("did", ServerUtility.Car_id));

            if(curloction != null) {
                params.add(new BasicNameValuePair("clat", Double.toString(curloction.getLatitude())));
                params.add(new BasicNameValuePair("clon", Double.toString(curloction.getLongitude())));
                params.add(new BasicNameValuePair("cat", "U"));
            }
            else{
                params.add(new BasicNameValuePair("cat", "G"));
            }

            JSONObject json = jParser.makeHttpRequest(ServerUtility.url_getcurrent(), "GET", params);
            Log.d("Current Listener: ", json.toString());

            try {
                int success = json.getInt(ServerUtility.TAG_SUCCESS);
                if (success == 1) {
                    products = json.getJSONArray("calls");
                    for (int i = 0; i < products.length(); i++) {
                        JSONObject c = products.getJSONObject(i);
                        Call itm = new Call();
                        itm.cid = c.getString("id");
                        itm.driverid = c.getString("did");
                        itm.userid = c.getString("uid");
                        itm.Date = c.getString("date");
                        itm.DropLoc = new LatLng(Double.valueOf(c.getString("droplat")),Double.valueOf(c.getString("droplon")));
                        itm.pickuploc = new LatLng(Double.valueOf(c.getString("pickuplat")),Double.valueOf(c.getString("pickuplon")));
                        values.add(itm);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
        protected void onPostExecute(String file_url) {
            if(values.size() >0)
            sendMessage(values.get(0));
        }
    }

    class updateloc extends AsyncTask<String, String, String>
    {
        protected String doInBackground(String... args) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("did", ServerUtility.Car_id));
                params.add(new BasicNameValuePair("clat", Double.toString(curloction.getLatitude())));
                params.add(new BasicNameValuePair("clon", Double.toString(curloction.getLongitude())));

            JSONObject json = jParser.makeHttpRequest(ServerUtility.url_uploadLoc(), "GET", params);
            Log.d("All Products: ", json.toString());

            return null;
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

}
