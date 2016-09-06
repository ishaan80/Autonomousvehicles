package com.example.jaga.autonomousvehicles;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class MapFragment extends Fragment {
    Button BtnPickup;
    TextView tvdurtation,tvnavi;
    AutoCompleteTextView atvPlaces;
    PlacesTask placesTask;
    ParserplaceTask parserTask;
    TextView TVdialogDuration;

    Handler mUiHandler = null;
        MapView mMapView;
    boolean isrun = false,ispickupedup = false,isstop = false;

        private GoogleMap googleMap;
        List<vehicle> values;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_map, container, false);
            values= new ArrayList<vehicle>();
            mMapView = (MapView) v.findViewById(R.id.mapView);
            BtnPickup = (Button) v.findViewById(R.id.btnpickup);
            tvdurtation= (TextView)v.findViewById(R.id.tvduration);
            tvnavi=(TextView)v.findViewById(R.id.tvnavi);

            mMapView.onCreate(savedInstanceState);
            mMapView.onResume();
            mUiHandler = new Handler() // Receive messages from service class
            {
                public void handleMessage(Message msg)
                {
                    switch(msg.what)
                    {
                        case 0:
                            // add the status which came from service and show on GUI
                            List<vehicle>  vehlist = (List<vehicle>) msg.obj;
                            if(vehlist.size()>0)
                            {
                                for(vehicle itm : vehlist) {
                                    boolean isfound = false;
                                    for(vehicle oitm : values)
                                    {
                                        if(oitm.vid.equals(itm.vid))
                                        {
                                            oitm.lat = itm.lat;
                                            oitm.lon = itm.lon;
                                            animateMarker(oitm.marker,new LatLng(itm.lat,itm.lon),false);
                                            isfound = true;
                                        }
                                    }
                                    if(!isfound) {
                                        itm.marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(itm.lat, itm.lon)).title(itm.vname));
                                        values.add(itm);
                                    }
                                }
                                if(!isrun) {
                                    task.run();
                                    isrun = true;
                                }
                            }
                            break;
                        default:
                            break;
                    }
                }
            };


            try {
                MapsInitializer.initialize(getActivity().getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
            Intent serviceintent =new Intent(getActivity(),Locationservice.class);
            serviceintent.putExtra("handler", new Messenger(mUiHandler));
            getActivity().startService(serviceintent);
            googleMap = mMapView.getMap();
            googleMap.setMyLocationEnabled(true);

            BtnPickup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    maputility.pickup_Loc = maputility.Inputlatlng;
                    //tvnavi.setText("move center point Destination");
                    ispickupedup = true;
                    // custom dialog
                    final Dialog dialog = new Dialog(getActivity());
                    dialog.setContentView(R.layout.dialoglayout);
                    dialog.setTitle("Select destination location");//atv_places

                    TVdialogDuration = (TextView)dialog.findViewById(R.id.tvduration);

                    atvPlaces = (AutoCompleteTextView) dialog.findViewById(R.id.atv_places);
                    atvPlaces.setThreshold(1);

                    atvPlaces.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String str = places.get(position).get("description");

                            maputility.Drop_Loc = getLocationFromAddress(getActivity(), str);
                            //Toast.makeText(getActivity(), maputility.Drop_Loc.toString(), Toast.LENGTH_SHORT).show();
                            ispickupedup = true;
                            DownloadTask downloadTask = new DownloadTask();
                            downloadTask.execute(getDirectionsUrl(maputility.pickup_Loc, maputility.Drop_Loc));
                        }
                    });

                    atvPlaces.addTextChangedListener(new TextWatcher() {

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            placesTask = new PlacesTask();
                            placesTask.execute(s.toString());
                        }

                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            // TODO Auto-generated method stub
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            // TODO Auto-generated method stub
                        }
                    });

                    // set the custom dialog components - text, image and button
                    Button dialogButton = (Button) dialog.findViewById(R.id.btndialog);
                    // if button is clicked, close the custom dialog
                    dialogButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ServerUtility.Car_id =values.get(0).vid;
                            new StoreCall().execute();
                            dialog.dismiss();
                        }
                    });

                    dialog.show();
                }
            });

            googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition cameraPosition) {
                    if(values != null)
                    {
                        for(vehicle vh : values)
                        {
                            vh.marker.remove();
                        }
                    }

                    values = new ArrayList<vehicle>();

                    Log.i("centerLat", String.valueOf(cameraPosition.target.latitude));
                    Log.i("centerLong", String.valueOf(cameraPosition.target.longitude));
                    maputility.Inputlatlng = cameraPosition.target;
                }
            });
            handler = new Handler();
            task = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        DownloadTask downloadTask = new DownloadTask();
                        if(!ispickupedup && values.size()>0 && maputility.Inputlatlng != null)
                            try{
                            downloadTask.execute(getDirectionsUrl(maputility.Inputlatlng, new LatLng(values.get(0).lat, values.get(0).lon)));
                            }catch (Exception e)
                            {

                            }
                        else if(ispickupedup) {
                            try {
                                downloadTask.execute(getDirectionsUrl(maputility.Drop_Loc, maputility.pickup_Loc));
                            }catch (Exception e)
                            {

                            }
                        }
                        if(!isstop)
                        handler.postDelayed(this, 1000);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
            return v;
        }

    public LatLng getLocationFromAddress(Context context,String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;
        //LatLng p1 = null;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng(location.getLatitude(), location.getLongitude() );

        } catch (Exception ex) {

            ex.printStackTrace();
        }

        return p1;
    }

    Thread task;
    Handler handler;
    public void animateMarker(final Marker marker, final LatLng toPosition,final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = googleMap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 3000;
        final Interpolator interpolator = new LinearInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);
                double lng = t * toPosition.longitude + (1 - t) * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t) * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));
                if (t < 1.0) {
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }

    public String getLocationFromAddress(Context context,double Lat, double Lng) {

        Geocoder coder = new Geocoder(context);
        List<Address> address = new ArrayList<Address>();

        try {
            address = coder.getFromLocation(Lat, Lng, 5);
            Address location = address.get(0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if(address.size() >0)
        return address.get(0).toString();
        else return "Location Not Found";
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
            Log.d("All vehicle distance: ", result);
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

            if(result == null || result.size()<1){
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

            if(ispickupedup)
            {
                try {
                    if(!distance.equals(""))
                        if(distance.split(" ")[1].equals("km"))
                            TVdialogDuration.setText("ETA:" + duration + "; fair:" + String.valueOf(Double.valueOf(distance.split(" ")[0]) * 8));
                    else
                            TVdialogDuration.setText("ETA:" + duration + "; fair:" + String.valueOf( 8));
                }catch (Exception e)
                {}
            }
            else
                tvdurtation.setText("ETA:"+duration);
        }
    }


    class StoreCall extends AsyncTask<String, String, String>
    {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        protected void onPreExecute() {
            super.onPreExecute();
            params.add(new BasicNameValuePair("uid", ServerUtility.uid));
            params.add(new BasicNameValuePair("pickuplat",String.valueOf(maputility.pickup_Loc.latitude)));
            params.add(new BasicNameValuePair("pickuplon",String.valueOf(maputility.pickup_Loc.longitude)));
            params.add(new BasicNameValuePair("droplat",String.valueOf(maputility.Drop_Loc.latitude)));
            params.add(new BasicNameValuePair("droplon",String.valueOf(maputility.Drop_Loc.longitude)));
            params.add(new BasicNameValuePair("did", values.get(0).vid));
            SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
            String dateInString = "31-08-1982 10:20:56";
            String date = sdf.format(new Date());
            params.add(new BasicNameValuePair("date",date));
        }

        protected String doInBackground(String... args) {
            try {
                JSONParser jParser = new JSONParser();
                JSONObject json = jParser.makeHttpRequest(ServerUtility.url_upload(), "GET", params);
                Log.d("call update: ", json.toString());

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String file_url) {
            //sendMessage(values);

//            Intent intent = new Intent(getActivity(), Locationservice.class);
//            getActivity().stopService(intent);
//            isstop = true;
            Toast.makeText(getActivity(),"Trip Added successfully",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), CurrentListenerService.class);
            getActivity().startService(intent);
////            FragmentManager manager = getFragmentManager();
////            manager.popBackStackImmediate();
        }
    }

    @Override
    public void onDetach() {
        Intent intent = new Intent(getActivity(), Locationservice.class);
        getActivity().stopService(intent);
        isstop = true;
        super.onDetach();
    }


    /** A method to download json data from url */
    private String downloadplaceUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
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
            //Log.d("Exception while downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches all places from GooglePlaces AutoComplete Web Service
    private class PlacesTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... place) {
            // For storing data from web service
            String data = "";

            // Obtain browser key from https://code.google.com/apis/console
            String key = "key=AIzaSyDeYhLPyis8pVzcmXV6I71thyPn1e6dNJQ";

            String input="";

            try {
                input = "input=" + URLEncoder.encode(place[0], "utf-8");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }


            // place type to be searched
            String types = "types=geocode";

            // Sensor enabled
            String sensor = "sensor=false";

            // Building the parameters to the web service
            String parameters = input+"&"+types+"&"+sensor+"&"+key;

            // Output format
            String output = "json";

            // Building the url to the web service
            String url = "https://maps.googleapis.com/maps/api/place/autocomplete/"+output+"?"+parameters;

            try{
                // Fetching the data from web service in background
                data = downloadplaceUrl(url);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // Creating ParserTask
            parserTask = new ParserplaceTask();

            // Starting Parsing the JSON string returned by Web Service
            parserTask.execute(result);
        }
    }

    List<HashMap<String, String>> places = null;
    /** A class to parse the Google Places in JSON format */
    private class ParserplaceTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{

        JSONObject jObject;

        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {



            PlaceJSONParser placeJsonParser = new PlaceJSONParser();

            try{
                jObject = new JSONObject(jsonData[0]);

                // Getting the parsed data as a List construct
                places = placeJsonParser.parse(jObject);

            }catch(Exception e){
                Log.d("Exception",e.toString());
            }
            return places;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> result) {

            String[] from = new String[] { "description"};
            int[] to = new int[] { android.R.id.text1 };

            // Creating a SimpleAdapter for the AutoCompleteTextView
            SimpleAdapter adapter = new SimpleAdapter(getActivity(), result, android.R.layout.simple_list_item_1, from, to);

            // Setting the adapter
            atvPlaces.setAdapter(adapter);
        }
    }

}