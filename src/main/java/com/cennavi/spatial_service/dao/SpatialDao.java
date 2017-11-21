package com.cennavi.spatial_service.dao;

import java.util.List;
import java.util.Map;

public interface SpatialDao {

	public List<Map<String,Object>> poiSearchByCity(String q,String name,int page_num,int page_size);
	
	public List<Map<String,Object>> poiSearchByBoundary(String q,
			double min_lng, double min_lat, double max_lng, double max_lat,
			int page_num, int page_size);
	
	public Map<String, Object> getRegionCenter(String nameOrcode);
	
	public Map<String, Object> getRegionBounds(String nameOrcode);
	
	public Map<String, Object> road(String roadName);
	
	public Map<String, Object> roadByCity(String roadName, String name);
	
	public Map<String, Object> roadByBounds(String roadName, double min_lng, double min_lat, double max_lng, double max_lat);
	
	public Map<String, Object> geocoding(double lng, double lat) ;
	
	public Map<String, Object> roadpile(String roadName, String kpile,
			String name) ;
	
	public Map<String, Object> geoRoadPile(double lng, double lat);
	
	
	
}
