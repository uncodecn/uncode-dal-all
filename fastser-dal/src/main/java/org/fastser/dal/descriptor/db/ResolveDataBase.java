package org.fastser.dal.descriptor.db;

import java.util.List;

import org.fastser.dal.descriptor.Table;

public interface ResolveDataBase {
    
    /**
     * 解析表
     * @param tableName
     * @return
     */
    public Table loadTable(String tableName);
    
    /**
     * 重新加载表数据
     * @param tableName
     * @return
     */
    public Table reloadTable(String tableName);
    
    /**
     * 解析表
     * @param tableName
     * @return
     */
    public Table loadTable(String database, String tableName);
    
    public Table loadTable(String database, String tableName, String versionField);
    
    /**
     * 重新加载表数据
     * @param tableName
     * @return
     */
    public Table reloadTable(String database, String tableName);
    
    public Table reloadTable(String database, String tableName, String versionField);
    
    /**
     * 加载所有页面
     * @return
     */
    public List<String> loadTables();

}
