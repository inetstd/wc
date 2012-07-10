/*
 * Copyright (C) 2009 Huan Erdao
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.inetstd.wc.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.graphics.Point;
import android.os.Handler;


import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;
import com.inetstd.wc.map.utils.GeoBounds;
import com.inetstd.wc.map.utils.GeoItem;

/**
 * Class for Clustering geotagged content.
 * this clustering came from "markerclusterer" which is available as opensource at
 * http://code.google.com/p/gmaps-utility-library/
 * this is android ported version with modification to fit to the application
 * @author Huan Erdao
 */
public class GeoClusterer {
	
	/** grid size for clustering(dip). */
	protected int GRIDSIZE = 56;

	/** screen density for multi-resolution
	 *	get from contenxt.getResources().getDisplayMetrics().density;  */
	protected float screenDensity_ = 1.0f;
	
	/** MapView object. */
	protected final MapView mapView_;
	/** GeoItem ArrayList object to be shown. */
	protected List<GeoItem> items_ = new ArrayList<GeoItem>();
	/** GeoItem ArrayList object that are out of viewport to be clustered. */
	protected List<GeoItem> leftItems_ = new ArrayList<GeoItem>();
	/** Clustered object list. */
	protected List<GeoCluster> clusters_ = new ArrayList<GeoCluster>();
	/** MarkerBitmap object for marker icons. */
	protected final List<MarkerBitmap> markerIconBmps_;
	/** selected cluster object. */
	protected GeoCluster selcluster_ = null;
	/** check counter for tapping all cluster object. */
	protected int tapCheckCount_ = 0;
	/** GeoBound to check moves of the map view. */
	protected GeoBounds savedBounds_;
	/** flag for detecting map moves. true if map is moving or zooming. */
	protected boolean isMoving_;
	/** handler to initiate moveend/zoomend event and reset view. */
	protected Handler handler_;
	
	/**
	 * @param mapView MapView object.
	 * @param markerIconBmps MarkerBitmap objects for icons.
	 * @param screenDensity Screen Density.
	 */
	public GeoClusterer(MapView mapView, List<MarkerBitmap> markerIconBmps, float screenDensity){
		mapView_ = mapView;
		markerIconBmps_ = markerIconBmps;
		screenDensity_ = screenDensity;
		handler_ = new Handler();
		isMoving_ = false;
	}

	/**
	 * add item and do clustering.
	 * NOTE: this method will not redraw screen. after adding all items,
	 * you must call redraw() method.
	 * @param item GeoItem to be clustered.
	 */
	public void addItem(GeoItem item) {
		// if not in viewport, add to leftItems_
		if(!isItemInViewport(item)) {
			leftItems_.add(item);
			return;
		}
		// else add to items_;
		items_.add(item);
		int length = clusters_.size();
		GeoCluster cluster = null;
		Projection proj = mapView_.getProjection();
		Point pos = proj.toPixels(item.getLocation(), null);
		// check existing cluster
		for(int i=length-1; i>=0; i--) {
			  cluster = clusters_.get(i);
			  GeoPoint gpCenter = cluster.getLocation();
			  if(gpCenter == null)
				  continue;
			  Point ptCenter = proj.toPixels(gpCenter,null);
			  // find a cluster which contains the marker.
			  final int GridSizePx = (int) (GRIDSIZE * screenDensity_ + 0.5f);
			  if(pos.x >= ptCenter.x - GridSizePx && pos.x <= ptCenter.x + GridSizePx &&
				  pos.y >= ptCenter.y - GridSizePx && pos.y <= ptCenter.y + GridSizePx) {
				  cluster.addItem(item);
				  return;
			  }
		}
		// No cluster contain the marker, create a new cluster.
		createCluster(item);
	}

