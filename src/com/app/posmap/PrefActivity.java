package com.app.posmap;

import java.util.HashMap;
import java.util.Map;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class PrefActivity extends PreferenceActivity {
	
	public static final String PREF_CALL_RANGE = "PREF_CALL_RANGE";
	public static final String PREF_VIBE_MODE = "PREF_VIBE_MODE";
	public static final String PREF_SOUND_MODE = "PREF_SOUND_MODE";
	public static final String PREF_NOW_TWEET = "PREF_NOW_TWEET";
	public static final String PREF_WILL_TWEET = "PREF_WILL_TWEET";
	public static final String PREF_SETTING_SOUND = "PREF_SETTING_SOUND";
	public static final String PREF_TITLE_KEY = "PREF_TITLE_KEY";
	
	Map<String, String> callRangeMap = new HashMap<String, String>();
	SharedPreferences prefs;
	CheckBoxPreference vibemodePref;
	CheckBoxPreference soundmodePref;
	EditTextPreference nowTweetPref;
	EditTextPreference willTweetPref;
	ListPreference callrangePref;
	static Preference soundselectPref;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		addPreferencesFromResource(R.xml.preference);
		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
				
		callrangePref = (ListPreference)findPreference(PREF_CALL_RANGE);
		vibemodePref = (CheckBoxPreference)findPreference(PREF_VIBE_MODE);
		soundmodePref = (CheckBoxPreference)findPreference(PREF_SOUND_MODE);
		nowTweetPref = (EditTextPreference)findPreference(PREF_NOW_TWEET);
		willTweetPref = (EditTextPreference)findPreference(PREF_WILL_TWEET);
		if(soundmodePref.isChecked()) {
			soundmodePref.setSummary("true");
		} else {
			soundmodePref.setSummary("false");
		}
		soundselectPref = (Preference)findPreference(PREF_SETTING_SOUND);
		soundselectPref.setDefaultValue(prefs.getString(MyDialogPreference.PREF_DEFAULT_SOUND, PosMapActivity.default_sound));
		if(vibemodePref.isChecked()) {
			vibemodePref.setSummary("true");
		} else {
			vibemodePref.setSummary("false");
		}
		
		String[] rangevalues = getResources().getStringArray(R.array.call_range);
		String[] rangekeys = getResources().getStringArray(R.array.call_range_values);
		
		for(int i = 0; i < rangekeys.length; i++) {
			callRangeMap.put(rangekeys[i], rangevalues[i]);
		}
		willTweetPref.setSummary(willTweetPref.getText().toString());
		nowTweetPref.setSummary(nowTweetPref.getText().toString());
		
		loadPreference(callrangePref.getSharedPreferences(), PREF_CALL_RANGE);
		loadPreference(soundselectPref.getSharedPreferences(), PREF_TITLE_KEY);
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
	}
	
	private SharedPreferences.OnSharedPreferenceChangeListener listener = 
		new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
				String key) {
			if(key.equals(PREF_CALL_RANGE)) {
				loadPreference(sharedPreferences, key);
			} else if (key.equals(PREF_VIBE_MODE)) {
				if(vibemodePref.isChecked()) {
					vibemodePref.setSummary("true");
				} else {
					vibemodePref.setSummary("false");
				}
			} else if (key.equals(PREF_SETTING_SOUND)) {
				soundselectPref.setSummary(prefs.getString(PREF_TITLE_KEY, "0"));
			} else if (key.equals(PREF_NOW_TWEET)) {
				nowTweetPref.setSummary(prefs.getString(PREF_NOW_TWEET, "イマココ!!"));
			} else if (key.equals(PREF_WILL_TWEET)) {
				willTweetPref.setSummary(prefs.getString(PREF_WILL_TWEET, "ココイク!!"));
			}
		}
	};

	private void loadPreference(SharedPreferences sharedPreferences, String key) {
		String index = sharedPreferences.getString(key, "");
		if(key.equals(PREF_CALL_RANGE)) {
			callrangePref.setSummary(callRangeMap.get(index));
		} else if (key.equals(PREF_TITLE_KEY)) {
			soundselectPref.setSummary(prefs.getString(PREF_TITLE_KEY, PosMapActivity.default_sound));
			
		}
	}
}
