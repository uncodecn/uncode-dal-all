package org.fastser.dal.mybatis.resolver;

import java.sql.Types;

import org.fastser.dal.descriptor.resolver.JavaType;
import org.fastser.dal.descriptor.resolver.JavaTypeResolver;

public class MybatisJavaTypeResolver{
    
    public static String calculateJavaType(int jdbcType) {
        String result = null;
        JavaType type = JavaTypeResolver.calculateJavaType(jdbcType);
        if (type == null) {
            switch (jdbcType) {
            case Types.DECIMAL:
            case Types.NUMERIC:
            }
        }else{
            if(JavaType.SHORT == type){
                result = "INTEGER";
            }else if(JavaType.BOOLEAN == type){
                result = "BIT";
            }else if(JavaType.BYTE == type || JavaType.STRING == type 
                        || JavaType.CHARACTER == type || JavaType.OBJECT == type){
                result = "VARCHAR";
            }else if(JavaType.DATE == type){
                result = "TIMESTAMP";
            }else{
                result = type.value().substring(type.value().lastIndexOf(".") + 1, type.value().length()).toUpperCase();
            }
        }
        return result;
    }

}
