package com.inetstd.wc;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.inetstd.wc.async.WCAsyncLoader;
import com.inetstd.wc.model.entities.WC;
import com.inetstd.wc.views.EventableMapView;
import com.inetstd.wc.views.MapViewPager;
import com.inetstd.wc.views.WCsOverlay;
import com.inetstd.wc.views.adapters.MainPagerAdapter;

public class LandingActivity extends FragmentActivity implements LoaderCallbacks<List<WC>>,
		MainPagerAdapter.MapViewConnector {

	/** Called when the activity is first created. */

	public static final String LOG_TAG = LandingActivity.class.getSimpleName();

	LocationManager locationManager;

	MapViewPager viewPager;
	MainPagerAdapter mainPagerAdapter;
	WCsOverlay wcsOverlay;
	EventableMapView mapView;
	List<WC> wcs = new ArrayList<WC>();

	LocationListener locationListener = new LocationListener() {

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

			Log.i(LOG_TAG, "status changed " + provider + " " + status);
		}


		@Override
		public void onProviderEnabled(String provider) {

			Log.i(LOG_TAG, "onProviderEnabled " + provider);
		}


		@Override
		public void onProviderDisabled(String provider) {

			Log.i(LOG_TAG, "onProviderDisabled " + provider);
		}


		@Override
		public void onLocationChanged(Location location) {

			Log.i(LOG_TAG, "onLocationChanged " + location.getAccuracy());
			if ( mapView != null ) {
				Log.i(LOG_TAG, "set location!! ");
				GeoPoint geoPoint = new GeoPoint((int)Math.round(location.getLatitude() * 1E6),
						(int)Math.round(location.getLongitude() * 1E6));
				mapView.getController().setCenter(geoPoint);
				mapView.getController().setZoom(18);

			}
		}
	};


	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.tab_content);
//		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		mainPagerAdapter = new MainPagerAdapter(this, this);
		viewPager = (MapViewPager)findViewById(R.id.viewpager);
		viewPager.setAdapter(mainPagerAdapter);

		locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);

		Log.i(LOG_TAG, "try to get location>>> ");
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
				locationListener);

//		for (int i = 1; i <= 3; i++) {
//			ActionBar.Tab tab = getSupportActionBar().newTab();
//			tab.setText("Tab " + i);
//			tab.setTabListener(this);
//			getSupportActionBar().addTab(tab);
//		}

	}


//
//	@Override
//	protected boolean isRouteDisplayed() {
//
//		return false;
//	}

	@Override
	public Loader<List<WC>> onCreateLoader(int arg0, Bundle arg1) {

		Log.i(LOG_TAG, "create loader for wcs");
		return new WCAsyncLoader(getApplicationContext(), getString(R.string.wc_data_file_path));
	}


	@Override
	public void onLoadFinished(Loader<List<WC>> arg0, List<WC> pwcs) {

		wcs.clear();
		wcs.addAll(pwcs);
		//Log.i(LOG_TAG, "recieved wcs " + wcs.size());

		wcsOverlay.updateLocations(wcs);

		redrawList();
	}


	@Override
	public void onLoaderReset(Loader<List<WC>> arg0) {

	}


	@Override
	protected boolean isRouteDisplayed() {

		// TODO Auto-generated method stub
		return false;
	}


	private void redrawList() {

		for (WC wc : wcs) {
			double distance = mapView.distTo(new GeoPoint(wc.getLat(), wc.getLng()));
			if ( distance < 0.5 ) {
				Log.i(LOG_TAG, "wc " + wc.getName());
			}
			wc.setDistance(distance);
		}

		Collections.sort(wcs, new Comparator<WC>() {

			@Override
			public int compare(WC lhs, WC rhs) {

				if ( lhs.getDistance() < rhs.getDistance() ) return 1;
				else return -1;
			}
		});

	}


	@Override
	public void onMapLoaded(MapView mapView) {

		Log.i(LOG_TAG, "onMapLoaded got map instance");
		this.mapView = (EventableMapView)mapView;
		this.mapView.setOnChangeListener(new EventableMapView.OnChangeListener() {

			@Override
			public void onChange(MapView view, GeoPoint newCenter, GeoPoint oldCenter, int newZoom,
					int oldZoom) {

				Log.i(LOG_TAG,
						"onChange : " + newCenter.getLatitudeE6() + " "
								+ newCenter.getLongitudeE6() + " :: " + newZoom + " " + oldZoom);
				redrawList();

			}
		});

		this.mapView.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if ( event.getAction() == MotionEvent.ACTION_DOWN ) {
					viewPager.setPagingEnabled(false);
				} else if ( event.getAction() == MotionEvent.ACTION_UP ) {
					viewPager.setPagingEnabled(true);
				}
				return false;
			}
		});

		this.viewPager.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				Log.i("pager", "p touch !!!!!!!!!!! " + event.getAction());
				return false;
			}
		});

		wcsOverlay = new WCsOverlay(getResources().getDrawable(R.drawable.pin_icon), this);
		mapView.getOverlays().add(wcsOverlay);

		Log.i(LOG_TAG, "get last known location>>> ");
		Location lastKnownLocation = locationManager
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if ( lastKnownLocation == null ) {
			Log.i(LOG_TAG, "last is null");
		} else {
			Log.i(LOG_TAG, "set last known location>>> ");
			locationListener.onLocationChanged(lastKnownLocation);
		}

		Log.i(LOG_TAG, "start loading wcs");
		getSupportLoaderManager().restartLoader(123123, null, this);

	}


	@Override
	public void onMapUnLoaded(MapView mapView) {

	}

}
