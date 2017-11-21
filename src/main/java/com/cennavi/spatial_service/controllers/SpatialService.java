package com.cennavi.spatial_service.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cennavi.spatial_service.dao.SpatialDao;

@RestController
public class SpatialService {

	@Autowired
	private SpatialDao dao;

	// lnglat.encoder.url=http://geocode.mapbar.com/Decode/getEncode.json
	// route.url=http://mapx.mapbar.com/route/getDriveByLatLon.json
	// lnglat.decoder.url=http://geocode.mapbar.com/decode/getDecode.json
	@Value("${lnglat.encoder.url}")
	private String encoderUrl;

	@Value("${route.url}")
	private String routeUrl;

	@Value("${lnglat.decoder.url}")
	private String decoderUrl;

	@RequestMapping(value = "/poiSearchByCity")
	public List<Map<String, Object>> poiSearchByCity(String q, String name,
			int page_num, int page_size) {

		return dao.poiSearchByCity(q, name, page_num, page_size);
	}

	@RequestMapping(value = "/poiSearchByBounds")
	public List<Map<String, Object>> poiSearchByBounds(String q,
			double min_lng, double min_lat, double max_lng, double max_lat,
			int page_num, int page_size) {

		return dao.poiSearchByBoundary(q, min_lng, min_lat, max_lng, max_lat,
				page_num, page_size);
	}

	@RequestMapping(value = "/getRegionCenter")
	public Map<String, Object> getRegionCenter(String nameOrcode) {


		return dao.getRegionCenter(nameOrcode);
	}

	@RequestMapping(value = "/getRegionBounds")
	public Map<String, Object> getRegionBounds(String nameOrcode) {


		return dao.getRegionBounds(nameOrcode);
	}

	@RequestMapping(value = "/road")
	public Map<String, Object> road(String roadName) {


		return dao.road(roadName);
	}

	@RequestMapping(value = "/roadByCity")
	public Map<String, Object> roadByCity(String roadName, String name) {


		return dao.roadByCity(roadName, name);
	}

	@RequestMapping(value = "/roadByBounds")
	public Map<String, Object> roadByBounds(String roadName, double min_lng,
			double min_lat, double max_lng, double max_lat) {


		return dao.roadByBounds(roadName, min_lng, min_lat, max_lng, max_lat);

	}

	@RequestMapping(value = "/geocoding")
	public Map<String, Object> geocoding(double lng, double lat) {


		return dao.geocoding(lng, lat);
	}

	@RequestMapping(value = "/roadpile")
	public Map<String, Object> roadpile(String roadName, String kpile,
			String name) {


		return dao.roadpile(roadName, kpile, name);
	}

	@RequestMapping(value = "/geoRoadPile")
	public Map<String, Object> geoRoadPile(double lng, double lat) {


		return dao.geoRoadPile(lng, lat);
	}

