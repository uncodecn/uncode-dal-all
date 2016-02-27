package cn.uncode.dal.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.datasource.lookup.DataSourceLookup;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;

import cn.uncode.dal.internal.shards.bo.Shard;



public class ShardsDataSource extends AbstractDataSource implements InitializingBean {
	
	//------------------------------
	// 可配置变量
	//------------------------------
	private Map<Object, Object> shardsDataSources = new LinkedHashMap<Object, Object>();
	private Object defaultDataSource;
	//------------------------------
	// 私有变量
	//------------------------------
	private DataSourceLookup dataSourceLookup = new JndiDataSourceLookup();

	private Map<Object, DataSource> resolvedShardsDataSources;
	
	private DataSource resolvedDefaultDataSource;
	
    /**
     * The public constructor.
     */
    public ShardsDataSource() {
    	super();
    }


	public void setShardsDataSources(Map<Object, Object> shardsDataSources) {
		this.shardsDataSources = shardsDataSources;
	}

	public void setDefaultDataSource(Object defaultDataSource) {
		this.defaultDataSource = defaultDataSource;
	}


	@Override
	public void afterPropertiesSet() throws Exception {
		if (this.shardsDataSources == null) {
			throw new IllegalArgumentException("Property 'shardsDataSources' is required");
		}
		this.resolvedShardsDataSources = new HashMap<Object, DataSource>(this.shardsDataSources.size());
		for (Map.Entry<Object, Object> entry : this.shardsDataSources.entrySet()) {
			DataSource dataSource = resolveSpecifiedDataSource(entry.getValue());
			LazyConnectionDataSourceProxy lazyDataSourceProxy = new LazyConnectionDataSourceProxy();
			lazyDataSourceProxy.setTargetDataSource(dataSource);
			this.resolvedShardsDataSources.put(entry.getKey(), lazyDataSourceProxy);
		}
		if (this.defaultDataSource == null) {
			throw new IllegalArgumentException("Property 'defaultDataSource' is required");
		}
		if(this.defaultDataSource != null){
			resolvedDefaultDataSource = this.resolveSpecifiedDataSource(defaultDataSource);
		}
		
	}
	


	protected DataSource resolveSpecifiedDataSource(Object dataSource) throws IllegalArgumentException {
		if (dataSource instanceof DataSource) {
			return (DataSource) dataSource;
		}else if (dataSource instanceof String) {
			return this.dataSourceLookup.getDataSource((String) dataSource);
		}else {
			throw new IllegalArgumentException(
					"Illegal data source value - only [javax.sql.DataSource] and String supported: " + dataSource);
		}
	}


	@Override
	public Connection getConnection() throws SQLException {
		return null;
	
	}


	@Override
	public Connection getConnection(String username, String password)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}


    

    

}
