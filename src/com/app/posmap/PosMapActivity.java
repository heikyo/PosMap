package com.app.posmap;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class PosMapActivity extends MapActivity implements LocationListener, OnClickListener{
	
	private static final String LOGTAG = "LOGTAG";
	static final int FROM_TAP = 0;
	static final int FROM_SEARCH = 1;
	private static final int ZOOM_UP = 18;
	private static final int SHOW_PREFERENCES = 1;
	
	protected static MyLocationOverlay myOverlay;
	private LogOverlay logOverlay;
	protected static MyItemizedOverlay itemizedOverlay;
	
	private MapView mapView;
	protected static LocationManager locationManager;
	protected static MapController mapController;
	
	private Context mContext;
	protected static Notification notification;
	protected static NotificationManager notificationManager;
	protected static PendingIntent pendingIntent;
	private ProgressDialog progressDialog;
	private AlertDialog alertDialog;
	
	private EditText searchText;
	private ImageButton destButton;
	private ImageButton mapModeButton;
	private ImageButton myLocationButton;
	private TextView textView;
	private EditText detailAddressText;
	
	private Drawable drawable;
	
	private int from;
	private int content_uri;
	private String sound_id;
	private String sound_name;
	static String default_sound;
	private int call_range;
	private boolean vibe_mode;
	private boolean sound_mode;
	
	protected static String destName;
	static String selectName;
	private String now_tweet_str;
	private String will_tweet_str;
	protected static int nowLat;
	protected static int nowLng;
	protected static int destLat;
	protected static int destLng;
	private boolean location_enabled;
	private boolean set_destination;
	private boolean get_mylocation;
	private boolean onTap;
	private boolean log_enabled;
	
	
	public PosMapActivity() {
		onTap = false;
		location_enabled = false;
		set_destination = false;
		get_mylocation = false;
		log_enabled = false;
	}
	
	Handler handler = new Handler();
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
        final Intent queryIntent = getIntent();
		final String queryAction = queryIntent.getAction();
		if(Intent.ACTION_SEARCH.equals(queryAction)) {
			doSearchWithIntent(queryIntent);
		}
		if(Intent.ACTION_VIEW.equals(queryAction)) {
			doSearchWithIntent(queryIntent);
		}
        
		AdView adView = (AdView)this.findViewById(R.id.adView);
        
        AdRequest adRequest = new AdRequest();
        adView.loadAd(adRequest);
		
        mapView = (MapView)findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        mapController = mapView.getController();
        mapController.setZoom(ZOOM_UP);
        
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		myOverlay = new MyLocationOverlay(this, mapView);
		myOverlay.onProviderEnabled(LocationManager.GPS_PROVIDER);
		mapView.getOverlays().add(myOverlay);
		logOverlay = new LogOverlay();
        
        searchText = (EditText)findViewById(R.id.editText1);
        searchText.setOnClickListener(this);
        destButton = (ImageButton)findViewById(R.id.button0);
        destButton.setImageResource(android.R.drawable.ic_menu_rotate);
        destButton.setOnClickListener(this);
        mapModeButton = (ImageButton)findViewById(R.id.button1);
        mapModeButton.setImageResource(android.R.drawable.ic_menu_mapmode);
        mapModeButton.setOnClickListener(this);
        myLocationButton = (ImageButton)findViewById(R.id.button2);
        myLocationButton.setImageResource(android.R.drawable.ic_menu_mylocation);
        myLocationButton.setOnClickListener(this);
        textView = (TextView)findViewById(R.id.textview1);
    }
    
    private void setDestination(Drawable drawable, MapView mapView) {
		Toast.makeText(this, "目的地をタップしてください", Toast.LENGTH_SHORT).show();
		itemizedOverlay = new MyItemizedOverlay(drawable, this);
		mapView.getOverlays().add(new Overlay() {
			@Override
			public boolean onTap(GeoPoint point2, MapView mapView) {
				super.onTap(point2, mapView);
				if(onTap == false) {
					textView.setText("検索中");
					from = FROM_TAP;
					pointToAddress(point2);
					SubHelper.setIcon(mapView, point2);
					sendPendingIntent(point2);
					set_destination = true;
					onTap = true;
				}
				return true;
			}
		});
	}
    
    private void sendPendingIntent(final GeoPoint point) {
		updateFromPreferences();
		if(vibe_mode == false && sound_mode == false) {
			Toast.makeText(PosMapActivity.this, "アラーム機能、バイブ機能共にOFFになっているため、バイブでお知らせします。", Toast.LENGTH_LONG).show();
		}
		Intent intent = new Intent(this, Receiver.class);
		intent.putExtra("VIBE_MODE", vibe_mode);
		intent.putExtra("SOUND_MODE", sound_mode);
		intent.putExtra("SOUND_ID", sound_id);
		intent.putExtra("SOUND_NAME", sound_name);
		intent.putExtra("CONTENT_URI", content_uri);
		
		pendingIntent = PendingIntent.getBroadcast(PosMapActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		locationManager.addProximityAlert(point.getLatitudeE6()/1E6, point.getLongitudeE6()/1E6, call_range, -1, pendingIntent);
	}
    
    private void updateFromPreferences() {
		Context context = getApplicationContext();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		ContentResolver cr = getContentResolver();
		Cursor cursor = cr.query(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, null, null, null, null);
		cursor.moveToFirst();
		default_sound = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
		call_range = Integer.parseInt(prefs.getString(PrefActivity.PREF_CALL_RANGE, "0"));
		sound_id = prefs.getString(MyDialogPreference.PREF_ID_KEY, "0");
		sound_name = prefs.getString(PrefActivity.PREF_SETTING_SOUND, default_sound);
		vibe_mode = prefs.getBoolean(PrefActivity.PREF_VIBE_MODE, true);
		sound_mode = prefs.getBoolean(PrefActivity.PREF_SOUND_MODE, true);
		content_uri = prefs.getInt(MyDialogPreference.PREF_CONTENT_URI, 0);
		now_tweet_str = prefs.getString(PrefActivity.PREF_NOW_TWEET, "現在地");
		will_tweet_str = prefs.getString(PrefActivity.PREF_WILL_TWEET, "目的地");
	}
    
    private void pointToAddress(final GeoPoint point) {
		Thread th = new Thread() {
			public void run() {
				Looper.prepare();
				final String destName = heavyProcess(point);
				handler.post(new Runnable() {
					@Override
					public void run() {
						if(destName != null) {
							mContext = getApplicationContext();
							SubHelper.setNotification(mContext);
							textView.setText("目的地: " + destName);
							float[] results = {0, 0, 0};
							if(get_mylocation != true || set_destination != true) {
								
							} else {
								Location.distanceBetween(nowLat/1E6, nowLng/1E6, destLat/1E6, destLng/1E6, results);
								String distance = Float.toString(results[0]);
								String replaced = distance.replaceAll("\\.[0-9]*", "");
								Toast.makeText(getApplicationContext(), "目的地まで: 約 " + replaced + "m", Toast.LENGTH_LONG).show();
							}
							AlertDialog.Builder builder = new AlertDialog.Builder(PosMapActivity.this);
							builder.setTitle("目的地を共有しますか？");
							builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									mContext = getApplicationContext();
									LocationHelper.tweet(mContext, will_tweet_str, destLat, destLng);
								}
							});
							builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									return;
								}
							});
							builder.create().show();
						} else {
							return;
						}
					}
				});
				Looper.loop();
			}
		};
		th.start();
	}
    
    private String heavyProcess(GeoPoint point) {
		Geocoder geocoder = new Geocoder(PosMapActivity.this, Locale.JAPAN);
		
		try {
			List<Address> addressList = geocoder.getFromLocation(point.getLatitudeE6()/1E6, point.getLongitudeE6()/1E6, 1);
			destLat = point.getLatitudeE6();
			destLng = point.getLongitudeE6();
			
			if(addressList.size() != 0) {
				
				Address address = addressList.get(0);
				destName = LocationHelper.getDestName(address, from);
			} else {
				Toast.makeText(PosMapActivity.this, "ココは設定できません", Toast.LENGTH_SHORT).show();
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return destName;
	}
    
    @Override
    protected void onResume() {
    	super.onResume();
    	GpsCheck();
    }
    
    @Override
	public void onDestroy() {
		super.onDestroy();
		destLat = 0;
		destLng = 0;
		if(notificationManager != null) {
			notificationManager.cancelAll();
			locationManager.removeUpdates(pendingIntent);
			locationManager.removeProximityAlert(pendingIntent);
			pendingIntent.cancel();
		}
	}
    
    @Override
    public void onNewIntent(Intent intent) {
    	Log.w(LOGTAG, "intent = " + intent.getAction());
    	Log.w(LOGTAG, "intent = " + intent.getClass());
    	if(intent.getAction() == null) {
    		Log.w(LOGTAG, "now");
    		return;
    	} else if(intent.getAction().equals(Intent.ACTION_SEARCH) || intent.getAction().equals(Intent.ACTION_VIEW)) {
    		doSearchWithIntent(intent);
    	}
    }
    
    private void doSearchWithIntent(Intent queryIntent) {
    	String queryString = queryIntent.getStringExtra(SearchManager.QUERY);
		SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, "com.app.posmap.provider", PosMapProvider.DATABASE_MODE_QUERIES);
		suggestions.saveRecentQuery(queryString, null);
		searchAddress(queryString);
	}

    private void searchAddress(final String queryString) {
		progressDialog();
		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				try {
					Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.JAPAN);
					final List<Address> addressList = geocoder.getFromLocationName(queryString, 10);
					final String[] items = LocationHelper.address(addressList);
					AlertDialog.Builder builder = new AlertDialog.Builder(PosMapActivity.this);
					builder.setTitle("目的地候補一覧");
					builder.setItems(items, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, final int number) {
							progressDialog();
							new Thread() {
								@Override
								public void run() {
									Looper.prepare();
									Address address = addressList.get(number);
									selectName = items[number];
									geo(address);
									Looper.loop();
								}
							}.start();
						}
					});
					builder.setNeutralButton("もっと詳しく", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							detailAddressText = new EditText(PosMapActivity.this);
							detailAddressText.setHint("都道府県名など");
							detailAddressText.setMaxLines(1);
							detailAddressText.setOnKeyListener(new View.OnKeyListener() {
								@Override
								public boolean onKey(View v, int keyCode, KeyEvent event) {
									if(event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
										InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
										inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
										searchDetailAddress(queryString);
									}
									return false;
								}
							});
							AlertDialog.Builder builder = new AlertDialog.Builder(PosMapActivity.this);
							builder.setView(detailAddressText);
							builder.setTitle("詳細検索");
							builder.setNeutralButton("検索", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									searchDetailAddress(queryString);
								}
							});
							alertDialog = builder.create();
							alertDialog = builder.show();
						}
					});
					builder.create().show();
				} catch (IOException e) {
					e.printStackTrace();
				}
				progressDialog.dismiss();
				Looper.loop();
			}
		}.start();
	}
	
	private void searchDetailAddress(final String queryString) {
		if(alertDialog.isShowing()) {
			alertDialog.dismiss();
		}
		progressDialog();
		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				try {
					Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.JAPAN);
					final List<Address> addressList = geocoder.getFromLocationName(queryString + " " + detailAddressText.getText().toString(), 5);
					final String[] items = LocationHelper.address(addressList);
					AlertDialog.Builder builder = new AlertDialog.Builder(PosMapActivity.this);
					builder.setTitle("目的地候補一覧");
					builder.setItems(items, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, final int number) {
							progressDialog();
							new Thread() {
								@Override
								public void run() {
									Looper.prepare();
									Address address = addressList.get(number);
									selectName = items[number];
									geo(address);
									Looper.loop();
								}
							}.start();
						}
					});
					builder.create().show();
				} catch (IOException e) {
					e.printStackTrace();
				}
				progressDialog.dismiss();
				Looper.loop();
			}
		}.start();
	}
	

	
	private void geo(Address address) {
		final GeoPoint geoPoint = LocationHelper.getGeoPoint(address);
		mapController.animateTo(geoPoint);
		mapController.setZoom(mapView.getZoomLevel());
		from =FROM_SEARCH;
		AlertDialog.Builder builder = new AlertDialog.Builder(PosMapActivity.this);
		builder.setTitle("目的地に設定しますか？");
		builder.setPositiveButton("はい", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				drawable = getResources().getDrawable(R.drawable.b);
				itemizedOverlay = new MyItemizedOverlay(drawable, mapView);
				OverlayItem overlayItem = new OverlayItem(geoPoint, null, null);
				itemizedOverlay.clearOverlay();
				itemizedOverlay.addOverlay(overlayItem);
				mapView.getOverlays().add(itemizedOverlay);
				set_destination = true;
				pointToAddress(geoPoint);
				sendPendingIntent(geoPoint);
			}
		});
		builder.setNegativeButton("いいえ", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO 自動生成されたメソッド・スタブ
				
			}
		});
		builder.create().show();
		try {
			Thread.sleep(2000);
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		progressDialog.dismiss();
	}
	
	private void progressDialog() {
		progressDialog = new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage("検索中");
		progressDialog.show();
	}


	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(0, 0, 0, "検索").setIcon(android.R.drawable.ic_menu_search);
    	menu.add(0, 1, 0, "目的地設定/解除").setIcon(android.R.drawable.ic_menu_myplaces);
    	menu.add(0, 2, 0, "ログ表示").setIcon(android.R.drawable.ic_menu_view);
    	menu.add(0, 3, 0, "現在地共有").setIcon(android.R.drawable.ic_menu_share);
    	menu.add(0, 4, 0, "設定").setIcon(android.R.drawable.ic_menu_preferences);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case 0:
    		onSearchRequested();
    		break;
    	case 1:
    		if(set_destination == false) {
    			drawable = getResources().getDrawable(R.drawable.b);
    			setDestination(drawable, mapView);
    		} else {
    			itemizedOverlay.clearOverlay();
				mapView.invalidate();
				set_destination = false;
				onTap = false;
				notificationManager = null;
				destLat = 0;
				destLng = 0;
    		}
    		break;
    	case 2:
    		if(this.log_enabled != true) {
				mapView.getOverlays().add(logOverlay);
				item.setTitle(R.string.delete_log);
				log_enabled = true;
			} else {
				mapView.getOverlays().remove(logOverlay);
				item.setTitle(R.string.show_log);
				log_enabled = false;
			}
			mapView.invalidate();
    		break;
    	case 3:
    		twitter();
    		break;
    	case 4:
    		Intent intent = new Intent(PosMapActivity.this, PrefActivity.class);
    		startActivityForResult(intent, SHOW_PREFERENCES);
    	}
    	return true;
    }
    
    private void twitter() {
    	mContext = getApplicationContext();
		if(nowLat == 0 && nowLng == 0) {
			AlertDialog.Builder builder = new AlertDialog.Builder(PosMapActivity.this);
			builder.setTitle("一度現在位置情報を取得します");
			builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					SubHelper.getMyLocation(mapView);
					get_mylocation = true;
					LocationHelper.nowtweet(mContext, now_tweet_str, nowLat, nowLng);
					return;
				}
			});
			builder.create().show();
		} else {
			LocationHelper.tweet(mContext, now_tweet_str, nowLat, nowLng);
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		
		return false;
	}
	@Override
	public void onLocationChanged(Location location) {	
		double lat = location.getLatitude();
		double lng = location.getLongitude();
		this.setPosition(lat, lng);
	}
	
	private void setPosition(double lat, double lng) {
		this.setPosition(lat, lng, mapView.getZoomLevel());
	}
	
	private void setPosition(double lat, double lng, int zoom) {
		GeoPoint p = new GeoPoint((int)(lat*1E6), (int)(lng*1E6));
		nowLat = (int)(lat*1E6);
		nowLng = (int)(lng*1E6);
		logOverlay.add(p);
		if(location_enabled == true) {
		mapController.animateTo(p);
		}
		mapController.setZoom(zoom);
		myOverlay.getMyLocation();
	}
    
	
	@Override
	public void onProviderDisabled(String arg0) {
	}
	@Override
	public void onProviderEnabled(String arg0) {	
	}
	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
	}

	@Override
	public void onClick(View view) {
		mContext = getApplicationContext();
		switch(view.getId()) {
		case R.id.editText1:
			onSearchRequested();
			break;
		case R.id.button0:
			if(get_mylocation != true || set_destination != true) {
				Toast.makeText(getApplicationContext(), "目的地まで約: ???m", Toast.LENGTH_SHORT).show();
			} else {
				float[] results = {0, 0, 0};
				
				Location.distanceBetween(nowLat/1E6, nowLng/1E6, destLat/1E6, destLng/1E6, results);
				String distance = Float.toString(results[0]);
				String replaced = distance.replaceAll("\\.[0-9]*", "");
				Toast.makeText(getApplicationContext(), "目的地まで: 約 " + replaced + "m", Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.button1:
			SubHelper.mapMode(mContext, mapView);
			break;
		case R.id.button2:
			Toast.makeText(this, "現在地を取得します", Toast.LENGTH_SHORT).show();
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, this);
			SubHelper.getMyLocation(mapView);
			get_mylocation = true;
			break;
		}
	}
	
	
	private void GpsCheck() {
		if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.gps_message);
			builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent SettingGps = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					startActivity(SettingGps);
				}
			});
			builder.setNegativeButton(R.string.finish_appli, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			});
			builder.create().show();
		}
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if(event.getAction() == KeyEvent.ACTION_DOWN) {
			if(event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
				if(notificationManager != null) {
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setMessage("目的地が設定されていますが、終了してもよろしいですか？");
					builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					});
					builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							return;
						}
					});
					builder.create().show();
				} else if (notification == null) {
					finish();
				}
			}
		}
		return false;
	}
}

