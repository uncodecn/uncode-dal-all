package cn.uncode.dal.descriptor.db.impl;


import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.uncode.dal.cache.CacheManager;
import cn.uncode.dal.descriptor.Column;
import cn.uncode.dal.descriptor.Content;
import cn.uncode.dal.descriptor.Table;
import cn.uncode.dal.descriptor.db.ResolveDataBase;
import cn.uncode.dal.descriptor.resolver.FieldSqlGenerator;
import cn.uncode.dal.internal.util.message.Messages;

public class SimpleResolveDatabase implements ResolveDataBase{
    
    private static final Logger LOG = LoggerFactory.getLogger(SimpleResolveDatabase.class);
    
    private static final ConcurrentMap<Object, Object> cache = new ConcurrentHashMap<Object, Object>();
    
    private FieldSqlGenerator fieldSqlGenerator;
    
    private CacheManager cacheManager;
    
    private DataSource dataSource;
    

    public Table loadTable(String tableName){
    	return loadTable(null, tableName);
    }
    
    public Table loadTable(String database, String tableName) {
    	return loadTable(database, tableName, null);
    }
    
	public Table loadTable(String database, String tableName, String versionField) {
    	Content content = null;
    	String cacheKey = tableName;
    	if(StringUtils.isNotBlank(database)){
    		cacheKey = database + "#" + tableName;
    	}
        if(cache.containsKey(cacheKey)){
        	content = (Content)cache.get(cacheKey);
        }
        Table table = null;
        if(content != null){
        	table = new Table(content);
            return table;
        }
        
        content = new Content();
        boolean tableExist = false;
        ResultSet pkRS = null;
        ResultSet columnsRS = null;
        ResultSet indexRS = null;
        try {
            DatabaseMetaData databaseMetaData = dataSource.getConnection().getMetaData();
            //primary keys
            pkRS = databaseMetaData.getPrimaryKeys(null, null,tableName);
            while (pkRS.next()) {
                String columnName = pkRS.getString("COLUMN_NAME");
                content.addPrimaryFieldName(columnName);
                tableExist = true;
            }
            
            //columns
            columnsRS = databaseMetaData.getColumns(null, null, tableName, null);
            while (columnsRS.next()) {
                Column field = new Column();
                field.setJdbcType(columnsRS.getInt("DATA_TYPE"));
                field.setFieldName(columnsRS.getString("COLUMN_NAME"));
                field.setRequest(columnsRS.getBoolean("NULLABLE"));
                field.setLength(columnsRS.getInt("COLUMN_SIZE"));
                if(StringUtils.isNotEmpty(columnsRS.getString("REMARKS"))){
                    field.setName(columnsRS.getString("REMARKS"));
                }
                if(fieldSqlGenerator != null){
                    field.setFieldSql(fieldSqlGenerator.buildSingleSql(field));
                }
                if(content.getPrimaryKey().getFields().contains(field.getFieldName())){
                    field.setPrimaryKey(true);
                }
                content.addField(field);
                tableExist = true;
            }
          //indexs
          indexRS = databaseMetaData.getIndexInfo(null, null, tableName, false, false);
          while (indexRS.next()) {
        	  String indexName = indexRS.getString("INDEX_NAME");//索引的名称  
        	  if(!"PRIMARY".equals(indexName)){
        		  String columnName = indexRS.getString("COLUMN_NAME");//列名  
        		  if(content.getIndexs().containsKey(indexName)){
        			  content.getIndexs().get(indexName).add(columnName);
        		  }else{
        			  List<String> list = new ArrayList<String>();
        			  list.add(columnName);
        			  content.getIndexs().put(indexName, list);
        		  }
        		  content.getIndexFields().put(columnName, indexName);
        	  }
            }
        } catch (SQLException e) {
            LOG.error(Messages.getString("RuntimeError.2"), e);
        } finally {
            closeResultSet(pkRS);
            closeResultSet(columnsRS);
        }
        
        if(tableExist){
        	content.setTableName(tableName);
        	content.setDatabase(database);
        	content.caculationAllColumn();
        	if(StringUtils.isNotEmpty(versionField) 
        			&& content.getFields().containsKey(versionField)){
        		content.setVersionField(content.getFields().get(versionField));
        	}
            cache.put(cacheKey, content);
            if(cacheManager != null){
                cacheManager.getCache().putObject(cacheKey, content);
            }
            return new Table(content);
        }else{
            return null;
        }
	}
    
    
    @Override
    public Table reloadTable(String tableName) {
        return reloadTable(null, tableName);
    }
    
    @Override
	public Table reloadTable(String database, String tableName) {
        return reloadTable(database, tableName, null);
	}
    
	public Table reloadTable(String database, String tableName, String versionField) {
    	String cacheKey = tableName;
    	if(StringUtils.isNotBlank(database)){
    		cacheKey = database + "#" + tableName;
    	}
    	cache.remove(cacheKey);
        if(cacheManager != null){
            cacheManager.getCache().removeObject(cacheKey);
        }
        return loadTable(database, tableName, versionField);
	}
    

    private void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                // ignore
                ;
            }
        }
    }


    public void setFieldSqlGenerator(FieldSqlGenerator fieldSqlGenerator) {
        this.fieldSqlGenerator = fieldSqlGenerator;
    }


    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }


    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }


	@Override
	public List<String> loadTables() {
		List<String> tables = new ArrayList<String>();
        ResultSet tableRS = null;
        try {
        	DatabaseMetaData databaseMetaData = dataSource.getConnection().getMetaData();
            tableRS = databaseMetaData.getTables(null, "%", "%", new String[]{"TABLE"});
            while (tableRS.next()) {
                String name = tableRS.getString("TABLE_NAME");
                tables.add(name);
            }
        } catch (SQLException e) {
            LOG.error(Messages.getString("RuntimeError.2"), e);
        } finally {
            closeResultSet(tableRS);
        }
		return tables;
	}



	

    

    

    

}
