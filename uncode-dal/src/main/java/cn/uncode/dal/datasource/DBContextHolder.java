package cn.uncode.dal.datasource;

public class DBContextHolder {
	
	public static final String WRITE = "write";
	public static final String READ = "read";
	
	private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<String>();

	public static void swithToWrite() {
		CONTEXT_HOLDER.set(WRITE);
	}
	
	public static void swithToRead() {
		CONTEXT_HOLDER.set(WRITE);
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
