package cn.uncode.dal.datasource;

public class DBContextHolder {
	
	public static final String WRITE = "write";
	public static final String READ = "read";
	public static final String STANDBY = "standby";
	public static final String REPORT = "report";
	public static final String TRANSACTION = "transaction";
	
	private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<String>();

	public static void swithToWrite() {
		if(!TRANSACTION.equals(CONTEXT_HOLDER.get())){
			CONTEXT_HOLDER.set(WRITE);
		}
	}
	
	public static void swithToRead() {
		if(!TRANSACTION.equals(CONTEXT_HOLDER.get())){
			CONTEXT_HOLDER.set(READ);
		}
	}
	
	public static void swithToReport() {
		CONTEXT_HOLDER.set(REPORT);
	}
	
	public static void swithTotransaction() {
		CONTEXT_HOLDER.set(TRANSACTION);
	}
	
	public static void swithTo(String dbType) {
		CONTEXT_HOLDER.set(dbType);
	}

	public static String getCurrentDataSourceKey() {
		return CONTEXT_HOLDER.get();
	}

	public static void clear() {
		CONTEXT_HOLDER.remove();
	}
	
}
