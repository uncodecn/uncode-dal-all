package cn.uncode.dal.router;

import cn.uncode.dal.datasource.DBContextHolder;

public class DefaultMasterSlaveRouter implements MasterSlaveRouter {

	@Override
	public void routeToMaster() {
		DBContextHolder.swithToWrite();

	}

	@Override
	public void routeToSlave() {
		DBContextHolder.swithToRead();

	}

	@Override
	public void routeToShard(String shard) {
		DBContextHolder.swithTo(shard);
	}

}
