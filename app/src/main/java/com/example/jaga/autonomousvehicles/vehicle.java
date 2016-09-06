package com.example.jaga.autonomousvehicles;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by jaja on 08-01-2016.
 */
public class vehicle {
    public String vid;
    public String vname;
    public String category;
    public double lat;
    public double lon;
    public String status;
    public Marker marker;
    public vehicle()
    {

    }

}

class Call{
    public String cid;
    public String userid;
    public String driverid;
    public LatLng pickuploc;
    public LatLng DropLoc;
    public String Date;
    public Call()
    {

    }

    public void setpickuploc(String val)
    {

        pickuploc = new LatLng(Double.valueOf(val.split("(")[1].split(")")[0].split(",")[0]),Double.valueOf(val.split("(")[1].split(")")[0].split(",")[1]));
    }
    public void setdroploc(String val)
    {
        DropLoc = new LatLng(Double.valueOf(val.split(",")[0]),Double.valueOf(val.split(",")[1]));
    }
}
