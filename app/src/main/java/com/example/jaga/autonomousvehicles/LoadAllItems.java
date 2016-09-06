package com.example.jaga.autonomousvehicles;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jaja on 28-12-2015.
 */
class LoadAllItems extends AsyncTask<String, String, String> {
    Activity activity;
    ListView Lview;
    LazyAdapter adapter;
    List<Call> values;
    private ProgressDialog pDialog;

    JSONParser jParser = new JSONParser();
    JSONArray products = null;
    String Type;

    public LoadAllItems(Activity _activity, ListView lView, String type)
    {
        Lview = lView;
        activity=_activity;
        Type = type;
        values = new ArrayList<Call>();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pDialog = new ProgressDialog(activity);
        pDialog.setMessage("Loading "+Type+". Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    protected String doInBackground(String... args) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        if(ServerUtility.Iscarlogin) {
            params.add(new BasicNameValuePair("id", ServerUtility.Car_id));
            params.add(new BasicNameValuePair("cat", "did"));
        }
        else {
            params.add(new BasicNameValuePair("id", ServerUtility.uid));
            params.add(new BasicNameValuePair("cat", "uid"));
        }

        JSONObject json = jParser.makeHttpRequest(ServerUtility.url_getcalls(), "GET", params);
        Log.d("All Products: ", json.toString());

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
        pDialog.dismiss();
        activity.runOnUiThread(new Runnable() {
            public void run() {adapter = new LazyAdapter(activity, values);
                Lview.setAdapter(adapter);
            }
        });
    }
}
