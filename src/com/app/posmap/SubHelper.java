package com.app.posmap;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class SubHelper extends PosMapActivity {

	public static void mapMode(Context context, MapView mapView) {
		if (mapView.isSatellite()) {
            mapView.setSatellite(false);
            Toast.makeText(context, "衛生写真モード: OFF", Toast.LENGTH_SHORT).show();
        } else {
            mapView.setSatellite(true);
            Toast.makeText(context, "衛生写真モード: ON", Toast.LENGTH_SHORT).show();
        }
	}

	public static void getMyLocation(final MapView mapView) {
		myOverlay.enableMyLocation();
		myOverlay.enableCompass();
		myOverlay.runOnFirstFix(new Runnable() {
			public void run() {
				mapController.animateTo(myOverlay.getMyLocation());
				mapController.setZoom(mapView.getZoomLevel());
				nowLat = myOverlay.getMyLocation().getLatitudeE6();
				nowLng = myOverlay.getMyLocation().getLongitudeE6();
			}
		});	
	}

	public static void setIcon(MapView mapView, GeoPoint point) {
		// TODO 自動生成されたメソッド・スタブ
		OverlayItem overlayItem = new OverlayItem(point, null, null);
		itemizedOverlay.clearOverlay();
		itemizedOverlay.addOverlay(overlayItem);
		mapController.animateTo(point);
		mapView.getOverlays().add(itemizedOverlay);
		mapView.invalidate();
	}

	public static void setNotification(Context context) {
		// TODO 自動生成されたメソッド・スタブ
		notification = new Notification();
		notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		notification.icon = R.drawable.notification;
		notification.tickerText = "お知らせ";
		notification.when = System.currentTimeMillis();
		PendingIntent pendingIntent2 = PendingIntent.getActivity(context, 0, null, 0);
		notification.setLatestEventInfo(context, "CocoIkuMap", "目的地: " + destName, pendingIntent2);
		notificationManager.notify(1, notification);
	}
}
