package cn.uncode.dal.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import cn.uncode.dal.cache.CacheManager;
import cn.uncode.dal.criteria.Model;
import cn.uncode.dal.criteria.QueryCriteria;
import cn.uncode.dal.descriptor.Content;
import cn.uncode.dal.descriptor.QueryResult;
import cn.uncode.dal.descriptor.Table;
import cn.uncode.dal.descriptor.db.ResolveDataBase;
import cn.uncode.dal.descriptor.resolver.JavaType;
import cn.uncode.dal.descriptor.resolver.JavaTypeConversion;
import cn.uncode.dal.descriptor.resolver.JavaTypeResolver;
import cn.uncode.dal.exception.StaleObjectStateException;
import cn.uncode.dal.internal.util.message.Messages;
import cn.uncode.dal.router.DefaultMasterSlaveRouter;
import cn.uncode.dal.router.MasterSlaveRouter;

public abstract class AbstractBaseDAL implements BaseDAL {

    private static final Logger LOG = Logger.getLogger(AbstractBaseDAL.class);

    protected CacheManager cacheManager;

    protected ResolveDataBase resolveDatabase;

    protected MasterSlaveRouter router = new DefaultMasterSlaveRouter();
    
    protected boolean useCache = true;
    
    protected String version;
    
    public QueryResult selectPageByCriteria(QueryCriteria queryCriteria){
    	List<String> fields = null;
    	return selectPageByCriteria(fields, queryCriteria, PERSISTENT_CACHE);
    }
    
    public QueryResult selectPageByCriteria(QueryCriteria queryCriteria, int seconds){
    	List<String> fields = null;
    	return selectPageByCriteria(fields, queryCriteria, seconds);
    }
    
    public QueryResult selectPageByCriteria(String[] fields, QueryCriteria queryCriteria){
    	return selectPageByCriteria(Arrays.asList(fields), queryCriteria, PERSISTENT_CACHE);
    }
    
    public QueryResult selectPageByCriteria(List<String> fields, QueryCriteria queryCriteria){
    	return selectPageByCriteria(fields, queryCriteria, PERSISTENT_CACHE);
    }
    
    public QueryResult selectPageByCriteria(String[] fields, QueryCriteria queryCriteria, int seconds){
    	return selectPageByCriteria(Arrays.asList(fields), queryCriteria, seconds);
    }
    
    public QueryResult selectPageByCriteria(List<String> fields, QueryCriteria queryCriteria, int seconds){
    	int total = countByCriteria(queryCriteria, seconds);
    	if(total > 0){
    		int pageCount = total / queryCriteria.getPageSize();
            if (total % queryCriteria.getPageSize() != 0) {
                pageCount++;
            }
            if(queryCriteria.getPageIndex() > pageCount){
            	queryCriteria.setPageIndex(pageCount);
            }
            QueryResult queryResult = selectByCriteria(fields, queryCriteria, seconds);
            Map<String, Object> page = new HashMap<String, Object>();
            page.put(PAGE_INDEX_KEY, queryCriteria.getPageIndex());
            page.put(PAGE_SIZE_KEY, queryCriteria.getPageSize());
            page.put(PAGE_COUNT_KEY, pageCount);
            page.put(RECORD_TOTAL_KEY, total);
            queryResult.setPage(page);
            return queryResult;
    	}
    	return null;
    }
    
