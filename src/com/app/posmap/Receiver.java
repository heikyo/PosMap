package com.app.posmap;

import java.io.IOException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.os.Vibrator;
import android.provider.MediaStore;

public class Receiver extends BroadcastReceiver {
	private boolean vibe_mode;
	private boolean sound_mode;
	private String sound_id;
	private int content_uri;
	private boolean sound_playing;
	
	static MediaPlayer mediaPlayer;
	static Vibrator vibrator;
	static boolean vibrator_playing;
	
	public Receiver() {
		vibrator_playing = false;
		sound_playing = false;
	}
	
	@Override
	public void onReceive(final Context context, Intent intent) {
		if(PosMapActivity.notificationManager != null) {
			PosMapActivity.notificationManager.cancelAll();
			PosMapActivity.notificationManager = null;
		}
		
		Bundle extras = intent.getExtras();
		vibe_mode = extras.getBoolean("VIBE_MODE");
		sound_mode = extras.getBoolean("SOUND_MODE");
		sound_id = extras.getString("SOUND_ID");
		content_uri = extras.getInt("CONTENT_URI");
		
		if(intent.hasExtra(LocationManager.KEY_PROXIMITY_ENTERING)) {
			if(vibe_mode == true && sound_mode == true) {
				runVibrate(context);
				new Thread() {
					@Override
					public void run() {
						Looper.prepare();
						runMediaPlayer(context);
						Looper.loop();
					}
				}.start();
			} else if (vibe_mode == false && sound_mode == true) {
				runMediaPlayer(context);
			} else {
				runVibrate(context);
			}
		}
		
		Intent toActivity = new Intent(context.getApplicationContext(), Stop.class);
		toActivity.putExtra("SOUND_PLAYING", sound_playing);
		toActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(toActivity);
	}
	
	private void runVibrate(Context context) {
		vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
		for(int i = 0; i < 100; i++) {
			vibrator.vibrate(1000000);
		}
		vibrator_playing = true;
	}
	
	private void runMediaPlayer(Context context) {
		mediaPlayer = new MediaPlayer();
		if(content_uri == 0) {
			try {
				mediaPlayer.setDataSource(context.getApplicationContext(), Uri.withAppendedPath(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, sound_id));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	
		} else {
			try {
				mediaPlayer.setDataSource(context.getApplicationContext(), Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, sound_id));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			mediaPlayer.prepare();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		mediaPlayer.setLooping(true);
		mediaPlayer.start();
				
		if(mediaPlayer.isPlaying()) {
			sound_playing = true;
		}
	}
}
