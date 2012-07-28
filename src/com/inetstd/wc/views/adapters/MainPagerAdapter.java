 package com.inetstd.wc.views.adapters;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.maps.MapView;
import com.inetstd.wc.R;


public class MainPagerAdapter extends PagerAdapter {
	
	public interface MapViewConnector {
		void onMapLoaded(MapView mapView);
		void onMapUnLoaded(MapView mapView);		
	}

	Context context;
	
	MapViewConnector mapViewConnector;

	Map<Integer, View> cache = new HashMap<Integer, View>();
		
	
	public MainPagerAdapter(Context context, MapViewConnector mapViewConnector) {
		this.context = context;
		this.mapViewConnector = mapViewConnector;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		Log.i("Pager", "new page " + position);
		View view = cache.get(position);
			
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
			switch ( position ) {
				case 0:
					view = inflater.inflate(R.layout.tab_item_list, null);
					break;
				case 1:
					Log.i("Pager", "instantiate map");
					view = inflater.inflate(R.layout.tab_item_map, null);
					mapViewConnector.onMapLoaded((MapView) view.findViewById(R.id.map));
					break;
				case 2:
					view = inflater.inflate(R.layout.tab_item_list, null);
					break;

				default:
					break;
			}		
			cache.put(position, view);
		}
		Log.i("Pager", "container " + container);
		container.addView(view, 0);
		return view	;
	}

	@Override
	public int getCount() {

		return 3;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {

		return arg0 == arg1;
	}



	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);			
	}

}
