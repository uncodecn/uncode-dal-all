package cn.uncode.dal.mongo;

import java.net.UnknownHostException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class MongoDataBase implements InitializingBean, MongoDB {
	
	private static Logger LOG = Logger.getLogger(MongoDataBase.class);
	
	private String host;
	
	private int port;
	
	private String db;
	
	private String username;
	
	private String password;
	
	private DB mongo;

	@Override
	public DB getDB() throws MongoException {
		if(null == mongo){
			try {
				mongo = initDB(host, port, db, username, password);
			} catch (UnknownHostException e) {
				LOG.error("Init mongo db error.", e);
			} 
		}
		return mongo;
	}

	@Override
	public DB getDB(String username, String password) throws MongoException {
		if(null == mongo){
			try {
				mongo = initDB(host, port, db, username, password);
			} catch (UnknownHostException e) {
				LOG.error("Init mongo db error.", e);
			} 
		}
		return mongo;
	}
	
	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setDb(String db) {
		this.db = db;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	private DB initDB(String url, Integer port, String dbName,
			String user, String password) throws UnknownHostException,
			MongoException {
		Mongo m = null;
		if (null != port) {
			m = new Mongo(url, port);
		} else {
			m = new Mongo(url);
		}
		DB db = m.getDB(dbName);
		if (!db.authenticate(user, password.toCharArray())) {
			LOG.error("Couldn't Authenticate MongoDB!!!!!!.........");
			throw new MongoException("Couldn't Authenticate !!!!!!.........");
		}
		return db;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if(StringUtils.isNotEmpty(host)){
			try {
				mongo = initDB(host, port, db, username, password);
			} catch (Exception e) {
			}
			
		}
		
	}

}
