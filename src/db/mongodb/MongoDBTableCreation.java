package db.mongodb;

import java.text.ParseException;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;

public class MongoDBTableCreation {
	public static void main(String args[]) throws ParseException{
		//connect to mongodb server
		MongoClient mongoClient = new MongoClient();
		MongoDatabase db = mongoClient.getDatabase(MongoDBUtil.DB_NAME);
		
		db.getCollection("users").drop();
		db.getCollection("items").drop();
		
		//If a collection does not exist, 
		//MongoDB creates the collection when you first store data for that collection.
		IndexOptions options = new IndexOptions().unique(true);
		db.getCollection("users").createIndex(new Document("user_id",1),options);
		db.getCollection("items").createIndex(new Document("item_id",1),options);
		db.getCollection("users").insertOne(
				new Document()
				.append("user_id", "1111")
				.append("password", "3229c1097c00d497a0fd282d586be050")
				.append("last_name", "Wang")
				.append("first_name", "Sirui")
				);
		mongoClient.close();
		System.out.println("import done successfully,db freshed");
	}

}
