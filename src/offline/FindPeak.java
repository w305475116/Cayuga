package offline;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.MapReduceIterable;
import com.mongodb.client.MongoDatabase;

import db.mongodb.MongoDBUtil;

public class FindPeak {
	private static List<LocalTime> bucket = initBuckets();
	private static List<LocalTime> initBuckets(){
		List<LocalTime> buckets = new ArrayList<>();
		LocalTime time = LocalTime.parse("00:00");//takes a string and parse it
		for(int i= 0; i< 96; ++i) {
			buckets.add(time);
			time = time.plusMinutes(15);//24 hours in total
		}
		return buckets;//each bucket is a 15 min time slot
	}
	
	//find the time bucket according to the current time
	private static String findBucket(String currentTime) {//current time is a string of xx:xx that can be parsed 
		LocalTime curTime = LocalTime.parse(currentTime);
		int left = 0;
		int right = bucket.size() - 1;
		//binary search to find 
		while(left < right-1){
			int mid = left + (right-left)/2;
			if(bucket.get(mid).isAfter(curTime)) {
				right = mid -1;
			}else {
				left = mid;
			}
		}
		if(bucket.get(right).isAfter(curTime)) {
			return bucket.get(left).toString();
		}
		return bucket.get(right).toString();
	}
	
	public static void main(String[] args) {
		// initialize mongodb
		MongoClient mongoClient = new MongoClient();
		MongoDatabase db = mongoClient.getDatabase(MongoDBUtil.DB_NAME);
		StringBuilder sb = new StringBuilder();
		//construct a mapper function
		sb.append("function() {")
		.append("if(this.url.startsWith(\"/Titan\")){")
		.append("emit(this.time.substring(0,5),1);}}");
		String map = sb.toString();
		
		//construct a reducer function
		sb.setLength(0);
		sb.append("function(key,values){")
		.append("return Array.sum(values)}");
		String reduce = sb.toString();
		
		MapReduceIterable<Document> results = db.getCollection("logs").mapReduce(map, reduce);
		Map<String,Double> timeMap = new HashMap<>();
		//put all the result into a hashmap which saves time and its count
		results.forEach(new Block<Document>(){
			@Override
			public void apply(final Document document) {
				String time = findBucket(document.getString("_id"));
				Double count = document.getDouble("value");
				if(timeMap.containsKey(time)) {
					timeMap.put(time, timeMap.get(time)+count);
				}else {
					timeMap.put(time, count);
				}
			}
		});
		
		List<Map.Entry<String, Double>> timeList = new ArrayList<Map.Entry<String,Double>>(timeMap.entrySet());
		Collections.sort(timeList, new Comparator<Map.Entry<String,Double>>(){
			@Override
			public int compare(Map.Entry<String, Double> o1,Map.Entry<String, Double> o2) {
				return Double.compare(o2.getValue(), o1.getValue());
			}
		});
		
		for(Map.Entry<String, Double> entry : timeList) {
			System.out.println("time:"+entry.getKey()+"count"+entry.getValue());
		}
		mongoClient.close();
		
	}
	
	

}
