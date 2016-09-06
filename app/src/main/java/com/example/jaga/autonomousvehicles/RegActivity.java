package com.example.jaga.autonomousvehicles;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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
import android.widget.Toast;

public class RegActivity extends Activity {
	
	String url_login = "http://"+ServerUtility.Server_URL+"/cereBro/vehicle/reg.php";
	private static final String TAG_SUCCESS = "success";
	private ProgressDialog pDialog;
	JSONParser jParser = new JSONParser();
	
	EditText Etname, Etemail, Etpass, Etconfirm,Etmno;
	Button Btnregister;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reg);
		
		Btnregister = (Button)findViewById(R.id.btnregister);
		
		Etname =(EditText)findViewById(R.id.etname);
		Etemail=(EditText)findViewById(R.id.etemail);
		Etpass=(EditText)findViewById(R.id.etpass);
		Etconfirm=(EditText)findViewById(R.id.etconfirm);
		Etmno = (EditText)findViewById(R.id.etmno);
		
		Btnregister.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(Etname.getText().toString().equals("") || Etemail.getText().toString().equals("") || Etpass.getText().toString().equals(""))
				{
					Toast.makeText(getApplicationContext(), "Fields should not be empty", Toast.LENGTH_SHORT).show();
					return;
				}
				if(!Etpass.getText().toString().equals(Etconfirm.getText().toString()))
				{
					Toast.makeText(getApplicationContext(), "password and confirm password is should be same", Toast.LENGTH_SHORT).show();
					return;
				}				
				new RegisterUser().execute();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.reg, menu);
		return true;
	}
	
	class RegisterUser extends AsyncTask<String, String, String> {
		Boolean isregistered = false;
		String val="";
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(RegActivity.this);
			pDialog.setMessage("Registering user details. Please wait...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
			params.add(new BasicNameValuePair("email", Etemail.getText().toString()));
			params.add(new BasicNameValuePair("pass", Etpass.getText().toString()));
			params.add(new BasicNameValuePair("uname", Etname.getText().toString()));
			params.add(new BasicNameValuePair("mno", Etmno.getText().toString()));
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
