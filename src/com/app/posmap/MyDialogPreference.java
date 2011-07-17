package com.app.posmap;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.DialogPreference;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;

public class MyDialogPreference extends DialogPreference {
	static final String PREF_TITLE_KEY = "PREF_TITLE_KEY";
	static final String PREF_ID_KEY = "PREF_ID_KEY";
	static final String PREF_CONTENT_URI = "PREF_CONTENT_URI";
	static final String PREF_DEFAULT_SOUND = "PREF_DEFAULT_SOUND";
	private ContentResolver cr;
	SharedPreferences pref;
	SharedPreferences.Editor editor;
	private String default_sound;
	static String name;
	private String id;
	ArrayAdapter<String> adapter;
	
	public MyDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		adapter = new ArrayAdapter<String>(context, R.layout.simple_list_item_hoge);
		cr = context.getContentResolver();
		Cursor cursor = cr.query(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, null, null, null, null);
		cursor.moveToFirst();
		default_sound = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
	
	}
	
	@Override
	protected void onClick() {
		String[] item = {"プリセット着信音", "マイファイル"};
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle("アラーム音選択");
		builder.setItems(item, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which == 0) {
					Cursor cursor = cr.query(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, null, null, null, null);
					selectSound(cursor, which);
				} else {
					Cursor cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
					selectSound(cursor, which);
				}
			}
		});
		builder.create().show();
	}
	
	private void selectSound(Cursor cursor, int which) {
		pref = getSharedPreferences(); 
		editor = pref.edit();
		editor.putInt(PREF_CONTENT_URI, which);
		int o = cursor.getCount();
		final String[] IDs = new String[o];
		final String[] DATAs = new String[o];
		//final String[] TITLEs = new String[o];
		final String[] NAMEs = new String[o];
		cursor.moveToFirst();
		
		for(int i = 0; i < o; i++) {
			IDs[i] = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
			DATAs[i] = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
			NAMEs[i] = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
			String[] splits = DATAs[i].split("/");
			// プリセット音じゃない場合
			if(which == 1) {
			NAMEs[i] = splits[splits.length-1];
			}
			cursor.moveToNext();
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle("候補一覧");
		builder.setItems(NAMEs, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				name = NAMEs[which];
				id = IDs[which];
				editor.putString(PREF_TITLE_KEY, name);
				editor.putString(PREF_ID_KEY, id);
				editor.putString(PREF_DEFAULT_SOUND, default_sound);
				editor.commit();
				MyDialogPreference.this.setSummary(name);
			}
		});
		builder.create().show();
	}
}
