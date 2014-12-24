package org.fastser.dal.descriptor;

import java.io.Serializable;

public class Column implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = 8000107635261090463L;
    
    
    /**
     * 别名
     */
    private String name;
    
    /**
     * 字段名
     */
    private String fieldName;
    
    /**
     * 类型
     */
    private int jdbcType;
    
    /**
     * 字段sql值
     */
    private String fieldSql;
    
    /**
     * 字段长度
     */
    private int length;
    
    /**
     * 是否必填
     */
    private boolean request;
    
    /**
     * 是否为主键
     */
    private boolean primaryKey;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getFieldName() {
        return fieldName;
    }
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    public int getJdbcType() {
        return jdbcType;
    }
    public void setJdbcType(int jdbcType) {
        this.jdbcType = jdbcType;
    }
  
    public String getFieldSql() {
        return fieldSql;
    }
    public void setFieldSql(String fieldSql) {
        this.fieldSql = fieldSql;
    }
    
    public boolean isPrimaryKey() {
        return primaryKey;
    }
    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public boolean isRequest() {
		return request;
	}
	public void setRequest(boolean request) {
		this.request = request;
	}
    
    
    
    

}