	/**
	 * Create Cluster Object.
	 * override this method, if you want to use custom GeoCluster class.
	 * @param item GeoItem to be set to cluster.
	 */
	 public void createCluster(GeoItem item){
		 GeoCluster cluster = new GeoCluster(this);
		 cluster.addItem(item);
		 clusters_.add(cluster);
	 }

	/**
	 * get current GeoItems list.
	 * @return GeoItems list.
	 */
	public final List<GeoItem> getItems(){
		return items_;
	}

	/**
	 * redraws clusters
	 */
	public void redraw(){
		for(int i=0; i<clusters_.size(); i++) {
			clusters_.get(i).redraw();
		}
	}

	/**
	 * zoom in with selected cluster to be fit within current viewport.
	 */
	public void zoomInFixing(){
		if(selcluster_!=null){
			GeoPoint gpt = selcluster_.getSelectedItemLocation();
			if(getCurBounds().isInBounds(gpt)){
				Projection pro = mapView_.getProjection();
				Point ppt = pro.toPixels(gpt, null);
				mapView_.getController().zoomInFixing(ppt.x, ppt.y);
			}
			else{
				mapView_.getController().zoomIn();
			}
		}
		else{
			mapView_.getController().zoomIn();
		}
	}
	
	/**
	 * check if the item is within current viewport.
	 * @return true if item is within viewport.
	 */
	protected final boolean isItemInViewport(GeoItem item){
		savedBounds_ = getCurBounds();
		return savedBounds_.isInBounds(item.getLocation());
	}

	/**
	 * get current Bound
	 * @return current GeoBounds
	 */
	protected final GeoBounds getCurBounds(){
		Projection proj = mapView_.getProjection();
		return new GeoBounds(proj.fromPixels(0,0),proj.fromPixels(mapView_.getWidth(),mapView_.getHeight()));
	}

	/**
	 * get clusters within current viewport.
	 * @return clusters within current viewport.
	 */
	protected List<GeoCluster> getClustersInViewport() {
		GeoBounds curBounds = getCurBounds();
		ArrayList<GeoCluster> clusters = new ArrayList<GeoCluster>();
		for(int i=0; i<clusters_.size(); i++) {
			GeoCluster cluster = clusters_.get(i);
			  if(cluster.isInBounds(curBounds)) {
				  clusters.add(cluster);
			  }
		}
		return clusters;
	}

	/**
	 * add items that were not clustered in last clustering.
	 */
	protected void addLeftItems() {
		if(leftItems_.size()==0){
			return;
		}
		ArrayList<GeoItem> currentLeftItems = new ArrayList<GeoItem>();
		currentLeftItems.addAll(leftItems_);
		leftItems_.clear();
		for(int i=0; i<currentLeftItems.size(); i++) {
			addItem(currentLeftItems.get(i));
		}
	}

	/**
	 * re-add items for clustering.
	 * @param items GeoItem list to be clustered.
	 */
	protected void reAddItems(List<GeoItem> items) {
		int len = items.size();
		for(int i=len-1; i>=0; i--) {
			addItem(items.get(i));
		}
		addLeftItems();
	}

	/**
	 * reset current viewport.
	 * @return current selected cluster object, if null it means no cluster is selected.
	 */
	public GeoCluster resetViewport() {
		List<GeoCluster> clusters = getClustersInViewport();
		List<GeoItem> tmpItems = new ArrayList<GeoItem>();
		int removed = 0;
		for(int i=0; i<clusters.size(); i++) {
			GeoCluster cluster = clusters.get(i);
			int oldZoom = cluster.getZoomLevel();
			int curZoom = mapView_.getZoomLevel();
			// If the cluster zoom level changed then destroy the cluster and collect its markers.
			if(curZoom != oldZoom) {
				tmpItems.addAll(cluster.getItems());
				cluster.clear();
				removed++;
				for(int j=0; j<clusters_.size(); j++) {
					if(cluster == clusters_.get(j)) {
						clusters_.remove(j);
					}
				}
			}
		}
		reAddItems(tmpItems);
		redraw();
		// Add the markers collected into marker cluster to reset
		if(removed>0){
			GeoCluster cluster = null;
			for(int i=0; i<clusters_.size(); i++) {
				cluster = clusters_.get(i);
				if( cluster.isSelected() )
					return cluster;
			}
			for(int i=0; i<items_.size(); i++) {
				items_.get(i).setSelect(false);
			}
			return null;
		}
		return selcluster_;
	}

