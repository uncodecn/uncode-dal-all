package cn.uncode.dal.event;

import java.util.Map;

import cn.uncode.dal.asyn.Method;


public interface EventListener {
	

	void before(Method method, Map<String, Object> content);
	
	
	void after(Method method, Map<String, Object> content);

}
