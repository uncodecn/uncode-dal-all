package cn.uncode.dal.log.asynlog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 异步日志主类
 * 
 * @author juny.ye
 */
public class AsynWriter<T> implements IWriter<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsynWriter.class);

    /**
     * 日志队列
     */
    private BlockingQueue<T> logQueue;

    /**
     * 写线程池
     */
    private ExecutorService asynWriterService;

    /**
     * 配置
     */
    private LogConfig config;

    private List<WriterTask<T>> tasks = new ArrayList<WriterTask<T>>();

    public AsynWriter() {
        this(new LogConfig());
    }

    public AsynWriter(Logger log) {
        this(new LogConfig());

        config.setLog(log);
    }

    public AsynWriter(LogConfig logConfig) {
        if (logConfig == null)
            config = new LogConfig();
        else
            config = logConfig;

        logQueue = new LinkedBlockingQueue<T>();
        asynWriterService = Executors.newFixedThreadPool(config
                .getAsynWriterThreadSize());
        for (int i = 0; i < config.getAsynWriterThreadSize(); i++) {
            WriterTask<T> task = new WriterTask<T>();
            task.setConfig(config);
            task.setLogQueue(logQueue);
            tasks.add(task);
            asynWriterService.submit(task);
        }

        LOGGER.info("Asyn log init ok!");
    }

    @Override
    public void write(T content) {
        if (content != null)
            logQueue.offer(content);
    }

    public void destroy() {
        logQueue.clear();

        for (WriterTask<T> task : tasks) {
            task.setActiveFlag(false);
        }

        asynWriterService.shutdown();
    }

}