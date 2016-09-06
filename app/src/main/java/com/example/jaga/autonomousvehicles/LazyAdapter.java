package com.example.jaga.autonomousvehicles;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class LazyAdapter extends BaseAdapter {
    
    private Activity activity;
    private List<Call> data;
    private static LayoutInflater inflater=null;

    
    public LazyAdapter(Activity a, List<Call> d) {
        activity = a;
        data=d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }
    
    public View getView(final int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.rowview, null);

        TextView tvdate = (TextView)vi.findViewById(R.id.tvtripdate); // title
        TextView tvcar = (TextView)vi.findViewById(R.id.tvcarid); // title
        TextView tvpick = (TextView)vi.findViewById(R.id.tvpickuploc); // title
        TextView tvdrop = (TextView)vi.findViewById(R.id.tvdrooploc); // title

        // Setting all values in listview
        tvdate.setText(data.get(position).Date);
        tvcar.setText(data.get(position).driverid);
        tvpick.setText(getLocationFromAddress(activity, data.get(position).pickuploc.latitude, data.get(position).pickuploc.longitude));
        tvdrop.setText(getLocationFromAddress(activity, data.get(position).DropLoc.latitude, data.get(position).DropLoc.longitude));
        return vi;
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
            return address.get(0).getSubLocality();
        else return "Location Not Found";
    }
}