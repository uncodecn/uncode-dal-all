package cn.uncode.dal.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 
 * @author juny.ye
 */
public class Observable {

	private Lock lock = new ReentrantLock(true);

	private List<OprateListener> listeners = new ArrayList<OprateListener>();

	public void addListener(OprateListener listener) {
		lock.lock();
		try {
			if (listener != null)
				listeners.add(listener);
		} finally {
			lock.unlock();
		}
	}

	public void deleteListener(OprateListener listener) {
		lock.lock();
		try {
			if (listener != null)
				listeners.remove(listener);
		} finally {
			lock.unlock();
		}
	}

	public void notifyListeners(Oprator oprator, OprateInfo oprateInfo) {
		lock.lock();
		try {
			if (listeners != null) {
				for (OprateListener obs : listeners) {
					obs.oprate(oprator, oprateInfo);
				}
			}
		} finally {
			lock.unlock();
		}
	}

}
