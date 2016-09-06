package com.example.jaga.autonomousvehicles;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CregActivity extends AppCompatActivity {


    String url_login = "http://"+ServerUtility.Server_URL+"/cereBro/vehicle/creg.php";
    private static final String TAG_SUCCESS = "success";
    private ProgressDialog pDialog;
    JSONParser jParser = new JSONParser();

    EditText Etname, Etemail, Etpass, Etconfirm, etcname, etmno;
    Button Btnregister;
    Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creg);

        Btnregister = (Button)findViewById(R.id.btnregister);

        Etname =(EditText)findViewById(R.id.etname);
        Etemail=(EditText)findViewById(R.id.etemail);
        Etpass=(EditText)findViewById(R.id.etpass);
        Etconfirm=(EditText)findViewById(R.id.etconfirm);
        etmno=(EditText)findViewById(R.id.etmno);
        etcname=(EditText)findViewById(R.id.etcname);
        spinner = (Spinner)findViewById(R.id.spinner);

        List<String> categories = new ArrayList<String>();
        categories.add("CAR");
        categories.add("SUV");
        categories.add("VAN");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

        Btnregister.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (Etname.getText().toString().equals("") || Etemail.getText().toString().equals("") || Etpass.getText().toString().equals("") || etcname.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Fields should not be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!Etpass.getText().toString().equals(Etconfirm.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "password and confirm password is should be same", Toast.LENGTH_SHORT).show();
                    return;
                }
                new RegisterUser().execute();
            }
        });
    }

    class RegisterUser extends AsyncTask<String, String, String> {
        Boolean isregistered = false;
        String val="";
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(CregActivity.this);
            pDialog.setMessage("Registering user details. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
            params.add(new BasicNameValuePair("email", Etemail.getText().toString()));
            params.add(new BasicNameValuePair("pass", Etpass.getText().toString()));
            params.add(new BasicNameValuePair("dname", Etname.getText().toString()));
            params.add(new BasicNameValuePair("mno", etmno.getText().toString()));
            params.add(new BasicNameValuePair("cname", etcname.getText().toString()));
            params.add(new BasicNameValuePair("cat", spinner.getSelectedItem().toString()));
        }

        protected String doInBackground(String... args) {
            JSONObject json = jParser.makeHttpRequest(url_login, "GET", params);
            Log.d("All Products: ", json.toString());
            isregistered = json.has(TAG_SUCCESS);
            return null;
        }

        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {
                    if(isregistered)
                    {
                        Toast.makeText(getApplicationContext(), "Registered successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "Registered failed", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }
    }
}