    @Override
    public QueryResult selectByCriteria(List<String> fields, QueryCriteria queryCriteria, int seconds) {

        if (router != null) {
            router.routeToSlave();
        }

        QueryResult queryResult = new QueryResult();

        int hashcode = 0;
        if (fields != null) {
            for (String str : fields) {
                hashcode += str.hashCode();
            }
        }
        hashcode += queryCriteria.hashCode();
        String cacheKey = queryCriteria.getTable() + "_selectByCriteria_" + hashcode;
        if (StringUtils.isNotBlank(queryCriteria.getDatabase())) {
            cacheKey = queryCriteria.getDatabase() + "#" + cacheKey;
        }
        if (cacheManager != null && seconds != NO_CACHE && useCache) {
            List<Map<String, Object>> value = (List<Map<String, Object>>) cacheManager.getCache().getObject(cacheKey);
            if (value != null && value.size() > 0) {
                queryResult.setResultList(value);
                return queryResult;
            }
        }
        Table table = retrievalTableByQueryCriteria(queryCriteria);
        if (fields != null && fields.size() > 0) {
            LinkedHashMap<String, Object> fieldMap = new LinkedHashMap<String, Object>();
            for (String field : fields) {
                if (table.getFields().containsKey(field)) {
                    fieldMap.put(field, true);
                }
            }
            table.setParams(fieldMap);
        }
        table.setQueryCriteria(queryCriteria);

        List<Map<String, Object>> result = _selectByCriteria(table);
        
        //查询结果存在,才进行缓存
        if (cacheManager != null && seconds != NO_CACHE && useCache && result.size() > 0) {
            if (seconds > 0) {
                cacheManager.getCache().putObject(cacheKey, result, seconds);
            } else {
        		cacheManager.getCache().putObject(cacheKey, result);
            }
        }

        if (result != null) {
            queryResult.setResultList(result);
            return queryResult;
        } else {
            return null;
        }
    }

    public abstract List<Map<String, Object>> _selectByCriteria(final Table table);
    
    public abstract boolean isNoSql();

    /**
     * 
     * 
     * @param queryCriteria query criteria
     * @return table
     */
    protected Table retrievalTableByQueryCriteria(QueryCriteria queryCriteria) {
        if (queryCriteria == null || StringUtils.isEmpty(queryCriteria.getTable())) {
            LOG.error(Messages.getString("RuntimeError.8", "queryCriteria"));
            throw new RuntimeException(Messages.getString("RuntimeError.8", "queryCriteria"));
        }
        Table table = null;
        if(isNoSql()){
        	Content content = new Content();
        	content.setTableName(queryCriteria.getTable());
        	content.setDatabase(queryCriteria.getDatabase());
        	table = new Table(content);
        }else{
        	table = resolveDatabase.loadTable(queryCriteria.getDatabase(), queryCriteria.getTable(), version);
        }
        if (table == null) {
            LOG.error(Messages.getString("RuntimeError.9", queryCriteria.getTable()));
            throw new RuntimeException(Messages.getString("RuntimeError.9", queryCriteria.getTable()));
        }
        return table;
    }

    @Override
    public int countByCriteria(QueryCriteria queryCriteria, int seconds) {

        if (router != null) {
            router.routeToSlave();
        }

        int hashcode = 0;
        hashcode += queryCriteria.hashCode();
        String cacheKey = queryCriteria.getTable() + "_countByCriteria_" + hashcode;
        if (StringUtils.isNotBlank(queryCriteria.getDatabase())) {
            cacheKey = queryCriteria.getDatabase() + "#" + cacheKey;
        }
        if (cacheManager != null && seconds != NO_CACHE && useCache) {
            Integer value = (Integer) cacheManager.getCache().getObject(cacheKey);
            if (value != null && value > 0) {
                return value;
            }
        }
        Table table = retrievalTableByQueryCriteria(queryCriteria);
        table.setQueryCriteria(queryCriteria);
        int result = _countByCriteria(table);
        if (cacheManager != null && seconds != NO_CACHE && useCache && result > 0) {
            if (seconds > 0) {
                cacheManager.getCache().putObject(cacheKey, result, seconds);
            } else {
                cacheManager.getCache().putObject(cacheKey, result);
            }
        }
        return result;
    }

    public abstract int _countByCriteria(final Table table);
    
    
    public QueryResult selectByPrimaryKey(Object obj) {
    	List<String> fields = null;
    	return selectByPrimaryKey(fields, obj, PERSISTENT_CACHE);
	}
    
	public QueryResult selectByPrimaryKey(Object obj, int seconds) {
    	List<String> fields = null;
    	return selectByPrimaryKey(fields, obj, seconds);
	}
	
	@Override
	public QueryResult selectByPrimaryKey(String[] fields, Object obj) {
		return selectByPrimaryKey(Arrays.asList(fields), obj, PERSISTENT_CACHE);
	}

	@Override
	public QueryResult selectByPrimaryKey(String[] fields, Object obj, int seconds) {
		return selectByPrimaryKey(Arrays.asList(fields), obj, seconds);
	}
    
