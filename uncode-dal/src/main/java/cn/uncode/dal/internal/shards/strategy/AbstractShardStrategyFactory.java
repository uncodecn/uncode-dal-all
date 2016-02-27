package cn.uncode.dal.internal.shards.strategy;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.InitializingBean;

import cn.uncode.dal.internal.shards.bo.Shard;

public abstract class AbstractShardStrategyFactory implements ShardStrategyFactory, InitializingBean {
	
	protected Map<String, String> tableRules = new HashMap<String, String>();
	
	protected Map<String, Shard> tableShard = new HashMap<String, Shard>();
	
	public Map<String, Shard> getTableShard() {
		return tableShard;
	}

	public void setTableRules(Map<String, String> tableRules) {
		this.tableRules = tableRules;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		for (Entry<String, String> item:tableRules.entrySet()) {
			tableShard.put(item.getKey(), new Shard(item.getValue()));
		}
		
	}
	

}
