package org.fastser.dal.descriptor.resolver;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

public class JavaTypeConversion {
	
	private static final Logger LOG = Logger.getLogger(JavaTypeConversion.class);
	
	private static final SimpleDateFormat[] formats = new SimpleDateFormat[] {
    	new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US),
    	new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),
        new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy", Locale.US),
        new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US),
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US),
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US),
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss", Locale.US),
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss"),
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss"),
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.ss"),
        new SimpleDateFormat("yyyy年MM月dd日"),
        new SimpleDateFormat("yyyy-MM-dd")};
	
	
	public static <T> T convert(JavaType type, Object valueObj, Class<T> beanClass){
		return beanClass.cast(convert(type, valueObj));
	}
	
	public static Object convert(JavaType type, Object valueObj){
		if(null == type){
			return valueObj;
		}
		
		Object result = null;
		if (JavaType.DATE == type) {
			if(valueObj instanceof String){
				boolean fmtFlag = false;
				LOG.debug("时间格式转换开始value:" + valueObj);
				String value = String.valueOf(valueObj);
				for (SimpleDateFormat format : formats) {
				    try {
				    	Date date = format.parse(value);
				    	LOG.info("时间格式转换成功format:" + format.toPattern());
				        result = new Timestamp(date.getTime());
				        fmtFlag = true;
				        break;
				    } catch (ParseException e) {
				    	LOG.error("时间格式转换失败format:" + format.toPattern());
				    }
				}
				if (!fmtFlag) {// 如果格式没转换成功 尝试毫秒值转换
				    if (NumberUtils.isNumber(value)) {
				        Long lv = NumberUtils.toLong(value);
				        result = new Timestamp(lv);
				        LOG.debug("时间格式转换成功:" + new Timestamp(lv));
				    } 
				}
			}else if(valueObj instanceof Long){
				result = new Timestamp((Long)valueObj);
			}else{
	        	result = valueObj;
	        }
        }else if (JavaType.INTEGER == type) {
        	if(valueObj instanceof String){
        		String[] vals = valueObj.toString().split("\\.");
                result = Integer.valueOf(vals[0]);
			}else if(valueObj instanceof Float || valueObj instanceof Double){
				result = (Integer)valueObj;
			}else{
	        	result = valueObj;
	        }
        }else{
        	result = valueObj;
        }
		return result;
	}

}
