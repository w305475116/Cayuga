package db.mysql;

import java.sql.Connection;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;

import db.DBConnection;
import entity.Item;
import entity.Item.itemBuilder;
import external.TicketMasterAPI;

public class MySQLConnection implements DBConnection {
	private Connection conn;
	public MySQLConnection() {//constructor for MySQLConnection class
		try {
			System.out.println("connecting to "+MySQLDBUtil.URL);
            Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();
            conn = DriverManager.getConnection(MySQLDBUtil.URL);
            if (conn==null) {
    			System.out.println("wrong url:failed to connect "+MySQLDBUtil.URL);
            	return;
            }
            System.out.println("connection successful");
		}catch(Exception e) {
			e.printStackTrace();
		}
	}


	@Override
	public void close() {
		try {
			if (conn!=null) {
				conn.close();
				System.out.println("db closed successfully");
			}
		}catch(SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void setFavoriteItems(String userId, String itemId) {
		if(conn==null) {
			System.out.println("conn is null,return");
			return;
		}
		try {
			String sql = "INSERT IGNORE INTO history(user_id,item_id) VALUES(?,?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			ps.setString(2, itemId);
			ps.execute();
			
		}
		catch(SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void unsetFavoriteItems(String userId, String itemId) {
		if(conn==null) {
			System.out.println("conn is null,return");
			return;
		}
		try {
			String sql = "DELETE from history WHERE user_id=? and item_id=?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			ps.setString(2, itemId);
			ps.execute();
			
		}
		catch(SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public Set<Item> getFavoriteItems(String userId) {
		if(conn==null) {
			System.out.println("conn is null,return");
			return new HashSet<>();
		}
		Set<Item> favoriteitems = new HashSet<>();
		Set<String> itemIds = getFavoriteItemIds(userId);
		try {
			String sql = "SELECT * from items WHERE item_id=? ";
			PreparedStatement ps = conn.prepareStatement(sql);
			for(String itemId:itemIds) {
				ps.setString(1,itemId);
				ResultSet rs = ps.executeQuery();
				itemBuilder builder = new itemBuilder();
				while(rs.next()) {
					builder.setItemId(rs.getString("item_id"));
					builder.setName(rs.getString("name"));
					builder.setAddress(rs.getString("address"));
					builder.setImageUrl(rs.getString("image_url"));
					builder.setUrl(rs.getString("url"));
					builder.setCategories(getCategories(itemId));
					builder.setDistance(rs.getDouble("distance"));
					builder.setRating(rs.getDouble("rating"));
					favoriteitems.add(builder.build());
				}
			}

			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return favoriteitems;
	}

	@Override
	public Set<String> getFavoriteItemIds(String userId) {
		if(conn==null) {
			System.out.println("conn is null,return");
			return new HashSet<>();
		}
		Set<String> favoriteItemIds = new HashSet<>();
		try {
			String sql = "SELECT item_id from history WHERE user_Id=? ";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				favoriteItemIds.add(rs.getString("item_id"));
			}
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
		return favoriteItemIds;
	}

	@Override
	public Set<String> getCategories(String itemId) {
		if(conn==null) {
			System.out.println("conn is null,return");
			return new HashSet<>();
		}
		Set<String> categories = new HashSet<>();
		try {
			String sql = "SELECT category from categories WHERE item_id=? ";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, itemId);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				categories.add(rs.getString("category"));
			}
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
		return categories;
	}

	@Override
	public List<Item> searchItems(double lat, double lon, String term) {
		TicketMasterAPI myAPI=new TicketMasterAPI();
		List <Item>items=myAPI.search(lat, lon, null);
		for(Item item:items) {
			saveItem(item);
//			System.out.println("save item successfully");
		}
		
		return items;
	}

	@Override
	public void saveItem(Item item) {
		if(conn==null) {
			System.out.println("conn is null,return");
			return;
		}
		try {
			String sql = "INSERT IGNORE INTO items VALUES(?,?,?,?,?,?,?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, item.getItemId());
			ps.setString(2,item.getName());
			ps.setDouble(3, item.getRating());
			ps.setString(4, item.getAddress());
			ps.setString(5,item.getImageUrl());
			ps.setString(6, item.getUrl());
			ps.setDouble(7, item.getDistance());
			ps.execute();
			System.out.println("insert one item");
			sql = "INSERT IGNORE INTO categories VALUES(?,?)";
			ps = conn.prepareStatement(sql);
			ps.setString(1, item.getItemId());
			for(String category:item.getCategories()) {
				ps.setString(2, category);
				ps.execute();
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public String getFullName(String userId) {
		String name = "";
		if(conn==null) {
			System.out.println("conn is null,return");
			return name;
		}
		try {
			String sql = "SELECT first_name,last_name from users where user_id=?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1,userId);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {//only first name+last name
				name = rs.getString("first_name")+" "+rs.getString("last_name");
			}
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
		
		return name;
	}


	@Override
	public boolean verifyLogin(String userId, String password) {
		
		if(conn==null) {
			System.out.println("conn is null,return false login");
			return false;//false by default
		}
		try {
			String sql = "SELECT user_id,password from users where user_id=?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1,userId);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
			String pw_in_db = rs.getString("password");
			if (pw_in_db.equals(password)) {//string compare, == is compare of two addresses! 
				return true;
			}
			}

		}
		catch(SQLException e) {
			e.printStackTrace();
		}
		
		return false;
	}


	@Override
	public boolean registerUser(String userId, String password, String firstname, String lastname) {
		if(conn==null) {
			System.out.println("conn is null,return false login");
			return false;//false by default
		}
		try {
			String sql = "INSERT INTO users VALUES(?,?,?,?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1,userId);
			ps.setString(2,password);
			ps.setString(3, firstname);
			ps.setString(4, lastname);
			return ps.executeUpdate()==1;
			}
		catch(SQLException e) {
			e.printStackTrace();
		}
		
		return false;
	}

}
