package com.example.jaga.autonomousvehicles;

import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.MapFragment;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FragmentManager fragmentManager = getFragmentManager();
        fragment = new com.example.jaga.autonomousvehicles.MapFragment();

        if (fragment != null) {
            fragmentManager.beginTransaction().replace(R.id.frame_container, fragment,fragment.getClass().getName()).addToBackStack(null).commit();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        if(getFragmentManager().getBackStackEntryCount() == 0)
        {
            finish();
        }
        else
        {
            FragmentManager manager = getFragmentManager();
            if (manager.getBackStackEntryCount() > 0) {
                manager.popBackStackImmediate();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            FragmentManager fragmentManager = getFragmentManager();
            fragment = new com.example.jaga.autonomousvehicles.MapFragment();

            if (fragment != null) {
                fragmentManager.beginTransaction().replace(R.id.frame_container, fragment,fragment.getClass().getName()).addToBackStack(null).commit();
            }

        } else if (id == R.id.nav_Payment) {

            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.alertdialoglayout);
            dialog.setTitle("SOS");//atv_places
            Button Btnbroken = (Button)dialog.findViewById(R.id.btnalertbroken);
            Btnbroken.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new UpdateMessage().execute();
                    dialog.dismiss();
                }
            });
            Button Btnaccident = (Button)findViewById(R.id.btnalertaccident);
            Button BtnTheft = (Button)findViewById(R.id.btntheft);
            dialog.show();



        } else if (id == R.id.nav_History) {

            FragmentManager fragmentManager = getFragmentManager();
            fragment = new com.example.jaga.autonomousvehicles.TripFragment();

            if (fragment != null) {
                fragmentManager.beginTransaction().replace(R.id.frame_container, fragment,fragment.getClass().getName()).addToBackStack(null).commit();
            }

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    class UpdateMessage extends AsyncTask<String, String, String>
    {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        protected void onPreExecute() {
            super.onPreExecute();
            params.add(new BasicNameValuePair("name",ServerUtility.Car_id+" Break Down"));
            params.add(new BasicNameValuePair("message","Break Down Message from Vehicle :"+ServerUtility.Car_id));
//            SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
//            String dateInString = "31-08-1982 10:20:56";
//            String date = sdf.format(new Date());
//            params.add(new BasicNameValuePair("date",date));
        }

        protected String doInBackground(String... args) {
            try {
                JSONParser jParser = new JSONParser();
                JSONObject json = jParser.makeHttpRequest(ServerUtility.url_Addmessage(), "GET", params);
                Log.d("call update: ", json.toString());

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String file_url) {
            Toast.makeText(MainActivity.this, "Broken message sended successfully", Toast.LENGTH_SHORT).show();
        }
    }

}
