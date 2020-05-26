package offline;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

import db.mongodb.MongoDBUtil;

public class Purify {

	public static void main(String[] args) {
		MongoClient mongoClient = new MongoClient();
		MongoDatabase db = mongoClient.getDatabase(MongoDBUtil.DB_NAME);
		String fileName = "./src/offline/tomcat_log.txt";
	    try {
	    	db.getCollection("logs").drop();//drop old log if exist
	    	FileReader fileReader = new FileReader(fileName);
	    	BufferedReader bufferedReader = new BufferedReader(fileReader);
	    	String line = null;
	    	while((line = bufferedReader.readLine())!=null) {
    			//73.223.210.212 - - [19/Aug/2017:22:00:24 +0000] "GET /Titan/history?user_id=1111 HTTP/1.1" 200 11410
	    		List<String> values = Arrays.asList(line.split(" "));
	    		String ip = values.size()>0 ? values.get(0):null; //must ensure that the get value will not over flow
	    		String time = values.size() > 3 ? values.get(3):null;
	    		String method = values.size() > 5 ? values.get(5):null; //get/post/delete/update
	    		String url = values.size() > 6 ? values.get(6):null;
	    		String status = values.size() > 8 ? values.get(8):null;
	    		Pattern pattern = Pattern.compile("\\[(.+?):(.+)");//matches a [
	    		Matcher matcher = pattern.matcher(time);
	    		matcher.find();
	    		//create a db collection named logs for the first time
	    		//add one line of log each time into the db
	    		db.getCollection("logs").insertOne(
	    				new Document()
	    				.append("ip",ip)
	    				.append("date", matcher.group(1))//found 06/sep/2017
	    				.append("time", matcher.group(2))//found 00:27:19
	    				.append("method", method.substring(1))//get rid of the " symbol
	    				.append("url", url)
	    				.append("status", status));
	    	}
	    	System.out.println("Import Done!"); 
	    	bufferedReader.close(); 
	    	mongoClient.close();	    		
	    }catch(Exception e) {
	    	e.printStackTrace();
	    }
	}
}


