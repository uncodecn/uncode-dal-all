package cn.uncode.dal.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.uncode.dal.asyn.AsynContext;
import cn.uncode.dal.asyn.AsynSQLTask;
import cn.uncode.dal.asyn.Method;
import cn.uncode.dal.cache.CacheManager;
import cn.uncode.dal.criteria.Model;
import cn.uncode.dal.criteria.QueryCriteria;
import cn.uncode.dal.descriptor.Column;
import cn.uncode.dal.descriptor.Content;
import cn.uncode.dal.descriptor.QueryResult;
import cn.uncode.dal.descriptor.Table;
import cn.uncode.dal.descriptor.db.ResolveDataBase;
import cn.uncode.dal.descriptor.resolver.JavaType;
import cn.uncode.dal.descriptor.resolver.JavaTypeConversion;
import cn.uncode.dal.descriptor.resolver.JavaTypeResolver;
import cn.uncode.dal.event.EventListener;
import cn.uncode.dal.event.EventManager;
import cn.uncode.dal.event.asyn.EventContext;
import cn.uncode.dal.exception.DalSqlException;
import cn.uncode.dal.internal.util.message.Messages;
import cn.uncode.dal.router.DefaultMasterSlaveRouter;
import cn.uncode.dal.router.MasterSlaveRouter;

public abstract class AbstractBaseDAL implements BaseDAL{

    private static final Logger LOG =LoggerFactory.getLogger(AbstractBaseDAL.class);
    
    private static final String CACHE_KEY_PREFIX = "uncode_dal_";
    private static final String CACHE_KEY_SELECT_BY_CRITERIA = "_selectByCriteria_";
    private static final String CACHE_KEY_COUNT_BY_CRITERIA = "_countByCriteria_";
    private static final String CACHE_KEY_SELECT_BY_PRIMARY_KEY = "_selectByPrimaryKey_";

    protected CacheManager cacheManager;

    protected ResolveDataBase resolveDatabase;

    protected MasterSlaveRouter router = new DefaultMasterSlaveRouter();
    
    protected boolean useCache = true;
    
    protected String version;
    
    protected List<String> versionTables;
    
    protected List<String> listenerNames;
    
    protected List<String> noCacheTables;
    
    protected String transactionalPackage;
    
    //-------------------------
    // 异步
    //-------------------------
    /**
     * 日志队列
     */
    private BlockingQueue<AsynContext> sqlQueue;
    /**
     * 写线程池
     */
    private ExecutorService asynWriterService;
    /**
     * 异步日志线程池大小
     */
    private int asynWriterThreadSize = 2;
    private List<AsynSQLTask> tasks = new ArrayList<AsynSQLTask>();
    
