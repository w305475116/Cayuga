package db.mongodb;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static com.mongodb.client.model.Filters.eq;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;

import db.DBConnection;
import entity.Item;
import entity.Item.itemBuilder;
import external.TicketMasterAPI;

public class MongoDBConnection implements DBConnection {
	private MongoClient mongoClient;
	private MongoDatabase db;
	public MongoDBConnection() {
		//create instances for the DB
		 mongoClient = new MongoClient();
		 db = mongoClient.getDatabase(MongoDBUtil.DB_NAME);
	}
	
	
	@Override
	public void close() {
		if(mongoClient != null) {
			mongoClient.close();
		}
	}

	@Override
	public void setFavoriteItems(String userId, String itemId) {
		db.getCollection("users").updateOne(new Document("user_id", userId),
				new Document("$push",new Document("favorite",itemId)));
		
	}

	@Override
	public void unsetFavoriteItems(String userId, String itemId) {
		db.getCollection("users").updateOne(new Document("user_id", userId),
				new Document("$pull",new Document("favorite",itemId)));

	}

	@Override
	public Set<Item> getFavoriteItems(String userId) {
		Set<Item> favoriteitems = new HashSet<>();
		Set<String> itemIds = getFavoriteItemIds(userId);
		for(String itemId : itemIds) {
			FindIterable<Document> iterable = db.getCollection("items").find(eq("item_id",itemId));
			if(iterable.first()!=null) {//can find the item
				Document doc = iterable.first();
				itemBuilder builder = new itemBuilder();

				builder.setItemId(doc.getString("item_id"));
				builder.setName(doc.getString("name"));
				builder.setAddress(doc.getString("address"));
				builder.setImageUrl(doc.getString("image_url"));
				builder.setUrl(doc.getString("url"));
				builder.setCategories(getCategories(itemId));
				builder.setDistance(doc.getDouble("distance"));
				builder.setRating(doc.getDouble("rating"));
				favoriteitems.add(builder.build());
			}
		}
		return favoriteitems;
	}

	@Override
	public Set<String> getFavoriteItemIds(String userId) {
		Set<String> favoriteItemIds = new HashSet<>();
		FindIterable<Document> iterable = db.getCollection("users").find(eq("user_id",userId));
		if(iterable.first()!=null&&iterable.first().containsKey("favorite")) {
			@SuppressWarnings("unchecked")
			List <String> list = (List<String>) iterable.first().get("favorite");
			favoriteItemIds.addAll(list);
		}
		return favoriteItemIds;
	}

	@Override
	public Set<String> getCategories(String itemId) {
		Set<String> categories = new HashSet<>();
		FindIterable<Document> iterable = db.getCollection("items").find(eq("item_id",itemId));
		if(iterable.first()!=null&&iterable.first().containsKey("categories")) {
			@SuppressWarnings("unchecked")
			List <String> list = (List<String>) iterable.first().get("categories");
			categories.addAll(list);
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
		
		FindIterable<Document> iterable = db.getCollection("items").find(eq("item_id",item.getItemId()));
		
		if(iterable.first()==null) {
			db.getCollection("items").insertOne(new Document()
					.append("item_id", item.getItemId())
					.append("distance", item.getDistance())
					.append("name", item.getName())
					.append("address", item.getAddress())
					.append("url", item.getUrl())
					.append("image_url", item.getImageUrl())
					.append("rating", item.getRating())
					.append("categories", item.getCategories())
					);
		}

	}



	@Override
	public String getFullName(String userId) {
		FindIterable<Document> iterable = db.getCollection("users").find(eq("user_id",userId));
		if(iterable.first()!=null) {
			String firstName = iterable.first().getString("first_name");
			String lastName = iterable.first().getString("last_name");
			return firstName + " " + lastName;
		}
		return "";
	}

	@Override
	public boolean verifyLogin(String userId, String password) {
		FindIterable<Document> iterable = db.getCollection("users").find(eq("user_id",userId));
		if(iterable.first()!=null) {
			String dbPassword = iterable.first().getString("password");
			return dbPassword.equals(password);
		}
		return false;
	}


	@Override
	public boolean registerUser(String userId, String password, String firstname, String lastname) {
		// TODO Auto-generated method stub
		return false;
	}

}
