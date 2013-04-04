package org.dhis2.androidsms;

import android.os.Bundle;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

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
 
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        
        setContentView(R.layout.activity_main);        
 
        btnSave = (Button) findViewById(R.id.btnSave);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        txtUsername = (EditText) findViewById(R.id.txtUsername);
        txtURL = (EditText) findViewById(R.id.txtURL);
		
		txtURL.setText(settings.getString("dhis2.url","http://apps.dhis2.org/dev/sms/smsinput.action"));
		txtUsername.setText(settings.getString("dhis2.username","admin"));
		txtPassword.setText(settings.getString("dhis2.password","district"));
		
        
        btnSave.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View v) 
            {   
            	String password = txtPassword.getText().toString();
                String username = txtUsername.getText().toString();
                String url = txtURL.getText().toString();                 
                // Save it in preferences
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("dhis2.username", username);
                editor.putString("dhis2.password", password);
                editor.putString("dhis2.url", url);
                editor.commit();
                
            }
        });        
    }   
    
    //---sends an SMS message to another device---
    private void sendSMS(String phoneNumber, String message)
    {        
        PendingIntent pi = PendingIntent.getActivity(this, 0,
            new Intent(this, MainActivity.class), 0);                
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, pi, null);        
    }  
}
