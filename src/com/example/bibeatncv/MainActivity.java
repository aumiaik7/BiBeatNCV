package com.example.bibeatncv;



import java.io.File;


import com.ipaulpro.afilechooser.FileChooserActivity;
import com.ipaulpro.afilechooser.utils.FileUtils;


import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends Activity {
	public boolean flag = true;
	NCVView ncv ;//= (NCVView) findViewById(R.layout.activity_main);
	
	private static final byte[] data = new byte[]{1,0,0,0,0,0,0,0}; //first three bits asre used for Control OUT
	
	private static final int REQUEST_CODE = 6384; // onActivityResult request code
	
	AlertDialog.Builder builder;// = new AlertDialog.Builder(this);
	String aboutText = "Both Machine and Software was designed and developed by \nBi-BEAT Ltd. \nContact info- \nPhone: +880-1817022834, +880-1843590069 \nWebsite: www.bibeat.com" +
			"\nE-mail: info@bibeat.com";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (getIntent().getBooleanExtra("EXIT", false)) {
			 finish();
			}
		
		ncv = new NCVView(this);
		setContentView(ncv);
		//Toast.makeText(this, "ABCD", Toast.LENGTH_SHORT).show();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		String state = Environment.getExternalStorageState();
		    if (Environment.MEDIA_MOUNTED.equals(state)) {
		       // return true;
		    	//Toast.makeText(this, "ABCD", Toast.LENGTH_SHORT).show();
		    }
		    
	   builder = new AlertDialog.Builder(this);    
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private void showChooser() {
		// Use the GET_CONTENT intent from the utility class
		/*Intent target = FileUtils.createGetContentIntent();
		// Create the chooser Intent
		Intent intent = Intent.createChooser(
				target, getString(R.string.chooser_title));*/
		Intent intent = new Intent(getBaseContext(), FileChooserActivity.class);
				
		try {
			startActivityForResult(intent, REQUEST_CODE);
		} catch (ActivityNotFoundException e) {
			// The reason for the existence of aFileChooser
		}				
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CODE:	
			// If the file selection was successful
			if (resultCode == RESULT_OK) {		
				if (data != null) {
					// Get the URI of the selected file
					final Uri uri = data.getData();

					try {
						// Create a file instance from the URI
						final File file = FileUtils.getFile(uri);
						//Toast.makeText(this, "File Selected: "+file.getAbsolutePath(), Toast.LENGTH_LONG).show();
						ncv.browsedFilePath = file.getAbsolutePath();
						ncv.countFlag = 0;
						ncv.dataCount = 0;
						ncv.nextButtonEnabled = false;
						ncv.prevButtonEnabled = false;
						ncv.drawFromFile();
						
					} catch (Exception e) {
						Log.e("FileSelectorTestActivity", "File select error", e);
					}
				}
			} 
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	
	
	public boolean onOptionsItemSelected(MenuItem item) {
		//respond to menu item selection
		switch (item.getItemId()) {
		//Radio button for changing time 
		case R.id.action_40:
			//Toast.makeText(this, "ABCD", Toast.LENGTH_SHORT).show();
		item.setChecked(true); 
		ncv.setFlag(false);	
		ncv.setPerDivision(1);
		return true;
		
		case R.id.action_80:
		item.setChecked(true); 
		ncv.setFlag(true);	
		ncv.setPerDivision(2);
		return true;
		
		//Radio buttons for changing gain
		case R.id.action_333:
		item.setChecked(true); 
		ncv.setGainFlag(1);	
		return true;
		
		case R.id.action_1000:
		item.setChecked(true); 
		ncv.setGainFlag(2);	
		return true;
			
		case R.id.action_3333:
		item.setChecked(true); 
		ncv.setGainFlag(3);	
		return true;
			
		case R.id.action_10000:
		item.setChecked(true); 
		ncv.setGainFlag(4);	
		return true;
		
		case R.id.browse:
		showChooser();
		return true;
		
		//Radio Button for changing pulse width
		case R.id.action_p_1:
		data[0] = 1;
		ncv.controlTransfer(data);
		ncv.setStimulationTime(0.1f);
		item.setChecked(true); 
		return true;
		
		case R.id.action_p_2:
		data[0] = 2;
		ncv.controlTransfer(data);
		ncv.setStimulationTime(0.2f);
		item.setChecked(true); 
		return true;
			
		case R.id.action_p_3:
		data[0] = 3;
		ncv.controlTransfer(data);	
		ncv.setStimulationTime(0.3f);
		item.setChecked(true); 
		return true;
		
		case R.id.action_p_4:
		data[0] = 4;
		ncv.controlTransfer(data);
		ncv.setStimulationTime(0.4f);
		item.setChecked(true); 
		return true;
		
		case R.id.action_p_5:
		data[0] = 5;
		ncv.controlTransfer(data);	
		ncv.setStimulationTime(0.5f);
		item.setChecked(true); 
		return true;
		
		case R.id.action_p_6:
		data[0] = 6;
		ncv.controlTransfer(data);	
		ncv.setStimulationTime(0.6f);
		item.setChecked(true); 
		return true;
		
		case R.id.action_p_7:
		data[0] = 7;
		ncv.controlTransfer(data);
		ncv.setStimulationTime(0.7f);
		item.setChecked(true); 
		return true;
		
		case R.id.action_p_8:
		data[0] = 8;
		ncv.controlTransfer(data);	
		ncv.setStimulationTime(0.8f);
		item.setChecked(true); 
		return true;
		
		case R.id.action_p_9:
		data[0] = 9;
		ncv.controlTransfer(data);	
		ncv.setStimulationTime(0.9f);
		item.setChecked(true); 
		return true;
		
		case R.id.action_p_10:
		data[0] = 10;
		ncv.controlTransfer(data);
		ncv.setStimulationTime(1.0f);
		item.setChecked(true); 
		return true;
		
		
		
		case R.id.about:
			
			builder.setMessage(aboutText)
            .setTitle("ABOUT")
            .setCancelable(false)
            .setPositiveButton("OK", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id) 
                {
             	   
                }
            });	
			
			 AlertDialog alert = builder.create();
		     alert.show();
		return true;
			
			
		
		default:
		return super.onOptionsItemSelected(item);
	}
	}

}
