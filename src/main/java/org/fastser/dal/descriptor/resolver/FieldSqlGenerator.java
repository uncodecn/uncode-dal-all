package org.fastser.dal.descriptor.resolver;

import org.fastser.dal.descriptor.Column;



public interface FieldSqlGenerator {
    
    public static final String CONDITION_PREFIX = "conditions";
    
    public static final String PARAM_PREFIX = "params";
    
    String buildSingleSql(Column field);
    

}
