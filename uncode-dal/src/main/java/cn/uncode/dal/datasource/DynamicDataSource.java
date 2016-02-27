package cn.uncode.dal.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.lookup.DataSourceLookup;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;


public class DynamicDataSource extends AbstractDataSource implements InitializingBean{
	
	private int counter = 1;
	
	private static Logger LOG = LoggerFactory.getLogger(DynamicDataSource.class);
	
	private long checkTimeInterval = 10000;
	
	private ConcurrentLinkedQueue<Object> disconnectDataSources = new ConcurrentLinkedQueue<Object>();
	
	private Map<Object, Object> slaveDataSources = new HashMap<Object, Object>();

	private DataSourceLookup dataSourceLookup = new JndiDataSourceLookup();

	private Map<Object, DataSource> resolvedSlaveDataSources;
	
	private String checkAvailableSql = "select 1";
	
	private AtomicInteger lock = new AtomicInteger(0);
	
	private Object masterDataSource;
	
	private Object standbyDataSource;
	
	private DataSource resolvedMasterDataSource;
	
	private DataSource resolvedStandbyDataSource;
	
	private DataSource currentDataSource;

	
	public void setSlaveDataSources(Map<Object, Object> slaveDataSources) {
		this.slaveDataSources = slaveDataSources;
	}
	
	public void setDataSourceLookup(DataSourceLookup dataSourceLookup) {
		this.dataSourceLookup = (dataSourceLookup != null ? dataSourceLookup : new JndiDataSourceLookup());
	}
	
	protected Object determineCurrentLookupKey() {
		String dataSourceKey = DBContextHolder.getCurrentDataSourceKey();
		if(LOG.isDebugEnabled()){
			LOG.debug("-->Thread local lookup key:" +  dataSourceKey );
		}
		return dataSourceKey;
	}
	
	public void setMasterDataSource(Object masterDataSource) {
		this.masterDataSource = masterDataSource;
	}

	public void setStandbyDataSource(Object standbyDataSource) {
		this.standbyDataSource = standbyDataSource;
	}
	
	public void afterPropertiesSet() {
		if (this.slaveDataSources == null) {
			//throw new IllegalArgumentException("Property 'slaveDataSources' is required");
		}
		this.resolvedSlaveDataSources = new HashMap<Object, DataSource>(this.slaveDataSources.size());
		for (Map.Entry<Object, Object> entry : this.slaveDataSources.entrySet()) {
			DataSource dataSource = resolveSpecifiedDataSource(entry.getValue());
			this.resolvedSlaveDataSources.put(entry.getKey(), dataSource);
		}
		if (this.masterDataSource == null) {
			throw new IllegalArgumentException("Property 'masterDataSource' is required");
		}
		if(this.standbyDataSource != null){
			resolvedStandbyDataSource = this.resolveSpecifiedDataSource(standbyDataSource);
		}
		resolvedMasterDataSource = this.resolveSpecifiedDataSource(masterDataSource);
		
		Thread thread = new CheckDataSourceDaemonThread();
		thread.start();
	}
	
	@Override
	public Connection getConnection() throws SQLException {
		Object lookupKey = determineCurrentLookupKey();
		DataSource dataSource = null;
		Object dataSourceKey = null;
		if(DBContextHolder.READ.equals(lookupKey)){
			if(!this.resolvedSlaveDataSources.isEmpty()){
				if(DBContextHolder.REPORT.equals(lookupKey)){
					dataSourceKey = lookupKey;
					dataSource = resolvedSlaveDataSources.get(dataSourceKey);
				}
				if(dataSource == null){
					int size = this.resolvedSlaveDataSources.size();
					int index = 0;
					int targetIndex = 0;
					if(size > 1){
						targetIndex = RANDOM.nextInt(size);
					}
					for(Map.Entry<Object,DataSource> entry: resolvedSlaveDataSources.entrySet()) {
						if(index == targetIndex){
							dataSource = entry.getValue();
							dataSourceKey = entry.getKey();
							break;
						}
						index++;
					}
				}
			}else{
				LOG.debug("Resolved slave data source is empty.");
			}
		}else{
			if(DBContextHolder.STANDBY.equals(lookupKey)){
				dataSource = resolvedStandbyDataSource;
				dataSourceKey = DBContextHolder.STANDBY;
			}
		}
		
		if (dataSource == null) {
			dataSource = this.getCurrentDataSource();
			dataSourceKey = MASTER_DATASOURCE_KEY;
		}
		if (dataSource == null) {
			throw new IllegalStateException("Cannot determine target DataSource for lookup key [" + lookupKey + "]");
		}
		
		LOG.debug("-->Dynamic datasource, [lookup key : " + lookupKey + ", datasource key : " + dataSourceKey);
		
		try{
			Connection connection = dataSource.getConnection();
			counter = 0;
			return connection;
		}catch(SQLException sqle){
			LOG.error("Get Connection Exception " + dataSource , sqle);
			counter++;
			if(counter == 3){
				if(!disconnectDataSources.contains(dataSourceKey)){
					disconnectDataSources.add(dataSourceKey);
				}
				if(DBContextHolder.WRITE.equals(lookupKey)){
					this.switchToAvailableDataSource();
				}else if(DBContextHolder.READ.equals(lookupKey)){
					resolvedSlaveDataSources.remove(dataSourceKey);
				}else{
					this.switchToAvailableDataSource();
				}
				counter = 0;
			}
			
			throw sqle;
		}
	}
	
	private static final String MASTER_DATASOURCE_KEY = "_master";
	private static final Random RANDOM = new Random();
	
