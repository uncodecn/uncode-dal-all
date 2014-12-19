package org.fastser.dal.descriptor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.fastser.dal.utils.ColumnWrapperUtils;

/**
 * 表内容
 * @author ywj
 *
 */
public class Content implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4742799207318016984L;

	/**
     * 所有字段-字符串
     */
    private String columns;
    
    
    /**
     * 表名
     */
    private String tableName;
    
    /**
     * 字段列表
     */
    private Map<String, Column> fields;
    
    /**
     * 主键
     */
    private PrimaryKey primaryKey;
    
    /**
     * 版本字段
     */
    private Column versionField;
    
    public Content(){
    	super();
        fields = new HashMap<String, Column>();
        primaryKey = new PrimaryKey();
    }
    
    /**
     * 处理后的表字段 {@code String} 格式的字符串
     * <ul>
     * <li>对字段时行排序处理，sql也会进行排序；</li>
     * <li>最高级别隐藏不需要显示的字段；</li>
     * <li>生成主键相关信息；</li>
     * <li>生成外键相关信息；</li>
     * <li>生成自定义显示信息；</li>
     * </ul>
     * @param fields 原始字段列表，可为null,null时计算当时实列信息
     * @return 处理后的表字段 {@code String} 格式的字符串。
     * @since 1.0
     */
    public String caculationAllColumn(){
        if(StringUtils.isNotEmpty(columns)){
            return columns;
        }else{
            StringBuffer sb = new StringBuffer();
            List<Column> fds = new ArrayList<Column>();
            if(fds == null || fds.size() == 0){
                fds.addAll(this.fields.values());
            }
            for(Column f:fds){
                sb.append(ColumnWrapperUtils.wrap(f.getFieldName())).append(",");
            }
            if(sb.length() > 0){
                columns = sb.deleteCharAt(sb.lastIndexOf(",")).toString();
            }
        }
        return columns;
    }
    
    public String getTableName() {
        return tableName;
    }
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    public Map<String, Column> getFields() {
        return fields;
    }
    
    public Column getField(String fieldName) {
        return fields.get(fieldName);
    }

    public void setFields(Map<String, Column> fields) {
        this.fields = fields;
    }

    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }
    
    public void addPrimaryFieldName(String fieldName){
        primaryKey.addFieldName(fieldName);
    }
    
    public void addField(Column field){
        this.fields.put(field.getFieldName(), field);
    }
    
    public String getColumns() {
        return columns;
    }

	public void setColumns(String columns) {
		this.columns = columns;
	}

	public void setPrimaryKey(PrimaryKey primaryKey) {
		this.primaryKey = primaryKey;
	}

	public Column getVersionField() {
		return versionField;
	}

	public void setVersionField(Column versionField) {
		this.versionField = versionField;
	}
	
	
    
}
