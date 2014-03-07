package pl.sointeractive.isaaclock.activities;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import pl.sointeractive.isaaclock.R;
import pl.sointeractive.isaaclock.data.App;
import pl.sointeractive.isaaclock.data.UserData;
import pl.sointeractive.isaacloud.connection.HttpResponse;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity {

	Button buttonRegister;
	EditText textEmail, textPassword, textPasswordRepeat, textFirstName, textLastName;
	Context context;
	ProgressDialog dialog;
	int errorCode = 0;
	// 1 - email not unique

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		context = this;

		textEmail = (EditText) findViewById(R.id.text_edit_email);
		textPassword = (EditText) findViewById(R.id.text_edit_password);
		textPasswordRepeat = (EditText) findViewById(R.id.text_edit_password_repeat);
		textFirstName = (EditText) findViewById(R.id.text_edit_first_name);
		textLastName = (EditText) findViewById(R.id.text_edit_last_name);

		buttonRegister = (Button) findViewById(R.id.button_register);
		buttonRegister.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String pw = textPassword.getEditableText().toString();
				String pw2 = textPasswordRepeat.getEditableText().toString();
				String email = textEmail.getEditableText().toString();
				String firstName = textFirstName.getEditableText().toString();
				String lastName = textLastName.getEditableText().toString();
				if(pw.length()>5 && email.length()>0 && firstName.length()>0 && lastName.length()>0){
					if (pw.equals(pw2)) {
						if (pw.matches("^((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%\\.]).{6,15})$")){
							new RegisterTask().execute();
						} else {
							Toast.makeText(context, R.string.error_password, Toast.LENGTH_LONG).show();
						}
					} else {
						resetPasswordFields();
						Toast.makeText(context, R.string.activity_register_passwords_dont_match, Toast.LENGTH_LONG).show();
					}
				} else {
					resetPasswordFields();
					Toast.makeText(context, R.string.activity_register_empty_fields, Toast.LENGTH_LONG).show();
				}
				
			}
		});

	}

	@Override
	public void onBackPressed() {
		finish();
	}
	
	private void resetPasswordFields(){
		textPassword.getEditableText().clear();
		textPasswordRepeat.getEditableText().clear();
	}

	private class RegisterTask extends AsyncTask<Object, Object, Object> {
		
		boolean success = false;

		@Override
		protected void onPreExecute() {
			Log.d("RegisterTask", "onPreExecute()");
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
			dialog = ProgressDialog.show(context, "Registering account", "Please wait");
		}

		@Override
		protected Object doInBackground(Object... params) {
			Log.d("RegisterTask", "doInBackground()");
			/*
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
			JSONObject jsonBody = new JSONObject();
			try {
				jsonBody.put("email", textEmail.getEditableText().toString());
				jsonBody.put("password", textPassword.getEditableText().toString());
				jsonBody.put("firstName", textFirstName.getEditableText().toString());
				jsonBody.put("lastName", textLastName.getEditableText().toString());
				jsonBody.put("status", 1);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			
			UserData userData = App.loadUserData();
			HttpResponse response = null;
			try {
				response = App.getWrapper().postUser(jsonBody);
				JSONObject json = response.getJSONObject();
				userData.setUserId(json.getInt("id"));
				userData.setName(json.getString("firstName") + " " + json.getString("lastName"));
				userData.setEmail(json.getString("email"));
				App.saveUserData(userData);
				success = true;
				
				// achievement for logging into isaaclock for the first time
				JSONObject jsonBody2 = new JSONObject();
				JSONObject body = new JSONObject();
				body.put("action", "create_account");
				jsonBody2.put("body", body);
				jsonBody2.put("priority", "PRIORITY_HIGH");
				jsonBody2.put("sourceId", 1);
				jsonBody2.put("subjectId", userData.getUserId());
				jsonBody2.put("subjectType", "USER");
				jsonBody2.put("type", "NORMAL");
				response = App.getWrapper().postEvent(jsonBody2);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			Log.d("RegisterTask", "onPostExecute()");
			dialog.dismiss();
			if(success){
				Intent intent = new Intent(context, UserActivityTabs.class);
				startActivity(intent);
			} else {
				Toast.makeText(context, R.string.error_register, Toast.LENGTH_LONG).show();
			}
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
			finish();
		}

	}

}