	/**
	 * clears selected state.
	 */
	public void clearSelect(){
		for(int i=0; i<clusters_.size(); i++) {
			if(selcluster_==clusters_.get(i)) {
				clusters_.get(i).clearSelect();
			}
		}
	}

	/**
	 * Hooking draw event from ClusterMarker to detect zoom/move event.
	 * hope there will be event notification for android equivalent to
	 * javascriptin the future....
	 */
	public void onNotifyDrawFromCluster(){
		// ignore if it is already recognized as moving state
		if(isMoving_)
			return;
		GeoBounds curBnd = getCurBounds();
		// checking bounds if it is moving or not.
		if( !savedBounds_.isEqual(curBnd) ){
			isMoving_ = true;
			savedBounds_ = curBnd;
			Timer timer = new Timer(true);
			timer.schedule(
				new TimerTask() {
					public void run() {
						GeoBounds curBnd = getCurBounds();
						// if there is no more moving, reset the viewport
						if( savedBounds_.isEqual(curBnd) ){
							isMoving_ = false;
							this.cancel();
							handler_.post( new Runnable() {
								public void run() {
									resetViewport();
								}
							});
						}
						savedBounds_ = curBnd;
					}
				}, 500, 500
			);
		}
	}

	/**
	 * onTap call from Cluster layer.
	 * this method will be called number of times equals to size of clusters.
	 * check isTapped to know which cluster was tapped.
	 * @param caller cluster object called this.
	 * @param isTapped if true, tapped.
	 */
	public void onTapCalledFromCluster(GeoCluster caller, boolean isTapped) {
		// if tapped, set selcluster_ to caller
		if(isTapped){
			if(selcluster_ == caller)
				return;
			clearSelect();
			selcluster_ = caller;
		}
		else{
			tapCheckCount_++;
			if( tapCheckCount_ == clusters_.size() ){
				tapCheckCount_ = 0;
				/* TODO : do something if no marker was tapped
				 */
			}
		}
	}

	/**
	 * clear cluster event handler.
	 * @param caller cluster object called this.
	 */
	public void onNotifyClearSelectFromCluster(GeoCluster caller){
		if(selcluster_ == caller){
			selcluster_.clearSelect();
			selcluster_ = null;
			return;
		}
	}

	/**
	 * get cluster
	 * @param pos position of cluster.
	 * @return GeoCluster object. null if index out of bounds
	 */
	public GeoCluster getCluster(int pos) {
		if( pos < 0 || pos > clusters_.size() )
			return null;
		return clusters_.get(pos);
	}
	
	/**
	 * GeoCluster class.
	 * contains single marker object(ClusterMarker). mostly wraps methods in ClusterMarker.
	 */
	public class GeoCluster {
		/**	GeoClusterer object	 */
		private final GeoClusterer clusterer_;
		/**	center of cluster */
		protected GeoPoint center_;
		/**	list of GeoItem within cluster */
		private List<GeoItem> items_ = new ArrayList<GeoItem>();
		/** ClusterMarker object */
		protected ClusterMarker clusterMarker_;
		/** zoomlevel at the point Cluster was made */
		private int zoom_;

		/**
		 * @param clusterer GeoClusterer object.
		 */
		public GeoCluster(GeoClusterer clusterer){
			clusterer_ = clusterer;
			clusterMarker_ = null;
			zoom_ = mapView_.getZoomLevel();
		}
		
