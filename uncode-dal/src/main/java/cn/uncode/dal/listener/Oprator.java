package cn.uncode.dal.listener;


/**
 * 
 * @author juny.ye
 */
public enum Oprator {
	
	GET("GET"), INSERT("INSERT"), UPDATE("UPDATE"), DELETE("DELETE"), COUNT("COUNT"), LIST("LIST"), PAGE("PAGE");
	
	public final String type;

	Oprator(String type) {
		this.type = type;
	}

}
