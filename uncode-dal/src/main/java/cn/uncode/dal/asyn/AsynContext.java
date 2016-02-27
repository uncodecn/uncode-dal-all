package cn.uncode.dal.asyn;

import java.util.HashMap;
import java.util.Map;

public class AsynContext extends HashMap<String, Object> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7574370441086237881L;
	
	public AsynContext(Method method, Object obj){
		this.put("_method", method);
		this.put("_obj", obj);
	}
	
	public AsynContext(Method method, String table, Map<String, Object> obj){
		this.put("_method", method);
		this.put("_table", table);
		this.put("_map", obj);
	}
	
	public AsynContext(Method method, String database, String table, Map<String, Object> obj){
		this.put("_method", method);
		this.put("_database", database);
		this.put("_table", table);
		this.put("_map", obj);
	}
	
	public Object getObj(){
		return this.get("_obj");
	}
	
	public String getTable(){
		return (String) this.get("_table");
	}
	
	public String getDatabase(){
		return (String) this.get("_database");
	}
	
	public Method getMethod(){
		return (Method) this.get("_method");
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> getMapObj(){
		return (Map<String, Object>) this.get("_map");
	}

}
