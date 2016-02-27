package cn.uncode.dal.jdbc.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionDefinition;

import cn.uncode.dal.datasource.DBContextHolder;

public class DataSourceTransactionManager extends org.springframework.jdbc.datasource.DataSourceTransactionManager {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8503950636535704538L;
	
	
	private static Logger LOG = LoggerFactory.getLogger(DataSourceTransactionManager.class);
	
	/**
	 * This implementation sets the isolation level but ignores the timeout.
	 */
	@Override
	protected void doBegin(Object transaction, TransactionDefinition definition) {
		DBContextHolder.swithTotransaction();
		LOG.debug("-->Transaction begin!");
		super.doBegin(transaction, definition);
	}

	@Override
	protected void doCleanupAfterCompletion(Object transaction) {
		super.doCleanupAfterCompletion(transaction);
		DBContextHolder.clear();
		LOG.debug("<--Transaction end!");
	}

}
