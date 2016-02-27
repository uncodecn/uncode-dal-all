package cn.uncode.dal.utils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion;


/**
 * <p>
 * <code>JSONUtils</code> instances should NOT be constructed in standard programming. Instead,
 * the class should be used as <code>JSONUtils.fromJson("foo");</code>.
 * </p>
 * <p>
 * This constructor is public to permit tools that require a JavaBean instance to operate.
 * </p>
 */
public class JsonUtils {
    
    private static final Logger LOG = LoggerFactory.getLogger(JsonUtils.class);
    
    /** 格式化时间的string */
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    
    private static ObjectMapper MAPPER;

    static {
        MAPPER = generateMapper(Inclusion.ALWAYS);
    }

    private JsonUtils() {
    }

    /**
     * fromJsonToObject<br>
     * jackjson把json字符串转换为Java对象的实现方法
     * 
     * <pre>
     * return Jackson.jsonToObj(this.answersJson, new TypeReference&lt;List&lt;StanzaAnswer&gt;&gt;() {
     * });
     * </pre>
     * 
     * @param <T>
     *            转换为的java对象
     * @param json
     *            json字符串
     * @param typeReference
     *            jackjson自定义的类型
     * @return 返回Java对象
     */
    public static <T> T jsonToObj(String json, TypeReference<T> typeReference) {
    	if(StringUtils.isNotEmpty(json)){
    		try {
                return MAPPER.readValue(json, typeReference);
            } catch (JsonParseException e) {
                LOG.error("JsonParseException: ", e);
            } catch (JsonMappingException e) {
                LOG.error("JsonMappingException: ", e);
            } catch (IOException e) {
                LOG.error("IOException: ", e);
            }
    	}
        return null;
    }
    
    public static <T> List<T> jsonToList(String json, Class<T> valueType) {
    	if(StringUtils.isNotEmpty(json)){
    		try {
    			List<T> resultList = new ArrayList<T>();
    			List<Map<?, ?>> list = MAPPER.readValue(json, List.class);
    			for(Map<?, ?> obj : list){
    				resultList.add(MAPPER.convertValue(obj, valueType));
    			}
                return resultList;
            } catch (JsonParseException e) {
                LOG.error("JsonParseException: ", e);
            } catch (JsonMappingException e) {
                LOG.error("JsonMappingException: ", e);
            } catch (IOException e) {
                LOG.error("IOException: ", e);
            }
    	}
        return null;
    }
    
    
    public static Map<?,?> objToMap(Object obj) {
    	Map<String, Object> map = MAPPER.convertValue(obj, Map.class);
    	Map<String, Object> result = new HashMap<String, Object>();
    	for(Entry<String, Object> item : map.entrySet()){
    		if(null != item.getValue()){
    			result.put(item.getKey(), item.getValue());
    		}
    	}
        return result;
    }
    
    public static <T> T mapToObj(Map<String, Object> map, Class<T> valueType) {
    	if(null != map){
    		Map<String, Object> keyMap = new HashMap<String, Object>();
        	for(Object key : map.keySet()){
        		keyMap.put(String.valueOf(key).toLowerCase(), key);
        	}
    		Object bean = null;
    		try {
    			bean = valueType.newInstance();
    			BeanMap beanMap = new BeanMap(bean);
    			for(Object key : beanMap.keySet()){
    				String keyName = String.valueOf(key).toLowerCase();
    				if(keyMap.containsKey(keyName)){
    					BeanUtils.copyProperty(bean, String.valueOf(key), map.get(keyName));
    				}
    			}
    		} catch (InstantiationException e) {
    			e.printStackTrace();
    		} catch (IllegalAccessException e) {
    			e.printStackTrace();
    		} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
            return valueType.cast(bean);
    	}
    	return null;
    }
    
    public static <T> T toObj(Object obj, Class<T> typeReference) {
    	T  result = MAPPER.convertValue(obj, typeReference);
        return result;
    }
    
    public static <T> List<T> mapListToObjList(List<Map<String, Object>> resultList, Class<T> beanClass) {
    	if(null != resultList && resultList.size() > 0){
    		BeanMap beanMap = null;
    		List<T> rtList = new ArrayList<T>();
    		try {
    			for(Map<String, Object> item:resultList){
    				Object bean = beanClass.newInstance();
    				beanMap = new BeanMap(bean);
        			for(Object key : beanMap.keySet()){
        				String keyName = String.valueOf(key).toLowerCase();
        				if(item.containsKey(keyName)){
        					try {
        						Object value = item.get(keyName);
        						if(value instanceof Map){
        							Map<String, Object> valueMap = (Map<String, Object>) value;
        							if(valueMap.containsKey("$numberLong")){
        								value = valueMap.get("$numberLong");
        							}else{
        								
        							}
        						}
        						BeanUtils.copyProperty(bean, String.valueOf(key), value);
							} catch (InvocationTargetException e) {
								LOG.error("Bean copy property error.",e);
							}
        				}
        			}
        			rtList.add(beanClass.cast(bean));
    			}
    			
    		} catch (InstantiationException e) {
    			e.printStackTrace();
    		} catch (IllegalAccessException e) {
    			e.printStackTrace();
    		}
            return rtList;
    	}
    	return null;
    }

