package cn.uncode.dal.event.asyn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.uncode.dal.asyn.Method;
import cn.uncode.dal.criteria.Model;
import cn.uncode.dal.criteria.QueryCriteria;
import cn.uncode.dal.descriptor.QueryResult;

public class EventContext extends HashMap<String, Object> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2007032465762839883L;
	
	private static final String EVENT_TYPE_KEY = "event_type_key";
	private static final String CONTENT_KEY = "content_key";
	private static final String IS_BEFORE_KEY = "before_key";
	private static final String SELECT_PARAMS_FIELDS_KEY = "select_params_fields_key";
	private static final String SELECT_PARAMS_QUERY_CRITERIA_KEY = "select_params_query_criteria_key";
	private static final String SELECT_PARAMS_SECONDS_KEY = "select_params_seconds_key";
	private static final String SELECT_QUERY_RESULT_KEY = "select_query_result_key";
	private static final String SELECT_COUNT_RESULT_KEY = "select_count_result_key";
	private static final String SELECT_QUERY_RESULT_CACHE_KEY = "select_query_result_cache_key";
	private static final String SELECT_MODEL_KEY = "update_model_key";
	
	
	public EventContext(Method method, Map<String, Object> content, boolean before, 
			List<String> fields, QueryCriteria queryCriteria, int seconds){
		this.put(EVENT_TYPE_KEY, method);
		this.put(CONTENT_KEY, content);
		this.put(IS_BEFORE_KEY, before);
		this.put(SELECT_PARAMS_FIELDS_KEY, fields);
		this.put(SELECT_PARAMS_QUERY_CRITERIA_KEY, queryCriteria);
		this.put(SELECT_PARAMS_SECONDS_KEY, seconds);
	}
	
	public EventContext(Method method, boolean before, Model model){
		this.put(EVENT_TYPE_KEY, method);
		this.put(SELECT_MODEL_KEY, model);
		this.put(IS_BEFORE_KEY, before);
	}
	
	public EventContext(Method method, boolean before, QueryCriteria queryCriteria){
		this.put(EVENT_TYPE_KEY, method);
		this.put(IS_BEFORE_KEY, before);
		this.put(SELECT_PARAMS_QUERY_CRITERIA_KEY, queryCriteria);
	}
	
	public EventContext(Method method, boolean before, Model model, QueryCriteria queryCriteria){
		this.put(EVENT_TYPE_KEY, method);
		this.put(SELECT_MODEL_KEY, model);
		this.put(IS_BEFORE_KEY, before);
		this.put(SELECT_PARAMS_QUERY_CRITERIA_KEY, queryCriteria);
	}
	
	public EventContext(Method method, boolean before, List<String> fields,  Model model, int seconds, boolean cache){
		this.put(EVENT_TYPE_KEY, method);
		this.put(SELECT_MODEL_KEY, model);
		this.put(IS_BEFORE_KEY, before);
		this.put(SELECT_PARAMS_FIELDS_KEY, fields);
		this.put(SELECT_PARAMS_SECONDS_KEY, seconds);
		this.put(SELECT_QUERY_RESULT_CACHE_KEY, cache);
	}
	
	public EventContext(Method method, boolean before, List<String> fields, QueryCriteria queryCriteria, int seconds, boolean cache){
		this.put(EVENT_TYPE_KEY, method);
		this.put(IS_BEFORE_KEY, before);
		this.put(SELECT_PARAMS_FIELDS_KEY, fields);
		this.put(SELECT_PARAMS_QUERY_CRITERIA_KEY, queryCriteria);
		this.put(SELECT_PARAMS_SECONDS_KEY, seconds);
		this.put(SELECT_QUERY_RESULT_CACHE_KEY, cache);
	}
	
	public EventContext(Method method, boolean before, QueryResult queryResult, boolean cache){
		this.put(EVENT_TYPE_KEY, method);
		this.put(IS_BEFORE_KEY, before);
		this.put(SELECT_QUERY_RESULT_KEY, queryResult);
		this.put(SELECT_QUERY_RESULT_CACHE_KEY, cache);
	}
	
	public EventContext(Method method, boolean before, int countResult, boolean cache){
		this.put(EVENT_TYPE_KEY, method);
		this.put(IS_BEFORE_KEY, before);
		this.put(SELECT_COUNT_RESULT_KEY, countResult);
		this.put(SELECT_QUERY_RESULT_CACHE_KEY, cache);
	}
	
	public EventContext(Method method, Map<String, Object> content, boolean before){
		this.put(EVENT_TYPE_KEY, method);
		this.put(CONTENT_KEY, content);
		this.put(IS_BEFORE_KEY, before);
	}
	
	public Method getOprateType(){
		return (Method) this.get(EVENT_TYPE_KEY);
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> getContent(){
		return  (Map<String, Object>) this.get(CONTENT_KEY);
	}
	
	public boolean isBefore(){
		return (boolean) this.get(IS_BEFORE_KEY);
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getFields(){
		return (List<String>) this.get(SELECT_PARAMS_FIELDS_KEY);
	}
	
	public QueryCriteria getQueryCriteria(){
		return (QueryCriteria)this.get(SELECT_PARAMS_QUERY_CRITERIA_KEY);
	}
	
	public int getSencods(){
		return (int) this.get(SELECT_PARAMS_SECONDS_KEY);
	}
	
	public QueryResult getQueryResult(){
		return (QueryResult) this.get(SELECT_QUERY_RESULT_KEY);
	}
	
	public boolean isCache(){
		return (boolean) this.get(SELECT_QUERY_RESULT_CACHE_KEY);
	}

}
