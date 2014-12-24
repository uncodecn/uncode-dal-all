package org.fastser.dal.router;

import org.fastser.dal.datasource.DBContextHolder;

public class DefaultMasterSlaveRouter implements MasterSlaveRouter {

	@Override
	public void routeToMaster() {
		DBContextHolder.swithToWrite();

	}

	@Override
	public void routeToSlave() {
		DBContextHolder.swithToRead();

	}

}
