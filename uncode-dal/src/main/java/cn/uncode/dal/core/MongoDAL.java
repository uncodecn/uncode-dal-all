package cn.uncode.dal.core;

import java.util.List;
import java.util.Map;

import cn.uncode.dal.criteria.QueryCriteria;
import cn.uncode.dal.descriptor.QueryResult;

public interface MongoDAL {
	
	int NO_CACHE = -2;
	
	int PERSISTENT_CACHE = 0;
	
	String PAGE_INDEX_KEY = "pageIndex";
	String PAGE_SIZE_KEY = "pageSize";
	String PAGE_COUNT_KEY = "pageCount";
	String RECORD_TOTAL_KEY = "recordTotal";
    
	//-------------------------
	// selectByCriteria
	//-------------------------
    QueryResult selectByCriteria(List<String> fields, QueryCriteria queryCriteria);
    
    QueryResult selectByCriteria(String[] fields, QueryCriteria queryCriteria);
    
    QueryResult selectByCriteria(List<String> fields, QueryCriteria queryCriteria, int seconds);
    
    QueryResult selectByCriteria(String[] fields, QueryCriteria queryCriteria, int seconds);
    
    QueryResult selectByCriteria(QueryCriteria queryCriteria);
    
    QueryResult selectByCriteria(QueryCriteria queryCriteria, int seconds);
    
    QueryResult selectPageByCriteria(List<String> fields, QueryCriteria queryCriteria);
    
    QueryResult selectPageByCriteria(String[] fields, QueryCriteria queryCriteria);
    
    QueryResult selectPageByCriteria(List<String> fields, QueryCriteria queryCriteria, int seconds);
    
    QueryResult selectPageByCriteria(String[] fields, QueryCriteria queryCriteria, int seconds);
    
    QueryResult selectPageByCriteria(QueryCriteria queryCriteria);
    
    QueryResult selectPageByCriteria(QueryCriteria queryCriteria, int seconds);
    
    //-------------------------
  	// countByCriteria
  	//-------------------------
    int countByCriteria(QueryCriteria queryCriteria);
    
    int countByCriteria(QueryCriteria queryCriteria, int seconds);
    
    //-------------------------
  	// selectByPrimaryKey
  	//-------------------------
    QueryResult selectByPrimaryKey(Object obj);
    
    QueryResult selectByPrimaryKey(Object obj, int seconds);
    
    QueryResult selectByPrimaryKey(List<String> fields, Object obj);
    
    QueryResult selectByPrimaryKey(String[] fields, Object obj);
    
    QueryResult selectByPrimaryKey(List<String> fields, Object obj, int seconds);
    
    QueryResult selectByPrimaryKey(String[] fields, Object obj, int seconds);
    
    QueryResult selectByPrimaryKey(String[] fields, String database, Object obj, int seconds);
    
    QueryResult selectByPrimaryKey(Class<?> clazz, Object id);
    
    QueryResult selectByPrimaryKey(String table, Object id);
    
    QueryResult selectByPrimaryKey(Class<?> clazz, Object id, int seconds);
    
    QueryResult selectByPrimaryKey(String table, Object id, int seconds);
    
    QueryResult selectByPrimaryKey(List<String> fields, Class<?> clazz, Object id);
    
    QueryResult selectByPrimaryKey(List<String> fields, String table, Object id);
    
    QueryResult selectByPrimaryKey(List<String> fields, Class<?> clazz, Object id, int seconds);
    
    QueryResult selectByPrimaryKey(List<String> fields, String table, Object id, int seconds);
    
    //-------------------------
  	// insert
  	//-------------------------
    Object insert(Object obj);
    
    Object insert(String table, Map<String, Object> obj);
    
    Object insert(String database, String table, Map<String, Object> obj);
    
    void asynInsert(Object obj);
    
    void asynInsert(String table, Map<String, Object> obj);
    
    void asynInsert(String database, String table, Map<String, Object> obj);
    
    //-------------------------
  	// update
  	//-------------------------
    int updateByCriteria(Object obj, QueryCriteria queryCriteria);
    
    int updateByPrimaryKey(Object obj);
    
    int updateByPrimaryKey(String table, Map<String, Object> obj);
    
    int updateByPrimaryKey(String database, String table, Map<String, Object> obj);
    
    //-------------------------
  	// delete
  	//-------------------------
    int deleteByPrimaryKey(Object obj);
    
    int deleteByPrimaryKey(String table, Map<String, Object> obj);
    
    int deleteByPrimaryKey(Class<?> clazz, Object id);
    
    int deleteByPrimaryKey(String table, Object id);
    
    int deleteByPrimaryKey(String database, String table, Object id);
    
    int deleteByCriteria(QueryCriteria queryCriteria);
    
    //-------------------------
  	// other
  	//-------------------------
    void reloadTable(String tableName);
    
    void clearCache(String tableName);
    
    void reloadTable(String database, String tableName);
    
    void clearCache(String database, String tableName);
    
    public Object getTemplate();
    
    public void runScript(String script);
    
    
}
