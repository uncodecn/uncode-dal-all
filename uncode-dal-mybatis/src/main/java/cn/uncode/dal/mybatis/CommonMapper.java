package cn.uncode.dal.mybatis;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.SelectKey;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;
import org.apache.ibatis.mapping.StatementType;
import cn.uncode.dal.descriptor.Table;
import cn.uncode.dal.mybatis.template.SqlTemplate;

public interface CommonMapper{

    /**
     * 条件查询
     * 自定议显示字段传入 model.params,为空时为所有字段
     * 条件传入model.QueryCriteria
     * @param model
     * @return
     */
    @SelectProvider(method = "selectByCriteria", type = SqlTemplate.class)
    @ResultType(value = List.class)
    List<Map<String, Object>> selectByCriteria(Table model);
    
    /**
     * 新增
     * 所有字段传入 model.params,不能为空
     * @param model
     * @return
     */
    @InsertProvider(method = "insert", type = SqlTemplate.class)
    @SelectKey(before=false,keyProperty="params.id",resultType=Object.class,statementType=StatementType.STATEMENT,statement="SELECT LAST_INSERT_ID() AS id")
    int insert(Table model);
    
    /**
     * 新增
     * 所有字段传入 model.params,不能为空
     * @param model
     * @return
     */
    @InsertProvider(method = "insert", type = SqlTemplate.class)
    int insertWithId(Table model);
    
    /**
     * 条件删除
     * 条件传入model.QueryCriteria
     * @param model
     * @return
     */
    @DeleteProvider(method = "deleteByCriteria", type = SqlTemplate.class)
    int deleteByCriteria(Table model);
    
    /**
     * 条件更新
     * 更新字段传入 model.params,不能为空
     * 条件传入model.QueryCriteria
     * @param model
     * @return
     */
    @UpdateProvider(method = "updateByCriteria", type = SqlTemplate.class)
    int updateByCriteria(Table model);
    
    /**
     * 主键查询
     * 自定议显示字段传入 model.params,为空时为所有字段
     * 主键参数传入 model.conditions,不能为空
     * @param model
     * @return
     */
    @SelectProvider(method = "selectByPrimaryKey", type = SqlTemplate.class)
    @ResultType(value = Map.class)
    Map<String, Object> selectByPrimaryKey(Table model);
    
    /**
     * 主键删除
     * 主键参数传入 model.conditions,不能为空
     * @param model
     * @return
     */
    @DeleteProvider(method = "deleteByPrimaryKey", type = SqlTemplate.class)
    int deleteByPrimaryKey(Table model);
    
    /**
     * 主键更新
     * 更新字段传入 model.params,不能为空
     * 主键参数传入 model.conditions,不能为空
     * @param model
     * @return
     */
    @UpdateProvider(method = "updateByPrimaryKey", type = SqlTemplate.class)
    int updateByPrimaryKey(Table model);
    
    /**
     * 条件count
     * 条件传入model.QueryCriteria
     * @param model
     * @return
     */
    @SelectProvider(method = "countByCriteria", type = SqlTemplate.class)
    @ResultType(value = Integer.class)
    int countByCriteria(Table model);


    

    

}
