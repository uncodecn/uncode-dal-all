package cn.uncode.dal.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import cn.uncode.dal.criteria.QueryCriteria;

public class ShardsUtils {
	
	public static List<Map<String, Object>> complieResult(List<Map<String, Object>> result, final QueryCriteria queryCriteria) {
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		if (StringUtils.isNotEmpty(queryCriteria.getOrderByClause())) {
			final String[] fds = queryCriteria.getOrderByClause().split(" ");
			Collections.sort(result, new Comparator<Map<String, Object>>() {
				@Override
				public int compare(Map<String, Object> a, Map<String, Object> b) {
					Object va = a.get(fds[0]);
					Object vb = b.get(fds[0]);
					if(fds.length > 1 && "desc".equals(fds[1].toLowerCase())){
						return String.valueOf(va).compareTo(String.valueOf(vb))*-1;
					}else{
						return String.valueOf(va).compareTo(String.valueOf(vb));
					}
				}
			});
		}
		if(queryCriteria.getSelectOne()){
			resultList.add(result.get(0));
		}else{
			resultList.addAll(result.subList(0, queryCriteria.getPageSize()));
		}
		return resultList;
	}
	
	
	public final static long BASE_TIME = 1422766646843L;

	private static int SEQUENCE = (int) System.currentTimeMillis() % 10000;

	/**
	 * 生成唯一ID
	 * 
	 */
	public static long generateId() {
		long time = System.currentTimeMillis() - BASE_TIME;
		SEQUENCE++;
		if (SEQUENCE == 9999) {
			SEQUENCE = 1000;
		}
		time = time * 10000 + SEQUENCE;
		return time;
	}
    
	public static void main(String[] args) {
		String[] tt = "aaa desc".split(" ");
		for(int i=0;i<tt.length;i++){
			System.out.println(tt[i]);
		}

    }

}
