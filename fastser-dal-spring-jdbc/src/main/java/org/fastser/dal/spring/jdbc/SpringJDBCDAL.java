package org.fastser.dal.spring.jdbc;

import java.util.List;
import java.util.Map;

import org.fastser.dal.core.AbstractBaseDAL;
import org.fastser.dal.core.BaseDAL;
import org.fastser.dal.descriptor.Table;

public class SpringJDBCDAL extends AbstractBaseDAL implements BaseDAL {


    private CommonJdbcSupport commonJdbcSupport;
    
	public void setCommonJdbcSupport(CommonJdbcSupport commonJdbcSupport) {
		this.commonJdbcSupport = commonJdbcSupport;
	}

	@Override
	public List<Map<String, Object>> _selectByCriteria(Table table) {
		return commonJdbcSupport.selectByCriteria(table);
	}

	@Override
	public int _countByCriteria(Table table) {
		return commonJdbcSupport.countByCriteria(table);
	}

	@Override
	public Map<String, Object> _selectByPrimaryKey(Table table) {
		return commonJdbcSupport.selectByPrimaryKey(table);
	}

	@Override
	public int _insert(Table table) {
		return commonJdbcSupport.insert(table);
	}

	@Override
	public int _updateByCriteria(Table table) {
		return commonJdbcSupport.updateByCriteria(table);
	}

	@Override
	public int _updateByPrimaryKey(Table table) {
		return commonJdbcSupport.updateByPrimaryKey(table);
	}

	@Override
	public int _deleteByPrimaryKey(Table table) {
		return commonJdbcSupport.deleteByPrimaryKey(table);
	}

	@Override
	public int _deleteByCriteria(Table table) {
		return commonJdbcSupport.deleteByCriteria(table);
	}

}