    /**
     * fromJsonToObject<br>
     * json转换为java对象
     * 
     * <pre>
     * return Jackson.jsonToObj(this.answersJson, Jackson.class);
     * </pre>
     * 
     * @param <T>
     *            要转换的对象
     * @param json
     *            字符串
     * @param valueType
     *            对象的class
     * @return 返回对象
     */
    
    public static <T> T jsonToObj(String json, Class<T> valueType) {
    	if(StringUtils.isNotEmpty(json)){
    		try {
                return MAPPER.readValue(json, valueType);
            } catch (JsonParseException e) {
                LOG.error("JsonParseException: ", e);
            } catch (JsonMappingException e) {
                LOG.error("JsonMappingException: ", e);
            } catch (IOException e) {
                LOG.error("IOException: ", e);
            }
    	}
        return null;
    }

    /**
     * 将给定的目标对象转换成 {@code JSON} 格式的字符串。<strong>此方法只用来转换普通的 {@code JavaBean} 对象。</strong>
     * <ul>
     * <li>该方法不会转换 {@code null} 值字段；</li>
     * </ul>
     *
     * @param object 要转换成 {@code JSON} 的目标对象。
     * @return 目标对象的 {@code JSON} 格式的字符串。
     * @since 1.0
     */
    public static String objToJson(Object object) {
    	if(object != null){
    		try {
                return MAPPER.writeValueAsString(object);
            } catch (JsonGenerationException e) {
                LOG.error("JsonGenerationException: ", e);
            } catch (JsonMappingException e) {
                LOG.error("JsonMappingException: ", e);
            } catch (IOException e) {
                LOG.error("IOException: ", e);
            }
    	}
        return null;
    }
    
   
    /**
     * 将json通过类型转换成对象
     * 
     * @param json json字符串
     * @param clazz 泛型类型
     * @return 返回对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
    	if(StringUtils.isNotEmpty(json)){
    		try {
                return clazz.equals(String.class) ? (T) json : MAPPER.readValue(json, clazz);
            } catch (JsonParseException e) {
                LOG.error("JsonParseException: ", e);
            } catch (JsonMappingException e) {
                LOG.error("JsonMappingException: ", e);
            } catch (IOException e) {
                LOG.error("IOException: ", e);
            }
    	}
        return null;
    }

    /**
     * 将json通过类型转换成对象
     *
     *
     * @param json json字符串
     * @param typeReference 引用类型
     * @return 返回对象
     */
    public static <T> T fromJson(String json, TypeReference<?> typeReference){
    	if(StringUtils.isNotEmpty(json)){
    		try {
                return (T) (typeReference.getType().equals(String.class) ? json : MAPPER.readValue(json, typeReference));
            } catch (JsonParseException e) {
                LOG.error("JsonParseException: ", e);
            } catch (JsonMappingException e) {
                LOG.error("JsonMappingException: ", e);
            } catch (IOException e) {
                LOG.error("IOException: ", e);
            }
    	}
        return null;
    }

    /**
     * 将对象转换成json
     *
     *
     * @param src 对象
     * @return 返回json字符串
     */
    public static <T> String toJson(T src)  {
        try {
            return src instanceof String ? (String) src : MAPPER.writeValueAsString(src);
        } catch (JsonProcessingException e) {
            LOG.error("JsonParseException: ", e);
        }
        return null;
    }

    /**
     * 将对象转换成json, 可以设置输出属性
     *
     *
     * @param src 对象
     * @param inclusion 传入一个枚举值, 设置输出属性
     * @return 返回json字符串
     */
    public static <T> String toJson(T src, Inclusion inclusion) {
        if (src instanceof String) {
            return (String) src;
        } else {
            ObjectMapper customMapper = generateMapper(inclusion);
            try {
                return customMapper.writeValueAsString(src);
            } catch (JsonProcessingException e) {
                LOG.error("JsonProcessingException: ", e);
            }
        }
        return null;
    }

    /**
     * 将对象转换成json, 传入配置对象
     *
     *
     * @see ObjectMapper
     *
     * @param src 对象
     * @param mapper 配置对象
     * @return 返回json字符串
     */
    public static <T> String toJson(T src, ObjectMapper mapper) {
        if (null != mapper) {
            if (src instanceof String) {
                return (String) src;
            } else {
                try {
                    return mapper.writeValueAsString(src);
                } catch (JsonProcessingException e) {
                    LOG.error("JsonProcessingException: ", e);
                }
            }
        } else {
            return null;
        }
        return null;
    }

    /**
     * 返回{@link ObjectMapper ObjectMapper}对象, 用于定制性的操作
     *
     * @return {@link ObjectMapper ObjectMapper}对象
     */
    public static ObjectMapper mapper() {
        return MAPPER;
    }

    /**
     * 通过Inclusion创建ObjectMapper对象
     *
     *
     * @param inclusion 传入一个枚举值, 设置输出属性
     * @return 返回ObjectMapper对象
     */
    private static ObjectMapper generateMapper(Inclusion inclusion) {

        ObjectMapper customMapper = new ObjectMapper();
        // 所有日期格式都统一为以下样式
        customMapper.setDateFormat(new SimpleDateFormat(DATE_TIME_FORMAT));

        return customMapper;
    }

        

    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

}
