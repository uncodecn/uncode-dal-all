package cn.uncode.dal.mongo;

import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.jongo.Jongo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.Block;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.util.JSON;

import cn.uncode.dal.core.AbstractMongoDAL;
import cn.uncode.dal.criteria.Criterion;
import cn.uncode.dal.criteria.QueryCriteria;
import cn.uncode.dal.criteria.QueryCriteria.Criteria;
import cn.uncode.dal.descriptor.Table;
import cn.uncode.dal.utils.JsonUtils;

public class Mongo3DAL extends AbstractMongoDAL implements cn.uncode.dal.core.MongoDAL {
	
	private static final Logger LOG = LoggerFactory.getLogger(Mongo3DAL.class);
	
	private MongoDB database;

	@Override
	public List<Map<String, Object>> _selectByCriteria(Table table) {
		final List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		Map<String, Object> coditon = new HashMap<String, Object>();
		try {
			QueryCriteria queryCriteria = table.getQueryCriteria();
			com.mongodb.client.MongoDatabase db = database.getMongoDB();
			for(Criteria criteria:queryCriteria.getOredCriteria()){
				for(Criterion criterion:criteria.getAllCriteria()){
					coditon = buildCriteria(criterion, coditon);
				}
			}
			FindIterable<Document> findIterable = db.getCollection(queryCriteria.getTable()).find(Document.parse((JSON.serialize(coditon))));
			LOG.debug("selectByCriteria->collection:"+queryCriteria.getTable()+",script:"+JSON.serialize(coditon));
		    if(StringUtils.isNotBlank(queryCriteria.getOrderByClause())){
		    	findIterable.sort(Document.parse(queryCriteria.getOrderByClause()));
		    }
		    if(queryCriteria.getSelectOne()){
		    	findIterable.skip(0);
		    	findIterable.limit(1);
		    }else{
		    	if(queryCriteria.getPageIndex() >= 0){
		        	int pageSize = 20, pageIndex = 1;
		        	if(queryCriteria.getPageSize() > 0){
		        		pageSize = queryCriteria.getPageSize();
		        	}
		        	if(queryCriteria.getPageIndex() > 1){
		        		pageIndex = queryCriteria.getPageIndex();
		        	}
		        	findIterable.skip((pageIndex - 1) * pageSize);
			    	findIterable.limit(pageSize);
		        }
		    }
		    StringBuffer sb = new StringBuffer();
		    if(null != table.getParams()){
		    	for(String fd : table.getParams().keySet()){
		        	sb.append(fd).append(",");
		        }
		    }
		    if(sb.length() > 0){
		    	findIterable.projection(Document.parse(JSON.serialize(table.getParams())));
		    }
		    if(findIterable != null){
		    	findIterable.forEach(new Block<Document>() {
					public void apply(Document document) {
						try {
							Map<String, Object> item = JsonUtils.fromJson(document.toJson(), Map.class);
							Map<String, String> idmap = (Map<String, String>) item.get("_id");
							item.put("id", idmap.get("$oid"));
							result.add(item);
						} catch (Exception e) {
							LOG.error(e.getMessage());
						}
					}
				});
		    }
		} catch (MongoException e) {
			LOG.error("mongo find error", e);
		}
		LOG.debug("selectByCriteria->result:"+result.toString());
		return result;
	}

	@Override
	public int _countByCriteria(Table table) {
		int count = 0;
		Map<String, Object> coditon = new HashMap<String, Object>();
		try {
			QueryCriteria queryCriteria = table.getQueryCriteria();
			com.mongodb.client.MongoDatabase db = database.getMongoDB();
			for(Criteria criteria:queryCriteria.getOredCriteria()){
				for(Criterion criterion:criteria.getAllCriteria()){
					coditon = buildCriteria(criterion, coditon);
				}
			}
			long size = db.getCollection(queryCriteria.getTable()).count(Document.parse((JSON.serialize(coditon))));
			LOG.debug("countByCriteria->collection:"+queryCriteria.getTable()+",script:"+JSON.serialize(coditon));
			count = (int) size;
		} catch (MongoException e) {
			LOG.error("mongo find error", e);
		}
		LOG.debug("_countByCriteria->result:"+count);
		return count;
	}

	@Override
	public Map<String, Object> _selectByPrimaryKey(Table table) {
		final Map<String, Object> result = new HashMap<String, Object>();
		try {
			com.mongodb.client.MongoDatabase db = database.getMongoDB();
			
			FindIterable<Document> findIterable = null;
			if (table.getConditions().containsKey("_id")) {
                findIterable = db.getCollection(table.getTableName()).find(eq("_id", new ObjectId(String.valueOf(table.getConditions().get("_id")))));
                LOG.debug("selectByPrimaryKey->collection:"+table.getTableName()+",script:"+eq("_id", new ObjectId(String.valueOf(table.getConditions().get("_id")))).toString());
            } else {
            	findIterable = db.getCollection(table.getTableName()).find(Document.parse(JSON.serialize(table.getConditions())));
            	LOG.debug("selectByPrimaryKey->collection:"+table.getTableName()+",script:"+JSON.serialize(table.getConditions()));
            }
			if(findIterable != null){
		    	findIterable.forEach(new Block<Document>() {
					public void apply(Document document) {
						try {
							Map<String, Object> item = JsonUtils.fromJson(document.toJson(), Map.class);
							item.put("id", item.get("_id").toString());
							result.putAll(item);
						} catch (Exception e) {
							LOG.error(e.getMessage());
						}
					}
				});
		    }
		} catch (MongoException e) {
			LOG.error("mongo findOne error", e);
		}
		LOG.debug("selectByPrimaryKey->result:"+result.toString());
		return result;
	}

