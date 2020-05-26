package rpc;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import entity.Item;
import entity.Item.itemBuilder;
import rpc.RpcHelper;
class RpcHelperTest {
/* 
 *   public static JSONArray getJSONArray(List<Item> items) {
    JSONArray result = new JSONArray();
    try {
      for (Item item : items) {
        result.put(item.toJSONObject());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }
  
  item attributes:
  	private String name;
	private double rating;
	private String address;
	private Set<String> categories;
	private String imageUrl;
	private String url;
	private double distance;
	private String itemId;

 */
	
	@Test
	void testJSONArray() throws JSONException {
		Set<String> category = new HashSet<String>();
		category.add("tested category");
		List<Item> items = new ArrayList<Item>();
		Item one = new itemBuilder().setAddress("117B Veterans' Pl")
				.setCategories(category)
				.setDistance(10)
				.setItemId("one")
				.setName("one")
				.setRating(5).build();// returns an item object
		
		Item two = new itemBuilder().setAddress("117B Veterans' Pl")
				.setCategories(category)
				.setDistance(20)
				.setItemId("two")
				.setName("two")
				.setRating(4).build();// returns an item object
		items.add(one);
		items.add(two);
		JSONArray jsonArray = new JSONArray();
		jsonArray.put(one.toJSONObject());
		jsonArray.put(two.toJSONObject());
		JSONAssert.assertEquals(jsonArray, RpcHelper.getJSONArray(items), true);
		
		Item empty = new itemBuilder().build();
		jsonArray.put(empty.toJSONObject());
		JSONAssert.assertEquals(jsonArray, RpcHelper.getJSONArray(items), true);

	}
	

}
