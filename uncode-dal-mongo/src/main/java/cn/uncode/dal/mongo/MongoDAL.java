package cn.uncode.dal.mongo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bson.types.ObjectId;
import org.jongo.Find;
import org.jongo.FindOne;
import org.jongo.Jongo;

import cn.uncode.dal.core.AbstractMongoDAL;
import cn.uncode.dal.criteria.Criterion;
import cn.uncode.dal.criteria.QueryCriteria;
import cn.uncode.dal.criteria.QueryCriteria.Criteria;
import cn.uncode.dal.descriptor.Table;
import cn.uncode.dal.utils.JsonUtils;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

public class MongoDAL extends AbstractMongoDAL implements cn.uncode.dal.core.MongoDAL {
	
	private static final Logger LOG = LoggerFactory.getLogger(MongoDAL.class);
	
	private MongoDB database;

	@Override
	public List<Map<String, Object>> _selectByCriteria(Table table) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		Map<String, Object> coditon = new HashMap<String, Object>();
		try {
			QueryCriteria queryCriteria = table.getQueryCriteria();
			DB db = database.getDB();
			Jongo jongo = new Jongo(db);
			for(Criteria criteria:queryCriteria.getOredCriteria()){
				for(Criterion criterion:criteria.getAllCriteria()){
					coditon = buildCriteria(criterion, coditon);
				}
			}
			Find find = jongo.getCollection(queryCriteria.getTable()).find(JsonUtils.objToJson(coditon));
		    if(StringUtils.isNotBlank(queryCriteria.getOrderByClause())){
		    	find.sort(queryCriteria.getOrderByClause());
		    }
		    if(queryCriteria.getSelectOne()){
		    	find.skip(0);
		        find.limit(1);
		    }else{
		    	if(queryCriteria.getPageIndex() >= 0){
		        	int pageSize = 20, pageIndex = 1;
		        	if(queryCriteria.getPageSize() > 0){
		        		pageSize = queryCriteria.getPageSize();
		        	}
		        	if(queryCriteria.getPageIndex() > 1){
		        		pageIndex = queryCriteria.getPageIndex();
		        	}
		        	
		        	find.skip((pageIndex - 1) * pageSize);
		            find.limit(pageSize);
		        }
		    }
		    StringBuffer sb = new StringBuffer();
		    if(null != table.getParams()){
		    	for(String fd : table.getParams().keySet()){
		        	sb.append(fd).append(",");
		        }
		    }
		    if(sb.length() > 0){
		    	find.projection(sb.deleteCharAt(sb.lastIndexOf(",")).toString());
		    }
		    Iterator<Map> iterator = find.as(Map.class).iterator();  
		    while(iterator.hasNext()){
		    	Map<String, Object> item = iterator.next();
		    	item.put("id", item.get("_id").toString());
		        result.add(item);
		    }
		} catch (MongoException e) {
			LOG.error("mongo find error", e);
		}
		return result;
	}

	@Override
	public int _countByCriteria(Table table) {
		int count = 0;
		Map<String, Object> coditon = new HashMap<String, Object>();
		try {
			QueryCriteria queryCriteria = table.getQueryCriteria();
			DB db = database.getDB();
			Jongo jongo = new Jongo(db);
			for(Criteria criteria:queryCriteria.getOredCriteria()){
				for(Criterion criterion:criteria.getAllCriteria()){
					coditon = buildCriteria(criterion, coditon);
				}
			}
			long size = jongo.getCollection(queryCriteria.getTable()).count(JsonUtils.objToJson(coditon));
			count = (int) size;
		} catch (MongoException e) {
			LOG.error("mongo find error", e);
		}
		return count;
	}

	@Override
	public Map<String, Object> _selectByPrimaryKey(Table table) {
		Map<String, Object> result = null;
		try {
			DB db = database.getDB();
			Jongo jongo = new Jongo(db);
			FindOne find = null;
			if(table.getConditions().containsKey("_id")){
				find = jongo.getCollection(table.getTableName()).findOne(new ObjectId(String.valueOf(table.getConditions().get("_id"))));
			}else{
				find = jongo.getCollection(table.getTableName()).findOne(JsonUtils.objToJson(table.getConditions()));
			}
	        result = find.as(Map.class);
	        result.put("id", result.get("_id").toString());
		} catch (MongoException e) {
			LOG.error("mongo findOne error", e);
		}
		return result;
	}

	@Override
	public int _insert(Table table) {
		try {
			DB db = database.getDB();
			Jongo jongo = new Jongo(db);
			ObjectId newOid = ObjectId.get();
			table.getParams().put("_id", newOid);
			jongo.getCollection(table.getTableName()).save(table.getParams());
			table.getParams().put("id", newOid.toString());
		} catch (MongoException e) {
			LOG.error("mongo insert error", e);
		}
		return 1;
	}

	@Override
	public int _updateByCriteria(Table table) {
		Map<String, Object> coditon = new HashMap<String, Object>();
		try {
	        QueryCriteria queryCriteria = table.getQueryCriteria();
	        DB db = database.getDB();
	        Jongo jongo = new Jongo(db);
			for(Criteria criteria:queryCriteria.getOredCriteria()){
				for(Criterion criterion:criteria.getAllCriteria()){
					coditon = buildCriteria(criterion, coditon);
				}
			}
			Find find = jongo.getCollection(queryCriteria.getTable()).find(JsonUtils.objToJson(coditon));
		    Iterator<Map> iterator = find.as(Map.class).iterator();  
			while (iterator.hasNext()) {
				Map<String, Object> map = iterator.next();
				if (null != map) {
					Iterator<String> iter = table.getParams().keySet().iterator();
					while (iter.hasNext()) {
						String key = iter.next();
						if (map.containsKey(key) && !"_id".equals(key)) {
							map.put(key, table.getParams().get(key));
						}
					}
				}
				jongo.getCollection(table.getTableName()).save(map);
			}
		} catch (MongoException e) {
			LOG.error("mongo update error", e);
		}
		return 1;
	}

	@Override
	public int _updateByPrimaryKey(Table table) {
		try {
			if(table.getParams().containsKey("_id")){
				DB db = database.getDB();
				Jongo jongo = new Jongo(db);
				FindOne find = jongo.getCollection(table.getTableName()).findOne(new ObjectId(String.valueOf(table.getParams().get("_id"))));
				Map<String, Object> map = find.as(Map.class);
				boolean update = false;
				if (null != map) {
					Iterator<String> iter = table.getParams().keySet().iterator();
					while (iter.hasNext()) {
						String key = iter.next();
						if (map.containsKey(key) && !"_id".equals(key)) {
							map.put(key, table.getParams().get(key));
							update = true;
						}
					}
				}
				if(update){
					jongo.getCollection(table.getTableName()).save(map);
				}
				
				return 1;
			}
		} catch (MongoException e) {
			LOG.error("mongo update error", e);
		}
		return 0;
	}

	@Override
	public int _deleteByPrimaryKey(Table table) {
		try {
			DB db = database.getDB();
			Jongo jongo = new Jongo(db);
			
			if(table.getConditions().containsKey("_id")){
				jongo.getCollection(table.getTableName()).remove(new ObjectId(String.valueOf(table.getConditions().get("_id"))));
			}else{
				jongo.getCollection(table.getTableName()).remove(JsonUtils.objToJson(table.getConditions()));
			}
		} catch (MongoException e) {
			LOG.error("mongo findOne error", e);
		}
		return 1;
	}

	@Override
	public int _deleteByCriteria(Table table) {
		Map<String, Object> coditon = new HashMap<String, Object>();
		try {
	        QueryCriteria queryCriteria = table.getQueryCriteria();
	        DB db = database.getDB();
	        Jongo jongo = new Jongo(db);
			for(Criteria criteria:queryCriteria.getOredCriteria()){
				for(Criterion criterion:criteria.getAllCriteria()){
					coditon = buildCriteria(criterion, coditon);
				}
			}
			Find find = jongo.getCollection(queryCriteria.getTable()).find(JsonUtils.objToJson(coditon));
		    Iterator<Map> iterator = find.as(Map.class).iterator();  
			while (iterator.hasNext()) {
				Map<String, Object> map = iterator.next();
				if (null != map) {
					if(map.containsKey("_id")){
						jongo.getCollection(table.getTableName()).remove(new ObjectId(String.valueOf(map.get("_id"))));
					}
				}
			}
		} catch (MongoException e) {
			LOG.error("mongo delete error", e);
		}
		return 1;
	}

	public void setDatabase(MongoDB database) {
		this.database = database;
	}

	private Map<String, Object> buildCriteria(Criterion criterion, Map<String, Object> valueMap) {
		Map<String, Object> cd = new HashMap<String, Object>();
        if(Criterion.Condition.IS_NULL == criterion.getCondition()){
        	cd.put(MongoCondition.IS_NULL, false);
        	valueMap.put(criterion.getColumn(), cd);
        }else if(Criterion.Condition.IS_NOT_NULL == criterion.getCondition()){
        	cd.put(MongoCondition.IS_NOT_NULL, true);
        	valueMap.put(criterion.getColumn(), cd);
        }else if(Criterion.Condition.EQUAL == criterion.getCondition()){
        	valueMap.put(criterion.getColumn(), criterion.getValue());
        }else if(Criterion.Condition.NOT_EQUAL == criterion.getCondition()){
        	cd.put(MongoCondition.NOT_EQUAL, criterion.getValue());
        	valueMap.put(criterion.getColumn(), cd);
        }else if(Criterion.Condition.GREATER_THAN == criterion.getCondition()){
        	cd.put(MongoCondition.GREATER_THAN, criterion.getValue());
        	valueMap.put(criterion.getColumn(), cd);
        }else if(Criterion.Condition.GREATER_THAN_OR_EQUAL == criterion.getCondition()){
        	cd.put(MongoCondition.GREATER_THAN_OR_EQUAL, criterion.getValue());
        	valueMap.put(criterion.getColumn(), cd);
        }else if(Criterion.Condition.LESS_THAN == criterion.getCondition()){
        	cd.put(MongoCondition.LESS_THAN, criterion.getValue());
        	valueMap.put(criterion.getColumn(), cd);
        }else if(Criterion.Condition.LESS_THAN_OR_EQUAL == criterion.getCondition()){
        	cd.put(MongoCondition.LESS_THAN_OR_EQUAL, criterion.getValue());
        	valueMap.put(criterion.getColumn(), cd);
        }else if(Criterion.Condition.IN == criterion.getCondition()){
            List<Object> values = (List<Object>) criterion.getValue();
            StringBuffer sb = new StringBuffer();
            for(Object value:values){
                sb.append(value).append(",");
            }
            if(sb.length() > 0){
            	String listStr = "[" + sb.deleteCharAt(sb.lastIndexOf(",")).toString() + "]";
            	cd.put(MongoCondition.IN, listStr);
            	valueMap.put(criterion.getColumn(), cd);
            }
        }else if(Criterion.Condition.NOT_IN == criterion.getCondition()){
        	List<Object> values = (List<Object>) criterion.getValue();
            StringBuffer sb = new StringBuffer();
            for(Object value:values){
                sb.append(value).append(",");
            }
            if(sb.length() > 0){
            	String listStr = "[" + sb.deleteCharAt(sb.lastIndexOf(",")).toString() + "]";
            	cd.put(MongoCondition.NOT_IN, listStr);
            	valueMap.put(criterion.getColumn(), cd);
            }
        }else if(Criterion.Condition.BETWEEN == criterion.getCondition()){
        	cd.put(MongoCondition.GREATER_THAN_OR_EQUAL, criterion.getValue());
        	cd.put(MongoCondition.LESS_THAN_OR_EQUAL, criterion.getSecondValue());
        	valueMap.put(criterion.getColumn(), cd);
        }else if(Criterion.Condition.NOT_BETWEEN == criterion.getCondition()){
        	cd.put(MongoCondition.LESS_THAN_OR_EQUAL, criterion.getValue());
        	cd.put(MongoCondition.GREATER_THAN_OR_EQUAL, criterion.getSecondValue());
        	valueMap.put(criterion.getColumn(), cd);
        }else if(Criterion.Condition.LIKE == criterion.getCondition()){
        	valueMap.put(criterion.getColumn(), criterion.getValue());
        }else if(Criterion.Condition.NOT_LIKE == criterion.getCondition()){
        	valueMap.put(criterion.getColumn(), criterion.getValue());
        }
        return valueMap;
	}
	
	public static class MongoCondition{
    	public static final String TAG= "$";
        public static final String IS_NULL = "$exists";
        public static final String IS_NOT_NULL = "$exists";
        public static final String EQUAL = "$eq";
        public static final String NOT_EQUAL = "$ne";
        public static final String GREATER_THAN = "$gt";
        public static final String GREATER_THAN_OR_EQUAL = "$gte";
        public static final String LESS_THAN = "$lt";
        public static final String LESS_THAN_OR_EQUAL = "$lte";
        public static final String LIKE = "$lk";
        public static final String NOT_LIKE = "$nlk";
        public static final String IN = "$in";
        public static final String NOT_IN = "$nin";
        public static final String BETWEEN = "$bt";
        public static final String NOT_BETWEEN = "$nbt";
       
    }

	@Override
	public Object getTemplate() {
		DB db = database.getDB();
		return new Jongo(db);
	}

	public void runScript(String script) {
        //有个格式化的作用
        try {
        	DB db = database.getDB();
            db.command(buildCommand(script));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 构建command命令
     *
     * @param script
     * @return
     */
    private DBObject buildCommand(String script) {
        return BasicDBObjectBuilder.start()
                .add("$eval", script)
                .add("nolock", true)
                .get();
    }



}