    public QueryResult selectByPrimaryKey(List<String> fields, Object obj){
    	return selectByPrimaryKey(fields, obj, PERSISTENT_CACHE);
    }
    
    public QueryResult selectByPrimaryKey(List<String> fields, Object obj, int seconds){
    	return selectByPrimaryKey(fields, new Model(obj), seconds);
    }
    
    public QueryResult selectByPrimaryKey(Class<?> clazz, Object id){
    	return selectByPrimaryKey(null, clazz, id, PERSISTENT_CACHE);
    }
    
	public QueryResult selectByPrimaryKey(Class<?> clazz, Object id, int seconds) {
		return selectByPrimaryKey(null, clazz, id, seconds);
	}
	
    public QueryResult selectByPrimaryKey(List<String> fields, Class<?> clazz, Object id){
    	return selectByPrimaryKey(fields, clazz, id, PERSISTENT_CACHE);
    }
    
    public QueryResult selectByPrimaryKey(List<String> fields, Class<?> clazz, Object id, int seconds){
    	Model model = new Model(clazz);
        model.setSinglePrimaryKey(id);
        return selectByPrimaryKey(fields, model, seconds);
    }
    
    @Override
	public QueryResult selectByPrimaryKey(String table, Object id) {
    	List<String> fields = null;
    	Model model = new Model(table);
        model.setSinglePrimaryKey(id);
        return selectByPrimaryKey(fields, model, PERSISTENT_CACHE);
	}


	@Override
	public QueryResult selectByPrimaryKey(String table, Object id, int seconds) {
		List<String> fields = null;
    	Model model = new Model(table);
        model.setSinglePrimaryKey(id);
        return selectByPrimaryKey(fields, model, seconds);
	}


	@Override
	public QueryResult selectByPrimaryKey(List<String> fields, String table, Object id) {
    	Model model = new Model(table);
        model.setSinglePrimaryKey(id);
        return selectByPrimaryKey(fields, model, PERSISTENT_CACHE);
	}


	@Override
	public QueryResult selectByPrimaryKey(List<String> fields, String table, Object id, int seconds) {
		Model model = new Model(table);
        model.setSinglePrimaryKey(id);
        return selectByPrimaryKey(fields, model, seconds);
	}
	
	@Override
	public  QueryResult selectByPrimaryKey(String[] fields, String database, Object obj, int seconds){
		Model model = new Model(obj);
		model.setDatabase(database);
		return selectByPrimaryKey(fields, model, seconds);
	}

    private QueryResult selectByPrimaryKey(List<String> fields, Model model, int seconds) {
        if (model == null) {
            return null;
        }
        if (router != null) {
            router.routeToSlave();
        }

        QueryResult queryResult = new QueryResult();
        int hashcode = 0;
        if (fields != null) {
            for (String str : fields) {
                hashcode += str.hashCode();
            }
        }
        hashcode += model.hashCode();
        String cacheKey = model.getTableName() + "_selectByPrimaryKey_" + hashcode;
        if (StringUtils.isNotBlank(model.getDatabase())) {
            cacheKey = model.getDatabase() + "#" + cacheKey;
        }
        if (cacheManager != null && seconds != NO_CACHE && useCache) {
            Map<String, Object> value = (Map<String, Object>) cacheManager.getCache().getObject(cacheKey);
            if (value != null && value.size() > 0) {
                queryResult.setResultMap(value);
                return queryResult;
            }
        }
        Table table = retrievalTableByModel(model);
        if (fields != null && fields.size() > 0) {
            LinkedHashMap<String, Object> fieldMap = new LinkedHashMap<String, Object>();
            for (String field : fields) {
                if (table.getFields().containsKey(field)) {
                    fieldMap.put(field, true);
                }
            }
            table.setParams(fieldMap);
        }
        if (model != null) {
        	if(isNoSql()){
        		if (null != model.getSinglePrimaryKey()) {
					LinkedHashMap<String, Object> condistions = new LinkedHashMap<String, Object>();
				    condistions.put("_id", model.getSinglePrimaryKey());
				    table.setConditions(condistions);
				}
        	}else{
		        List<String> names = table.getPrimaryKey().getFields();
				if (null != model.getSinglePrimaryKey()) {
					if(null != names && names.size() > 0){
						LinkedHashMap<String, Object> condistions = new LinkedHashMap<String, Object>();
					    condistions.put(names.get(0), model.getSinglePrimaryKey());
					    table.setConditions(condistions);
					}
				} else {
				    table.setConditions(model.getContent());
				}
			}
        }
        Map<String, Object> result = _selectByPrimaryKey(table);

        if (cacheManager != null && seconds != NO_CACHE && useCache && result.size() > 0) {
            if (seconds > 0) {
                cacheManager.getCache().putObject(cacheKey, result, seconds);
            } else {
                cacheManager.getCache().putObject(cacheKey, result);
            }
        }
        if (result != null) {
            queryResult.setResultMap(result);
            return queryResult;
        } else {
            return null;
        }
    }

