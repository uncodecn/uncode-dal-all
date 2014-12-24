package org.fastser.dal.descriptor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.fastser.dal.utils.JsonUtils;

public class QueryResult implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = 5675310807548144110L;
    
    private List<Map<String, Object>> resultList;
    
    private Map<String, Object> resultMap;
    
    private Map<String, Object> page;

    public void setResultList(List<Map<String, Object>> resultList) {
        this.resultList = resultList;
    }

    public void setResultMap(Map<String, Object> resultMap) {
        this.resultMap = resultMap;
    }
    
    public Map<String, Object> get(){
    	if(resultMap != null && resultMap.size() > 0){
    		return resultMap;
    	}
    	if(resultList != null && resultList.size() > 0){
    		return resultList.get(0);
    	}
        return resultMap;
    }
    
    public Map<String, Object> get(List<String> hiddenFields){
    	Map<String, Object> temp = null;
    	if(resultMap != null && resultMap.size() > 0){
    		temp = resultMap;
    	}
    	if(resultList != null && resultList.size() > 0){
    		temp = resultList.get(0);
    	}
    	if(temp != null){
    		for(String field : hiddenFields){
    			if(temp.containsKey(field)){
    				temp.remove(field);
    			}
        	}
    	}
        return temp;
    }
    
    public List<Map<String, Object>> getList(){
        return resultList;
    }
    
    public List<Map<String, Object>> getList(List<String> hiddenFields){
    	if(hiddenFields != null && resultList != null){
    		for(String field : hiddenFields){
        		for(Map<String, Object> map : resultList){
        			if(map.containsKey(field)){
        				map.remove(field);
        			}
        		}
        	}
    	}
        return resultList;
    }
    
    public <T> T as(Class<T> beanClass){
    	Map<String, Object> result = get();
    	if(result != null){
    		return JsonUtils.mapToObj(result, beanClass);
    	}
        return null;
    }
    
    public <T> List<T> asList(Class<T> beanClass){
        List<T> list = new ArrayList<T>();
        if(resultList != null){
			for (Map<String, Object> obj : resultList) {
				list.add(JsonUtils.mapToObj(obj, beanClass));
            }
        }
        return list;
    }

	public Map<String, Object> getPage() {
		return page;
	}

	public void setPage(Map<String, Object> page) {
		this.page = page;
	}
    
    
    
}
