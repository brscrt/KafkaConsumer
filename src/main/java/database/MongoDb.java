package database;

import java.util.HashMap;
import java.util.Map;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class MongoDb {	

	public void addToTable(String table, String key, String value) {
		MongoClient mongoClient = null;
		try {
			//mongoClient = new MongoClient("localhost",27017); //for direct running in ubuntu
			mongoClient = new MongoClient("172.17.0.1",27017);  //if this app is in docker
			MongoDatabase db = mongoClient.getDatabase("datas");
			
			MongoCollection<Document> collection = db.getCollection(table);

			Map<String, Object> map = new HashMap<String, Object>();
			map.put("topic", key);
			map.put("data", value);

			collection.insertOne(new Document(map));
			System.out.println("Added to mongodb");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Failed to add to mongodb");
		} finally {
			mongoClient.close();
		}
	}
	
	public void find(){
		
		MongoClient mongoClient = null;
		try {
			mongoClient = new MongoClient("localhost", 27017);
			MongoDatabase db = mongoClient.getDatabase("datas");
			// New way to get collection
			MongoCollection<Document> collection = db.getCollection("kafka");

			System.out.println(collection.count());
			
			MongoCursor<Document> cursor = collection.find().iterator();
			
			while (cursor.hasNext()) {
				System.out.println(cursor.next());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mongoClient.close();
		}
		
		
	}
}
