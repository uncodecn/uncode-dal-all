package cn.uncode.dal.descriptor.db;

import java.util.List;

import cn.uncode.dal.descriptor.Table;

public interface ResolveDataBase {
    
    /**
     * 解析表
     * @param tableName table name
     * @return table
     */
    public Table loadTable(String tableName);
    
    /**
     * 重新加载表数据
     * @param tableName table name
     * @return table
     */
    public Table reloadTable(String tableName);
    
    /**
     * 解析表
     * 
     * @param database database
     * @param tableName table name
     * @return table
     */
    public Table loadTable(String database, String tableName);
    
    public Table loadTable(String database, String tableName, String versionField);
    
    /**
     * 重新加载表数据
     * @param database database
     * @param tableName table name
     * @return table
     */
    public Table reloadTable(String database, String tableName);
    
    public Table reloadTable(String database, String tableName, String versionField);
    
    /**
     * 加载所有页面
     * @return all table names
     */
    public List<String> loadTables();

}
