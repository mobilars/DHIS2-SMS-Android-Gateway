package org.dhis2.androidsms2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import org.apache.http.conn.util.InetAddressUtils;
import org.dhis2.androidsms2.R;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

	private static String TAG = "MainActivity";
	public static final String PREFS_NAME = "DHIS2PrefsFile";
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	Button btnSave;
	EditText txtPassword;
	EditText txtUsername;
	EditText txtURL;
	EditText txtCommands;
	ToggleButton toggleForward;
	TextView textIPAddress;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent i = new Intent(this, WebService.class);
		// potentially add data to the intent
		// i.putExtra("KEY1", "Value to be used by the service");
		this.startService(i);
		// bindService(i, connection, this.BIND_AUTO_CREATE);

		Log.i(TAG, "Created thread for server socket.");

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

		setContentView(R.layout.activity_main);

		btnSave = (Button) findViewById(R.id.btnSave);
		txtPassword = (EditText) findViewById(R.id.txtPassword);
		txtUsername = (EditText) findViewById(R.id.txtUsername);
		txtURL = (EditText) findViewById(R.id.txtURL);
		txtCommands = (EditText) findViewById(R.id.txtCommands);
		toggleForward = (ToggleButton) findViewById(R.id.toggleForward);
		textIPAddress = (TextView) findViewById(R.id.textIPAddress);

		txtURL.setText(settings.getString("dhis2.url",
				"http://apps.dhis2.org/dev/sms/smsinput.action"));
		txtUsername.setText(settings.getString("dhis2.username", "admin"));
		txtPassword.setText(settings.getString("dhis2.password", "district"));
		txtCommands.setText(settings.getString("dhis2.commands", "report"));
		toggleForward.setChecked(settings.getBoolean("dhis2.forward", true));

		// Show IP address
		textIPAddress.setText("Listening at: http://" + getLocalIpAddress()+ ":8000/send?recipient={recipient}&content={content}");

		btnSave.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String password = txtPassword.getText().toString();
				String username = txtUsername.getText().toString();
				String url = txtURL.getText().toString();
				String commands = txtCommands.getText().toString();

				// Save it in preferences
				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString("dhis2.username", username);
				editor.putString("dhis2.password", password);
				editor.putString("dhis2.url", url);
				editor.putString("dhis2.commands", commands.trim());
				editor.putBoolean("dhis2.forward", toggleForward.isChecked());
				editor.commit();
				Toast.makeText(getApplicationContext(), "Settings saved",
						Toast.LENGTH_SHORT).show();

			}
		});
	}

	// ---sends an SMS message to another device---
	private void sendSMS(String phoneNumber, String message) {
		PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this,
				MainActivity.class), 0);
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, pi, null);
	}

	public String getLocalIpAddress() {
		
		String ipv4 = "";
		
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					Log.i(TAG,"ip1--:" + inetAddress);
					Log.i(TAG,"ip2--:" + inetAddress.getHostAddress());

					// for getting IPV4 format
					if (!inetAddress.isLoopbackAddress()
							&& InetAddressUtils
									.isIPv4Address(ipv4 = inetAddress
											.getHostAddress())) {

						String ip = inetAddress.getHostAddress().toString();
						//System.out.println("ip---::" + ip);
						//EditText tv = (EditText) findViewById(R.id.ipadd);
						//tv.setText(ip);
						// return inetAddress.getHostAddress().toString();
						return ipv4;
					}
				}
			}
		} catch (Exception ex) {
			Log.e("IP Address", ex.toString());
		}
		return null;
	}
	/*
	 * public String getLocalIpAddress() {
	 * 
	 * try { for (Enumeration < NetworkInterface > en =
	 * NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	 * NetworkInterface intf = en.nextElement(); for (Enumeration < InetAddress
	 * > enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	 * InetAddress inetAddress = enumIpAddr.nextElement(); if
	 * (!inetAddress.isLoopbackAddress()) { return
	 * inetAddress.getHostAddress().toString(); } } } } catch (SocketException
	 * ex) { Log.e(TAG, ex.toString()); } return null; }
	 */

}
