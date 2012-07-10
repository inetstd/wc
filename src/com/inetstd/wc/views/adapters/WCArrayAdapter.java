package com.inetstd.wc.views.adapters;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.inetstd.wc.R;
import com.inetstd.wc.model.entities.WC;


public class WCArrayAdapter extends ArrayAdapter<WC> {
	
	List<WC> wcs;
	Context context;
	
	static int layoutItemResource = R.layout.wc_list_item;

	public WCArrayAdapter(Context context, List<WC> list) {
		super(context, layoutItemResource);
		wcs = list;
	}
	
	class Holder {
		TextView name;
		TextView distance;
	}
	

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder = null;
		if (convertView == null) {
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
			convertView = inflater.inflate(layoutItemResource, parent, false);
            
            holder = new Holder();
            //holder.imgIcon = (ImageView)convertView.findViewById(R.id.imgIcon);
            holder.name = (TextView)convertView.findViewById(R.id.name);
            holder.distance = (TextView)convertView.findViewById(R.id.distance);
            
            convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		
		WC wc = getItem(position);
        holder.name.setText(wc.getName());
        holder.distance.setText(wc.getConfort() + " cmf");
		
		return convertView;
	}
	
	@Override
	public int getCount() {
		return wcs.size();				
	}
	
	
	@Override
	public WC getItem(int position) {	
		return wcs.get(position);
	}
	
}
