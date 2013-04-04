package org.dhis2.androidsms;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpConnection;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.telephony.gsm.SmsMessage;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver {
	private static final String TAG = "SmsReceiver";

	public static final String PREFS_NAME = "DHIS2PrefsFile";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		 SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
	     //boolean silent = settings.getBoolean("silentMode", false);
	       
		// ---get the SMS message passed in---
		Bundle bundle = intent.getExtras();
		SmsMessage[] msgs = null;
		String str = "";
		if (bundle != null) {
			// ---retrieve the SMS message received---
			Object[] pdus = (Object[]) bundle.get("pdus");
			msgs = new SmsMessage[pdus.length];
			for (int i = 0; i < msgs.length; i++) {
				msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
				str += "SMS from " + msgs[i].getOriginatingAddress();
				str += " :";
				str += msgs[i].getMessageBody().toString();
				str += "\n";

				forwardMessage(
						settings.getString("dhis2.url","http://apps.dhis2.org/dev/sms/smsinput.action"), 
						msgs[i].getOriginatingAddress(), 
						msgs[i].getMessageBody().toString(), 
						settings.getString("dhis2.username","admin"), 
						settings.getString("dhis2.password","district"));
			}
			// ---display the new SMS message---
			Toast.makeText(context, str, Toast.LENGTH_SHORT).show();

		}
	}

	public void forwardMessage(String urlString, String sender, String message, String username,
			String password) {
		try {
			
			String query = "?sender="+URLEncoder.encode(sender, "utf-8")+
					"&message="+URLEncoder.encode(message, "utf-8");
			String url = urlString + query;
			
			HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
			c.setRequestProperty("Authorization", "Basic " + Base64.encodeToString((username+":"+password).getBytes(), Base64.NO_WRAP));
			c.setUseCaches(false);
			c.connect();
			
			readStream(c.getInputStream());
			
			c.disconnect();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void readStream(InputStream in) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(in));
			String line = "";
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}