package external;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import entity.Item;
import entity.Item.itemBuilder;


public class TicketMasterAPI {
	//https://app.ticketmaster.com/discovery/v2/events.json?apikey=12345&latlong=37,-120&keyword=music&radius=50
	private final static String URL =  "https://app.ticketmaster.com/discovery/v2/events.json";
	private final static String DEFAULT_KEYWORD = "";
	private final static String API_KEY = "i8fgiifCOqZ8l5Y0gqhQxzcNQ2Lw7uLj"; 
	
	
	public List<Item> search(double lat, double lon, String keyword) {
		if (keyword==null) keyword = DEFAULT_KEYWORD;
		//keyword!=null
		try {
			keyword = URLDecoder.decode(keyword,"UTF-8");
		}
		catch(UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String geoHash=GeoHash.encodeGeohash(lat, lon, 8);
		String query = String.format("apikey=%s&geoPoint=%s&keyword=%s&radius=%s", API_KEY,geoHash,keyword,"50");
		String url = URL+"?"+query;
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestMethod("GET");
			int responsecode = connection.getResponseCode();
			System.out.println("Sending request to url:"+url);
			System.out.println("Response Code:"+responsecode);
			if (responsecode!=200) {
				return new ArrayList<>();
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			StringBuilder response = new StringBuilder();
			while((line=reader.readLine())!=null) {
				response.append(line);
			}
			reader.close();
			JSONObject obj = new JSONObject(response.toString());
			if (!obj.isNull("_embedded")) {
				JSONObject embedded = obj.getJSONObject("_embedded");
				return getItemList(embedded.getJSONArray("events"));
			
		}

	}
		catch(Exception e) {e.printStackTrace();}
		return new ArrayList<>();
	}
	
	
	private List<Item> getItemList(JSONArray events) throws JSONException{
		List<Item> itemList = new ArrayList<>();
		for(int i=0;i<events.length();i++) {
			JSONObject event = events.getJSONObject(i);
			itemBuilder builder = new itemBuilder();
			builder.setAddress(getAddress(event))
			.setCategories(getCategories(event))
			.setImageUrl(getImageUrl(event));
			if(!event.isNull("distance")) {
				builder.setDistance(event.getDouble("distance"));
			}
			if(!event.isNull("id")) {
				builder.setItemId(event.getString("id"));
			}
			if(!event.isNull("name")) {
				builder.setName(event.getString("name"));
			}
			if(!event.isNull("rating")) {
				builder.setRating(event.getDouble("rating"));
			}
			if(!event.isNull("url")) {
				builder.setUrl(event.getString("url"));
			}
			itemList.add(builder.build());
//			private double rating; null in the api
		}
		return itemList;
	}
	private String getAddress(JSONObject event)throws JSONException{
		if(!event.isNull("_embedded")) {
			JSONObject embedded = event.getJSONObject("_embedded");
			if(!embedded.isNull("venues")) {
				JSONArray venues = embedded.getJSONArray("venues");
				for(int i =0;i<venues.length();i++) {
					JSONObject venue = venues.getJSONObject(i);
					StringBuilder builder = new StringBuilder();
					if(!venue.isNull("address")) {
						JSONObject address = venue.getJSONObject("address");
						if(!address.isNull("line1")) {
							builder.append(address.getString("line1"));
						}
						if(!address.isNull("line2")) {
							builder.append(address.getString("line2"));
						}
						if(!address.isNull("line3")) {
							builder.append(address.getString("line3"));
						}
					}
					if(!venue.isNull("city")) {
						JSONObject city = venue.getJSONObject("city");
						builder.append(",");
						builder.append(city.getString("name"));
					}
					String result = builder.toString();
					if(!result.isEmpty())
						return result;
				}
				
			}
		}
		return "";
	}
	private String getImageUrl(JSONObject event) throws JSONException{
		if(!event.isNull("images")) {
			JSONArray images = event.getJSONArray("images");
			for(int i=0;i<images.length();i++) {
				JSONObject image = images.getJSONObject(i);				
				if(!image.isNull("url")) {
					return image.getString("url");
				}
			}
		}
		return "";
	}
	private Set<String> getCategories(JSONObject event) throws JSONException{
		Set<String> categories = new HashSet<>();
		if(!event.isNull("classifications")) {
			JSONArray classifications = event.getJSONArray("classifications");
			for(int i=0;i<classifications.length();i++) {
				JSONObject classification = classifications.getJSONObject(i);
				if (!classification.isNull("segment")) {
					JSONObject segment = classification.getJSONObject("segment");
					if(!segment.isNull("name")) {
						categories.add(segment.getString("name"));
					}
				}
			}
		}
		return categories;
	}
	
	private void queryAPI(double lat,double lon) {
		List<Item> events = search(lat,lon,null);
		try {
			for(Item event:events) {
				System.out.println(event.toJSONObject());
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]) {
		TicketMasterAPI tmpApi = new TicketMasterAPI();
		tmpApi.queryAPI(37.38, -122.08);
	}
	
	
	}