	@Override
	public int _insert(Table table) {
		try {
			com.mongodb.client.MongoDatabase db = database.getMongoDB();
			ObjectId newOid = ObjectId.get();
			table.getParams().put("_id", newOid);
			db.getCollection(table.getTableName()).insertOne(Document.parse(JSON.serialize(table.getParams())));
			table.getParams().put("id", newOid.toString());
			LOG.debug("insert->collection:"+table.getTableName()+",script:"+JSON.serialize(table.getParams()));
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
	        com.mongodb.client.MongoDatabase db = database.getMongoDB();
			for(Criteria criteria:queryCriteria.getOredCriteria()){
				for(Criterion criterion:criteria.getAllCriteria()){
					coditon = buildCriteria(criterion, coditon);
				}
			}
			Map<String, Object> vaule = new HashMap<String, Object>();
			vaule.put("$set", table.getParams());
			db.getCollection(queryCriteria.getTable()).updateMany(Document.parse((JSON.serialize(coditon))), Document.parse(JSON.serialize(vaule)));
			LOG.debug("updateByCriteria->collection:"+table.getTableName()+",value:"+JSON.serialize(vaule)+",condition:"+JSON.serialize(coditon));
		} catch (MongoException e) {
			LOG.error("mongo update error", e);
		}
		return 1;
	}

	@Override
	public int _updateByPrimaryKey(Table table) {
		try {
			if(table.getConditions().containsKey("_id")){
				com.mongodb.client.MongoDatabase db = database.getMongoDB();
				Object id = table.getConditions().get("_id");
				table.getParams().remove("id");//id被永久屏蔽
				Map<String, Object> vaule = new HashMap<String, Object>();
				vaule.put("$set", table.getParams());
				db.getCollection(table.getTableName()).updateOne(eq("_id", new ObjectId(String.valueOf(id))), Document.parse(JSON.serialize(vaule)));
				LOG.debug("updateByPrimaryKey->collection:"+table.getTableName()+",value:"+JSON.serialize(vaule)+",condition:"+eq("_id", new ObjectId(String.valueOf(id)).toString()));
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
			com.mongodb.client.MongoDatabase db = database.getMongoDB();
			if(table.getConditions().containsKey("_id")){
				db.getCollection(table.getTableName()).deleteOne(eq("_id", new ObjectId(String.valueOf(table.getConditions().get("_id")))));
				LOG.debug("deleteByPrimaryKey->collection:"+table.getTableName()+",condition:"+eq("_id", new ObjectId(String.valueOf(table.getConditions().get("_id"))).toString()));
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
	        com.mongodb.client.MongoDatabase db = database.getMongoDB();
			for(Criteria criteria:queryCriteria.getOredCriteria()){
				for(Criterion criterion:criteria.getAllCriteria()){
					coditon = buildCriteria(criterion, coditon);
				}
			}
			db.getCollection(queryCriteria.getTable()).deleteMany(Document.parse((JSON.serialize(coditon))));
			LOG.debug("deleteByCriteria->collection:"+table.getTableName()+",condition:"+JSON.serialize(coditon));
		} catch (MongoException e) {
			LOG.error("mongo delete error", e);
		}
		return 1;
	}

	public void setDatabase(MongoDB database) {
		this.database = database;
	}
	
	

	private Map<String, Object> buildCriteria(Criterion criterion, Map<String, Object> valueMap) {
		Object cdObj=valueMap.get(criterion.getColumn());
		Map<String, Object> cd = null;
		if(null!=cdObj){
			cd=(Map<String, Object>)cdObj;
		}else{
			cd=new HashMap<String, Object>();
		}
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
            if(values!= null && values.size() > 0){
            	cd.put(MongoCondition.IN, values);
            	valueMap.put(criterion.getColumn(), cd);
            }
        }else if(Criterion.Condition.NOT_IN == criterion.getCondition()){
        	List<Object> values = (List<Object>) criterion.getValue();
        	 List<String> ins = new ArrayList<String>(); 
             for(Object value:values){
             	ins.add(String.valueOf(value));
             }
            if(ins.size() > 0){
            	cd.put(MongoCondition.NOT_IN, ins);
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
        	cd.put(MongoCondition.LIKE, criterion.getValue());
        	valueMap.put(criterion.getColumn(), cd);
        }else if(Criterion.Condition.NOT_LIKE == criterion.getCondition()){
        	valueMap.put(criterion.getColumn(), criterion.getValue());
        }else{
        	if(StringUtils.isNotEmpty(criterion.getColumn()) && criterion.getValue() == null){
            	Map<String, Object> sqlMap = Document.parse(criterion.getColumn());
            	valueMap.putAll(sqlMap);
        	}
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
        public static final String LIKE = "$regex";
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

	@Override
	public void runScript(String script) {
		try {
			com.mongodb.client.MongoDatabase db = database.getMongoDB();
			db.runCommand(buildCommand(""));
		} catch (MongoException e) {
			LOG.error("mongo findOne error", e);
		}
	}

    /**
     * 构建command命令
     *
     * @param script
     * @return
     */
    private Document buildCommand(String script) {
    	DBObject dbObject = BasicDBObjectBuilder.start()
					        .add("$eval", script)
					        .add("nolock", true)
					        .get();
    	
        return Document.parse(JSON.serialize(dbObject));
    }

}
