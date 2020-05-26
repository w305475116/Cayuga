package db;
import db.mongodb.MongoDBConnection;
import db.mysql.MySQLConnection;
public class DBConnectionFactory {
	private final static String DEFAULT_DB="mysql";
	public static DBConnection getDBConnection(String db) {
		switch(db) {
		case "mysql":
			//add later
			return new MySQLConnection();
		case "mongoDB":
			return new MongoDBConnection();
		default:
			throw new IllegalArgumentException("Invalid db:"+db);
		}
		
	}
	public static DBConnection getDBConnection() {//function reload
		return getDBConnection(DEFAULT_DB);
	}
}
