package cn.uncode.dal.asyn;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.uncode.dal.core.BaseDAL;
import cn.uncode.dal.core.MongoDAL;



/**
 * 日志写任务
 * 
 * @author juny.ye
 */
public class AsynSQLTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsynSQLTask.class);
    
    /**
     * 日志列表的最大记录数
     */
    private int recordsMaxSize = 1;

    /**
     * 日志刷新时间间隔(单位:秒)
     */
    private int flushInterval = 1;

    /**
     * 日志队列
     */
    private BlockingQueue<AsynContext> asynQueue;

    /**
     * 开关
     */
    private volatile boolean activeFlag = true;

    private List<AsynContext> records = new ArrayList<AsynContext>();

    private long timestamp = System.currentTimeMillis();
    
    private BaseDAL baseDAL;
    
    public AsynSQLTask(BaseDAL baseDAL){
    	this.baseDAL = baseDAL;
    }
    
    @Override
    public void run() {

		while (activeFlag) {
			try {
				// 记录满
				if (records.size() >= recordsMaxSize) {
					flush();
				}
				// 定时
				if (records.size() > 0
						&& System.currentTimeMillis() >= (timestamp + flushInterval * 1000L)) {
					flush();
				}
				AsynContext r = asynQueue.poll(100, TimeUnit.MILLISECONDS);
				if (r != null) {
					records.add(r);
				} else {
					Thread.sleep(1000L);
				}
			} catch (Exception e) {
				LOGGER.error("【严重】日志任务失败!", e);
			}
		}

    }

    private void flush() {
		for (AsynContext r : records) {
			try {
				if (r.getMethod() == Method.INSERT) {
					baseDAL.insert(r.getObj());
				} else if (r.getMethod() == Method.INSERT_TABLE) {
					baseDAL.insert(r.getTable(), r.getMapObj());
				}
			} catch (Exception e) {
				LOGGER.error("Mongo dal update error.", e);
			}
		}
        records.clear();
        timestamp = System.currentTimeMillis();
    }

    public BlockingQueue<AsynContext> getLogQueue() {
        return asynQueue;
    }

    public void setLogQueue(BlockingQueue<AsynContext> asynQueue) {
        this.asynQueue = asynQueue;
    }

    public boolean isActiveFlag() {
        return activeFlag;
    }

    public void setActiveFlag(boolean activeFlag) {
        this.activeFlag = activeFlag;
    }



}
