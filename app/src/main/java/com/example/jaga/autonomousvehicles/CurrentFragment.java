package com.example.jaga.autonomousvehicles;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.vision.barcode.Barcode;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class CurrentFragment extends Fragment {


    MapView mMapView;
    private GoogleMap googleMap;
    Handler mUiHandler = null;
    TextView tvalert;
    Button Btnconfirm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // inflate and return the layout
        View v = inflater.inflate(R.layout.fragment_current, container, false);
        mMapView = (MapView) v.findViewById(R.id.mapView);
        Btnconfirm = (Button)v.findViewById(R.id.Btnconfirm);
        tvalert = (TextView)v.findViewById(R.id.tvcuralert);
        mMapView.onCreate(savedInstanceState);

        mUiHandler = new Handler() // Receive messages from service class
        {
            public void handleMessage(Message msg)
            {
                switch(msg.what)
                {
                    case 0:
                        // add the status which came from service and show on GUI
                        cl = ServerUtility.currentcall = (Call) msg.obj;
                        googleMap.addMarker(new MarkerOptions().position(cl.pickuploc).title("Pickup").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                        //googleMap.addMarker(new MarkerOptions().position(cl.DropLoc).title("Drop").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
                        tvalert.setVisibility(View.GONE);
                        mMapView.setVisibility(View.VISIBLE);
                        Btnconfirm.setVisibility(View.VISIBLE);

                        break;
                    default:
                        break;
                }
            }
        };

        Btnconfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleMap.addMarker(new MarkerOptions().position(cl.DropLoc).title("Drop").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
                CameraPosition cameraPosition = new CameraPosition.Builder().target(cl.DropLoc).zoom(15).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                new Updatecall().execute();
            }
        });

        mMapView.onResume();// needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        googleMap = mMapView.getMap();
        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
//                Toast.makeText(CurrentFragment.this.getActivity(), location.toString(), Toast.LENGTH_SHORT).show();

            }
        });

        Intent serviceintent =new Intent(getActivity(),CurrentListenerService.class);
        serviceintent.putExtra("handler", new Messenger(mUiHandler));
        getActivity().startService(serviceintent);

        return v;
    }
    List<Call> values;
    private ProgressDialog pDialog;
Call cl = new Call();
    class Updatecall extends AsyncTask<String, String, String>
    {
        JSONParser jParser = new JSONParser();
        JSONArray products;
        boolean reachable;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("updating confirmation. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... args) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("cid", ServerUtility.currentcall.cid));
            JSONObject json = jParser.makeHttpRequest(ServerUtility.updatecallstatus(), "GET", params);
            Log.d("All Products: ", json.toString());
            return null;
        }

        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Intent intent = new Intent(getActivity(), CurrentListenerService.class);
        getActivity().stopService(intent);
    }
}