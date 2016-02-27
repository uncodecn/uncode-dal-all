package cn.uncode.dal.internal.shards.strategy.impl;

import cn.uncode.dal.internal.shards.bo.Shard;
import cn.uncode.dal.internal.shards.strategy.AbstractShardStrategyFactory;
import cn.uncode.dal.internal.shards.strategy.ShardStrategy;

public class DefaultShardStrategyFactory extends AbstractShardStrategyFactory{

	@Override
	public ShardStrategy newShardStrategy() {
		return new ShardStrategy(){
			@Override
			public String[] selectShardFromData(String table) {
				if(tableShard.containsKey(table)){
					Shard shard = tableShard.get(table);
					return (String[])shard.getAllPartition().toArray();
				}
				return null;
			}

			@Override
			public String[] selectShardForNewObject(long id) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String[] selectShardForPrimaryKey(String table, Object id) {
				if(tableShard.containsKey(table)){
					long idLong = Long.valueOf(id.toString());
					long psd = idLong % SHARDS_GROUP_TOTAL;
					return new String[]{tableShard.get(table).getPartition().get(psd)};
				}
				return null;
			}

		};
	}

}
