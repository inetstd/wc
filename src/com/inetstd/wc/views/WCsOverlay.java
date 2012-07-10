package com.inetstd.wc.views;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;
import com.inetstd.wc.model.entities.WC;


public class WCsOverlay extends ItemizedOverlay<OverlayItem> {

	List<OverlayItem> overlays = new ArrayList<OverlayItem>();
	Drawable defaultMarker;
	Context context;

	@Override
	protected boolean onTap(int index) {
		OverlayItem item = overlays.get(index);
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle(item.getTitle());
		dialog.setMessage(item.getSnippet());
		dialog.show();
		return true;
	}

	public WCsOverlay(Drawable pdefaultMarker, Context context) {		
		super(boundCenterBottom(pdefaultMarker));		
		this.defaultMarker = pdefaultMarker;
		this.context = context;
	}

	@Override
	protected synchronized OverlayItem createItem(int location) {
		return overlays.get(location);
	}

	@Override
	public synchronized int size() {
		return overlays.size();
	}


	public synchronized void updateLocations(List<WC> newWcs) {
		for (WC wc : newWcs) {			
			GeoPoint point = new GeoPoint(wc.getLat(), wc.getLng());
			Log.i("WCsOverlay", "create " + point.getLatitudeE6() + " " + point.getLongitudeE6());
			OverlayItem overlayitem = new OverlayItem(point, wc.getName(), "Service: " + wc.getConfort());
			overlayitem.setMarker(defaultMarker);
			overlays.add(overlayitem);
		};

		populate(); // this was missing
	}

}
