package org.fastser.dal.core;

import java.util.List;

import org.fastser.dal.criteria.QueryCriteria;
import org.fastser.dal.descriptor.QueryResult;

public interface BaseDAL {
	
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
    
    QueryResult selectByPrimaryKey(Class<?> clazz, Object id);
    
    QueryResult selectByPrimaryKey(Class<?> clazz, Object id, int seconds);
    
    QueryResult selectByPrimaryKey(List<String> fields, Class<?> clazz, Object id);
    
    QueryResult selectByPrimaryKey(List<String> fields, Class<?> clazz, Object id, int seconds);
    
    //-------------------------
  	// insert
  	//-------------------------
    int insert(Object obj);
    
    //-------------------------
  	// update
  	//-------------------------
    int updateByCriteria(Object obj, QueryCriteria queryCriteria);
    
    int updateByPrimaryKey(Object obj);
    
    //-------------------------
  	// delete
  	//-------------------------
    int deleteByPrimaryKey(Object obj);
    
    int deleteByPrimaryKey(Class<?> clazz, Object id);
    
    int deleteByCriteria(QueryCriteria queryCriteria);
    
    //-------------------------
  	// other
  	//-------------------------
    void reloadTable(String tableName);
    
    void clearCache(String tableName);
    
    void reloadTable(String database, String tableName);
    
    void clearCache(String database, String tableName);
    

    
    
}
