package cn.uncode.dal.event;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.uncode.dal.event.asyn.EventContext;
import cn.uncode.dal.event.asyn.NotifyTask;


/**
 * 异步日志主类
 * 
 * @author juny.ye
 */
public class EventManager{

    private static final Logger LOGGER = LoggerFactory.getLogger(EventManager.class);
    
    /**
     * 异步日志线程池大小
     */
    private int asynWriterThreadSize = 2;

    /**
     * 日志队列
     */
    private BlockingQueue<EventContext> logQueue;

    /**
     * 写线程池
     */
    private ExecutorService asynWriterService;

    private List<NotifyTask> tasks = new ArrayList<NotifyTask>();
    
    private EventObservable observable = new EventObservable();
    
    private static final EventManager instance = new EventManager();
    
    private EventManager() {
        logQueue = new LinkedBlockingQueue<EventContext>();
        asynWriterService = Executors.newFixedThreadPool(asynWriterThreadSize);
        for (int i = 0; i < asynWriterThreadSize; i++) {
            NotifyTask task = new NotifyTask(observable);
            task.setLogQueue(logQueue);
            tasks.add(task);
            asynWriterService.submit(task);
        }
        LOGGER.info("Asyn log init ok!");
    }
	
	public static EventManager getInstance(){
		return instance;
	}

	public void sendEvent(EventContext eventContext) {
        if (eventContext != null)
            logQueue.offer(eventContext);
    }

    public void destroy() {
        logQueue.clear();
        for (NotifyTask task : tasks) {
            task.setActiveFlag(false);
        }
        asynWriterService.shutdown();
    }

	public EventObservable getObservable() {
		return observable;
	}
	
	public void addEventListener(EventListener listener){
		observable.addListener(listener);
	}
    
    

}