    public abstract Map<String, Object> _selectByPrimaryKey(final Table table);

    /**
     * 
     * 
     * @param model instance
     * @return table
     */
    protected Table retrievalTableByModel(Model model) {
        if (model == null || StringUtils.isEmpty(model.getTableName())) {
            LOG.error(Messages.getString("RuntimeError.8", "model"));
            throw new RuntimeException(Messages.getString("RuntimeError.8", "model"));
        }
        Table table = null;
        if(isNoSql()){
        	Content content = new Content();
        	content.setTableName(model.getTableName());
        	content.setDatabase(model.getDatabase());
        	table = new Table(content);
        }else{
        	table = resolveDatabase.loadTable(model.getDatabase(), model.getTableName(), version);
        }
        if (table == null) {
            LOG.error(Messages.getString("RuntimeError.9", model.getTableName()));
            throw new RuntimeException(Messages.getString("RuntimeError.9", model.getTableName()));
        }
        return table;
    }
    
    @Override
    public Object insert(Object obj) {
    	return insert(new Model(obj));
    }
    
    public Object insert(String table, Map<String, Object> obj) {
    	Model model = new Model(table);
    	model.addContent(obj);
    	return insert(model);
    }
    
    @Override
	public Object insert(String database, String table, Map<String, Object> obj) {
    	Model model = new Model(database, table);
    	model.addContent(obj);
    	return insert(model);
	}

    private Object insert(Model model) {
        if (router != null) {
            router.routeToMaster();
        }
        Table table = retrievalTableByModel(model);
        if (model != null && model.getContent() != null && model.getContent().size() > 0) {
            table.setParams(model.getContent());
        } else {
            LOG.error(Messages.getString("RuntimeError.8", "model.params"));
            throw new RuntimeException(Messages.getString("RuntimeError.8", "model.params"));
        }

        int result = _insert(table);
        Object idObj = null;
        if(result > 0){
        	idObj = table.getParams().get("id");
        	if(null == idObj){
        		idObj = result;
        	}
        }

        if (cacheManager != null && useCache) {
            String cacheKey = model.getTableName();
            if (StringUtils.isNotEmpty(model.getDatabase())) {
                cacheKey = model.getDatabase() + "#" + cacheKey;
            }
            cacheManager.getCache().clear(cacheKey);
        }

        return idObj;
    }


    /**
     * insert option
     * 
     * @param table table instance
     * @return result
     */
    public abstract int _insert(Table table);

    @Override
	public int updateByCriteria(Object obj, QueryCriteria queryCriteria) {
    	return updateByCriteria(new Model(obj), queryCriteria);
	}

    private int updateByCriteria(Model model, QueryCriteria queryCriteria) {
        if (router != null) {
            router.routeToMaster();
        }
        Table table = retrievalTableByQueryCriteria(queryCriteria);
        if (model != null && model.getContent() != null && model.getContent().size() > 0) {
            table.setParams(model.getContent());
        } else {
            LOG.error(Messages.getString("RuntimeError.8", "model.params"));
            throw new RuntimeException(Messages.getString("RuntimeError.8", "model.params"));
        }
        
        table.setQueryCriteria(queryCriteria);
        
        int result = _updateByCriteria(table);
        if (cacheManager != null && useCache) {
            String cacheKey = model.getTableName();
            if (StringUtils.isNotEmpty(model.getDatabase())) {
                cacheKey = model.getDatabase() + "#" + cacheKey;
            }
            cacheManager.getCache().clear(cacheKey);
        }

        return result;
    }

