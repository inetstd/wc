package com.inetstd.wc.model.dao.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.inetstd.wc.model.dao.IWCDAO;
import com.inetstd.wc.model.entities.WC;

import android.content.Context;
import android.util.Log;
import android.view.inputmethod.InputBinding;


public class WCLocalJsonDAO implements IWCDAO {
	String filePath;
	Context context;
	
	public WCLocalJsonDAO(Context ctx, String filePath) {
		this.filePath = filePath;
		this.context = ctx;
	}

	@Override
	public List<WC> getAll() {
		List<WC> wcs = new ArrayList<WC>();
		try {
			InputStream stream = this.context.getAssets().open(filePath);			
			final char[] buffer = new char[0x10000];
			StringBuilder out = new StringBuilder();
			Reader in = new InputStreamReader(stream, "UTF-8");
			try {
			  int read;
			  do {
			    read = in.read(buffer, 0, buffer.length);
			    if (read>0) {
			      out.append(buffer, 0, read);
			    }
			  } while (read>=0);
			} finally {
			  in.close();
			}			
			JSONArray jsonArray = new JSONArray(out.toString());
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jo = jsonArray.getJSONObject(i);
				
				int late6 = (int) Math.round(jo.getDouble("lat") * 1e6);
				int lnge6 = (int) Math.round(jo.getDouble("lng") * 1e6);
				WC wc = new WC();
				
				wc.setLat(late6);
				wc.setLng(lnge6);
				
				wc.setName(jo.getString("name"));							
				wcs.add(wc);				
			}
			
			
		} catch (IOException e) {			
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return wcs;
	}
	
	
}
