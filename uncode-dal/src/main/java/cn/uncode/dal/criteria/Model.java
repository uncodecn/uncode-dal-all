package cn.uncode.dal.criteria;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import cn.uncode.dal.utils.JsonUtils;

public class Model implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 3485804608796833321L;

    private final LinkedHashMap<String, Object> content;

    private final Map<String, Object> context;

    public static final String MODEL_NAME = "table_name";

    public static final String DATA_BASE = "data_base";

    public static final String MODEL_ID = "single_primary_key";
    
    public static final String VERSION = "version";

    public Model(String modelName) {
        content = new LinkedHashMap<String, Object>();
        context = new HashMap<String, Object>();
        context.put(MODEL_NAME, modelName.toLowerCase().trim());
    }

    public Model(String database, String modelName) {
        content = new LinkedHashMap<String, Object>();
        context = new HashMap<String, Object>();
        context.put(MODEL_NAME, modelName.toLowerCase().trim());
        context.put(DATA_BASE, database.toLowerCase().trim());
    }

    public Model(Class<?> clazz) {
        content = new LinkedHashMap<String, Object>();
        context = new HashMap<String, Object>();
        String name = clazz.getName();
        name = name.substring(name.lastIndexOf(".") + 1);
        char[] array = name.toCharArray();
        array[0] += 32;
        context.put(MODEL_NAME, String.valueOf(array).toLowerCase().trim());
    }
    
    public Model(Object obj) {
    	this(null, obj);
    }

    public Model(String database, Object obj) {
    	if(obj instanceof Model){
    		Model temp = (Model) obj;
    		content = temp.getContent();
    		context = temp.getContext(); 
    	}else{
    		content = new LinkedHashMap<String, Object>();
            context = new HashMap<String, Object>();
            Map<String, Object> map = null;
            if(obj instanceof Map){
            	map = (Map<String, Object>) obj;
            }else{
            	String name = obj.getClass().getName();
                context.put(MODEL_NAME, name.substring(name.lastIndexOf(".") + 1).toLowerCase().trim());
                map = (Map<String, Object>) JsonUtils.objToMap(obj);
            }
            content.putAll(map);
            if(StringUtils.isNotEmpty(database)){
            	context.put(DATA_BASE, database.toLowerCase().trim());
            }
            if(null != map && map.containsKey("id")){
            	this.setSinglePrimaryKey(map.get("id"));
            }
    	}
    }

    public Model(String modelName, Map<String, Object> obj) {
        context = new HashMap<String, Object>();
        content = new LinkedHashMap<String, Object>();
        context.put(MODEL_NAME, modelName.toLowerCase().trim());
        content.putAll(obj);
        if(null != obj && obj.containsKey("id")){
        	this.setSinglePrimaryKey(obj.get("id"));
        }
    }

    public void setSinglePrimaryKey(Object primaryKey) {
        context.put(MODEL_ID, primaryKey);
    }

    public void setDatabase(String database) {
        context.put(DATA_BASE, database);
    }
    
    public void setVersion(Object version) {
        context.put(VERSION, version);
    }

    public String getSinglePrimaryKey() {
    	Object id = context.get(MODEL_ID);
    	if(null != id){
    		return String.valueOf(id);
    	}
        return null;
    }

    public String getDatabase() {
        return (String) context.get(DATA_BASE);
    }

    public String getTableName() {
        return (String) context.get(MODEL_NAME);
    }
    
    public Object getVersion() {
        return content.get(VERSION);
    }

    public void setField(String field, Object value) {
        if (StringUtils.isEmpty(field)) {
            return;
        }
        content.put(field, value);
    }

    public Object getField(String field) {
        if (StringUtils.isEmpty(field)) {
            return null;
        }
        return content.get(field);
    }

    public LinkedHashMap<String, Object> getContent() {
        return content;
    }

    public void addContent(Map<String, Object> content) {
        this.content.putAll(content);
    }
    
    public Map<String, Object> getContext() {
		return context;
	}

	public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + content.hashCode();
        result = prime * result + context.hashCode();
        return result;
    }
    
    private String firstToLower(String str) {
        char[] array = str.toCharArray();
        array[0] += 32;
        return String.valueOf(array);
    }

}
