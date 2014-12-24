package org.fastser.dal.spring.jdbc.template;


import org.apache.commons.lang3.StringUtils;
import org.fastser.dal.descriptor.Table;
import org.fastser.dal.jdbc.template.AbstractTemplate;
import org.fastser.dal.utils.ColumnWrapperUtils;

public class SqlTemplate extends  AbstractTemplate{
    
    
    protected String buildSingleParamSql(String prefix, String column, Table model, String keyword){
        StringBuffer sql = new StringBuffer();
        if(StringUtils.isNotEmpty(keyword)){
            sql.append(ColumnWrapperUtils.wrap(column)).append(" ").append(keyword);
        }
        sql.append(" ? ");
        return sql.toString();
    }
    protected String buildBetweenParamSql(String prefix, String column, Table model, String keyword){
        StringBuffer sql = new StringBuffer();
        if(StringUtils.isNotEmpty(keyword)){
            sql.append(ColumnWrapperUtils.wrap(column)).append(" ").append(keyword);
        }
        sql.append(" ? and ? ");
        return sql.toString();
    }
    protected String buildListParamSql(String prefix, String column, Table model, String keyword){
        StringBuffer sql = new StringBuffer();
        sql.append(ColumnWrapperUtils.wrap(column)).append(" ").append(keyword).append(" (?) ");
        return sql.toString();
    }
    
    
    
   
}
