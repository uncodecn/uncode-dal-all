package cn.uncode.dal.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cn.uncode.dal.asyn.Method;

/**
 * 
 * @author juny.ye

 */
public class EventObservable {

	private Lock lock = new ReentrantLock(true);

	private List<EventListener> listeners = new ArrayList<EventListener>();

	public void addListener(EventListener listener) {
		lock.lock();
		try {
			if (listener != null)
				listeners.add(listener);
		} finally {
			lock.unlock();
		}
	}

	public void deleteListener(EventListener listener) {
		lock.lock();
		try {
			if (listener != null)
				listeners.remove(listener);
		} finally {
			lock.unlock();
		}
	}

	public void notifyListenersBefore(Method method, Map<String, Object> content) {
		lock.lock();
		try {
			if (listeners != null) {
				for (EventListener obs : listeners) {
					obs.before(method, content);
				}
			}
		} finally {
			lock.unlock();
		}
	}
	
	public void notifyListenersAfter(Method method, Map<String, Object> content) {
		lock.lock();
		try {
			if (listeners != null) {
				for (EventListener obs : listeners) {
					obs.after(method, content);
				}
			}
		} finally {
			lock.unlock();
		}
	}

}