    public AbstractBaseDAL(){
    	noCacheTables = new ArrayList<String>();
    	sqlQueue = new LinkedBlockingQueue<AsynContext>();
        asynWriterService = Executors.newFixedThreadPool(asynWriterThreadSize);
        for (int i = 0; i < asynWriterThreadSize; i++) {
        	AsynSQLTask task = new AsynSQLTask(this);
            task.setLogQueue(sqlQueue);
            tasks.add(task);
            asynWriterService.submit(task);
        }
        LOG.info("Asyn dal init ok!");
    }
    
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
    	EventManager.getInstance().sendEvent(new EventContext(Method.SELECT_BY_CRITERIA, true, fields, queryCriteria, seconds, false));
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
        String cacheKey = queryCriteria.getTable() + CACHE_KEY_SELECT_BY_CRITERIA + hashcode;
        if (StringUtils.isNotBlank(queryCriteria.getDatabase())) {
            cacheKey = CACHE_KEY_PREFIX + queryCriteria.getDatabase() + "#" + cacheKey;
        }else{
        	cacheKey = CACHE_KEY_PREFIX + cacheKey;
        }
        if (cacheManager != null && seconds != NO_CACHE && useCache && !noCacheTables.contains(queryCriteria.getTable())) {
            List<Map<String, Object>> value = (List<Map<String, Object>>) cacheManager.getCache().getObject(cacheKey);
            if (value != null && value.size() > 0) {
                queryResult.setResultList(value);
                EventManager.getInstance().sendEvent(new EventContext(Method.SELECT_BY_CRITERIA, false, queryResult, true));
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
        if (cacheManager != null && seconds != NO_CACHE && useCache && result.size() > 0 && !noCacheTables.contains(queryCriteria.getTable())) {
            if (seconds > 0) {
                cacheManager.getCache().putObject(cacheKey, result, seconds);
            } else {
        		cacheManager.getCache().putObject(cacheKey, result);
            }
        }

        if (result != null) {
            queryResult.setResultList(result);
            EventManager.getInstance().sendEvent(new EventContext(Method.SELECT_BY_CRITERIA, false, queryResult, false));
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
    	return countByCriteria(null, queryCriteria, seconds);
    }
    @Override
	public int countByCriteria(List<String> fields, QueryCriteria queryCriteria) {
    	return countByCriteria(fields, queryCriteria, PERSISTENT_CACHE);
	}
    @Override
    public int countByCriteria(List<String> fields, QueryCriteria queryCriteria, int seconds) {
    	EventManager.getInstance().sendEvent(new EventContext(Method.COUNT_BY_CRITERIA, true, fields, queryCriteria, seconds, false));
        if (router != null) {
            router.routeToSlave();
        }

        int hashcode = 0;
        hashcode += queryCriteria.hashCode();
        String cacheKey = queryCriteria.getTable() + CACHE_KEY_COUNT_BY_CRITERIA + hashcode;
        if (StringUtils.isNotBlank(queryCriteria.getDatabase())) {
            cacheKey = CACHE_KEY_PREFIX + queryCriteria.getDatabase() + "#" + cacheKey;
        }else{
        	cacheKey = CACHE_KEY_PREFIX + cacheKey;
        }
        if (cacheManager != null && seconds != NO_CACHE && useCache && !noCacheTables.contains(queryCriteria.getTable())) {
            Integer value = (Integer) cacheManager.getCache().getObject(cacheKey);
            if (value != null && value >= 0) {
            	EventManager.getInstance().sendEvent(new EventContext(Method.COUNT_BY_CRITERIA, false, value, true));
                return value;
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
        int result = _countByCriteria(table);
        if (cacheManager != null && seconds != NO_CACHE && useCache && result >= 0 && !noCacheTables.contains(queryCriteria.getTable())) {
            if (seconds > 0) {
                cacheManager.getCache().putObject(cacheKey, result, seconds);
            } else {
                cacheManager.getCache().putObject(cacheKey, result);
            }
        }
        EventManager.getInstance().sendEvent(new EventContext(Method.COUNT_BY_CRITERIA, false, result, false));
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
    	EventManager.getInstance().sendEvent(new EventContext(Method.SELECT_BY_PRIMARY_KEY, true, fields, model, seconds, false));
        if (model == null) {
            return null;
        }
        if (router != null) {
            router.routeToSlave();
        }

        QueryResult queryResult = new QueryResult();
        String cacheKey = model.getTableName() + CACHE_KEY_SELECT_BY_PRIMARY_KEY + model.getSinglePrimaryKey();
        if (StringUtils.isNotBlank(model.getDatabase())) {
            cacheKey = CACHE_KEY_PREFIX + model.getDatabase() + "#" + cacheKey;
        }else{
        	cacheKey = CACHE_KEY_PREFIX + cacheKey;
        }
        if (cacheManager != null && seconds != NO_CACHE && useCache) {
            Map<String, Object> value = (Map<String, Object>) cacheManager.getCache().getObject(cacheKey);
            if (value != null && value.size() > 0) {
                queryResult.setResultMap(value);
                EventManager.getInstance().sendEvent(new EventContext(Method.SELECT_BY_PRIMARY_KEY, false, fields, model, seconds, true));
                return queryResult;
            }
        }
        Table table = retrievalTableByModel(model);
        /*if (fields != null && fields.size() > 0) {
            LinkedHashMap<String, Object> fieldMap = new LinkedHashMap<String, Object>();
            for (String field : fields) {
                if (table.getFields().containsKey(field)) {
                    fieldMap.put(field, true);
                }
            }
            table.setParams(fieldMap);
        }*/
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

        if (cacheManager != null && seconds != NO_CACHE && useCache && result != null && result.size() > 0) {
            if (seconds > 0) {
                cacheManager.getCache().putObject(cacheKey, result, seconds);
            } else {
                cacheManager.getCache().putObject(cacheKey, result);
            }
        }
        if (result != null) {
        	if(fields != null && fields.size() > 0){
        		Map<String, Object> resultMap = new HashMap<String, Object>();
            	for(Entry<String, Object> item:result.entrySet()){
            		if(fields.contains(item.getKey())){
            			resultMap.put(item.getKey(), item.getValue());
            		}
            	}
            	queryResult.setResultMap(resultMap);
    		}else{
    			queryResult.setResultMap(result);
    		}
            EventManager.getInstance().sendEvent(new EventContext(Method.SELECT_BY_PRIMARY_KEY, false, fields, model, seconds, false));
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
    	EventManager.getInstance().sendEvent(new EventContext(Method.INSERT, true, model));
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

        Long result = _insert(table);
        Object idObj = null;
        if(result > 0){
        	idObj = table.getParams().get("id");
        	if(null == idObj){
        		idObj = result;
        	}
        }

        if (cacheManager != null && useCache) {
            String cacheKey = model.getTableName();
            if (StringUtils.isNotBlank(model.getDatabase())) {
                cacheKey = CACHE_KEY_PREFIX + model.getDatabase() + "#" + cacheKey;
            }else{
            	cacheKey = CACHE_KEY_PREFIX + cacheKey;
            }
            cacheManager.getCache().clear(cacheKey + CACHE_KEY_SELECT_BY_CRITERIA);
            cacheManager.getCache().clear(cacheKey + CACHE_KEY_COUNT_BY_CRITERIA);
            cacheKey += CACHE_KEY_SELECT_BY_PRIMARY_KEY + model.getSinglePrimaryKey();
            cacheManager.getCache().clear(cacheKey);
            /*cacheKey += CACHE_KEY_SELECT_BY_PRIMARY_KEY + String.valueOf(idObj);
            if(table.hasVersion()){
        		Object versionObj = table.getParams().get(version);
        		if(versionObj == null){
        			table.getParams().put(version, 0);
        		}
            }
            cacheManager.getCache().putObject(cacheKey, table.getParams());*/
        }
        
        EventManager.getInstance().sendEvent(new EventContext(Method.INSERT, false, model));
        return idObj;
    }
    
    @Override
	public void asynInsert(Object obj) {
    	sqlQueue.offer(new AsynContext(Method.INSERT, obj));
	}

	@Override
	public void asynInsert(String table, Map<String, Object> obj) {
		sqlQueue.offer(new AsynContext(Method.INSERT_TABLE, table, obj));
	}

	@Override
	public void asynInsert(String database, String table, Map<String, Object> obj) {
		sqlQueue.offer(new AsynContext(Method.INSERT_DATABASE_TABLE, database, table, obj));
	}


    /**
     * insert option
     * 
     * @param table table instance
     * @return result
     */
    public abstract long _insert(Table table);

    @Override
	public int updateByCriteria(Object obj, QueryCriteria queryCriteria) {
    	return updateByCriteria(new Model(obj), queryCriteria);
	}

    private int updateByCriteria(Model model, QueryCriteria queryCriteria) {
    	EventManager.getInstance().sendEvent(new EventContext(Method.UPDATE, true, model, queryCriteria));
    	if(queryCriteria != null && queryCriteria.getOredCriteria() != null && queryCriteria.getOredCriteria().size() > 0){
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
            
            if(containsTables(model.getTableName()) == false){
            	table.getContent().setVersionField(null);
            }
            if(table.hasVersion()){
            	Object value = queryCriteria.getVersion();
            	if(null == value){
            		throw new DalSqlException("Version is request.");
            	}
            }
            
            table.setQueryCriteria(queryCriteria);
            
            int result = _updateByCriteria(table);
            if (cacheManager != null && useCache) {
                String cacheKey = queryCriteria.getTable();
                if (StringUtils.isNotBlank(model.getDatabase())) {
                    cacheKey = CACHE_KEY_PREFIX + queryCriteria.getDatabase() + "#" + cacheKey;
                }else{
                	cacheKey = CACHE_KEY_PREFIX + cacheKey;
                }
                cacheManager.getCache().clear(cacheKey + CACHE_KEY_SELECT_BY_CRITERIA);
                cacheManager.getCache().clear(cacheKey + CACHE_KEY_COUNT_BY_CRITERIA);
                cacheManager.getCache().clear(cacheKey + CACHE_KEY_SELECT_BY_PRIMARY_KEY);
            }
            EventManager.getInstance().sendEvent(new EventContext(Method.UPDATE, false, model, queryCriteria));
            return result;
    	}
    	return 0;
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
    	EventManager.getInstance().sendEvent(new EventContext(Method.UPDATE, true, model));
        if (router != null) {
            router.routeToMaster();
        }
        Table table = retrievalTableByModel(model);
        LinkedHashMap<String, Object> params = new LinkedHashMap<String, Object>();
        LinkedHashMap<String, Object> conditions = new LinkedHashMap<String, Object>();
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
            
            Iterator<String> iter = model.getContent().keySet().iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                Object value = model.getContent().get(key);
                if(null != value){
                	if(isNoSql()){
                		params.put(key, value);
                	}else{
                		Column column = table.getField(key);
                		if(column != null){
                			JavaType javaType = JavaTypeResolver.calculateJavaType(table.getField(key).getJdbcType());
                            if (table.getPrimaryKey().getFields().contains(key)) {
                            	conditions.put(key, JavaTypeConversion.convert(javaType, value));
                            } else {
                                params.put(key, JavaTypeConversion.convert(javaType, value));
                            }
                		}
                	}
                }
            }
            if(containsTables(model.getTableName()) == false){
            	table.getContent().setVersionField(null);
            }
            if(table.hasVersion()){
            	Object value = model.getVersion();
            	if(null == value){
            		throw new DalSqlException("Version is request.");
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
            if (StringUtils.isNotBlank(model.getDatabase())) {
                cacheKey = CACHE_KEY_PREFIX + model.getDatabase() + "#" + cacheKey;
            }else{
            	cacheKey = CACHE_KEY_PREFIX + cacheKey;
            }
            cacheManager.getCache().clear(cacheKey + CACHE_KEY_SELECT_BY_CRITERIA);
            cacheManager.getCache().clear(cacheKey + CACHE_KEY_COUNT_BY_CRITERIA);
            cacheKey += CACHE_KEY_SELECT_BY_PRIMARY_KEY + model.getSinglePrimaryKey();
            cacheManager.getCache().clear(cacheKey);
            /*Map<String, Object> value = (Map<String, Object>) cacheManager.getCache().getObject(cacheKey);
            if (value != null && value.size() > 0) {
            	for(Entry<String, Object> entry:params.entrySet()){
        			if(entry.getKey().equals(version)){
        				int vers = 0;
        				if(entry.getValue() != null){
        					vers = (Integer)entry.getValue();
        				}
        				value.put(entry.getKey(), vers+1);
        			}else{
        				value.put(entry.getKey(), entry.getValue());
        			}
            	}
            	cacheManager.getCache().putObject(cacheKey, value);
            }*/
        }
        
        EventManager.getInstance().sendEvent(new EventContext(Method.UPDATE, false, model));
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
    	EventManager.getInstance().sendEvent(new EventContext(Method.DELETE, true, model));
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
            if (StringUtils.isNotBlank(model.getDatabase())) {
                cacheKey = CACHE_KEY_PREFIX + model.getDatabase() + "#" + cacheKey;
            }else{
            	cacheKey = CACHE_KEY_PREFIX + cacheKey;
            }
            cacheManager.getCache().clear(cacheKey + CACHE_KEY_SELECT_BY_CRITERIA);
            cacheManager.getCache().clear(cacheKey + CACHE_KEY_COUNT_BY_CRITERIA);
            cacheKey += CACHE_KEY_SELECT_BY_PRIMARY_KEY + model.getSinglePrimaryKey();
            cacheManager.getCache().clear(cacheKey);
        }
        
        EventManager.getInstance().sendEvent(new EventContext(Method.DELETE, false, model));

        return result;
    }

    public abstract int _deleteByPrimaryKey(Table table);

    

    @Override
    public int deleteByCriteria(QueryCriteria queryCriteria) {
    	EventManager.getInstance().sendEvent(new EventContext(Method.DELETE, true, queryCriteria));
        if (router != null) {
            router.routeToMaster();
        }
        Table table = retrievalTableByQueryCriteria(queryCriteria);
        table.setQueryCriteria(queryCriteria);
        int result = _deleteByCriteria(table);
        if (cacheManager != null && useCache) {
            String cacheKey = queryCriteria.getTable();
            if (StringUtils.isNotEmpty(queryCriteria.getDatabase())) {
                cacheKey = CACHE_KEY_PREFIX + queryCriteria.getDatabase() + "#" + cacheKey;
            }else{
            	cacheKey = CACHE_KEY_PREFIX + cacheKey;
            }
            cacheManager.getCache().clear(cacheKey + CACHE_KEY_SELECT_BY_CRITERIA);
            cacheManager.getCache().clear(cacheKey + CACHE_KEY_COUNT_BY_CRITERIA);
            cacheManager.getCache().clear(cacheKey + CACHE_KEY_SELECT_BY_PRIMARY_KEY);
        }
        EventManager.getInstance().sendEvent(new EventContext(Method.DELETE, true, queryCriteria));
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
            cacheKey = CACHE_KEY_PREFIX + database + "#" + cacheKey;
        }else{
        	cacheKey = CACHE_KEY_PREFIX + cacheKey;
        }
       cacheManager.getCache().clear(cacheKey + CACHE_KEY_SELECT_BY_CRITERIA);
       cacheManager.getCache().clear(cacheKey + CACHE_KEY_COUNT_BY_CRITERIA);
       cacheManager.getCache().clear(cacheKey + CACHE_KEY_SELECT_BY_PRIMARY_KEY);
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
    
    public BaseDAL update(){
    	if (router != null) {
            router.routeToMaster();
        }
		return this;
    	
    }
    public BaseDAL select(){
    	if (router != null) {
            router.routeToSlave();
        }
    	return this;
    }

	public void setUseCache(boolean useCache) {
		this.useCache = useCache;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setVersionTables(List<String> versionTables) {
		this.versionTables = versionTables;
	}
	
	public boolean containsTables(String table){
		if(versionTables == null){
			return false;
		}
		return versionTables.contains(table);
	}

	public void setListenerNames(List<String> listenerNames) {
		this.listenerNames = listenerNames;
	}
	
	public void setNoCacheTables(List<String> noCacheTables) {
		this.noCacheTables = noCacheTables;
	}
	
	public void setTransactionalPackage(String transactionalPackage) {
		this.transactionalPackage = transactionalPackage;
	}

	public void init(){
		if(null != listenerNames && listenerNames.size() > 0){
			for(String name:listenerNames){
				//EventListener listener = (EventListener)ContextLoader.getCurrentWebApplicationContext().getBean(name);
				EventListener listener = null;
				if(null != listener){
					EventManager.getInstance().addEventListener(listener);
				}
			}
		}
	}

}
