/*
 * @(#)ShardStrategy.java 2012-8-1 下午10:00:00
 *
 * Copyright (c) 2011-2012 Makersoft.org all rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 */
package cn.uncode.dal.internal.shards.strategy;



/**
 * 
 * @author juny.ye
 */
public interface ShardStrategy {
	
	int SHARDS_GROUP_TOTAL = 100;
	
	String[] selectShardForNewObject(long id);
	String[] selectShardFromData(String table);
	String[] selectShardForPrimaryKey(String table, Object id);
	
	
/*	ShardSelectionStrategy getShardSelectionStrategy();

	ShardResolutionStrategy getShardResolutionStrategy();

	ShardAccessStrategy getShardAccessStrategy();
	
	ShardReduceStrategy getShardReduceStrategy();*/
	
}