	@Override
	public Connection getConnection(String username, String password)throws SQLException {
		Object lookupKey = determineCurrentLookupKey();
		DataSource dataSource = null;
		Object dataSourceKey = null;
		if(DBContextHolder.READ.equals(lookupKey)){
			if(!this.resolvedSlaveDataSources.isEmpty()){
				if(DBContextHolder.REPORT.equals(lookupKey)){
					dataSourceKey = lookupKey;
					dataSource = resolvedSlaveDataSources.get(dataSourceKey);
				}
				if(dataSource == null){
					int size = this.resolvedSlaveDataSources.size();
					int index = 0;
					int targetIndex = 0;
					if(size > 1){
						targetIndex = RANDOM.nextInt(size);
					}
					for(Map.Entry<Object,DataSource> entry: resolvedSlaveDataSources.entrySet()) {
						if(index == targetIndex){
							dataSource = entry.getValue();
							dataSourceKey = entry.getKey();
							break;
						}
						index++;
					}
				}
			}else{
				LOG.debug("Resolved slave data source is empty.");
			}
		}else{
			if(DBContextHolder.STANDBY.equals(lookupKey)){
				dataSource = resolvedStandbyDataSource;
				dataSourceKey = DBContextHolder.STANDBY;
			}
		}
		
		if (dataSource == null) {
			dataSource = this.getCurrentDataSource();
			dataSourceKey = MASTER_DATASOURCE_KEY;
		}
		if (dataSource == null) {
			throw new IllegalStateException("Cannot determine target DataSource for lookup key [" + lookupKey + "]");
		}
		
		LOG.debug("-->Dynamic datasource, [lookup key : " + lookupKey + ", datasource key : " + dataSourceKey);
		
		try{
			Connection connection = dataSource.getConnection();
			counter = 0;
			return connection;
		}catch(SQLException sqle){
			LOG.error("Get Connection Exception " + dataSource , sqle);
			counter++;
			if(counter == 3){
				if(!disconnectDataSources.contains(dataSourceKey)){
					disconnectDataSources.add(dataSourceKey);
				}
				if(DBContextHolder.WRITE.equals(lookupKey)){
					this.switchToAvailableDataSource();
				}else if(DBContextHolder.READ.equals(lookupKey)){
					resolvedSlaveDataSources.remove(dataSourceKey);
				}else{
					this.switchToAvailableDataSource();
				}
				counter = 0;
			}
			
			throw sqle;
		}
	
	}
	
	protected DataSource resolveSpecifiedDataSource(Object dataSource) throws IllegalArgumentException {
		if (dataSource instanceof DataSource) {
			return (DataSource) dataSource;
		}
		else if (dataSource instanceof String) {
			return this.dataSourceLookup.getDataSource((String) dataSource);
		}
		else {
			throw new IllegalArgumentException(
					"Illegal data source value - only [javax.sql.DataSource] and String supported: " + dataSource);
		}
	}
	
	
	protected void switchToAvailableDataSource(){
		try{
			if(lock.incrementAndGet() > 1){
				return;
			}
			
			if(currentDataSource == resolvedStandbyDataSource){
				if(this.isDataSourceAvailable(resolvedMasterDataSource)){
					currentDataSource = resolvedMasterDataSource;
				}
			}else{
				currentDataSource = resolvedMasterDataSource;
				if(!this.isDataSourceAvailable(resolvedMasterDataSource)){
					currentDataSource =  resolvedStandbyDataSource;
				}
			}
		}finally{
			lock.decrementAndGet();
		}
	}
	
	
	private boolean isDataSourceAvailable(DataSource dataSource){
		Connection  conn = null;
		try{
			 conn = dataSource.getConnection();
			 Statement stmt = conn.createStatement();
			 boolean success = stmt.execute(checkAvailableSql); 
			 stmt.close();
			 return success;
		}catch(SQLException e){
			LOG.error("CheckDataSourceAvailable Exception", e);
			return false;
		}finally{
			if(conn != null){
				try {
					conn.close();
				} catch (SQLException e) {
					LOG.error("Close Connection Exception", e);
				}
			}
		}
	}
	
	public DataSource getCurrentDataSource(){
		if(this.currentDataSource == null){
			this.currentDataSource = this.resolveSpecifiedDataSource(masterDataSource);
		}
		return this.currentDataSource;
	}
	
	private class CheckDataSourceDaemonThread extends Thread{
		public CheckDataSourceDaemonThread(){
			this.setDaemon(true);
			this.setName("CheckDataSourceDaemonThread");
		}
		 @Override
		 public void run() {
			 while(true){
				 if(!disconnectDataSources.isEmpty()){
					 for(Object name:disconnectDataSources){
						 if(MASTER_DATASOURCE_KEY.equals(name)){
							 if(isDataSourceAvailable(resolvedMasterDataSource)){
								 disconnectDataSources.remove(name);
								 switchToAvailableDataSource();
							 }
						 }else{
							 DataSource dataSource = resolveSpecifiedDataSource(slaveDataSources.get(name));
							 if(isDataSourceAvailable(dataSource)){
								 disconnectDataSources.remove(name);
								 resolvedSlaveDataSources.put(name, dataSource);
							 }
						 }
						 
						 try {
							Thread.sleep(checkTimeInterval);
						 } catch (InterruptedException e) {
							logger.warn("Check Master InterruptedException", e);
						 }
					 }
				 }else{
					 try {
						Thread.sleep(checkTimeInterval);
					 } catch (InterruptedException e) {
						logger.warn("Check Master InterruptedException", e);
					 }
				 }
			 }
		 }
	}

}
