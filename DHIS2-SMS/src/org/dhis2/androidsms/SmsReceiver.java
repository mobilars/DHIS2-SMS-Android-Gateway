package org.dhis2.androidsms;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.StringTokenizer;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver {
	private static final String TAG = "SmsReceiver";

	public static final String PREFS_NAME = "DHIS2PrefsFile";
	String urlString;
	String username;
	String password;
	

	Context context;
	
	@Override
	public void onReceive(Context context, Intent intent) {

		try {
			
			this.context = context;

			SharedPreferences settings = context.getSharedPreferences(
					PREFS_NAME, 0);
			boolean forward = settings.getBoolean("dhis2.forward", false);
			String commands = settings.getString("dhis2.commands", "");

			StringTokenizer tokenizer = new StringTokenizer(commands, ",");
			while (tokenizer.hasMoreElements()) {
				Log.d(TAG, "Token:" + tokenizer.nextToken());
			}

			if (!forward || commands == null || commands.equals("")) {
				return;
			}

			// ---get the SMS message passed in---
			Bundle bundle = intent.getExtras();
			SmsMessage[] msgs = null;
			if (bundle != null) {
				// ---retrieve the SMS message received---
				Object[] pdus = (Object[]) bundle.get("pdus");
				msgs = new SmsMessage[pdus.length];
				for (int i = 0; i < msgs.length; i++) {

					msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);

					String command = msgs[i].getMessageBody().toString();
					Log.d(TAG, "message before parsing=(" + command + ")");
					if (command.indexOf(' ') != -1) {
						command = command.substring(0, command.indexOf(' '))
								.trim();
					}

					Log.d(TAG, "command=(" + command + ")");

					tokenizer = new StringTokenizer(commands, ",");
					while (tokenizer.hasMoreElements()) {
						Log.d(TAG, "Checking token ");
						if (tokenizer.nextToken().equalsIgnoreCase(command)) {

							urlString = settings
									.getString("dhis2.url",
											"http://apps.dhis2.org/dev/sms/smsinput.action");
							username = settings.getString("dhis2.username",
									"admin");
							password = settings.getString("dhis2.password",
									"district");

							new ForwardMessageTask().execute(msgs[i]
									.getOriginatingAddress(), msgs[i]
									.getMessageBody().toString());

//							Toast.makeText(context, "Forwarded SMS to DHIS2",
//									Toast.LENGTH_SHORT).show();

						}
					}

				}
			}

		} catch (Exception e) {
			Toast.makeText(context, "Failed to handle SMS forwarding",
					Toast.LENGTH_SHORT).show();
			Log.e(TAG, "Exception:" + e, e);

		}

	}

	private boolean readStream(InputStream in) {
		BufferedReader reader = null;
		boolean success = false;
		try {
			reader = new BufferedReader(new InputStreamReader(in));
			String line = "";
			while ((line = reader.readLine()) != null) {
				// If found success or forwarded in served page, it was hopefully a success. 
				if (line.toUpperCase().indexOf("SUCCESS") != 1 || line.toUpperCase().indexOf("FORWARDED")  != 1) {
					success = true;
				} 
			}
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "Exception:" + e, e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, "Exception:" + e, e);
				}
			}
		}
		return success;
	}

	class ForwardMessageTask extends AsyncTask<String, Void, Void> {

		private Exception exception;
		boolean success = false;
					  
		// Args: sender, message
		@Override
		protected Void doInBackground(String... arg0) {
			try {

				String query = "?sender=" + URLEncoder.encode(arg0[0], "utf-8")
						+ "&message=" + URLEncoder.encode(arg0[1], "utf-8");
				String url = urlString + query;

				if (url.startsWith("https")) {
					
					// Create a trust manager that does not validate certificate chains
			        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			                return null;
			            }
			            public void checkClientTrusted(X509Certificate[] certs, String authType) {
			            }
			            public void checkServerTrusted(X509Certificate[] certs, String authType) {
			            }
			        } };
			        // Install the all-trusting trust manager
			        final SSLContext sc = SSLContext.getInstance("SSL");
			        sc.init(null, trustAllCerts, new java.security.SecureRandom());
			        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			        // Create all-trusting host name verifier
			        HostnameVerifier allHostsValid = new HostnameVerifier() {
			            @Override
						public boolean verify(String hostname,
								SSLSession session) {
							return true;
						}
			        };

			        // Install the all-trusting host verifier
			        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

//			        URL u = new URL(url);
//			        URLConnection con = u.openConnection();
//			        final Reader reader = new InputStreamReader(con.getInputStream());
//			        final BufferedReader br = new BufferedReader(reader);        
//			        String line = "";
//			        while ((line = br.readLine()) != null) {
//			            System.out.println(line);
//			        }        
//			        br.close();
					
					HttpsURLConnection c = (HttpsURLConnection) new URL(url)
							.openConnection();
					c.setRequestProperty(
							"Authorization",
							"Basic "
									+ Base64.encodeToString(
											(username + ":" + password)
													.getBytes(), Base64.NO_WRAP));
					c.setUseCaches(false);
					c.connect();

					success = readStream(c.getInputStream());

					c.disconnect();
				} else {
					HttpURLConnection c = (HttpURLConnection) new URL(url)
							.openConnection();
					c.setRequestProperty(
							"Authorization",
							"Basic "
									+ Base64.encodeToString(
											(username + ":" + password)
													.getBytes(), Base64.NO_WRAP));
					c.setUseCaches(false);
					c.connect();

					success = readStream(c.getInputStream());

					c.disconnect();
				}

			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG, "Exception:" + e, e);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void v) {
			if (success) {
				toastOnUIThread("SMS Forward success");
				Log.i(TAG, "SMS Forward success");
			}
			else {
				toastOnUIThread("SMS forward failure");
				Log.e(TAG, "SMS Forward failure");
			}	
	     }
	}

	public void toastOnUIThread(String string) {
		Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
	}

}
