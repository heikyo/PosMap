package com.app.posmap;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class LogOverlay extends Overlay {
	
	private Paint linePaint;
	private List<GeoPoint> points;
	
	public LogOverlay() {
		this.points = new ArrayList<GeoPoint>();
		this.linePaint = new Paint();
		linePaint.setARGB(255, 255, 0, 0);
		linePaint.setStrokeWidth(2);
		linePaint.setDither(true);
		linePaint.setStyle(Style.FILL);
		linePaint.setAntiAlias(true);
		linePaint.setStrokeJoin(Paint.Join.ROUND);
		linePaint.setStrokeCap(Paint.Cap.ROUND);
	}
	
	public void add(GeoPoint p) {
		points.add(p);
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if(points.size()<2) return;
		Point p_a = new Point();
		Point p_b = new Point();
		for(int i = 0; i < points.size()-1; i++) {
			mapView.getProjection().toPixels(points.get(i), p_a);
			mapView.getProjection().toPixels(points.get(i+1), p_b);
			canvas.drawLine(p_a.x, p_a.y, p_b.x, p_b.y, linePaint);
		}
	}
	
	public int size() {
		return points.size();
	}
	
	public List<GeoPoint> getPoints() {
		return this.points;
	}
	
}
