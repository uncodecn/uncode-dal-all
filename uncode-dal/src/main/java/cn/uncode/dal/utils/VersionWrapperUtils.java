package cn.uncode.dal.utils;

import cn.uncode.dal.descriptor.Column;
import cn.uncode.dal.descriptor.resolver.JavaType;
import cn.uncode.dal.descriptor.resolver.JavaTypeResolver;

public class VersionWrapperUtils {

    public static String wrapSetSql(Column column) {
    	JavaType javaType = JavaTypeResolver.calculateJavaType(column.getJdbcType());
    	StringBuffer sql = new StringBuffer();
        if (JavaType.INTEGER == javaType) {
        	sql.append(ColumnWrapperUtils.wrap(column.getFieldName()));
            sql.append("=");
            sql.append(ColumnWrapperUtils.wrap(column.getFieldName()));
            sql.append("+1");
        } 
        return sql.toString();
    }
    
    public static String wrapWhereSql(Column column, Object value) {
    	JavaType javaType = JavaTypeResolver.calculateJavaType(column.getJdbcType());
    	StringBuffer sql = new StringBuffer();
        if (JavaType.INTEGER == javaType) {
        	sql.append(ColumnWrapperUtils.wrap(column.getFieldName()));
            sql.append("=");
            if(value instanceof String){
            	sql.append(Integer.valueOf((String)value));
            }else{
            	sql.append((Integer)value);
            }
        } 
        return sql.toString();
    }

}

