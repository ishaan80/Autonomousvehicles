package com.example.jaga.autonomousvehicles;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AuthActivity extends Activity {
	
	EditText ETname, ETpass;
	TextView TVreg;
	Button Btnlgn;
	
	String url_clogin = "http://"+ServerUtility.Server_URL+"/cereBro/vehicle/clogin.php";
	String url_rlogin = "http://"+ServerUtility.Server_URL+"/cereBro/vehicle/login.php";
	private static final String TAG_SUCCESS = "success";
	
	private ProgressDialog pDialog;
	JSONParser jParser = new JSONParser();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_auth);
		
		ETname = (EditText)findViewById(R.id.etlgnemail);
		ETpass=(EditText)findViewById(R.id.etlgnpass);		
		Btnlgn=(Button)findViewById(R.id.btnlogin);
		TVreg=(TextView)findViewById(R.id.tvreg);
		
		Btnlgn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (!ETname.getText().toString().equals("") && !ETpass.getText().toString().equals("")) {
					new CheckUser().execute();
				} else {
					Toast.makeText(getApplicationContext(), "Fields should not be empty", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		TVreg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (ServerUtility.Iscarlogin) {
					Intent intent = new Intent(AuthActivity.this, CregActivity.class);
					startActivity(intent);
				} else {
					Intent intent = new Intent(AuthActivity.this, RegActivity.class);
					startActivity(intent);
				}
			}
		});
	}
	
	class CheckUser extends AsyncTask<String, String, String> {
		Boolean isloggedin = false;
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(AuthActivity.this);
			pDialog.setMessage("checking user details. Please wait...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
			params.add(new BasicNameValuePair("email", ETname.getText().toString()));
			params.add(new BasicNameValuePair("pass", ETpass.getText().toString()));
		}

		protected String doInBackground(String... args) {
			JSONObject json;
			if(ServerUtility.Iscarlogin)
				json = jParser.makeHttpRequest(url_clogin, "GET", params);
			else
				json = jParser.makeHttpRequest(url_rlogin, "GET", params);
			Log.d("All Products: ", json.toString());
			isloggedin = json.has(TAG_SUCCESS);
			return null;
		}

		protected void onPostExecute(String file_url) {
			pDialog.dismiss();
			runOnUiThread(new Runnable() {
				public void run() {
					if (isloggedin) {
						Toast.makeText(getApplicationContext(), "Logged in successfully", Toast.LENGTH_SHORT).show();
						if (ServerUtility.Iscarlogin) {
							ServerUtility.Car_id = ETname.getText().toString();
							Intent intent = new Intent(getApplicationContext(), DriverActivity.class);
							startActivity(intent);
						} else {
							ServerUtility.uid = ETname.getText().toString();
							Intent intent = new Intent(getApplicationContext(), MainActivity.class);
							startActivity(intent);
						}
						finish();
					} else {
						Toast.makeText(getApplicationContext(), "Invalid username and password", Toast.LENGTH_SHORT).show();
					}

				}
			});
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

}
