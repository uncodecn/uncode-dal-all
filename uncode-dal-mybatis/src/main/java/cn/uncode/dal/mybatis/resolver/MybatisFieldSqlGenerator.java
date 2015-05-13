package cn.uncode.dal.mybatis.resolver;

import cn.uncode.dal.descriptor.Column;
import cn.uncode.dal.descriptor.resolver.FieldSqlGenerator;

public class MybatisFieldSqlGenerator implements FieldSqlGenerator {

    @Override
    public String buildSingleSql(Column field) {
        String javaType = MybatisJavaTypeResolver.calculateJavaType(field.getJdbcType());
        return javaType;
    }



}