    public abstract int _updateByCriteria(Table table);

    @Override
    public int updateByPrimaryKey(Object obj) {
    	return updateByPrimaryKey(new Model(obj));
	}
    
    public int updateByPrimaryKey(String table, Map<String, Object> obj) {
    	Model model = new Model(table);
    	model.addContent(obj);
    	return updateByPrimaryKey(model);
	}
    
    @Override
	public int updateByPrimaryKey(String database, String table,
			Map<String, Object> obj) {
    	Model model = new Model(database, table);
    	model.addContent(obj);
    	return updateByPrimaryKey(model);
	}

    private int updateByPrimaryKey(Model model) {
        if (router != null) {
            router.routeToMaster();
        }
        Table table = retrievalTableByModel(model);
        if (model != null && model.getContent() != null && model.getContent().size() > 0) {
        	if(isNoSql()){
        		if (null != model.getSinglePrimaryKey()) {
                    model.getContent().put("_id", model.getSinglePrimaryKey());
                }
        	}else{
        		List<String> names = table.getPrimaryKey().getFields();
                if (null != model.getSinglePrimaryKey() && names.size() == 1) {
                    model.getContent().put(names.get(0), model.getSinglePrimaryKey());
                }
        	}
            LinkedHashMap<String, Object> params = new LinkedHashMap<String, Object>();
            LinkedHashMap<String, Object> conditions = new LinkedHashMap<String, Object>();
            Iterator<String> iter = model.getContent().keySet().iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                Object value = model.getContent().get(key);
                if(null != value){
                	if(isNoSql()){
                		params.put(key, value);
                	}else{
                		JavaType javaType = JavaTypeResolver.calculateJavaType(table.getField(key).getJdbcType());
                        if (table.getPrimaryKey().getFields().contains(key)) {
                        	conditions.put(key, JavaTypeConversion.convert(javaType, value));
                        } else {
                            params.put(key, JavaTypeConversion.convert(javaType, value));
                        }
                	}
                }else{
                	params.put(key, null);
                }
            }
            if(table.hasVersion()){
            	Object value = model.getVersion();
            	if(null == value){
            		throw new StaleObjectStateException("Version is request.");
            	}
            	conditions.put(version,  value);
            }
            table.setParams(params);
            table.setConditions(conditions);
        } else {
            LOG.error(Messages.getString("RuntimeError.8", "model.params"));
            throw new RuntimeException(Messages.getString("RuntimeError.8", "model.params"));
        }
        int result = _updateByPrimaryKey(table);

        if (cacheManager != null && useCache) {
            String cacheKey = model.getTableName();
            if (StringUtils.isNotEmpty(model.getDatabase())) {
                cacheKey = model.getDatabase() + "#" + cacheKey;
            }
            cacheManager.getCache().clear(cacheKey);
        }