		/**
		 * add item to cluster object
		 * @param item GeoItem object to be added.
		 */
		public void addItem(GeoItem item){
			if(center_ == null){
				center_ = item.getLocation();
			}
			items_.add(item);
		}
		
		/**
		 * get center of the cluster.
		 * @return center of the cluster in GeoPoint.
		 */
		public GeoPoint getLocation(){
			return center_;
		}

		/**
		 * get selected item's location.
		 * @return selected item's location.
		 */
		public GeoPoint getSelectedItemLocation(){
			if(clusterMarker_==null)
				return null;
			return clusterMarker_.getSelectedItemLocation();
		}

		/**
		 * clears selected state.
		 */
		public void clearSelect(){
			clusterMarker_.clearSelect();
		}

		/**
		 * clear cluster event handler.
		 */
		public void onNotifyClearSelectFromMarker(){
			clusterer_.onNotifyClearSelectFromCluster(this);
		}

		/**
		 * check if the cluster is selected.
		 * @return true if selected.
		 */
		public boolean isSelected(){
			return clusterMarker_.isSelected();
		}

		/**
		 * get zoomlevel.
		 * @return zoom level of the cluster.
		 */
		public int getZoomLevel(){
			return zoom_;
		}
		
		/**
		 * get list of GeoItem.
		 * @return list of GeoItem within cluster.
		 */
		public List<GeoItem> getItems(){
			return items_;
		}

		/**
		 * Hooking Overlay.draw event to detect if it is moving/zooming.
		 * calls GeoCluster.onNotifyDraw.
		 */
		public void onNotifyDrawFromMarker(){
			clusterer_.onNotifyDrawFromCluster();
		}

		/**
		 * Hooking Tap event from ClusterMarker layer.
		 * @param flg true if the tap event was captured, else false.
		 */
		public void onTapCalledFromMarker(boolean flg) {
			clusterer_.onTapCalledFromCluster(this,flg);
		}
		
		/**
		 * clears cluster object.
		 */
		public void clear() {
			if(clusterMarker_ != null) {
				List<Overlay> mapOverlays = mapView_.getOverlays();
				if(mapOverlays.contains(clusterMarker_)){
					mapOverlays.remove(clusterMarker_);
				}
				clusterMarker_ = null;
			}
			items_ = null;
		}

		/**
		 * redraw cluster. if needed create ClusterMarker object.
		 */
		public void redraw(){
			if(!isInBounds(clusterer_.getCurBounds())) {
				return;
			}
			if(clusterMarker_ == null) {
				clusterMarker_ = new ClusterMarker(this,markerIconBmps_,screenDensity_);
				List<Overlay> mapOverlays = mapView_.getOverlays();
				mapOverlays.add(clusterMarker_);
			}
		}

		/**
		 * check if the GeoBounds are within cluster.
		 * @return true if bounds are within this cluster size.
		 */
		protected boolean isInBounds(GeoBounds bounds) {
			if(center_ == null) {
				return false;
			}
			Projection pro = mapView_.getProjection();
			Point nw = pro.toPixels(bounds.getNorthWest(),null);
			Point se = pro.toPixels(bounds.getSouthEast(),null);
			Point centxy = pro.toPixels(center_,null);
			boolean inViewport = true;
			int GridSizePx = (int) (GRIDSIZE * screenDensity_ + 0.5f);
			if(zoom_ != mapView_.getZoomLevel()) {
				int diff = mapView_.getZoomLevel() - zoom_;
				GridSizePx = (int) (Math.pow(2, diff) * GridSizePx);
			}
			if(nw.x != se.x && (centxy.x + GridSizePx < nw.x || centxy.x - GridSizePx > se.x)) {
				inViewport = false;
			}
			if(inViewport && (centxy.y + GridSizePx < nw.y || centxy.y - GridSizePx > se.y)) {
				inViewport = false;
			}
			return inViewport;
		}
	};
}
