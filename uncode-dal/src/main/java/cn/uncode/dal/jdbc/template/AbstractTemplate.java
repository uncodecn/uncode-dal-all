package cn.uncode.dal.jdbc.template;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import cn.uncode.dal.criteria.Criterion;
import cn.uncode.dal.criteria.Criterion.Condition;
import cn.uncode.dal.criteria.QueryCriteria;
import cn.uncode.dal.criteria.QueryCriteria.Criteria;
import cn.uncode.dal.descriptor.Column;
import cn.uncode.dal.descriptor.Table;
import cn.uncode.dal.descriptor.resolver.FieldSqlGenerator;
import cn.uncode.dal.descriptor.resolver.JavaType;
import cn.uncode.dal.descriptor.resolver.JavaTypeResolver;
import cn.uncode.dal.exception.StaleObjectStateException;
import cn.uncode.dal.jdbc.SQL;
import cn.uncode.dal.utils.ColumnWrapperUtils;
import cn.uncode.dal.utils.VersionWrapperUtils;


public abstract class AbstractTemplate {
    private static final Logger LOG = Logger.getLogger(AbstractTemplate.class);
    
    /**
     * 根据查询条件成生sql
     * @param sql sql
     * @param model table
     */
    protected void caculationQueryCriteria(SQL sql, Table model){
        List<Criteria> criterias = model.getQueryCriteria().getOredCriteria();
        if(criterias != null && criterias.size() > 0){
            for(Criteria criteria:criterias){
                sql.OR();
                if(criteria.isValid()){
                    List<Criterion> criterions = criteria.getCriteria();
                    for(Criterion criterion:criterions){
                        sql.AND(); sql.WHERE(convertCondition(criterion, model));
                    }
                }
            }
        }else{
            sql.AND(); sql.WHERE("1=2");
        }
        
    }
    
    protected void caculationPrimaryKey(SQL sql, Table model){
        List<String> names = model.getPrimaryKey().getFields();
        for(String name : names){
            sql.AND(); sql.WHERE(buildSingleParamSql(FieldSqlGenerator.CONDITION_PREFIX, name, model, "="));
        }
    }
    
