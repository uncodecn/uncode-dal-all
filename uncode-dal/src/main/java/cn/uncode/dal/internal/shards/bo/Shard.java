package cn.uncode.dal.internal.shards.bo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class Shard {
	
	private Map<Long, String> partition = new HashMap<Long, String>();
	private List<String> allPartition = new ArrayList<String>();
	
	public Shard(String value){
		//one:1-2,two:2
		if(StringUtils.isNotEmpty(value)){
			String[] sds = value.split(",");
			for(String item:sds){
				String[] its = item.split(":");
				String[] se = its[1].split("-");
				if(se.length > 1){
					for(int i = Integer.valueOf(se[0]);i <= Integer.valueOf(se[1]); i++){
						partition.put((long)i, its[0]);
					}
				}else{
					partition.put(Long.valueOf(se[0]), its[0]);
				}
				allPartition.add(its[0]);
			}
		}
	}

	public Map<Long, String> getPartition() {
		return partition;
	}

	public List<String> getAllPartition() {
		return allPartition;
	}
	

}
