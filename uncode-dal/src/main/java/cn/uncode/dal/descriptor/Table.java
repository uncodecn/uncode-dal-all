package cn.uncode.dal.descriptor;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import cn.uncode.dal.criteria.Model;
import cn.uncode.dal.criteria.QueryCriteria;

public class Table {
     
    /**
     * 表内容
     */
    private Content content;
    
    /**
     * 查询条件
     */
    private  QueryCriteria queryCriteriaLocal;
    
    /**
     * 条件参数封装
     */
    private  LinkedHashMap<String,Object> conditionsLocal;
    
    /**
     * 更新参数封装
     */
    private  LinkedHashMap<String,Object> paramsLocal;
    
    
    public Table(Content content) {
        this.content = content;
    }
    

    /**
     * 处理后的表字段 {@code String} 格式的字符串
     * <ul>
     * <li>对字段时行排序处理，sql也会进行排序；</li>
     * <li>最高级别隐藏不需要显示的字段；</li>
     * <li>生成主键相关信息；</li>
     * <li>生成外键相关信息；</li>
     * <li>生成自定义显示信息；</li>
     * </ul>
     * @return 处理后的表字段 {@code String} 格式的字符串。
     * @since 1.0
     */
    public String caculationAllColumn(){
        return content.caculationAllColumn();
    }
   
    public String getTableName() {
        return content.getTableName();
    }
    public void setTableName(String tableName) {
    	content.setTableName(tableName);
    }
    
    public String getDatabase() {
		return content.getDatabase();
	}

	public void setDatabase(String database) {
		content.setDatabase(database);
	}
    
    public Map<String, Column> getFields() {
        return content.getFields();
    }
    
    public Column getField(String fieldName) {
        return content.getFields().get(fieldName.toLowerCase());
    }

    public void setFields(Map<String, Column> fields) {
    	content.setFields(fields);
    }

    public PrimaryKey getPrimaryKey() {
        return content.getPrimaryKey();
    }
    
    public void addPrimaryFieldName(String fieldName){
    	content.getPrimaryKey().addFieldName(fieldName);
    }
    
    public void addField(Column field){
    	content.getFields().put(field.getFieldName().toLowerCase(), field);
    }

    public QueryCriteria getQueryCriteria() {
        return queryCriteriaLocal;
    }

    public void setQueryCriteria(QueryCriteria queryCriteria) {
        this.queryCriteriaLocal = queryCriteria;
    }
    
    public LinkedHashMap<String, Object> getConditions() {
        return conditionsLocal;
    }

    public LinkedHashMap<String, Object> getParams() {
        return paramsLocal;
    }
    
    public void setConditions(LinkedHashMap<String, Object> condition) {
        conditionsLocal = condition;
    }

    public void setParams(LinkedHashMap<String, Object> params) {
        LinkedHashMap<String, Object> tmpParams = new LinkedHashMap<String, Object>();
        Iterator<String> keys = params.keySet().iterator();
        while(keys.hasNext()){
            String key = keys.next();
            Object value = params.get(key);
            //if(null != value){
            	if(Model.MODEL_NAME.equals(key) || Model.MODEL_ID.equals(key)){
                    continue;
                }else{
                    tmpParams.put(key, value);
                }
            //}
        }
        paramsLocal = tmpParams;
    }

    public void putCondition(String key, Object value){
        if(conditionsLocal == null){
            conditionsLocal = new LinkedHashMap<String, Object>();
        }
        this.conditionsLocal.put(key, value);
    }
    
    public void putParams(String key, Object value){
        if(paramsLocal == null){
            paramsLocal = new LinkedHashMap<String, Object>();
        }
        this.paramsLocal.put(key, value);
    }
    
    public void resetQueryCriteria(){
        this.queryCriteriaLocal = new QueryCriteria();
    }
    
    public void resetQueryConditions(){
        conditionsLocal = new LinkedHashMap<String, Object>();
    }
    
    public void resetQueryParams(){
        paramsLocal = new LinkedHashMap<String, Object>();
    }

    public String getColumns() {
        return content.getColumns();
    }


	public Content getContent() {
		return content;
	}

    public boolean hasVersion(){
    	boolean result = false;
    	if(null != this.content){
    		result = null != this.content.getVersionField();
    	}
    	return result;
    }
    
    public Column getVersion(){
    	if(null != content){
    		return content.getVersionField();
    	}
    	return null;
    }
    
}