    /**
     * 根据不同条件拼装sql
     * @param criterion criterion
     * @param model table
     * @return condition
     */
    protected String convertCondition(Criterion criterion,Table model){

        String conditionStr = "";
        if(null != criterion.getCondition()){
        	if(Condition.IS_NULL == criterion.getCondition()){
                conditionStr = ColumnWrapperUtils.wrap(criterion.getColumn()) + " is null ";
            }else if(Condition.IS_NOT_NULL == criterion.getCondition()){
                conditionStr = ColumnWrapperUtils.wrap(criterion.getColumn()) + " is not null ";
            }else if(Condition.EQUAL == criterion.getCondition()){
                conditionStr = buildSingleParamSql(FieldSqlGenerator.CONDITION_PREFIX, criterion.getColumn(), model, "=");
                model.putCondition(criterion.getColumn(), criterion.getValue());
            }else if(Condition.NOT_EQUAL == criterion.getCondition()){
                conditionStr = buildSingleParamSql(FieldSqlGenerator.CONDITION_PREFIX, criterion.getColumn(), model, "<>");
                model.putCondition(criterion.getColumn(), criterion.getValue());
            }else if(Condition.GREATER_THAN == criterion.getCondition()){
            	conditionStr = buildSingleParamSql(FieldSqlGenerator.CONDITION_PREFIX, criterion.getColumn(), criterion.getColumn()+"Min", model, ">");
                model.putCondition(criterion.getColumn()+"Min", criterion.getValue());
            }else if(Condition.GREATER_THAN_OR_EQUAL == criterion.getCondition()){
                conditionStr = buildSingleParamSql(FieldSqlGenerator.CONDITION_PREFIX, criterion.getColumn(), criterion.getColumn()+"Min", model, ">=");
                model.putCondition(criterion.getColumn()+"Min", criterion.getValue());
            }else if(Condition.LESS_THAN == criterion.getCondition()){
                conditionStr = buildSingleParamSql(FieldSqlGenerator.CONDITION_PREFIX, criterion.getColumn(), criterion.getColumn()+"Max", model, "<");
                model.putCondition(criterion.getColumn()+"Max", criterion.getValue());
            }else if(Condition.LESS_THAN_OR_EQUAL == criterion.getCondition()){
                conditionStr = buildSingleParamSql(FieldSqlGenerator.CONDITION_PREFIX, criterion.getColumn(), criterion.getColumn()+"Max", model, "<=");
                model.putCondition(criterion.getColumn()+"Max", criterion.getValue());
            }else if(Condition.IN == criterion.getCondition()){
                List<Object> values = (List<Object>) criterion.getValue();
                StringBuffer sb = new StringBuffer();
                for(Object value:values){
                    sb.append(value).append(",");
                }
                if(sb.length() > 0){
                	conditionStr = buildListParamSql(FieldSqlGenerator.CONDITION_PREFIX, criterion.getColumn(), model, "in");
                    model.putCondition(criterion.getColumn(),  sb.deleteCharAt(sb.lastIndexOf(",")).toString());
                }
            }else if(Condition.NOT_IN == criterion.getCondition()){
                List<Object> values = (List<Object>) criterion.getValue();
                StringBuffer sb = new StringBuffer();
                for(Object value:values){
                    sb.append(value).append(",");
                }
                if(sb.length() > 0){
                	conditionStr = buildListParamSql(FieldSqlGenerator.CONDITION_PREFIX, criterion.getColumn(), model, "not in");
                    model.putCondition(criterion.getColumn(),  sb.deleteCharAt(sb.lastIndexOf(",")).toString());
                }
            }else if(Condition.BETWEEN == criterion.getCondition()){
                conditionStr = buildBetweenParamSql(FieldSqlGenerator.CONDITION_PREFIX, criterion.getColumn(), model, "between");
                model.putCondition(criterion.getColumn()+"Value", criterion.getValue());
                model.putCondition(criterion.getColumn()+"SecondValue", criterion.getSecondValue());
            }else if(Condition.NOT_BETWEEN == criterion.getCondition()){
                conditionStr = buildBetweenParamSql(FieldSqlGenerator.CONDITION_PREFIX, criterion.getColumn(), model, "not between");
                model.putCondition(criterion.getColumn()+"Value", criterion.getValue());
                model.putCondition(criterion.getColumn()+"SecondValue", criterion.getSecondValue());
            }else if(Condition.LIKE == criterion.getCondition()){
                conditionStr = buildSingleParamSql(FieldSqlGenerator.CONDITION_PREFIX, criterion.getColumn(), model, "like");
                model.putCondition(criterion.getColumn(), criterion.getValue()+"%");
            }else if(Condition.NOT_LIKE == criterion.getCondition()){
                conditionStr = buildSingleParamSql(FieldSqlGenerator.CONDITION_PREFIX, criterion.getColumn(), model, "not like");
                model.putCondition(criterion.getColumn(), criterion.getValue());
            }
        }else{
        	if(StringUtils.isNotEmpty(criterion.getColumn())){
        		conditionStr += criterion.getColumn();
        	}
        }
        return conditionStr;
    }
    
    protected abstract String buildSingleParamSql(String prefix, String column, Table model, String keyword);
    
    protected abstract String buildSingleParamSql(String prefix, String column, String columnTwo, Table model, String keyword);
        
    protected abstract String buildBetweenParamSql(String prefix, String column, Table model, String keyword);
        
    protected abstract String buildListParamSql(String prefix, String column, Table model, String keyword);
       

    //--------------------------------------
    // delete
    //--------------------------------------
    public String deleteByCriteria(Table model) {
        model.resetQueryConditions();
        SQL sql = new SQL();
        sql.DELETE_FROM(model.getTableName());
        QueryCriteria queryCriteria = model.getQueryCriteria();
        if(queryCriteria.getOredCriteria() != null && queryCriteria.getOredCriteria().size() > 0){
            caculationQueryCriteria(sql, model);
        }
        model.resetQueryCriteria();
        LOG.debug(sql.toString());
        return sql.toString();  
    }
    
    
    public String deleteByPrimaryKey(Table model){
        SQL sql = new SQL();
        sql.DELETE_FROM(model.getTableName());
        caculationPrimaryKey(sql, model);
        LOG.debug(sql.toString());
        return sql.toString(); 
    }
    