	@RequestMapping(value = "/geoRoadRoute")
	public List<Map<String, Double>> geoRoadRoute(String start, String mids,
			String end) {


		String startCode, endCode;

		String url = null;

		if (mids != null && mids.length() > 0) {
			url = encoderUrl + "?latlon=" + start + ";" + end + ";" + mids;
		} else {
			url = encoderUrl + "?latlon=" + start + ";" + end;
		}

		try {
			// 根据地址获取请求
			HttpGet request = new HttpGet(url);// 这里发送get请求
			// 获取当前客户端对象
			@SuppressWarnings("deprecation")
			HttpClient httpClient = new DefaultHttpClient();
			// 通过请求对象获取响应对象
			HttpResponse response = httpClient.execute(request);

			// 判断网络连接状态码是否正常(0--200都数正常)
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String result = EntityUtils.toString(response.getEntity(),
						"utf-8");

				JSONObject json = JSON.parseObject(result);

				JSONArray ja = json.getJSONObject("pois").getJSONArray("item");

				startCode = ja.getJSONObject(0).getString("latlon");
				endCode = ja.getJSONObject(1).getString("latlon");

				StringBuilder sb = new StringBuilder();

				if (ja.size() > 2) {
					sb.append(ja.getJSONObject(2).getString("latlon"));
					if (ja.size() > 3) {
						for (int i = 3; i < ja.size(); i++) {
							sb.append(";");
							sb.append(ja.getJSONObject(i).getString("latlon"));
						}
					}
				}

				String routeUrlRequest = null;

				if (sb.length() > 0) {
					routeUrlRequest = routeUrl + "?" + "orig=" + startCode + "&"
							+ "dest=" + endCode + "&mid=" + sb.toString();
				} else {
					routeUrlRequest = routeUrl + "?" + "orig=" + startCode + "&"
							+ "dest=" + endCode;
				}

				request = new HttpGet(routeUrlRequest);// 这里发送get请求
				// 获取当前客户端对象
				@SuppressWarnings("deprecation")
				HttpClient httpClient2 = new DefaultHttpClient();
				// 通过请求对象获取响应对象
				response = httpClient2.execute(request);

				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String result1 = EntityUtils.toString(response.getEntity(),
							"utf-8");

					JSONObject json1 = JSON.parseObject(result1);

					String routelatlon = json1.getString("routelatlon");

					routelatlon = routelatlon.substring(0,
							routelatlon.length() - 1);
					
					String[] splits = routelatlon.split(",");
					List<Map<String, Double>> resultList = new ArrayList<Map<String, Double>>();
					int num =0;
					StringBuilder sb1 = new StringBuilder();
					for(int j=0;j<splits.length;j++){
						num++;
						sb1.append(splits[j]);
						sb1.append(",");
						if (num % 10 == 0){
							String callUrl = decoderUrl + "?latlon="
									+sb1.toString().substring(0, sb1.length()-1);
							sb1 = new StringBuilder();
							
							request = new HttpGet(callUrl);// 这里发送get请求
							// 获取当前客户端对象
							@SuppressWarnings("deprecation")
							HttpClient httpClient3 = new DefaultHttpClient();
							// 通过请求对象获取响应对象
							response = httpClient3.execute(request);

							if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
								String result2 = EntityUtils.toString(
										response.getEntity(), "utf-8");

								JSONObject json2 = JSON.parseObject(result2);

								JSONArray ja2 = json2.getJSONObject("pois")
										.getJSONArray("item");

								

								for (int i = 0; i < ja2.size(); i++) {
									JSONObject tmp = ja2.getJSONObject(i);

									Map<String, Double> m = new HashMap<String, Double>();
									m.put("lng", tmp.getDouble("lon"));
									m.put("lat", tmp.getDouble("lat"));
									resultList.add(m);
								}


							}
						}
					}

					if (sb1.length()>0) {
						String callUrl = decoderUrl + "?latlon="
								+sb1.toString().substring(0, sb1.length()-1);
						request = new HttpGet(callUrl);// 这里发送get请求
						// 获取当前客户端对象
						@SuppressWarnings("deprecation")
						HttpClient httpClient3 = new DefaultHttpClient();
						// 通过请求对象获取响应对象
						response = httpClient3.execute(request);
						if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
							String result2 = EntityUtils.toString(
									response.getEntity(), "utf-8");

							JSONObject json2 = JSON.parseObject(result2);

							JSONArray ja2 = json2.getJSONObject("pois")
									.getJSONArray("item");

							for (int i = 0; i < ja2.size(); i++) {
								JSONObject tmp = ja2.getJSONObject(i);

								Map<String, Double> m = new HashMap<String, Double>();
								m.put("lng", tmp.getDouble("lon"));
								m.put("lat", tmp.getDouble("lat"));
								resultList.add(m);
							}

						}
					}
					return resultList;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return new ArrayList<Map<String, Double>>();
	}

}
