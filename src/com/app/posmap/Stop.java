package com.app.posmap;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

public class Stop extends Activity {
	
	private Button button;
	private boolean vibe_playing;
	private boolean sound_playing;
	private SharedPreferences prefs;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stop);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		vibe_playing = prefs.getBoolean(PrefActivity.PREF_VIBE_MODE, false);
		sound_playing = prefs.getBoolean(PrefActivity.PREF_SOUND_MODE, false);
		button = (Button)findViewById(R.id.button1);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(sound_playing == true && vibe_playing == true) {
					Receiver.mediaPlayer.stop();
					Receiver.vibrator.cancel();
				} else if(sound_playing == true && vibe_playing == false) {
					Receiver.mediaPlayer.stop();
				} else {
					Receiver.vibrator.cancel();
				}
				
				Intent intent = new Intent(Stop.this, PosMapActivity.class);
				startActivity(intent);
				PosMapActivity.notificationManager = null;
				PosMapActivity.locationManager.removeProximityAlert(PosMapActivity.pendingIntent);
				PosMapActivity.locationManager.removeUpdates(PosMapActivity.pendingIntent);
				finish();
			}
		});
	}
	
	
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if(event.getAction() == KeyEvent.ACTION_DOWN) {
			switch(event.getKeyCode()) {
			case KeyEvent.KEYCODE_BACK:
				return false;
			}
		}
		return super.dispatchKeyEvent(event);
	}
}
