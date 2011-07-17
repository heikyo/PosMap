package com.app.posmap;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class MyItemizedOverlay extends ItemizedOverlay<OverlayItem>{
	
	private ArrayList<OverlayItem> itemOverlays = new ArrayList<OverlayItem>();
	Context context;
	
	public MyItemizedOverlay(Drawable defaultMarker, Context context) {
		super(boundCenterBottom(defaultMarker));
		this.context = context;
	}

	public MyItemizedOverlay(Drawable defaultMarker, MapView mapView) {
		super(boundCenterBottom(defaultMarker));
		context = mapView.getContext();
	}

	@Override
	protected OverlayItem createItem(int i) {
		return itemOverlays.get(i);
	}

	@Override
	public int size() {
		return itemOverlays.size();
	}
	
	@Override
	protected boolean onTap(int index) {
		super.onTap(index);
		return true;
	}
	
	public void addOverlay(OverlayItem overlay) {
		itemOverlays.add(overlay);
		populate();
	}
	
	public void clearOverlay() {
		itemOverlays.clear();
		populate();
	}

}
