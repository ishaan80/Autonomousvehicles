package com.example.jaga.autonomousvehicles;

/**
 * Created by jaja on 28-12-2015.
 */
public class ServerUtility {
    public static Boolean Iscarlogin= false;
    public static String Server_URL="192.168.1.46";
    public static Call currentcall;
    public static String ctype = "CAR";
    public static String uid;
    public static String Car_id ;
    public static String url_Vehicles()
    {
       return  "http://"+Server_URL+"/cereBro/vehicle/get_vehicle.php";
    }

    public static String url_upload()
    {
        return  "http://"+Server_URL+"/cereBro/vehicle/add_call.php";
    }

    public static String url_Addmessage()
    {
        return  "http://"+Server_URL+"/cereBro/addMessage";
    }

    public static String url_getcalls()
    {
        return  "http://"+Server_URL+"/cereBro/vehicle/get_calls.php";
    }

    public static String updatecallstatus()
    {
        return  "http://"+Server_URL+"/cereBro/vehicle/updatecallstatus.php";
    }
    public static String url_getcurrent()
    {
        return  "http://"+Server_URL+"/cereBro/vehicle/getcurrent.php";
    }

    public static String url_uploadLoc()
    {
        return  "http://"+Server_URL+"/cereBro/vehicle/updatecurrent.php";
    }



    public static final String TAG_SUCCESS = "success";
    private static final String TAG_PRODUCTS = "items";
    public static final String TAG_ITEMS = "cars";
    public static final String TAG_CAT = "cat";

}
