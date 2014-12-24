package org.fastser.dal.mybatis.resolver;

import org.fastser.dal.descriptor.Column;
import org.fastser.dal.descriptor.resolver.FieldSqlGenerator;

public class MybatisFieldSqlGenerator implements FieldSqlGenerator {

    @Override
    public String buildSingleSql(Column field) {
        String javaType = MybatisJavaTypeResolver.calculateJavaType(field.getJdbcType());
        return javaType;
    }



}
