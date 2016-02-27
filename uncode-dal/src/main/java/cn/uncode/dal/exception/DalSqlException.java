package cn.uncode.dal.exception;

public class DalSqlException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public DalSqlException() {
        super();
    }

    public DalSqlException(String message) {
        super(message);
    }


    public DalSqlException(String message, Throwable cause) {
        super(message, cause);
    }


    public DalSqlException(Throwable cause) {
        super(cause);
    }


    protected DalSqlException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }


}
