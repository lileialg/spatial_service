package com.cennavi.spatial_service.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.cennavi.spatial_service.dao.SpatialDao;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

@Repository
public class SpatialDaoImpl implements SpatialDao {

	@Value("${schema.name}")
	private String schemaName;

	@Autowired
	protected JdbcTemplate jdbc;

	@Override
	public List<Map<String, Object>> poiSearchByCity(String q, String name,
			int page_num, int page_size) {

		String sql = null;

		char c = name.charAt(0);

		boolean flag = false;

		if (c >= '0' && c <= '9') {
			flag = true;
		}

		if (flag) {
			sql = "select * from ("
					+ "select a.name_zh as name,st_astext(a.geometry) geom,a.address,a.telephone from poi a,admin_face_all b where"
					+ " a.admin_id =b.admin_id and a.name_zh like ?"
					+ " and b.admin_id=? ) tmp limit ? offset ?";
		} else {
			sql = "select * from ("
					+ "select a.name_zh as name,st_astext(a.geometry) geom,a.address,a.telephone from poi a,admin_face_all b where"
					+ " a.admin_id =b.admin_id and a.name_zh like ?"
					+ " and b.name_zh=? ) tmp limit ? offset ?";
		}

		System.out.println(sql);

		List<Map<String, Object>> results = null;

		if (flag) {
			results = jdbc.queryForList(sql, "%" + q + "%",
					Integer.parseInt(name), page_size, (page_num - 1)
							* page_size);
		} else {
			results = jdbc.queryForList(sql, "%" + q + "%", name, page_size,
					(page_num - 1) * page_size);
		}

		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		for (Map<String, Object> map : results) {
			String w = (String) map.get("geom");

			map.remove("geom");

			try {
				Geometry geom = new WKTReader().read(w);

				Map<String, Double> m = new HashMap<String, Double>();

				double lng = geom.getCoordinate().x;
				double lat = geom.getCoordinate().y;
				m.put("lng", lng);
				m.put("lat", lat);
				map.put("location", m);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			list.add(map);
		}

		return list;
	}

	@Override
	public List<Map<String, Object>> poiSearchByBoundary(String q,
			double min_lng, double min_lat, double max_lng, double max_lat,
			int page_num, int page_size) {

		String sql = "select * from ("
				+ "select name_zh as name,st_astext(geometry) geom,address,telephone from poi where"
				+ " name_zh like ?"
				+ " and st_within(geometry,st_geomfromtext(?,4326))) tmp limit ? offset ?";

		String wkt = "Polygon((" + min_lng + " " + min_lat + "," + max_lng
				+ " " + min_lat + "," + max_lng + " " + max_lat + "," + min_lng
				+ " " + max_lat + "," + min_lng + " " + min_lat + "))";

		List<Map<String, Object>> results = jdbc.queryForList(sql, "%" + q
				+ "%", wkt, page_size, (page_num - 1) * page_size);

		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		for (Map<String, Object> map : results) {
			String w = (String) map.get("geom");

			map.remove("geom");

			try {
				Geometry geom = new WKTReader().read(w);

				Map<String, Double> m = new HashMap<String, Double>();

				double lng = geom.getCoordinate().x;
				double lat = geom.getCoordinate().y;
				m.put("lng", lng);
				m.put("lat", lat);
				map.put("location", m);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			list.add(map);
		}

		return list;
	}

	@Override
	public Map<String, Object> getRegionCenter(String nameOrcode) {

		String sql = null;

		char c = nameOrcode.charAt(0);

		boolean flag = false;

		if (c >= '0' && c <= '9') {
			flag = true;
		}

		if (flag) {
			sql = "select name_zh as name,admin_id as adcode,st_astext(ST_Centroid(geometry)) as geom from admin_face_all where admin_id=? ";
		} else {
			sql = "select name_zh as name,admin_id as adcode,st_astext(ST_Centroid(geometry)) as geom from admin_face_all where name_zh=? ";
		}
		List<Map<String, Object>> results = null;
		if (flag) {
			results = jdbc.queryForList(sql, Integer.parseInt(nameOrcode));
		} else {
			results = jdbc.queryForList(sql, nameOrcode);
		}

		for (Map<String, Object> map : results) {
			String w = (String) map.get("geom");

			map.remove("geom");

			try {
				Geometry geom = new WKTReader().read(w);

				Map<String, Double> m = new HashMap<String, Double>();

				double lng = geom.getCoordinate().x;
				double lat = geom.getCoordinate().y;
				m.put("lng", lng);
				m.put("lat", lat);
				map.put("location", m);

				return map;
			} catch (ParseException e) {
				e.printStackTrace();
			}

		}

		return new HashMap<String, Object>();

	}

	@Override
	public Map<String, Object> getRegionBounds(String nameOrcode) {

		String sql = null;

		char c = nameOrcode.charAt(0);

		boolean flag = false;

		if (c >= '0' && c <= '9') {
			flag = true;
		}

		if (flag) {
			sql = "select name_zh as name,admin_id as adcode,st_astext(geometry) as geom from admin_face_all where admin_id=? ";
		} else {
			sql = "select name_zh as name,admin_id as adcode,st_astext(geometry) as geom from admin_face_all where name_zh=? ";
		}

		List<Map<String, Object>> results = null;
		if (flag) {
			results = jdbc.queryForList(sql, Integer.parseInt(nameOrcode));
		} else {
			results = jdbc.queryForList(sql, nameOrcode);
		}

		for (Map<String, Object> map : results) {
			String w = (String) map.get("geom");

			map.remove("geom");

			try {
				Geometry geom = new WKTReader().read(w);

				List<Map<String, Double>> location = new ArrayList<Map<String, Double>>();

				Coordinate[] cs = geom.getCoordinates();

				for (Coordinate co : cs) {
					Map<String, Double> m = new HashMap<String, Double>();

					double lng = co.x;
					double lat = co.y;
					m.put("lng", lng);
					m.put("lat", lat);
					location.add(m);
				}

				map.put("location", location);

				return map;
			} catch (ParseException e) {
				e.printStackTrace();
			}

		}

		return new HashMap<String, Object>();

	}

	@Override
	public Map<String, Object> road(String roadName) {

		String sql = "select * from (select * from ("
				+ "select name_zh as name,st_astext(geometry) geom from road where"
				+ " name_zh like ?"
				+ " ) tmp1 order by length(name)) tmp limit 1";

		List<Map<String, Object>> results = jdbc.queryForList(sql, "%"
				+ roadName + "%");

		for (Map<String, Object> map : results) {
			String w = (String) map.get("geom");

			map.remove("geom");

			try {
				Geometry geom = new WKTReader().read(w);

				List<Map<String, Double>> list = new ArrayList<Map<String, Double>>();

				Coordinate[] cs = geom.getCoordinates();

				for (Coordinate c : cs) {
					Map<String, Double> m = new HashMap<String, Double>();

					double lng = c.x;
					double lat = c.y;
					m.put("lng", lng);
					m.put("lat", lat);
					list.add(m);
				}

				map.put("location", list);

				return map;
			} catch (ParseException e) {
				e.printStackTrace();
			}

		}

		return new HashMap<String, Object>();

	}

	@Override
	public Map<String, Object> roadByCity(String roadName, String name) {

		char c = name.charAt(0);

		boolean flag = false;

		if (c >= '0' && c <= '9') {
			flag = true;
		}

		String adminSql = null;
		if (flag) {
			adminSql = "select st_astext(geometry) geometry from admin_face_all where type>0 and admin_id=?";
		} else {
			adminSql = "select st_astext(geometry) geometry from admin_face_all where type>0 and name_zh=?";
		}
		List<Map<String, Object>> results = null;
		if (flag) {
			results = jdbc.queryForList(adminSql, Integer.parseInt(name));
		} else {
			results = jdbc.queryForList(adminSql, name);
		}

		List<String> wkts = new ArrayList<String>();
		
//		String wkt = null;

		for (Map<String, Object> map : results) {
			wkts.add((String) map.get("geometry"));
		}

		String sql = null;
		
		List<Map<String,Double>> ps = new ArrayList<Map<String,Double>>();
		
		Map<String,Object> result = new HashMap<String,Object>();
		
		result.put("name", roadName);
		
		for(String w1 : wkts){

		sql = "select a.name_zh as name,st_astext(a.geometry) geom from road a where"
				+ " a.name_zh like ?"
				+ " and st_intersects(a.geometry,st_geomfromtext(?,4326))";

		results = jdbc.queryForList(sql, "%" + roadName + "%", w1);

		for (Map<String, Object> map : results) {
			String w = (String) map.get("geom");

			map.remove("geom");

			try {
				Geometry geom = new WKTReader().read(w);

//				List<Map<String, Double>> list = new ArrayList<Map<String, Double>>();

				Coordinate[] cs = geom.getCoordinates();

				for (Coordinate co : cs) {
					Map<String, Double> m = new HashMap<String, Double>();

					double lng = co.x;
					double lat = co.y;
					m.put("lng", lng);
					m.put("lat", lat);
//					list.add(m);
					ps.add(m);
				}

//				map.put("location", list);

//				return map;
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
		}
		
		

		}
		
		result.put("location", ps);
		
		return result;

//		return new HashMap<String, Object>();

	}

	@Override
	public Map<String, Object> roadByBounds(String roadName, double min_lng,
			double min_lat, double max_lng, double max_lat) {

		String sql = "select * from (select * from ("
				+ "select name_zh as name,st_astext(geometry) geom from road where"
				+ "  name_zh like ?"
				+ " and st_intersects(geometry,st_geomfromtext(?,4326))) tmp1 order by length(name)) tmp2 limit 1";

		String wkt = "Polygon((" + min_lng + " " + min_lat + "," + max_lng
				+ " " + min_lat + "," + max_lng + " " + max_lat + "," + min_lng
				+ " " + max_lat + "," + min_lng + " " + min_lat + "))";

		List<Map<String, Object>> results = jdbc.queryForList(sql, "%"
				+ roadName + "%", wkt);

		for (Map<String, Object> map : results) {
			String w = (String) map.get("geom");

			map.remove("geom");

			try {
				Geometry geom = new WKTReader().read(w);

				List<Map<String, Double>> list = new ArrayList<Map<String, Double>>();

				Coordinate[] cs = geom.getCoordinates();

				for (Coordinate c : cs) {
					Map<String, Double> m = new HashMap<String, Double>();

					double lng = c.x;
					double lat = c.y;
					m.put("lng", lng);
					m.put("lat", lat);
					list.add(m);
				}

				map.put("location", list);

				return map;
			} catch (ParseException e) {
				e.printStackTrace();
			}

		}

		return new HashMap<String, Object>();

	}

	@Override
	public Map<String, Object> geocoding(double lng, double lat) {

		String wkt = "Point(" + lng + " " + lat + ")";
		String sql = "select name_zh,admin_id as adcode from admin_face_all where type>=0 and "
				+ "st_within(st_geomfromtext(?,4326),geometry)";

		List<Map<String, Object>> results = jdbc.queryForList(sql, wkt);

		Map<String, Object> map = new HashMap<String, Object>();

		for (Map<String, Object> m : results) {
			String name = (String) m.get("name_zh");
			map.put("adcode", m.get("adcode").toString());
			if (name.endsWith("自治区") || name.endsWith("省")) {
				map.put("privince", name);
			} else if (name.endsWith("市")) {
				map.put("city", name);
			} else if (name.endsWith("区")) {
				map.put("district", name);
			}
		}

		return map;
	}

	@Override
	public Map<String, Object> roadpile(String roadName, String kpile,
			String name) {

		char c = name.charAt(0);

		boolean flag = false;

		if (c >= '0' && c <= '9') {
			flag = true;
		}

		String sql = null;

		if (flag) {
			sql = "select a.road_name as name,st_astext(a.geometry) geom from mileage_pile a,road b where a.road_name like ?"
					+ " and a.mileage_num=? and a.link_id=b.pid and ? in (b.admin_left,b.admin_right) limit 1";
		} else {
			sql = "select a.road_name as name,st_astext(geometry) geom from mileage_pile a,road b,admin_face_all c "
					+ "where "
					+ " a.road_name like ? "
					+ " and a.mileage_num=? "
					+ " and a.link_pid=b.pid "
					+ " and c.admin_id in (b.admin_left,b.admin_right) "
					+ " and c.name_zh=?";
		}

		List<Map<String, Object>> results = null;
		if (flag) {
			results = jdbc.queryForList(sql, "%" + roadName + "%", kpile,
					Integer.parseInt(name));
		} else {
			results = jdbc.queryForList(sql, "%" + roadName + "%", kpile, name);
		}

		for (Map<String, Object> map : results) {
			String w = (String) map.get("geom");

			map.remove("geom");

			try {
				Geometry geom = new WKTReader().read(w);

				double lng = geom.getCoordinate().x;
				double lat = geom.getCoordinate().y;
				Map<String, Double> location = new HashMap<String, Double>();
				location.put("lng", lng);
				location.put("lat", lat);

				map.put("location", location);

				Map<String, Object> pos = this.geocoding(lng, lat);

				for (Entry<String, Object> en : pos.entrySet()) {
					map.put(en.getKey(), en.getValue());
				}

				return map;
			} catch (ParseException e) {
				Map<String, Object> map1 = new HashMap<String, Object>();

				map1.put("error", e.getMessage());

				return map;
			}

		}

		return new HashMap<String, Object>();
	}

	@Override
	public Map<String, Object> geoRoadPile(double lng, double lat) {

		String wkt = "Point(" + lng + " " + lat + ")";

		try {
			Geometry point = new WKTReader().read(wkt);

			double dist = 0.001;

			while (true) {
				Geometry buf = point.buffer(dist);

				String bufWkt = new WKTWriter().write(buf);

				String sql = "select a.road_name,st_astext(geometry) geom from mileage_pile a "
						+ " where st_within(a.geometry,st_geomfromtext(?,4326))";

				List<Map<String, Object>> results = null;

				results = jdbc.queryForList(sql, bufWkt);

				Geometry targetGeom = null;
				String targetName = null;

				if (results.size() > 0) {
					double distance = 1000;

					for (Map<String, Object> m : results) {
						String w = (String) m.get("geom");

						Geometry tmpGeom = new WKTReader().read(w);
						double tmpDis = tmpGeom.distance(point);
						if (tmpDis < distance) {
							distance = tmpDis;
							targetGeom = tmpGeom;
							targetName = (String) m.get("road_name");
						}
					}

					Map<String, Object> map = new HashMap<String, Object>();

					map.put("name", targetName);

					double tlng = targetGeom.getCoordinate().x;
					double tlat = targetGeom.getCoordinate().y;
					Map<String, Double> location = new HashMap<String, Double>();
					location.put("lng", tlng);
					location.put("lat", tlat);

					map.put("location", location);

					Map<String, Object> pos = this.geocoding(tlng, tlat);

					for (Entry<String, Object> en : pos.entrySet()) {
						map.put(en.getKey(), en.getValue());
					}

					return map;
				}

				dist = dist * 10;
			}
		} catch (Exception e) {
			e.printStackTrace();

			Map<String, Object> map = new HashMap<String, Object>();

			map.put("error", e.getMessage());

			return map;
		}

	}

}
