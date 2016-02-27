package cn.uncode.dal.log.asynlog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * 日志写任务
 * 
 * @author juny.ye
 */
public class WriterTask<T> implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(WriterTask.class);

    /**
     * 日志队列
     */
    private BlockingQueue<T> logQueue;

    /**
     * 配置对象
     */
    private LogConfig config;

    /**
     * 开关
     */
    private volatile boolean activeFlag = true;

    private List<T> records = new ArrayList<T>();

    private long timestamp = System.currentTimeMillis();

    @Override
    public void run() {

        try {
            while (activeFlag) {

                // 记录满
                if (records.size() >= config.getRecordsMaxSize()) {
                    flush();
                }

                // 定时
                if (records.size() > 0
                        && System.currentTimeMillis() >= (timestamp + config.getFlushInterval() * 1000L)) {
                    flush();
                }

                T r = logQueue.poll(100, TimeUnit.MILLISECONDS);
                if (r != null)
                    records.add(r);
            }
        } catch (Exception e) {
            LOGGER.error("【严重】日志任务失败!", e);
        }

    }

    private void flush() {
        Logger logWriter = config.getLog();
        if (logWriter == null)
            logWriter = LOGGER;

        for (T r : records) {
            if (logWriter.isWarnEnabled()) {
                logWriter.info(String.valueOf(r));
            }
        }

        records.clear();
        timestamp = System.currentTimeMillis();
    }

    public BlockingQueue<T> getLogQueue() {
        return logQueue;
    }

    public void setLogQueue(BlockingQueue<T> logQueue) {
        this.logQueue = logQueue;
    }

    public boolean isActiveFlag() {
        return activeFlag;
    }

    public void setActiveFlag(boolean activeFlag) {
        this.activeFlag = activeFlag;
    }

    public LogConfig getConfig() {
        return config;
    }

    public void setConfig(LogConfig config) {
        this.config = config;
    }

}
