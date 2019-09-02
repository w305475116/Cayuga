package db.mysql;

public class MySQLDBUtil {//this util saves the constants of the mysql db
	private static final String HOSTNAME = "localhost";
	private static final String PORT_NUM = "3306";
	public static final String DB_NAME = "laiproject";
	private static final String USERNAME = "root";
	private static final String PASSWORD = "root";
	// jdbc:mysql://localhost:3306/laiproject?user=root&password=root&autoReconnect=true&serverTimezone=UTC"
	public static final String URL = "jdbc:mysql://"+HOSTNAME+":"+PORT_NUM+"/"+DB_NAME+"?user="+USERNAME+"&password="+PASSWORD+
			"&autoReconnect=true&serverTimezone=UTC";
	
	
}