    //--------------------------------------
    // insert
    //--------------------------------------
    public String insert(Table model) {
        SQL sql = new SQL();
        sql.INSERT_INTO(model.getTableName());
        LinkedHashMap<String, Object> params = model.getParams();
        if(params != null){
        	Iterator<Entry<String, Object>> iterator = params.entrySet().iterator();
            while(iterator.hasNext()){
                Entry<String, Object> entry = iterator.next();
                String key = entry.getKey();
                if(model.getContent().getFields().containsKey(key)){
                	if (entry.getValue() != null && StringUtils.isNotBlank(entry.getValue().toString()) ) {
                    	sql.VALUES(ColumnWrapperUtils.wrap(key), buildSingleParamSql(FieldSqlGenerator.PARAM_PREFIX, key, model, null));
    				}
                }
            }
        }
        LOG.debug(sql.toString());
        return sql.toString();
    }
    
    //--------------------------------------
    // select
    //--------------------------------------
    public String selectByCriteria(Table model) {
        SQL sql = new SQL();
        model.resetQueryConditions();
        QueryCriteria queryCriteria = model.getQueryCriteria();
        String customFields = caculationCustomField(model);
        if(queryCriteria.isDistinct()){
            if(StringUtils.isNotEmpty(customFields)){
                sql.SELECT_DISTINCT(customFields);
            }else{
                sql.SELECT_DISTINCT(model.caculationAllColumn());
            }
        }else{
            if(StringUtils.isNotEmpty(customFields)){
                sql.SELECT(customFields);
            }else{
                sql.SELECT(model.caculationAllColumn());
            }
        }
        sql.FROM(model.getTableName());
        if(queryCriteria.getOredCriteria() != null && queryCriteria.getOredCriteria().size() > 0){
            caculationQueryCriteria(sql, model);
        }
        if(StringUtils.isNotEmpty(queryCriteria.getOrderByClause())){
            sql.ORDER_BY(queryCriteria.getOrderByClause());
        }
        if (StringUtils.isNotBlank(model.getQueryCriteria().getGroupBy())) {
        	sql.GROUP_BY(model.getQueryCriteria().getGroupBy());
		}
        if(queryCriteria.getSelectOne()){
        	LOG.debug(sql.toString()+" limit 0,1");
            return sql.toString()+" limit 0,1";
        }
        if(queryCriteria.getPageIndex() > 0 && queryCriteria.getPageSize() > 0){
            int start = (queryCriteria.getPageIndex() - 1) * queryCriteria.getPageSize();
            LOG.debug(sql.toString()+" limit " + start + "," + queryCriteria.getPageSize());
            return sql.toString()+" limit " + start + "," + queryCriteria.getPageSize();
        }else{
        	if(queryCriteria.getRecordIndex() > 0 && queryCriteria.getPageSize() > 0){
        		LOG.debug(sql.toString()+" limit " + queryCriteria.getRecordIndex() + "," + queryCriteria.getPageSize());
        		return sql.toString()+" limit " + queryCriteria.getRecordIndex() + "," + queryCriteria.getPageSize();
        	}
        }
        model.resetQueryCriteria();
        model.resetQueryParams();
        LOG.debug(sql.toString());
        return sql.toString();  
    }


    /**
     * 计算自定义定段，
     * 已经过排序和字段隐藏处理
     * @param model table
     * @return fields string
     */
    private String caculationCustomField(Table model) {
        StringBuffer sb = new StringBuffer();
        if(model.getParams() != null && model.getParams().size() > 0){
        	Map<String, Column> columns = model.getContent().getFields();
        	List<String> keys = new ArrayList<String>(columns.keySet());
            int len = keys.size();
            List<String> fds = new ArrayList<String>();
            for(int i=0;i<len;i++){
                if(model.getParams().containsKey(keys.get(i))){
                    fds.add(keys.get(i));
                }
            }
            for(String fd:fds){
                sb.append(ColumnWrapperUtils.wrap(fd)).append(",");
            }
            if(sb.length() > 0){
                sb.deleteCharAt(sb.lastIndexOf(","));
            }
        }
        return sb.toString();
    }
    
    
    public String selectByPrimaryKey(Table model){
        SQL sql = new SQL();
        String customFields = caculationCustomField(model);
        if(StringUtils.isNotEmpty(customFields)){
            sql.SELECT(customFields);
        }else{
            sql.SELECT(model.caculationAllColumn());
        }
        sql.FROM(model.getTableName());
        caculationPrimaryKey(sql, model);
        LOG.debug(sql.toString());
        return sql.toString();
    }
    
