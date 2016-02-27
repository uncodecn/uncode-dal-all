package cn.uncode.dal.listener;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import cn.uncode.dal.criteria.QueryCriteria;


/**
 * 
 * @author juny.ye
 */
public class OprateInfo implements Serializable {

    //
    private static final long serialVersionUID = 7100282651039776916L;
    
    private String table;
    private List<String> fields;
    private QueryCriteria queryCriteria;
    private int seconds;
    private boolean useCache;
    private Map<String, Object> resultMap;
    private List<Map<String, Object>> resultList;
    private Object result;
    private Object id;
    
    public OprateInfo(String table, Map<String, Object> resultMap) {
		this.table = table;
		this.resultMap = resultMap;
	}
    
    public OprateInfo(String table, Object id) {
		this.table = table;
		this.id = id;
	}
    
    public OprateInfo(String table, int seconds, boolean useCache, Object id, Map<String, Object> resultMap) {
		this.table = table;
		this.seconds = seconds;
		this.useCache = useCache;
		this.id = id;
		this.resultMap = resultMap;
	}
    
    public OprateInfo(String table, QueryCriteria queryCriteria) {
		this.table = table;
		this.queryCriteria = queryCriteria;
	}
    
    public OprateInfo(String table, QueryCriteria queryCriteria, 
    		int seconds, boolean useCache, Object result) {
		this.table = table;
		this.queryCriteria = queryCriteria;
		this.seconds = seconds;
		this.useCache = useCache;
		this.result = result;
	}
    
    public OprateInfo(String table, QueryCriteria queryCriteria, Map<String, Object> resultMap) {
		this.table = table;
		this.queryCriteria = queryCriteria;
		this.resultMap = resultMap;
	}
    
    public OprateInfo(String table, Object id, Map<String, Object> resultMap) {
		this.table = table;
		this.id = id;
		this.resultMap = resultMap;
	}

    public OprateInfo(String table, List<String> fields, QueryCriteria queryCriteria, 
    		int seconds, boolean useCache, List<Map<String, Object>> resultList) {
		this.table = table;
		this.fields = fields;
		this.queryCriteria = queryCriteria;
		this.seconds = seconds;
		this.useCache = useCache;
		this.resultList = resultList;
	}
    
    public OprateInfo(String table, List<String> fields,
			QueryCriteria queryCriteria, int seconds, boolean useCache,
			Map<String, Object> resultMap, List<Map<String, Object>> resultList) {
		this.table = table;
		this.fields = fields;
		this.queryCriteria = queryCriteria;
		this.seconds = seconds;
		this.useCache = useCache;
		this.resultMap = resultMap;
		this.resultList = resultList;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public List<String> getFields() {
		return fields;
	}

	public void setFields(List<String> fields) {
		this.fields = fields;
	}

	public QueryCriteria getQueryCriteria() {
		return queryCriteria;
	}

	public void setQueryCriteria(QueryCriteria queryCriteria) {
		this.queryCriteria = queryCriteria;
	}

	public int getSeconds() {
		return seconds;
	}

	public void setSeconds(int seconds) {
		this.seconds = seconds;
	}

	public boolean isUseCache() {
		return useCache;
	}

	public void setUseCache(boolean useCache) {
		this.useCache = useCache;
	}

	public Map<String, Object> getResultMap() {
		return resultMap;
	}

	public void setResultMap(Map<String, Object> resultMap) {
		this.resultMap = resultMap;
	}

	public List<Map<String, Object>> getResultList() {
		return resultList;
	}

	public void setResultList(List<Map<String, Object>> resultList) {
		this.resultList = resultList;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public Object getId() {
		return id;
	}

	public void setId(Object id) {
		this.id = id;
	}



}
