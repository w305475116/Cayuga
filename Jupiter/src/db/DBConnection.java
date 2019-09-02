package db;

import java.util.List;
import java.util.Set;

import entity.Item;

public interface DBConnection {
	//functions will be implemented later
	public void close();
	public void setFavoriteItems(String userId,String itemId);
	/*
	 * add favorite item for the user
	 * @param userId
	 * @param itemId
	 * */
	
	public void unsetFavoriteItems(String userId,String itemId);
	public Set<Item> getFavoriteItems(String userId);
	public Set<String> getFavoriteItemIds(String userId);
	public Set<String> getCategories(String itemId);
	public List<Item> searchItems(double lat,double lon, String term);
	public void saveItem(Item item);
	public String getFullName(String userId);
	public boolean verifyLogin(String userId,String password);
	
}
