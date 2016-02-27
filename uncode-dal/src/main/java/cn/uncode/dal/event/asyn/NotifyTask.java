package cn.uncode.dal.event.asyn;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.uncode.dal.event.EventObservable;





/**
 * 日志写任务
 * 
 * @author juny.ye
 */
public class NotifyTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotifyTask.class);
    
    /**
     * 日志列表的最大记录数
     */
    private int recordsMaxSize = 100;

    /**
     * 日志刷新时间间隔(单位:秒)
     */
    private int flushInterval = 1;

    /**
     * 日志队列
     */
    private BlockingQueue<EventContext> logQueue;

    /**
     * 开关
     */
    private volatile boolean activeFlag = true;

    private List<EventContext> records = new ArrayList<EventContext>();

    private long timestamp = System.currentTimeMillis();
    
    private EventObservable observable;
    
    public NotifyTask(EventObservable observable){
    	this.observable = observable;
    }

    @Override
    public void run() {
        try {
            while (activeFlag) {
                // 记录满
                if (records.size() >= recordsMaxSize) {
                    flush();
                }
                // 定时
                if (records.size() > 0
                        && System.currentTimeMillis() >= (timestamp + flushInterval * 1000L)) {
                    flush();
                }
                EventContext r = logQueue.poll(100, TimeUnit.MILLISECONDS);
                if (r != null)
                    records.add(r);
            }
        } catch (Exception e) {
            LOGGER.error("【严重】日志任务失败!", e);
        }
    }

    private void flush() {
		for (EventContext r : records) {
			if(r.isBefore()){
				observable.notifyListenersBefore(r.getOprateType(), r.getContent());
			}else{
				observable.notifyListenersAfter(r.getOprateType(), r.getContent());
			}
        }
        records.clear();
        timestamp = System.currentTimeMillis();
    }

    public BlockingQueue<EventContext> getLogQueue() {
        return logQueue;
    }

    public void setLogQueue(BlockingQueue<EventContext> logQueue) {
        this.logQueue = logQueue;
    }

    public boolean isActiveFlag() {
        return activeFlag;
    }

    public void setActiveFlag(boolean activeFlag) {
        this.activeFlag = activeFlag;
    }



}
