package rpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;

/**
 * Servlet implementation class RecommendItem
 */
@WebServlet("/recommendation")
public class RecommendItem extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RecommendItem() {
        super();
        // TODO Auto-generated constructor stub
    }
    public List<Item> recommendItems(String userId, double lat, double lon){
    	List<Item> result = new ArrayList<Item>();
    	// get favorite item ids
    	DBConnection conn = DBConnectionFactory.getDBConnection("mysql");
    	Set<String> itemIds = conn.getFavoriteItemIds(userId);
    	
    	// get all categories, sort by count
    	Map<String,Integer> allUserCategories = new HashMap<String,Integer> ();
    	for(String itemId : itemIds) {
    		Set<String> categories = conn.getCategories(itemId);
    		for(String category : categories) {
    			allUserCategories.put(category, allUserCategories.getOrDefault(category, 0)+1);//get value or default value if does not exist,can be replaced using if null
    		}
    	}
    	
    	// by now the map is complete, need to put it into a list so that we can sort
    	List<Map.Entry<String, Integer>> categoryList = new ArrayList<>(allUserCategories.entrySet());  
    	Collections.sort(categoryList, new Comparator<Map.Entry<String, Integer>>(){

			@Override
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				// TODO Auto-generated method stub
				return o2.getValue()-o1.getValue();
			}
    		
    	});
    	
    	// categoryList now has been sorted from highest to lowest
    	// now restart searching and delete the duplicate items already in history
    	Set<String> visitedItemIds = new HashSet<String>();
    	for(Entry<String, Integer> category: categoryList) {
    		List<Item> items = conn.searchItems(lat, lon, category.getKey());
    		for(Item item : items) {
    			if(!visitedItemIds.contains(item.getItemId())&&!itemIds.contains(item.getItemId())) {
    				result.add(item);
    				visitedItemIds.add(item.getItemId());
    			}
    		}
    	}
    	conn.close();
    	return result;
    }
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		HttpSession session = request.getSession(false);
		if(session==null) {
			response.setStatus(403);
			return;
		}
		String userId = session.getAttribute("user_id").toString();
		double lat = Double.parseDouble(request.getParameter("lat"));
		double lon = Double.parseDouble(request.getParameter("lon"));
		RecommendItem rec = new RecommendItem();
		List<Item> items = rec.recommendItems(userId, lat, lon);
		JSONArray array = new JSONArray();
		for(Item item: items) {
			array.put(item.toJSONObject());
		}
		RpcHelper.writeJsonArray(response, array);
		}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
