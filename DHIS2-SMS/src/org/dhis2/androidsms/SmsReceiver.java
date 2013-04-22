package org.dhis2.androidsms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.StringTokenizer;

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

	@Override
	public void onReceive(Context context, Intent intent) {

		try {

			SharedPreferences settings = context.getSharedPreferences(
					PREFS_NAME, 0);
			boolean forward = settings.getBoolean("dhis2.forward", false);
			String commands = settings.getString("dhis2.commands", "");
			
			StringTokenizer tokenizer = new StringTokenizer(commands,
					",");
			while (tokenizer.hasMoreElements()) {
				Log.d(TAG,"Token:"+tokenizer.nextToken());
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

					msgs[i] = SmsMessage
							.createFromPdu((byte[]) pdus[i]);

					String command = msgs[i].getMessageBody().toString();
					Log.d(TAG, "message before parsing=("+command+")");
					if (command.indexOf(' ') != -1) {
						command = command.substring(0, command.indexOf(' '))
								.trim();
					}

					Log.d(TAG, "command=("+command+")");
					
					tokenizer = new StringTokenizer(commands,
							",");
					while (tokenizer.hasMoreElements()) {
						Log.d(TAG,"Checking token ");
						if (tokenizer.nextToken().equalsIgnoreCase(command)) {

							urlString = settings.getString("dhis2.url",
									"http://apps.dhis2.org/dev/sms/smsinput.action");
							username = settings.getString("dhis2.username","admin");
							password = settings.getString("dhis2.password", "district");

							new ForwardMessageTask().execute(msgs[i].getOriginatingAddress(), 
							msgs[i].getMessageBody().toString());

							Toast.makeText(context, "Forwarded SMS to DHIS2",
									Toast.LENGTH_SHORT).show();

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
	}
	
    class ForwardMessageTask extends AsyncTask<String, Void, Void> {

        private Exception exception;

//        protected RSSFeed doInBackground(String... urls) {
//            try {
//                URL url= new URL(urls[0]);
//                SAXParserFactory factory =SAXParserFactory.newInstance();
//                SAXParser parser=factory.newSAXParser();
//                XMLReader xmlreader=parser.getXMLReader();
//                RssHandler theRSSHandler=new RssHandler();
//                xmlreader.setContentHandler(theRSSHandler);
//                InputSource is=new InputSource(url.openStream());
//                xmlreader.parse(is);
//                return theRSSHandler.getFeed();
//            } catch (Exception e) {
//                this.exception = e;
//                return null;
//            }
//        }
        

//        protected void onPostExecute(RSSFeed feed) {
//            // TODO: check this.exception 
//            // TODO: do something with the feed
//        }


        // Args: sender, message
		@Override
		protected Void doInBackground(String... arg0) {
    		try {

    			String query = "?sender=" + URLEncoder.encode(arg0[0], "utf-8")
    					+ "&message=" + URLEncoder.encode(arg0[1], "utf-8");
    			String url = urlString + query;

    			HttpURLConnection c = (HttpURLConnection) new URL(url)
    					.openConnection();
    			c.setRequestProperty(
    					"Authorization",
    					"Basic "
    							+ Base64.encodeToString(
    									(username + ":" + password).getBytes(),
    									Base64.NO_WRAP));
    			c.setUseCaches(false);
    			c.connect();

    			readStream(c.getInputStream());

    			c.disconnect();

    		} catch (Exception e) {
    			e.printStackTrace();
    			Log.e(TAG, "Exception:" + e, e);
    		}
    		return null;
		}
     }

}