        return result;
    }
    
    

    public abstract int _updateByPrimaryKey(Table table);
    
    
    public int deleteByPrimaryKey(Class<?> clazz, Object id){
    	Model model = new Model(clazz);
        model.setSinglePrimaryKey(id);
        return deleteByPrimaryKey(model);
    }
    
    public int deleteByPrimaryKey(String table, Object id){
    	Model model = new Model(table);
        model.setSinglePrimaryKey(id);
        return deleteByPrimaryKey(model);
    }
    
    @Override
	public int deleteByPrimaryKey(String database, String table, Object id) {
    	Model model = new Model(database, table);
        model.setSinglePrimaryKey(id);
        return deleteByPrimaryKey(model);
	}
    
    public int deleteByPrimaryKey(Object obj){
    	return deleteByPrimaryKey(new Model(obj));
    }
    
    public int deleteByPrimaryKey(String table, Map<String, Object> obj){
    	Model model = new Model(table);
    	model.addContent(obj);
    	return deleteByPrimaryKey(model);
    }

    private int deleteByPrimaryKey(Model model) {
        if (router != null) {
            router.routeToMaster();
        }
        Table table = retrievalTableByModel(model);
        if (model != null) {
        	if(isNoSql()){
        		if (null != model.getSinglePrimaryKey()) {
	                LinkedHashMap<String, Object> condistions = new LinkedHashMap<String, Object>();
	                condistions.put("_id", model.getSinglePrimaryKey());
	                table.setConditions(condistions);
	            }
        	}else{
	            List<String> names = table.getPrimaryKey().getFields();
	            if (null != model.getSinglePrimaryKey() && names.size() == 1) {
	                LinkedHashMap<String, Object> condistions = new LinkedHashMap<String, Object>();
	                condistions.put(names.get(0), model.getSinglePrimaryKey());
	                table.setConditions(condistions);
	            } else {
	                table.setConditions(model.getContent());
	            }
        	}
        } else {
            LOG.error(Messages.getString("RuntimeError.8", "model.conditions"));
            throw new RuntimeException(Messages.getString("RuntimeError.8", "model.conditions"));
        }

        int result = _deleteByPrimaryKey(table);
        if (cacheManager != null && useCache) {
            String cacheKey = model.getTableName();
            if (StringUtils.isNotEmpty(model.getDatabase())) {
                cacheKey = model.getDatabase() + "#" + cacheKey;
            }
            cacheManager.getCache().clear(cacheKey);
        }

        return result;
    }

    public abstract int _deleteByPrimaryKey(Table table);

    

    @Override
    public int deleteByCriteria(QueryCriteria queryCriteria) {
        if (router != null) {
            router.routeToMaster();
        }
        Table table = retrievalTableByQueryCriteria(queryCriteria);
        table.setQueryCriteria(queryCriteria);
        int result = _deleteByCriteria(table);
        if (cacheManager != null && useCache) {
            String cacheKey = queryCriteria.getTable();
            if (StringUtils.isNotEmpty(queryCriteria.getDatabase())) {
                cacheKey = queryCriteria.getDatabase() + "#" + cacheKey;
            }
            cacheManager.getCache().clear(cacheKey);
        }
        return result;
    }

    public abstract int _deleteByCriteria(Table table);


    @Override
    public QueryResult selectByCriteria(QueryCriteria queryCriteria, int seconds) {
    	List<String> fields = null;
        return selectByCriteria(fields, queryCriteria, seconds);
    }

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void setResolveDatabase(ResolveDataBase resolveDatabase) {
        this.resolveDatabase = resolveDatabase;
    }
    
    public void setRouter(MasterSlaveRouter router) {
        this.router = router;
    }

    public void reloadTable(String tableName) {
        reloadTable(null, tableName);
    }

    public void clearCache(String tableName) {
        clearCache(null, tableName);
    }

    public void reloadTable(String database, String tableName) {
        resolveDatabase.reloadTable(database, tableName);
    }

    public void clearCache(String database, String tableName) {
        String cacheKey = tableName;
        if (StringUtils.isNotEmpty(database)) {
            cacheKey = database + "#" + cacheKey;
        }
        this.cacheManager.getCache().clear(cacheKey);
    }
    @Override
	public QueryResult selectByCriteria(String[] fields, QueryCriteria queryCriteria) {
    	return selectByCriteria(Arrays.asList(fields), queryCriteria, PERSISTENT_CACHE);
	}


	@Override
	public QueryResult selectByCriteria(String[] fields, QueryCriteria queryCriteria, int seconds) {
		return selectByCriteria(Arrays.asList(fields), queryCriteria, seconds);
	}

    

    @Override
    public QueryResult selectByCriteria(List<String> fields, QueryCriteria queryCriteria) {
        return selectByCriteria(fields, queryCriteria, PERSISTENT_CACHE);
    }

    @Override
    public QueryResult selectByCriteria(QueryCriteria queryCriteria) {
        return selectByCriteria(queryCriteria, PERSISTENT_CACHE);
    }

    @Override
    public int countByCriteria(QueryCriteria queryCriteria) {
        return countByCriteria(queryCriteria, PERSISTENT_CACHE);
    }

	public void setUseCache(boolean useCache) {
		this.useCache = useCache;
	}

	public String isVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	

}
