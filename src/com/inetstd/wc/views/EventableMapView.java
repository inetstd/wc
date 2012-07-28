package com.inetstd.wc.views;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;


public class EventableMapView extends MapView
{	
	// ------------------------------------------------------------------------
	// LISTENER DEFINITIONS
	// ------------------------------------------------------------------------

	// Change listener
	public interface OnChangeListener
	{
		public void onChange(MapView view, GeoPoint newCenter, GeoPoint oldCenter, int newZoom, int oldZoom);
	}

	// ------------------------------------------------------------------------
	// MEMBERS
	// ------------------------------------------------------------------------

	private EventableMapView mThis;
	private long mEventsTimeout = 250L; 	// Set this variable to your preferred timeout
	private boolean mIsTouched = false;
	private GeoPoint mLastCenterPosition;
	private int mLastZoomLevel;
	private Timer mChangeDelayTimer = new Timer();
	private EventableMapView.OnChangeListener mChangeListener = null;

	// ------------------------------------------------------------------------
	// CONSTRUCTORS
	// ------------------------------------------------------------------------

	public EventableMapView(Context context, String apiKey)
	{
		super(context, apiKey);
		init();
	}

	public EventableMapView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	public EventableMapView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init();		
	}

	private void init()
	{
		mThis = this;
		mLastCenterPosition = this.getMapCenter();
		mLastZoomLevel = this.getZoomLevel();
	}

	// ------------------------------------------------------------------------
	// GETTERS / SETTERS
	// ------------------------------------------------------------------------

	public void setOnChangeListener(EventableMapView.OnChangeListener l)
	{
		mChangeListener = l;
	}

	// ------------------------------------------------------------------------
	// EVENT HANDLERS
	// ------------------------------------------------------------------------

	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{		
		// Set touch internal
		mIsTouched = (ev.getAction() != MotionEvent.ACTION_UP);

		return super.onTouchEvent(ev);
	}

	@Override
	public void computeScroll()
	{
		super.computeScroll();

		// Check for change
		if (isSpanChange() || isZoomChange())
		{
			// If computeScroll called before timer counts down we should drop it and 
			// start counter over again
			resetMapChangeTimer();
		}
	}

	// ------------------------------------------------------------------------
	// TIMER RESETS
	// ------------------------------------------------------------------------

	private void resetMapChangeTimer()
	{
		mChangeDelayTimer.cancel();
		mChangeDelayTimer = new Timer();
		mChangeDelayTimer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				if (mChangeListener != null) mChangeListener.onChange(mThis, getMapCenter(), mLastCenterPosition, getZoomLevel(), mLastZoomLevel);
				mLastCenterPosition = getMapCenter();
				mLastZoomLevel = getZoomLevel();
			}
		}, mEventsTimeout);
	}

	// ------------------------------------------------------------------------
	// CHANGE FUNCTIONS
	// ------------------------------------------------------------------------

	private boolean isSpanChange()
	{
		return !mIsTouched && !getMapCenter().equals(mLastCenterPosition);
	}

	private boolean isZoomChange()
	{
		return (getZoomLevel() != mLastZoomLevel);
	}


	public double distTo(GeoPoint geoPoint) {
		double lat1 = (double) getMapCenter().getLatitudeE6() / (double) 1e6; 
		double lng1 = (double) getMapCenter().getLongitudeE6() / (double) 1e6;
		double lat2 = (double) geoPoint.getLatitudeE6() / (double) 1e6;
		double lng2 = (double) geoPoint.getLongitudeE6() / (double) 1e6;
		
	//	Log.i("test", lat1 + ":" + lng2 + "   " + lat2 + ":" + lng2);
		double earthRadius = 3958.75;
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);
		double sindLat = Math.sin(dLat / 2);
		double sindLng = Math.sin(dLng / 2);
		double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		double dist = earthRadius * c;

		return dist;
	}

}