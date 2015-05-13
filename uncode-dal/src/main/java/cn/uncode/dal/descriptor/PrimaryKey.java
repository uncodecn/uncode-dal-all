package cn.uncode.dal.descriptor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PrimaryKey implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = 5675310807548144110L;
    
    private String primaryKeyCondition;
    
    /**
     * 字段列表
     */
    private List<String> fields;
    
    public PrimaryKey() {
        super();
        fields = new ArrayList<String>();
    }
    
    
    public String getPrimaryKeyCondition() {
        return primaryKeyCondition;
    }



    public void setPrimaryKeyCondition(String primaryKeyCondition) {
        this.primaryKeyCondition = primaryKeyCondition;
    }



    public List<String> getFields() {
        return fields;
    }



    public void setFields(List<String> fields) {
        this.fields = fields;
    }



    public void addFieldName(String field){
        fields.add(field);
    }

}
