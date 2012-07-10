package com.inetstd.wc.async;

import java.util.List;

import com.inetstd.wc.model.dao.IWCDAO;
import com.inetstd.wc.model.dao.impl.WCLocalJsonDAO;
import com.inetstd.wc.model.entities.WC;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;


public class WCAsyncLoader extends AsyncTaskLoader<List<WC>> {

	IWCDAO iwcdao;
	
	public WCAsyncLoader(Context context, String file) {
		
		super(context); 
		iwcdao = new WCLocalJsonDAO(context, file);
	}

	@Override
	public List<WC> loadInBackground() {	
		Log.i("WCAsyncLoader", "loadInBackground");
		return iwcdao.getAll();
	}
	
	
	@Override
	protected void onStartLoading() {
		Log.i("WCAsyncLoader", "onStartLoading >> forceLoad");
		forceLoad();
	}
	
	
}
