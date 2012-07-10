package com.inetstd.wc;

import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.inetstd.wc.async.WCAsyncLoader;
import com.inetstd.wc.map.GeoClusterer;
import com.inetstd.wc.map.MarkerBitmap;
import com.inetstd.wc.model.entities.WC;
import com.inetstd.wc.views.MapViewPager;
import com.inetstd.wc.views.WCsOverlay;
import com.inetstd.wc.views.adapters.MainPagerAdapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;

public class LandingActivity extends SherlockFragmentActivity implements ActionBar.TabListener, LoaderCallbacks<List<WC>>, MainPagerAdapter.MapViewConnector {
    /** Called when the activity is first created. */
	
	public static final String LOG_TAG = LandingActivity.class.getSimpleName();
	
	LocationManager locationManager;
	
	MapViewPager viewPager;
	MainPagerAdapter mainPagerAdapter;
	WCsOverlay wcsOverlay;
	MapView mapView;
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
			if (mapView != null) {
				Log.i(LOG_TAG, "set location!! ");
				GeoPoint geoPoint = new GeoPoint((int)Math.round(location.getLatitude() * 1E6), (int)Math.round(location.getLongitude() * 1E6));
				mapView.getController().setCenter(geoPoint);
				mapView.getController().setZoom(18);
			}
		}
	};
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Sherlock); //Used for theme switching in samples
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.tab_content);        
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        mainPagerAdapter = new MainPagerAdapter(this, this);               
        viewPager = (MapViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(mainPagerAdapter);
        
        
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        
        Log.i(LOG_TAG, "try to get location>>> ");
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                            
        for (int i = 1; i <= 3; i++) {
            ActionBar.Tab tab = getSupportActionBar().newTab();
            tab.setText("Tab " + i);
            tab.setTabListener(this);
            getSupportActionBar().addTab(tab);
        }              
    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction transaction) {
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction transaction) {
     //   mSelected.setText("Selected: " + tab.getText());
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction transaction) {
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
		Log.i(LOG_TAG, "recieved wcs " + wcs.size());
	
		/*
		List<MarkerBitmap> markerIconBmps_ = new ArrayList<MarkerBitmap>();
		
		markerIconBmps_.add(
				new MarkerBitmap(
						BitmapFactory.decodeResource(getResources(), R.drawable.pin_icon),
						BitmapFactory.decodeResource(getResources(), R.drawable.pin_icon),
						new Point(20,20),
						14,
						10)
				);
		// large icon. 100 will be ignored.
		markerIconBmps_.add(
				new MarkerBitmap(
						BitmapFactory.decodeResource(getResources(), R.drawable.pin_icon),
						BitmapFactory.decodeResource(getResources(), R.drawable.pin_icon),
						new Point(28,28),
						16,
						100)
				);
		
		
		float screenDensity = this.getResources().getDisplayMetrics().density;
		GeoClusterer clusterer = new GeoClusterer(mapView, markerIconBmps_,screenDensity);
		
		// add geoitems for clustering
		for(int i = 0; i < wcs.size(); i++) {
			clusterer.addItem(wcs.get(i));
		}		
		clusterer.redraw();
		*/
		wcsOverlay.updateLocations(wcs);
		
	
	}
		
	@Override
	public void onLoaderReset(Loader<List<WC>> arg0) {
	}

	@Override
	protected boolean isRouteDisplayed() {

		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onMapLoaded(MapView mapView) {
		Log.i(LOG_TAG, "onMapLoaded got map instance");
		this.mapView = mapView;
	
		
		this.mapView.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					viewPager.setPagingEnabled(false);
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
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
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (lastKnownLocation == null) {
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
