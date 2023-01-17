package com.speno.xmon.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtil {

	public static Map jsonToMap(JSONObject json) throws JSONException {
		Map<String, Object> retMap = new HashMap<String, Object>();

		if (json != JSONObject.NULL) {
			retMap = toMap(json);
		}
		return retMap;
	}

	public static Map toMap(JSONObject object) throws JSONException {
		Map<String, Object> map = new HashMap<String, Object>();

		Iterator<String> keysItr = object.keys();
		while (keysItr.hasNext()) {
			String key = keysItr.next();
			Object value = object.get(key);

			if (value instanceof JSONArray) {
				value = toList((JSONArray) value);
			}

			else if (value instanceof JSONObject) {
				value = toMap((JSONObject) value);
			}
			map.put(key, value);
		}
		return map;
	}

	public static List toList(JSONArray array) throws JSONException {
		List<Object> list = new ArrayList<Object>();
		for (int i = 0; i < array.length(); i++) {
			Object value = array.get(i);
			if (value instanceof JSONArray) {
				value = toList((JSONArray) value);
			}

			else if (value instanceof JSONObject) {
				value = toMap((JSONObject) value);
			}
			list.add(value);
		}
		return list;
	}
	
	public static JSONObject getJsonFromList(List<List<String>> originalData) {
		JSONObject json 		= new JSONObject();
	  	JSONArray jsonArray 			= new JSONArray();

		int row = 0;
		int column = 0;
		String[] columnNames = null;

		try {
			for (List<String> list : originalData) {
				row ++;
				column = 0;

				// column
				if (row == 1) {
					if (columnNames == null)
						columnNames = new String[list.size()];
					
					for (int i = 0; i < columnNames.length; i++) {
						columnNames[i] = list.get(i);
					}
					continue;
				}

			  	JSONObject subJson = new JSONObject();
				for (String value : list) {
					subJson.put(columnNames[column++], value);
				}
				jsonArray.put(subJson);
			}
			json.put("Data", jsonArray);
			
			return json;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}
}
