package com.app.posmap;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.location.Address;

import com.google.android.maps.GeoPoint;

public class LocationHelper {
	
	private LocationHelper() {
	}
	
	public static final String[] address (List<Address> addressList) {
		String[] items = new String[addressList.size()];
		for(int i = 0; i <= addressList.size() -1; i++) {
			Address address = addressList.get(i);
			String item;
			if (address.getMaxAddressLineIndex() == 0) {
				item = address.getAddressLine(0);
				items [i] = item;
			} else {
				for(int j = 1; j < address.getMaxAddressLineIndex()+1; j++) {
					item = address.getAddressLine(j);
					items[i] = item;
				}
			}
		}
		return items;
	}
	
	public static final GeoPoint getGeoPoint(Address address) {
		int lat = (int)(address.getLatitude()*1E6);
		int lng = (int)(address.getLongitude()*1E6);
		return new GeoPoint(lat, lng);
	}

	public static void tweet(Context context, String str, int lat, int lng) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SEND);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setType("text/plain");
		String url = "http://maps.google.co.jp/maps?q=" + lat/1E6 + ",+" + lng/1E6;
		String shortUrl = Bitly.shorten(url);
		intent.putExtra(Intent.EXTRA_TEXT, str + ": " + shortUrl);
		context.startActivity(intent);
	}

	public static String getDestName(Address address, int from) {
		int addLength = address.getMaxAddressLineIndex();
		String destName = null;
		if(from == PosMapActivity.FROM_TAP) {
			switch(addLength) {
			case 0:
				destName = address.getAddressLine(0);
				break;
			case 1:
				destName = address.getAddressLine(1);
				break;
			case 2:
				destName = address.getAddressLine(2);
				break;
			}
		} else {
			switch(address.getMaxAddressLineIndex()) {
			case 0:
				destName = address.getAddressLine(0);
				break;
			case 1:
			case 2:
				destName = PosMapActivity.selectName;
				break;
			}
		}
		return destName;
	}

	public static void nowtweet(Context context, String str, int lat, int lng) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SEND);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT, str + ": " 
				+ "http://maps.google.co.jp/maps?q=" + lat/1E6 + ",+" + lng/1E6
				+ "+(%E7%8F%BE%E5%9C%A8%E5%9C%B0)&iwloc=A&hl=ja");
		context.startActivity(intent);
	}
}
