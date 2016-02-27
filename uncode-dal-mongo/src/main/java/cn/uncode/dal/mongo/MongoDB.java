package cn.uncode.dal.mongo;

import com.mongodb.DB;
import com.mongodb.MongoException;


public interface MongoDB {
	

	DB getDB() throws MongoException;


	DB getDB(String username, String password) throws MongoException;
	
	
	com.mongodb.client.MongoDatabase getMongoDB() throws MongoException;


	com.mongodb.client.MongoDatabase getMongoDB(String username, String password) throws MongoException;

}