    public String countByCriteria(Table model){
        SQL sql = new SQL();
        model.resetQueryConditions();
        QueryCriteria queryCriteria = model.getQueryCriteria();
        sql.SELECT(" count(1) ");
        sql.FROM(model.getTableName());
        if(queryCriteria.getOredCriteria() != null && queryCriteria.getOredCriteria().size() > 0){
            caculationQueryCriteria(sql, model);
        }
        model.resetQueryCriteria();
        LOG.debug(sql.toString());
        return sql.toString();  
    }
    
    //--------------------------------------
    // update
    //--------------------------------------
    public String updateByCriteria(Table model) {
        model.resetQueryConditions();
        SQL sql = new SQL();
        sql.UPDATE(model.getTableName());
        LinkedHashMap<String, Object> params = model.getParams();
        if(params != null){
            Iterator<String> iter = params.keySet().iterator();
            while(iter.hasNext()){
                String key = iter.next();
                if(!model.getPrimaryKey().getFields().contains(key)){
                	if(null == params.get(key)){
                		sql.SET(key + " = null");
                	}else{
                		
                		sql.SET(buildSingleParamSql(FieldSqlGenerator.PARAM_PREFIX, key, model, "="));
                	}
                }
            }
        }
        if(model.hasVersion()){
        	sql.SET(VersionWrapperUtils.wrapSetSql(model.getVersion()));
        }
        QueryCriteria queryCriteria = model.getQueryCriteria();
        if(queryCriteria.getOredCriteria() != null && queryCriteria.getOredCriteria().size() > 0){
            caculationQueryCriteria(sql, model);
        }
        if(model.hasVersion()){
        	Object value = queryCriteria.getVersion();
        	if(null == value){
        		throw new StaleObjectStateException("Version is request.");
        	}
        	sql.AND();
        	sql.WHERE(VersionWrapperUtils.wrapWhereSql(model.getVersion(), value));
        }
        model.resetQueryCriteria();
        LOG.debug(sql.toString());
        return sql.toString();  
    }
    
    
    public String updateByPrimaryKey(Table model){
        SQL sql = new SQL();
        sql.UPDATE(model.getTableName());
        LinkedHashMap<String, Object> params = model.getParams();
        if(params != null){
            Iterator<String> iter = params.keySet().iterator();
            while(iter.hasNext()){
                String key = iter.next();
                if(!model.getPrimaryKey().getFields().contains(key)){
                	Object value = params.get(key);
                	if(null == value){
                		sql.SET(key + " = null");
                	}else{
                		String vstr = String.valueOf(value).trim();
                		if(vstr.startsWith("=")){
                			sql.SET(ColumnWrapperUtils.wrap(key) + value);
                			params.remove(key);
                		}else{
                			sql.SET(buildSingleParamSql(FieldSqlGenerator.PARAM_PREFIX, key, model, "="));
                		}
                	}
                }
            }
        }
        if(model.hasVersion()){
        	sql.SET(VersionWrapperUtils.wrapSetSql(model.getVersion()));
        }
        caculationPrimaryKey(sql, model);
        if(model.hasVersion()){
        	Object value = model.getConditions().get(model.getVersion().getFieldName());
        	if(null == value){
        		throw new StaleObjectStateException("Version is request.");
        	}
        	sql.AND();
        	sql.WHERE(VersionWrapperUtils.wrapWhereSql(model.getVersion(), value));
        }
        LOG.debug(sql.toString());
        return sql.toString();
    }
}
