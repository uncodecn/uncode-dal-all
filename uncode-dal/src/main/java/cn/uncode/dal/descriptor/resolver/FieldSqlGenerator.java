package cn.uncode.dal.descriptor.resolver;

import cn.uncode.dal.descriptor.Column;



public interface FieldSqlGenerator {
    
    public static final String CONDITION_PREFIX = "conditions";
    
    public static final String PARAM_PREFIX = "params";
    
    String buildSingleSql(Column field);
    

}
