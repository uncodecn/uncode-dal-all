/*
 * @(#)ShardStrategyFactory.java 2012-8-1 下午10:00:00
 *
 * Copyright (c) 2011-2012 Makersoft.org all rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 */
package cn.uncode.dal.internal.shards.strategy;


/**
 * 策略
 */
public interface ShardStrategyFactory {

	ShardStrategy newShardStrategy();
}
