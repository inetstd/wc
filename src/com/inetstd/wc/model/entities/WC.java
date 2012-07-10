package com.inetstd.wc.model.entities;

import com.inetstd.wc.map.utils.GeoItem;


public class WC{
	
	String name;
	int confort;
	int lat;
	int lng;
	
	
	public String getName() {
	
		return name;
	}
	
	public void setName(String name) {
	
		this.name = name;
	}
	
	public int getConfort() {
	
		return confort;
	}
	
	public void setConfort(int confort) {
	
		this.confort = confort;
	}
	
	public int getLat() {
	
		return lat;
	}
	
	public void setLat(int lat) {
	
		this.lat = lat;
	}
	
	public int getLng() {
	
		return lng;
	}
	
	public void setLng(int lng) {
	
		this.lng = lng;
	}
}
